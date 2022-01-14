@file:Suppress("EXPERIMENTAL_API_USAGE")

import commands.commands
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.reply
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("bigbluebot")

suspend fun main() {
    logger.info("Logging in...")

    val kord = Kord("") // TODO TOKEN


    val guild = kord.getGuild(Snowflake(758693167942860820))!!
    val vc = guild.getChannelOf<VoiceChannel>(Snowflake(931240223113883719))
    val debugChannel = guild.getChannelOf<TextChannel>(Snowflake(806143660172640306))

    // Register Commands

    commands.forEach {
        kord.createGlobalApplicationCommands {
            input(it.name, it.description, it.builder)
        }
    }

    kord.globalCommands.collect { command ->
        if (commands.all { it.name != command.name}) command.delete()
    }


    kord.on<InteractionCreateEvent> {
        println("Interaction")
        val commandInteraction = interaction as? CommandInteraction ?: return@on
        commands.forEach {
            if (it.name == commandInteraction.command.rootName) it.execution(commandInteraction)
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

    kord.login()

    guild.members.collect {
        val message = "Nick name: ${it.nickname} | User name: ${it.username} | DisplayName: ${it.displayName}"
        //debugChannel.createMessage(message)
        println(message)
    }
}