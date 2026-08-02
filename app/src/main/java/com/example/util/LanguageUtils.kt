package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.ui.theme.EditorColorScheme

object LanguageUtils {

    // Extension -> Language Identifier
    private val extensionMap = mapOf(
        "kt" to "kotlin",
        "kts" to "kotlin",
        "java" to "java",
        "py" to "python",
        "pyw" to "python",
        "js" to "javascript",
        "jsx" to "javascript",
        "mjs" to "javascript",
        "ts" to "typescript",
        "tsx" to "typescript",
        "c" to "c",
        "cpp" to "cpp",
        "cxx" to "cpp",
        "cc" to "cpp",
        "h" to "cpp",
        "hpp" to "cpp",
        "rs" to "rust",
        "go" to "go",
        "html" to "html",
        "htm" to "html",
        "css" to "css",
        "scss" to "css",
        "less" to "css",
        "json" to "json",
        "jsonc" to "json",
        "yaml" to "yaml",
        "yml" to "yaml",
        "sh" to "shell",
        "bash" to "shell",
        "zsh" to "shell",
        "sql" to "sql",
        "md" to "markdown",
        "markdown" to "markdown",
        "xml" to "xml",
        "php" to "php",
        "rb" to "ruby",
        "lua" to "lua",
        "cs" to "csharp",
        "dart" to "dart",
        "swift" to "swift",
        "proto" to "protobuf",
        "toml" to "toml",
        "ini" to "ini",
        "cfg" to "ini",
        "properties" to "properties",
        "asm" to "assembly",
        "s" to "assembly"
    )

    // Exact Filename -> Language
    private val filenameMap = mapOf(
        "dockerfile" to "dockerfile",
        "makefile" to "makefile",
        "cmakelists.txt" to "cmake",
        "cargo.toml" to "toml",
        "package.json" to "json",
        ".gitignore" to "git",
        ".gitattributes" to "git",
        ".editorconfig" to "properties",
        ".env" to "properties",
        "readme" to "markdown",
        "readme.md" to "markdown",
        "license" to "markdown",
        "pom.xml" to "xml",
        "build.gradle" to "groovy",
        "build.gradle.kts" to "kotlin",
        "settings.gradle" to "groovy",
        "settings.gradle.kts" to "kotlin",
        "androidmanifest.xml" to "xml"
    )

    // User custom overrides (extension -> language)
    private val userCustomAssociations = mutableMapOf<String, String>()

    fun registerCustomAssociation(ext: String, language: String) {
        userCustomAssociations[ext.lowercase()] = language.lowercase()
    }

    fun detectLanguage(filename: String, content: String = ""): String {
        val lowerName = filename.lowercase().trim()

        // 1. Exact filename match
        filenameMap[lowerName]?.let { return it }

        // 2. User custom association
        val ext = if (lowerName.contains('.')) lowerName.substringAfterLast('.') else ""
        if (ext.isNotEmpty() && userCustomAssociations.containsKey(ext)) {
            return userCustomAssociations[ext]!!
        }

        // 3. Known extension match
        if (ext.isNotEmpty() && extensionMap.containsKey(ext)) {
            return extensionMap[ext]!!
        }

        // 4. Shebang detection in file header
        if (content.startsWith("#!")) {
            val firstLine = content.lines().firstOrNull() ?: ""
            when {
                firstLine.contains("python") -> return "python"
                firstLine.contains("bash") || firstLine.contains("sh") -> return "shell"
                firstLine.contains("node") || firstLine.contains("js") -> return "javascript"
                firstLine.contains("perl") -> return "perl"
                firstLine.contains("ruby") -> return "ruby"
            }
        }

        // 5. Content heuristic fallback
        val trimmed = content.trimStart()
        if (trimmed.startsWith("<?xml") || trimmed.startsWith("<html") || trimmed.startsWith("<!DOCTYPE html")) return "html"
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) return "json"

        return if (ext.isNotEmpty()) ext else "plaintext"
    }

    // Language Icon Color Hex
    fun getLanguageIconColor(language: String): Color {
        return when (language.lowercase()) {
            "kotlin" -> Color(0xFF7F52FF)
            "java" -> Color(0xFFE76F51)
            "python" -> Color(0xFF3572A5)
            "javascript" -> Color(0xFFF7DF1E)
            "typescript" -> Color(0xFF3178C6)
            "c", "cpp" -> Color(0xFF00599C)
            "rust" -> Color(0xFFDEA584)
            "go" -> Color(0xFF00ADD8)
            "html" -> Color(0xFFE34F26)
            "css" -> Color(0xFF1572B6)
            "json" -> Color(0xFF292929)
            "yaml" -> Color(0xFFCB171E)
            "shell" -> Color(0xFF4E9A06)
            "sql" -> Color(0xFFe38c00)
            "markdown" -> Color(0xFF083FA1)
            "xml" -> Color(0xFF006064)
            "php" -> Color(0xFF4F5D95)
            "dockerfile" -> Color(0xFF2496ED)
            "git" -> Color(0xFFF05032)
            else -> Color(0xFF90A4AE)
        }
    }

    // Syntax Highlight Parser
    fun highlightCode(
        code: String,
        language: String,
        colors: EditorColorScheme
    ): AnnotatedString {
        return buildAnnotatedString {
            val lines = code.lines()
            val keywordColor = Color(0xFFC586C0)
            val stringColor = Color(0xFFCE9178)
            val commentColor = Color(0xFF6A9955)
            val functionColor = Color(0xDCDCAA)
            val numberColor = Color(0xFFB5CEA8)
            val defaultColor = colors.editorTextColor

            lines.forEachIndexed { index, line ->
                val trimmed = line.trimStart()
                if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("/*") || trimmed.startsWith("<!--")) {
                    withStyle(SpanStyle(color = commentColor, fontWeight = FontWeight.Normal)) {
                        append(line)
                    }
                } else {
                    val tokens = line.split(Regex("(?<=\\s)|(?=\\s)|(?=[(){}\\[\\];,.:=+\\-*/<>])|(?<=[(){}\\[\\];,.:=+\\-*/<>])"))
                    var inQuotes = false
                    var quoteChar = ' '

                    tokens.forEach { token ->
                        if (token.contains("\"") || token.contains("'")) {
                            withStyle(SpanStyle(color = stringColor)) { append(token) }
                        } else if (isKeyword(token, language)) {
                            withStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold)) { append(token) }
                        } else if (token.toDoubleOrNull() != null) {
                            withStyle(SpanStyle(color = numberColor)) { append(token) }
                        } else if (token.length > 2 && token.first().isUpperCase()) {
                            withStyle(SpanStyle(color = Color(0xFF4EC9B0))) { append(token) }
                        } else {
                            withStyle(SpanStyle(color = defaultColor)) { append(token) }
                        }
                    }
                }
                if (index < lines.size - 1) append("\n")
            }
        }
    }

    private fun isKeyword(word: String, lang: String): Boolean {
        val keywords = when (lang.lowercase()) {
            "kotlin" -> setOf("package", "import", "class", "fun", "val", "var", "if", "else", "when", "return", "data", "sealed", "object", "interface", "override", "private", "public", "internal", "coroutine", "suspend", "null", "true", "false")
            "java" -> setOf("package", "import", "public", "private", "protected", "class", "void", "static", "final", "int", "boolean", "if", "else", "return", "new", "this", "super", "null", "true", "false")
            "python" -> setOf("import", "from", "def", "class", "if", "elif", "else", "return", "for", "while", "in", "with", "as", "try", "except", "pass", "None", "True", "False", "lambda", "yield", "async", "await")
            "javascript", "typescript" -> setOf("import", "export", "from", "const", "let", "var", "function", "class", "return", "if", "else", "for", "while", "async", "await", "try", "catch", "new", "this", "null", "undefined", "true", "false")
            "c", "cpp" -> setOf("include", "define", "struct", "class", "void", "int", "float", "double", "char", "bool", "return", "if", "else", "for", "while", "auto", "const", "namespace", "using", "public", "private")
            "rust" -> setOf("fn", "let", "mut", "use", "pub", "struct", "enum", "impl", "trait", "return", "if", "else", "match", "loop", "while", "for", "in", "self", "Self", "true", "false")
            "go" -> setOf("package", "import", "func", "type", "struct", "interface", "var", "const", "return", "if", "else", "for", "range", "go", "defer", "nil", "true", "false")
            "sql" -> setOf("SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER", "INSERT", "INTO", "UPDATE", "DELETE", "GROUP", "BY", "ORDER", "HAVING", "CREATE", "TABLE", "select", "from", "where")
            "html", "xml" -> setOf("div", "span", "html", "head", "body", "script", "link", "meta", "style", "p", "a", "h1", "h2", "h3", "img", "table", "tr", "td", "form", "input", "button")
            else -> setOf("if", "else", "for", "while", "return", "class", "function", "var", "val", "const", "import", "export", "null", "true", "false")
        }
        return keywords.contains(word)
    }

    // Language Auto-Formatter
    fun formatCode(code: String, language: String, indentSpaces: Int = 4): String {
        val indent = " ".repeat(indentSpaces)
        val lines = code.lines()
        val result = StringBuilder()
        var currentIndentLevel = 0

        lines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                result.append("\n")
                return@forEach
            }

            val decreaseIndentBefore = trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith(")") || trimmed.startsWith("</")
            if (decreaseIndentBefore && currentIndentLevel > 0) {
                currentIndentLevel--
            }

            result.append(indent.repeat(currentIndentLevel)).append(trimmed).append("\n")

            val openBrackets = trimmed.count { it == '{' || it == '[' || it == '(' } - trimmed.count { it == '}' || it == ']' || it == ')' }
            val isTagOpen = (trimmed.startsWith("<") && !trimmed.startsWith("</") && !trimmed.endsWith("/>") && !trimmed.contains("</"))
            
            if (openBrackets > 0 || isTagOpen) {
                currentIndentLevel += maxOf(1, openBrackets)
            }
        }

        return result.toString().trimEnd()
    }
}
