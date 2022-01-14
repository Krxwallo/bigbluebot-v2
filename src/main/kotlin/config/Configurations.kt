package config

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Secrets(
    val token: String = "TOKEN",
)

var secrets = Secrets()
val secretsFile = File("secrets.json")


@Serializable
data class Config(
    val testGuild: String = "TEST_GUILD",
)

var config = Config()
val configFile = File("config.json")