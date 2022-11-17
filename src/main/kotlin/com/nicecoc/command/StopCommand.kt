package com.nicecoc.command

import com.google.common.eventbus.EventBus
import com.nicecoc.event.StopBotEvent
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import org.koin.core.annotation.Single

/**
 * Command to (gracefully) shut down the bot.
 *
 * @author Ryan Porterfield
 */
@Single
class StopCommand(
    /** Event bus to publish events to. */
    private val eventBus: EventBus
) : Command, Logging by LoggingImpl<StopCommand>() {
    /** Command name. */
    override val name: String = "stop"

    /** [ApplicationCommandRequest] to register the command with Discord APIs. */
    override val request: ApplicationCommandRequest
        get() = ApplicationCommandRequest.builder()
            .name(name)
            .description("Shut down the bot")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name("is_graceful")
                    .description("Should the bot be shut down gracefully, or force quit?")
                    .type(ApplicationCommandOption.Type.BOOLEAN.value)
                    .required(false)
                    .build()
            )
            .build()

    /**
     * Function which can be registered to listen to chat input interaction events.
     *
     * @param event Chat command event.
     */
    override fun listener(event: ChatInputInteractionEvent) {
        if (event.commandName == "stop") {
            val isGraceful: Boolean = event.getOption("is_graceful")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true)
            val reply: String = if (isGraceful) "Stopping the bot gracefully" else "Force stopping the bot"
            val stopEvent = StopBotEvent(isGraceful)

            event.reply(reply).withEphemeral(true).subscribe()
            eventBus.post(stopEvent)
        }
    }
}
