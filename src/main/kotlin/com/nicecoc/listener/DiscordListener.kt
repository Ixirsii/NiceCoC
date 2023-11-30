package com.nicecoc.listener

import com.google.common.eventbus.EventBus
import com.nicecoc.command.Command
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import org.koin.core.annotation.Single

/**
 * Discord event listener.
 *
 * @author Ryan Porterfield
 */
@Single
class DiscordListener(
    /** Map of command name to [Command]. */
    private val commands: Map<String, Command>,
) : Logging by LoggingImpl<DiscordListener>() {

    /**
     * [ChatInputInteractionEvent] listener.
     *
     * @param event Chat input interaction event.
     */
    fun chatInputInteractionListener(event: ChatInputInteractionEvent) {
        log.info("Got command /{} {}", event.commandName, event.options.map { "${it.name}: ${it.value}" })

        commands[event.commandName]?.listener(event)
    }

    /**
     * [ReadyEvent] listener.
     *
     * @param event Ready (bot logged in) event.
     */
    fun readyEventListener(event: ReadyEvent) {
        log.info("Logged in as {}", event.self.username)
    }
}
