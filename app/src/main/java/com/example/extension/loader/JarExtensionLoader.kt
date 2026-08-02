package com.example.extension.loader

import android.content.Context
import android.util.Log
import com.example.extension.api.ExtensionLifecycle
import com.example.extension.manifest.ExtensionManifest
import com.example.extension.manifest.ExtensionPermission
import dalvik.system.DexClassLoader
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.ZipFile

data class ExtensionPackage(
    val manifest: ExtensionManifest,
    val jarFile: File,
    val extensionDir: File,
    val instance: ExtensionLifecycle?,
    val isLoaded: Boolean,
    val loadedNativeLibs: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val loadError: String? = null
)

class JarExtensionLoader(private val appContext: Context) {

    private val TAG = "JarExtensionLoader"

    fun loadExtensionJar(jarFile: File, extensionsBaseDir: File): ExtensionPackage {
        if (!jarFile.exists()) {
            throw IllegalArgumentException("Extension JAR file does not exist: ${jarFile.absolutePath}")
        }

        Log.i(TAG, "Loading Extension JAR: ${jarFile.name}")

        // 1. Read manifest.json from inside the .jar zip stream
        var manifestJsonStr: String? = null
        try {
            ZipFile(jarFile).use { zip ->
                val entry = zip.getEntry("manifest.json")
                    ?: zip.getEntry("META-INF/manifest.json")
                if (entry != null) {
                    zip.getInputStream(entry).use { input ->
                        manifestJsonStr = BufferedReader(InputStreamReader(input)).readText()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read manifest.json from JAR ${jarFile.name}: ${e.message}", e)
            return ExtensionPackage(
                manifest = createErrorManifest(jarFile.name),
                jarFile = jarFile,
                extensionDir = jarFile.parentFile ?: extensionsBaseDir,
                instance = null,
                isLoaded = false,
                loadError = "Invalid JAR format or missing manifest.json: ${e.message}"
            )
        }

        if (manifestJsonStr == null) {
            return ExtensionPackage(
                manifest = createErrorManifest(jarFile.name),
                jarFile = jarFile,
                extensionDir = jarFile.parentFile ?: extensionsBaseDir,
                instance = null,
                isLoaded = false,
                loadError = "manifest.json not found inside extension .jar"
            )
        }

        val manifest = try {
            ExtensionManifest.parseFromJson(manifestJsonStr!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse manifest.json: ${e.message}", e)
            return ExtensionPackage(
                manifest = createErrorManifest(jarFile.name),
                jarFile = jarFile,
                extensionDir = jarFile.parentFile ?: extensionsBaseDir,
                instance = null,
                isLoaded = false,
                loadError = "Manifest JSON Syntax Error: ${e.message}"
            )
        }

        // 2. Prepare isolated working & native directories for extension
        val extDir = File(extensionsBaseDir, manifest.id).apply { if (!exists()) mkdirs() }
        val nativeDir = File(extDir, "native").apply { if (!exists()) mkdirs() }
        val dexOptDir = File(extDir, "dex_opt").apply { if (!exists()) mkdirs() }

        // 3. Extract and load native .so libraries if present
        val loadedNativeLibs = if (manifest.permissions.contains(ExtensionPermission.NATIVE_CODE)) {
            NativeLoader.extractAndLoadNativeLibraries(jarFile, nativeDir, manifest.nativeLibraries)
        } else {
            emptyList()
        }

        // 4. Load compiled class via DexClassLoader
        var lifecycleInstance: ExtensionLifecycle? = null
        var loadErrorMsg: String? = null

        try {
            val dexClassLoader = DexClassLoader(
                jarFile.absolutePath,
                dexOptDir.absolutePath,
                nativeDir.absolutePath,
                appContext.classLoader
            )

            val mainClassName = manifest.entryPoints.mainClass
            Log.i(TAG, "Instantiating extension main class: $mainClassName")

            val clazz = try {
                dexClassLoader.loadClass(mainClassName)
            } catch (_: ClassNotFoundException) {
                appContext.classLoader.loadClass(mainClassName)
            }
            val rawInstance = clazz.getDeclaredConstructor().newInstance()

            if (rawInstance is ExtensionLifecycle) {
                lifecycleInstance = rawInstance
                Log.i(TAG, "Extension ${manifest.displayName} (v${manifest.version}) loaded successfully!")
            } else {
                Log.w(TAG, "Main class $mainClassName does not implement ExtensionLifecycle")
                // Fallback: reflection check if onActivate method exists
            }
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "Class ${manifest.entryPoints.mainClass} not found in DEX loader: ${e.message}")
            loadErrorMsg = "Class ${manifest.entryPoints.mainClass} not found in bytecode"
        } catch (e: Throwable) {
            Log.e(TAG, "Error initializing extension class ${manifest.entryPoints.mainClass}: ${e.message}", e)
            loadErrorMsg = "Initialization error: ${e.message}"
        }

        return ExtensionPackage(
            manifest = manifest,
            jarFile = jarFile,
            extensionDir = extDir,
            instance = lifecycleInstance,
            isLoaded = lifecycleInstance != null,
            loadedNativeLibs = loadedNativeLibs,
            loadError = loadErrorMsg
        )
    }

    private fun createErrorManifest(fileName: String): ExtensionManifest {
        return ExtensionManifest(
            id = "invalid_" + fileName.hashCode(),
            name = fileName,
            displayName = "Corrupted Extension ($fileName)",
            version = "0.0.0",
            description = "Failed to load extension package",
            author = "Unknown",
            entryPoints = com.example.extension.manifest.ExtensionEntryPoints("UnknownClass")
        )
    }
}
