package com.example.data.security

import java.util.regex.Pattern

object SecurityGuard {

    // Admin whitelist for StudySphere
    private val ADMIN_EMAILS = setOf(
        "sampangiveerabadhra@gmail.com",
        "admin@studysphere.ai",
        "moderator@studysphere.ai"
    )

    private val ADMIN_UIDS = setOf(
        "admin_master",
        "user_admin",
        "dev_admin"
    )

    /**
     * Verifies if a user has Admin role permissions.
     */
    fun isAdmin(userId: String?, email: String?): Boolean {
        if (userId != null && ADMIN_UIDS.contains(userId)) return true
        if (email != null && ADMIN_EMAILS.contains(email.lowercase().trim())) return true
        return false
    }

    /**
     * Sanitizes user input to prevent XSS / malicious scripts in posts, comments and chats.
     */
    fun sanitizeInput(input: String, maxLength: Int = 5000): String {
        var clean = input.trim()
        if (clean.length > maxLength) {
            clean = clean.substring(0, maxLength)
        }
        // Remove dangerous script tags and event handlers
        clean = clean.replace(Regex("(?i)<script[\\s\\S]*?</script>"), "")
        clean = clean.replace(Regex("(?i)javascript:"), "")
        clean = clean.replace(Regex("(?i)onload\\s*="), "")
        clean = clean.replace(Regex("(?i)onerror\\s*="), "")
        return clean
    }

    /**
     * Validates Gemini API input against Prompt Injection & Jailbreak attacks.
     */
    fun validateAiPrompt(prompt: String): PromptValidationResult {
        val sanitized = sanitizeInput(prompt, 3000)
        if (sanitized.isBlank()) {
            return PromptValidationResult(isValid = false, message = "Prompt cannot be empty.")
        }

        // Detect common prompt injection / jailbreak patterns
        val injectionPatterns = listOf(
            "ignore all previous instructions",
            "disregard previous directives",
            "you are now in developer mode",
            "jailbreak",
            "dan mode",
            "system override",
            "reveal system prompt",
            "dump api key",
            "expose your instructions"
        )

        val lower = sanitized.lowercase()
        for (pattern in injectionPatterns) {
            if (lower.contains(pattern)) {
                return PromptValidationResult(
                    isValid = false,
                    sanitizedPrompt = sanitized,
                    message = "Prompt contains disallowed bypass syntax. Please ask an educational query."
                )
            }
        }

        return PromptValidationResult(isValid = true, sanitizedPrompt = sanitized)
    }

    /**
     * Validates student code submissions in the Coding Lab Sandbox to prevent malicious executions.
     */
    fun analyzeCodeSandboxSafety(code: String, language: String): SandboxSafetyResult {
        val lower = code.lowercase()

        // Forbidden malicious system commands & unsafe operations
        val dangerousPatterns = when (language.lowercase()) {
            "python", "py" -> listOf(
                "os.system", "subprocess.popen", "subprocess.call", "shutil.rmtree",
                "open('/etc/passwd", "socket.socket", "ctypes.cdll", "__import__('os')",
                "rm -rf", ":(){ :|:& };:"
            )
            "java", "kotlin", "kt" -> listOf(
                "Runtime.getRuntime().exec", "ProcessBuilder", "System.exit",
                "sun.misc.Unsafe", "/etc/shadow", "chmod 777"
            )
            "javascript", "js", "html" -> listOf(
                "eval(", "document.cookie", "window.localStorage.clear", "indexedDB.deleteDatabase"
            )
            "c++", "cpp", "c" -> listOf(
                "system(", "fork()", "execl(", "execv(", "ptrace"
            )
            else -> listOf("rm -rf /", ":(){ :|:& };:", "del /f /s /q")
        }

        for (pattern in dangerousPatterns) {
            if (lower.contains(pattern.lowercase())) {
                return SandboxSafetyResult(
                    isSafe = false,
                    warning = "Security Guardrail: Disallowed system/network command detected ('$pattern'). Sandboxed execution restricted for safety."
                )
            }
        }

        return SandboxSafetyResult(isSafe = true, warning = null)
    }
}

data class PromptValidationResult(
    val isValid: Boolean,
    val sanitizedPrompt: String = "",
    val message: String? = null
)

data class SandboxSafetyResult(
    val isSafe: Boolean,
    val warning: String?
)
