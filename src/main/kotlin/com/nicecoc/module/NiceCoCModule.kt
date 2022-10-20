package com.nicecoc.module

import com.google.common.eventbus.EventBus
import com.lycoon.clashapi.core.ClashAPI
import com.nicecoc.listener.DiscordListener
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
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
    fun clashApi(): ClashAPI = ClashAPI("")

    /**
     * Singleton provider for [EventBus].
     *
     * @return [EventBus] singleton.
     */
    @Single
    fun eventBus(): EventBus {
        log.trace("Creating EventBus")

        return EventBus()
    }

    /**
     * Singleton provider for [EventDispatcher].
     *
     * @return [EventDispatcher] singleton.
     */
    @Single
    fun eventDispatcher(client: GatewayDiscordClient): EventDispatcher {
        log.trace("Creating EventDispatcher")

        return client.eventDispatcher
    }

    /**
     * Singleton provider for [GatewayDiscordClient].
     *
     * @return [GatewayDiscordClient] singleton.
     */
    @Single
    fun gatewayDiscordClient(discordListener: DiscordListener): GatewayDiscordClient {
        log.trace("Creating GatewayDiscordClient")

        return DiscordClient.create("MTAwNzc0ODUyMjM1Mjg0NDg3Mw.GjBTEX.mwo3fAYPyhOxmE7JV_G5_X1t7ddD9JAMIFE46c")
            .gateway()
            .withEventDispatcher {
                it.on(ChatInputInteractionEvent::class.java).subscribe(discordListener::chatInputInteractionListener)
                it.on(MessageCreateEvent::class.java).subscribe(discordListener::messageCreateListener)
                it.on(ReadyEvent::class.java).doOnNext(discordListener::readyEventListener)
            }
            .login()
            .block()!!
    }
}
