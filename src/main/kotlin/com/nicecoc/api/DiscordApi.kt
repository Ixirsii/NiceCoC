package com.nicecoc.api

import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import org.koin.core.annotation.Single

/**
 * Discord API implementation.
 *
 * @author Ryan Porterfield
 */
@Single
class DiscordApi(
    /** Discord4J gateway client. */
    private val client: GatewayDiscordClient
) : AutoCloseable, Logging by LoggingImpl<DiscordApi>() {

    fun init() {
        log.trace("Initializing...")

        val applicationId: Long = client.restClient.applicationId.block() ?: 0L

        client.restClient.applicationService.createGlobalApplicationCommand(applicationId, stopCommandRequest())
            .subscribe()
    }

    /**
     * Close the client connection and log out.
     */
    override fun close() {
        log.trace("Closing Discord gateway")

        client.logout().block()
    }

    /**
     * Discord command to stop the bot.
     */
    private fun stopCommandRequest(): ApplicationCommandRequest = ApplicationCommandRequest.builder()
        .name("stop")
        .description("Shut down the bot")
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("is_graceful")
                .description("Should the bot be shut down gracefully, or force quit?")
                .type(ApplicationCommandOption.Type.STRING.value)
                .required(false)
                .build()
        )
        .build()
}
