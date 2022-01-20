package config

import kotlinx.serialization.Serializable
import java.io.File


private fun configFile(path: String) = File("config/$path.json").apply { parentFile.mkdirs() }

@Serializable
data class Secrets(
    val token: String = "TOKEN",
    val moodleUser: String = "MOODLE_USER",
    val moodlePassword: String = "MOODLE_PW"
)

var secrets = Secrets()
val secretsFile = configFile("secrets")


@Serializable
data class DiscordConfig(
    val testGuild: String = "TEST_GUILD",
    val debugChannel: String = "DEBUG_CHANNEL",
    val messagesChannel: String = "MESSAGES_CHANNEL",
    val testMode: Boolean = false,
)

var discordConfig = DiscordConfig()
val discordConfigFile = configFile("discord")

@Serializable
var users = hashMapOf<String, String>() // discord member id -> BBB name
val usersFile = configFile("users")

@Serializable
data class Moodle(
    val moodleSite: String = "MOODLE_SITE" // e.x. https://moodle.hglabor.de
)

var moodle = Moodle()
val moodleFile = configFile("moodle")

@Serializable
data class Messages(
    var status: String? = null
)

var messages = Messages()
var messagesFile = configFile("messages")