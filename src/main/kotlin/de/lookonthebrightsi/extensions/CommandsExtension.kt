package de.lookonthebrightsi.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.lookonthebrightsi.config.discordUsers
import de.lookonthebrightsi.debugChannel
import de.lookonthebrightsi.logger
import de.lookonthebrightsi.shutdown
import de.lookonthebrightsi.web.joinConference
import de.lookonthebrightsi.web.leaveConference
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed

class CommandsExtension : Extension() {
    override val name = "commands"

    override suspend fun setup() {
        logger.info("Setting up commands...")

        ephemeralSlashCommand(::LinkArguments) {
            name = "link"
            description = "Link your BigBlueButton username to BigBlueBot"

            action {
                discordUsers[user.id.asString] = arguments.name

                debugChannel.createMessage("User with id **${user.id.asString}** (${user.asUser().username}) has linked the BigBlueButton username **${arguments.name}**")

                respond {
                    embed {
                        color = Color(0, 255, 0)
                        title = "Successful Link"
                        description = "You successfully linked the BigBlueButton name **${arguments.name}**!"
                    }
                }
            }
        }

        ephemeralSlashCommand(::JoinArguments) {
            name = "join"
            description = "Join a bigbluebutton conference"
            requiredPerms += Permission.Administrator
            action {
                respond {
                    embed {
                        color = Color(255, 255, 0)
                        title = "Joining conference..."
                        description = "Finding a bigbluebutton conference in **${arguments.name}**"
                    }
                }.edit {
                    joinConference(arguments.name)?.let {
                        // Error occured
                        embed {
                            color = Color(255, 0, 0)
                            title = "Error while joining confernence for ${arguments.name}"
                            description = it
                        }
                    } ?: run {
                        // No error occured
                        embed {
                            color = Color(0, 255, 0)
                            title = "Successfully joined ${arguments.name}"
                            description = "Type `/leave` to leave the conference."
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "leave"
            description = "Leave the current bigbluebutton conference"
            requiredPerms += Permission.Administrator
            action {
                respond {
                    embed {
                        color = Color(255, 255, 0)
                        title = "Leaving conference..."
                    }
                }.edit {
                    leaveConference()?.let {
                        // Successfully left
                        embed {
                            color = Color(0, 255, 0)
                            title = "Successful"
                            description = "Left **${it.name}**"
                        }
                    } ?: run {
                        // There was no conference to leave
                        embed {
                            color = Color(255, 255, 0)
                            title = "Error"
                            description = "No conference to leave"
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "shutdown"
            description = "Stops the bigbluebot"
            requiredPerms += Permission.Administrator

            action {
                respond {
                    embed {
                        color = Color(255, 0, 0)
                        title = "Shutting down..."
                    }
                }
                shutdown()
            }
        }
    }

    inner class LinkArguments: Arguments() {
        val name by string("name", "The name you are using in BigBlueButton conferences")
    }
    inner class JoinArguments: Arguments() {
        val name by string("name", "The name of the conference")
    }

}