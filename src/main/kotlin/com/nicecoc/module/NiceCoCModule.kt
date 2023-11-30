package com.nicecoc.module

import com.google.common.eventbus.EventBus
import com.lycoon.clashapi.core.ClashAPI
import com.nicecoc.listener.DiscordListener
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Koin module for bot dependencies.
 *
 * @author Ryan Porterfield
 */
@Module
class NiceCoCModule : Logging by LoggingImpl<NiceCoCModule>() {

    @Single
    fun clashAPI(): ClashAPI =
        ClashAPI("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjZhMWNjMDkzLTM2MWMtNDE2Yi04NjNmLTJmZGQzMTU3M2ZlYiIsImlhdCI6MTY2ODQ3MTg5MCwic3ViIjoiZGV2ZWxvcGVyL2M1OTE4ZDhlLWIzZWEtYzViNi1lMTA3LTQ2YWM0MDQ4M2U1OCIsInNjb3BlcyI6WyJjbGFzaCJdLCJsaW1pdHMiOlt7InRpZXIiOiJkZXZlbG9wZXIvc2lsdmVyIiwidHlwZSI6InRocm90dGxpbmcifSx7ImNpZHJzIjpbIjk4LjM4LjI0MS4xMjAiXSwidHlwZSI6ImNsaWVudCJ9XX0.5UqrDkq22Bbw_gNXohEXwNpNSXC1EB4ARTJsJE1p701nXtwM1QvI2pdQ44LupAzRSWfLDZvsuU4Por9vK8M-jA")

    /**
     * Singleton provider for [EventBus].
     *
     * @return [EventBus] singleton.
     */
    @Single
    fun eventBus(): EventBus = EventBus()

    /**
     * Singleton provider for [GatewayDiscordClient].
     *
     * @return [GatewayDiscordClient] singleton.
     */
    @Single
    fun gatewayDiscordClient(discordListener: DiscordListener): GatewayDiscordClient {
        log.trace("Creating GatewayDiscordClient")

        val client: GatewayDiscordClient = DiscordClientBuilder
            .create("MTAwNzc0ODUyMjM1Mjg0NDg3Mw.GjBTEX.mwo3fAYPyhOxmE7JV_G5_X1t7ddD9JAMIFE46c")
            .build()
            .login()
            .block()!!

        client.eventDispatcher.on(ChatInputInteractionEvent::class.java)
            .subscribe(discordListener::chatInputInteractionListener)
        client.eventDispatcher.on(ReadyEvent::class.java).subscribe(discordListener::readyEventListener)

        return client
    }
}
