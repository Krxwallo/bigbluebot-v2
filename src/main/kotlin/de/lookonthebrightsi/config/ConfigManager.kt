package de.lookonthebrightsi.config

import de.lookonthebrightsi.logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    if (checkFile(moodleFile, moodle, true)) moodle = json.decodeFromString(moodleFile.readText())
    if (checkFile(usersFile, discordUsers)) discordUsers = json.decodeFromString(usersFile.readText())
    if (checkFile(messagesFile, messages)) messages = json.decodeFromString(messagesFile.readText())
}

fun saveConfigs() {
    usersFile.writeText(json.encodeToString(discordUsers))
    messagesFile.writeText(json.encodeToString(messages))
    logger.info("Saved users.")
}