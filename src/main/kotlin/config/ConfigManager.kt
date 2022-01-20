package config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import java.io.File

private val json = Json {
    prettyPrint = true
    encodeDefaults = true
    @Suppress("EXPERIMENTAL_API_USAGE")
    prettyPrintIndent = "  "
    ignoreUnknownKeys = true
}

/**
 * @return true if file exists else false
 */
private inline fun <reified T> checkFile(file: File, data: T, required: Boolean = false): Boolean = if (!file.exists()) {
    file.writeText(json.encodeToString(data))
    if (required) error("Configure ${file.name} before using.") // Throw error
    // Show warning/info
    logger.info("${file.name} doesn't exist. Creating new one.")
    false
} else true

fun loadConfigs() {
    if (checkFile(secretsFile, secrets, true)) secrets = json.decodeFromString(secretsFile.readText())
    if (checkFile(discordConfigFile, discordConfig, true)) discordConfig = json.decodeFromString(discordConfigFile.readText())
    if (checkFile(usersFile, users)) users = json.decodeFromString(usersFile.readText())
    if (checkFile(moodleFile, moodle)) moodle = json.decodeFromString(moodleFile.readText())
}

fun saveConfigs() {
    usersFile.writeText(json.encodeToString(users))
    logger.info("Saved users.")
}