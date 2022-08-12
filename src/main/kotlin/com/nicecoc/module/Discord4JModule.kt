package com.nicecoc.module

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.EventDispatcher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class Discord4JModule {

    @Single
    fun gatewayDiscordClient(): GatewayDiscordClient =
        DiscordClientBuilder.create("MTAwNzc0ODUyMjM1Mjg0NDg3Mw.GjBTEX.mwo3fAYPyhOxmE7JV_G5_X1t7ddD9JAMIFE46c")
            .build()
            .login()
            .block()!!

    @Single
    fun eventDispatcher(client: GatewayDiscordClient): EventDispatcher =
        client.eventDispatcher
}
