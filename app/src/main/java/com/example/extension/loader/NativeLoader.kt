package com.example.extension.loader

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

object NativeLoader {
    private const val TAG = "ExtensionNativeLoader"

    fun extractAndLoadNativeLibraries(
        jarFile: File,
        extensionNativeDir: File,
        declaredNativeLibs: List<String>
    ): List<String> {
        val loadedLibs = mutableListOf<String>()

        if (!jarFile.exists()) return loadedLibs
        if (!extensionNativeDir.exists()) extensionNativeDir.mkdirs()

        val supportedAbis = Build.SUPPORTED_ABIS
        Log.d(TAG, "Supported ABIs for extension native libraries: ${supportedAbis.joinToString()}")

        try {
            ZipFile(jarFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name

                    // Look for lib/<abi>/<libname>.so inside the JAR
                    if (entryName.startsWith("lib/") && entryName.endsWith(".so")) {
                        val pathParts = entryName.split("/")
                        if (pathParts.size >= 3) {
                            val abi = pathParts[1]
                            val libFileName = pathParts.last()

                            if (supportedAbis.contains(abi)) {
                                val outFile = File(extensionNativeDir, libFileName)
                                zip.getInputStream(entry).use { input ->
                                    FileOutputStream(outFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                Log.i(TAG, "Extracted native extension library ($abi): ${outFile.absolutePath}")

                                try {
                                    System.load(outFile.absolutePath)
                                    loadedLibs.add(libFileName)
                                    Log.i(TAG, "Successfully loaded native extension library: ${outFile.name}")
                                } catch (e: Throwable) {
                                    Log.e(TAG, "Failed to load native library ${outFile.name}: ${e.message}", e)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting native libraries from ${jarFile.name}: ${e.message}", e)
        }

        return loadedLibs
    }
}
