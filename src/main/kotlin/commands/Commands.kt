package commands

import config.users
import debugChannel
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.string
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.flow.count
import shutdown
import web.conference
import web.joinConference
import web.leaveConference

val bbbCommands = listOf(
    Command("link", "Link your BigBlueButton username to BigBlueBot", {
        string("name", "The name you are using in BigBlueButton conferences") { required = true }
    }) {
        val name = command.options["name"]?.string()!!
        acknowledgeEphemeral().followUp {
            users[member.id.toString()] = name
            embed {
                color = Color(0, 255, 0)
                title = "Successful Link"
                description = "You successfully linked the BigBlueButton name **$name**!"
            }
            debugChannel.createMessage("User with id **${member.id}** (${member.username}) has linked the BigBlueButton username **$name**")
        }
    },
    Command("join", "Join a bigbluebutton conference", {
        string("name", "The name of the conference") {
            required = true
            //TODO correct choices
            choice("Deutsch", "10a-D-Bg-2022")
        }
    }) {
        if (!hasPermission(Permission.Administrator)) return@Command
        val name = command.options["name"]?.string()!!
        acknowledgeEphemeral().followUp {
            embed {
                color = Color(255, 255, 0)
                title = "Joining conference..."
                description = "Finding a bigbluebutton conference in **$name**"
            }
        }.edit {
            joinConference(name)?.let {
                // Error occured
                    embed {
                        color = Color(255, 0, 0)
                        title = "Error while joining confernence for $name"
                        description = it
                    }
            } ?: run {
                // No error occured
                embed {
                    color = Color(0, 255, 0)
                    title = "Successfully joined $name"
                    description = "Type `/leave` to leave the conference."
                }
            }
        }
    },
    Command("leave", "Leave the current bigbluebutton conference") {
        if (!hasPermission(Permission.Administrator)) return@Command
        acknowledgeEphemeral().followUp {
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
                    description = "Left **$it**"
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
    },
    Command("info", "Spit out some information") {
        acknowledgePublic().followUp {
            embed {
                color = Color(0, 255, 0)
                title = "BigBlueBot Information"
                description = """
                    Linked users: **${users.size}**
                    -> Link your discord and BigBlueButton account with `/link`!
                    
                    Current conference: **$conference**
                    
                    More information coming soon...
                    
                    -------------------------------
                    
                    Developed by [Krxwallo](https://github.com/Krxwallo/) | [Source Code](https://github.com/Krxwallo/bigbluebot-v2)
                """.trimIndent()
                footer {
                    text = ""
                }
            }
        }
    },
    Command("clear", "Clears the channel") {
        if (!hasPermission(Permission.ManageMessages)) return@Command
        val replyMessage = acknowledgePublic().followUp {
            embed {
                color = Color(255, 255, 0)
                val messageAmount = channel.messages.count()-1
                title = "Deleting $messageAmount ${if (messageAmount == 1) "message" else "messages"}"
                description = "This can take some time..."
            }
        }
        channel.messages.collect { msg ->
            if (msg.id != replyMessage.id) msg.delete("BBB /clear command (by ${member.username})")
        }
        replyMessage.edit {
            embed {
                color = Color(0, 255, 0)
                title = "Done"
                description = "Deleted all messages in this channel."
            }
        }
    },
    Command("shutdown", "Stops the BigBlueBot") {
        if (!hasPermission(Permission.Administrator)) return@Command
        acknowledgeEphemeral().followUp {
            embed {
                color = Color(255, 0, 0)
                title = "Shutting down..."
            }
        }
        shutdown()
    }
)