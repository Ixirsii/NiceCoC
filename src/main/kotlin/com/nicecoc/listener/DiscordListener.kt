package com.nicecoc.listener

import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Member
import org.koin.core.annotation.Single

@Single
class DiscordListener(eventDispatcher: EventDispatcher) : Logging by LoggingImpl<DiscordListener>() {

    init {
        log.trace("Registering Discord event listeners")
        eventDispatcher.on(MessageCreateEvent::class.java).subscribe(::messageCreateListener)
        eventDispatcher.on(ReadyEvent::class.java).subscribe(::readyEventListener)
    }

    /**
     * [MessageCreateEvent] listener.
     *
     * @param event Message creation event.
     */
    private fun messageCreateListener(event: MessageCreateEvent) {
        log.info(
            "#{} [{}]: {}",
            event.message.channelId.asLong(),
            event.member.map { member: Member -> member.displayName }.orElse(""),
            event.message.content
        )
    }

    /**
     * [ReadyEvent] listener.
     *
     * @param event Ready (bot logged in) event.
     */
    private fun readyEventListener(event: ReadyEvent) {
        log.info("Logged in as {}#{}", event.self.username, event.self.discriminator)
    }
}
