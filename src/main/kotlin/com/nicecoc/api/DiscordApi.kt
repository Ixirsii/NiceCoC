package com.nicecoc.api

import com.nicecoc.command.Command
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.GatewayDiscordClient
import org.koin.core.annotation.Single

/**
 * Discord API implementation.
 *
 * @author Ryan Porterfield
 */
@Single
class DiscordApi(
    /** Discord4J gateway client. */
    private val client: GatewayDiscordClient,
    /** All bot commands. */
    private val commands: Map<String, Command>,
) : AutoCloseable, Logging by LoggingImpl<DiscordApi>() {

    fun init() {
        log.trace("Initializing...")

        val applicationId: Long = client.restClient.applicationId.block() ?: 0L
        // Transient Gamers guild ID
        val guildId: Long = 107871773818658816

        commands.forEach { (_, command) ->
            client.restClient.applicationService.createGuildApplicationCommand(
                applicationId,
                guildId,
                command.request
            )
                .subscribe()
        }
    }

    /**
     * Close the client connection and log out.
     */
    override fun close() {
        log.trace("Closing Discord gateway")

        client.logout().block()
    }

    fun run() {
        client.onDisconnect().block()
    }
}
