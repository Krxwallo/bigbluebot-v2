@file:Suppress("EXPERIMENTAL_API_USAGE")

import commands.commands
import config.config
import config.configFile
import config.secrets
import config.secretsFile
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.reply
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("bigbluebot")


private val json = Json {
    prettyPrint = true
    encodeDefaults = true
    @Suppress("EXPERIMENTAL_API_USAGE")
    prettyPrintIndent = "  "
    ignoreUnknownKeys = true
}

lateinit var kord: Kord

suspend fun main() {
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

    kord = Kord(secrets.token)

    val testGuild = kord.getGuild(Snowflake(config.testGuild))!!
    val vc = testGuild.getChannelOf<VoiceChannel>(Snowflake(931240223113883719))
    val debugChannel = testGuild.getChannelOf<TextChannel>(Snowflake(806143660172640306))

    // Register Commands

    commands.forEach {
        kord.createGlobalApplicationCommands {
            input(it.name, it.description, it.builder)
            logger.info("Created '${it.name}' command.")
        }
    }
    // Delete deleted commands
    kord.globalCommands.collect { command ->
        if (commands.all { it.name != command.name }) {
            logger.info("Deleted '${command.name}' command.")
            command.delete()
        }
    }


    kord.on<InteractionCreateEvent> {
        val commandInteraction = interaction as? GuildChatInputCommandInteraction ?: return@on
        commands.forEach {
            if (it.name == commandInteraction.command.rootName) {
                it.execution(commandInteraction)
            }
        }
    }

    kord.on<MessageCreateEvent> {
        val args = message.content.split(" ")
        when (args[0]) {
            "b!ping" -> {
                val response = message.channel.createMessage("Pong!")
                response.addReaction(Emojis.desktopComputer)

                delay(5000)
                message.delete()
                response.delete()
            }
            "b!link" -> {
                if (args.size < 2) {
                    message.reply {
                        embed {
                            this.color = Color(255, 0, 0)
                            this.title = "Invalid Arguments"
                            this.description = "Expected 1 argument but got 0"
                        }
                    }
                }
                else {
                    args[1]
                }
            }
        }
    }

    logger.info("Logging in...")

    kord.login()

    kord.editPresence {
        listening("Beathoven")
    }
}