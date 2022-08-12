package com.nicecoc.api

import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.GatewayDiscordClient
import org.koin.core.annotation.Single

@Single
class DiscordApi(
    private val client: GatewayDiscordClient
) : Logging by LoggingImpl<DiscordApi>() {

    /**
     * Close the client connection and log out.
     */
    fun logout() {
        log.trace("Logging out of Discord Gateway")
        client.logout().block()
    }
}
