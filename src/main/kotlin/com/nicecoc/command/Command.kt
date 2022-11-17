package com.nicecoc.command

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.ApplicationCommandRequest

/**
 * A Discord command wrapper, containing the [ApplicationCommandRequest] and a command listener.
 *
 * @author Ryan Porterfield
 */
interface Command {
    /** Command name. */
    val name: String
    /** [ApplicationCommandRequest] to register the command with Discord APIs. */
    val request: ApplicationCommandRequest

    /**
     * Function which can be registered to listen to chat input interaction events.
     *
     * @param event Chat command event.
     */
    fun listener(event: ChatInputInteractionEvent)
}
