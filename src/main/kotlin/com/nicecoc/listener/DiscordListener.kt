package com.nicecoc.listener

import com.google.common.eventbus.EventBus
import com.nicecoc.event.StopBotEvent
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import org.koin.core.annotation.Single

/**
 * Discord event listener.
 *
 * @author Ryan Porterfield
 */
@Single
class DiscordListener(
    /** Event bus to publish events to. */
    private val eventBus: EventBus
) : Logging by LoggingImpl<DiscordListener>() {

    /**
     * [ChatInputInteractionEvent] listener.
     *
     * @param event Chat input interaction event.
     */
    fun chatInputInteractionListener(event: ChatInputInteractionEvent): InteractionApplicationCommandCallbackReplyMono? {
        log.info("Got command /{} {}", event.commandName, event.options.map { "${it.name}: ${it.value}" })

        if (event.commandName.equals("stop")) {
            val isGraceful: Boolean = event.getOption("isGraceful")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true)
            val reply: String = if (isGraceful) "Stopping the bot gracefully" else "Force stopping the bot"
            val stopEvent = StopBotEvent(isGraceful)

            event.reply(reply).withEphemeral(true).block()
            eventBus.post(stopEvent)
        }

        return null
    }

    /**
     * [MessageCreateEvent] listener.
     *
     * @param event Message creation event.
     */
    fun messageCreateListener(event: MessageCreateEvent) {
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
    fun readyEventListener(event: ReadyEvent) {
        log.info("Logged in as {}#{}", event.self.username, event.self.discriminator)
    }
}
