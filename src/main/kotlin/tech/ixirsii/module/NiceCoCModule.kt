/*
 * Copyright (c) Ryan Porterfield 2024.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of NiceCoC nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package tech.ixirsii.module

import com.google.common.eventbus.EventBus
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.listener.DiscordListener

/**
 * Koin module for bot dependencies.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Module
class NiceCoCModule {

    /**
     * Singleton provider for [ClashAPI].
     *
     * @return [ClashAPI] singleton.
     */
    @Single
    fun clashAPI(): ClashAPI = ClashAPI(
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6ImY4ZjI1MzUzLWFmYmMtNGI1MS05N2IyLWExY2EzMDI2MzFhNiIsImlhdCI6MTcwOTg1MTg2MCwic3ViIjoiZGV2ZWxvcGVyL2M1OTE4ZDhlLWIzZWEtYzViNi1lMTA3LTQ2YWM0MDQ4M2U1OCIsInNjb3BlcyI6WyJjbGFzaCJdLCJsaW1pdHMiOlt7InRpZXIiOiJkZXZlbG9wZXIvc2lsdmVyIiwidHlwZSI6InRocm90dGxpbmcifSx7ImNpZHJzIjpbIjczLjM0LjIzNS45OSJdLCJ0eXBlIjoiY2xpZW50In1dfQ._whuI-q-XyMhdSPAFyut-Ea1orfn_Dm8uy7qOWd1ylg3mU1yxcH7qMYj_aft-FLomn07o0xaT17ByciTMwwuWQ"
    )

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

        client.eventDispatcher.on(ChatInputInteractionEvent::class.java, discordListener::chatInputInteractionListener)
            .subscribe()

        return client
    }
}
