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
class NiceCoCModule {

    @Single
    fun clashAPI(): ClashAPI =
        ClashAPI("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjdjYWU1MjlmLTQ5M2UtNDUyYS1hMTdjLWFkZjI5MjdjMmQzNCIsImlhdCI6MTcwMTQ2ODc2MCwic3ViIjoiZGV2ZWxvcGVyL2M1OTE4ZDhlLWIzZWEtYzViNi1lMTA3LTQ2YWM0MDQ4M2U1OCIsInNjb3BlcyI6WyJjbGFzaCJdLCJsaW1pdHMiOlt7InRpZXIiOiJkZXZlbG9wZXIvc2lsdmVyIiwidHlwZSI6InRocm90dGxpbmcifSx7ImNpZHJzIjpbIjczLjM0LjIzNS45OSJdLCJ0eXBlIjoiY2xpZW50In1dfQ.FHWYbW4i7MNdUitqS-SNY87G9qrV0nQ9OOy7VgHvN6MIvq0BMRNUw0LjsY-1xWxec7L8L1Zx_ILmFe_Ja7XX1Q")

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
        val client: GatewayDiscordClient =
            DiscordClient.create("MTAwNzc0ODUyMjM1Mjg0NDg3Mw.GjBTEX.mwo3fAYPyhOxmE7JV_G5_X1t7ddD9JAMIFE46c")
                .gateway()
                .withEventDispatcher { it.on(ReadyEvent::class.java).doOnNext(discordListener::readyEventListener) }
                .login()
                .block()!!

        client.eventDispatcher.on(ChatInputInteractionEvent::class.java)
            .subscribe(discordListener::chatInputInteractionListener)

        return client
    }
}
