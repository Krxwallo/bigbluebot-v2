package config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger

private val json = Json {
    prettyPrint = true
    encodeDefaults = true
    @Suppress("EXPERIMENTAL_API_USAGE")
    prettyPrintIndent = "  "
    ignoreUnknownKeys = true
}

fun loadConfigs() {
    if (!secretsFile.exists()) {
        secretsFile.writeText(json.encodeToString(secrets))
        error("Configure secrets.json before using.")
    }
    secrets = json.decodeFromString(secretsFile.readText())
    logger.debug("Loaded secrets")

    if (!configFile.exists()) {
        configFile.writeText(json.encodeToString(config))
        error("Configure config.json before using.")
    }
    config = json.decodeFromString(configFile.readText())
    logger.debug("Loaded config")

    if (!usersFile.exists()) {
        usersFile.writeText(json.encodeToString(users))
        logger.warn("users.json doesn't exist. Creating new file")
    }
    else users = json.decodeFromString(usersFile.readText())
}

fun saveConfigs() {
    usersFile.writeText(json.encodeToString(users))
    logger.info("Saved users.")
}