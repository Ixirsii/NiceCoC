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

package tech.ixirsii

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tech.ixirsii.api.DiscordApi
import tech.ixirsii.event.StopBotEvent
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl
import kotlin.system.exitProcess

/**
 * Main bot class.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
class NiceCoCBot : AutoCloseable, KoinComponent, Logging by LoggingImpl<NiceCoCBot>(), Runnable {
    /**
     * Discord API interface.
     */
    private val discordApi: DiscordApi by inject()

    /**
     * Internal event bus.
     */
    private val eventBus: EventBus by inject()

    /**
     * Internal event bus subscribers.
     */
    // private val subscribers: List<Any> by inject()

    /**
     * Clean up resources.
     */
    override fun close() {
        log.trace("Cleaning up...")
        discordApi.close()
    }

    /**
     * Initialize the bot.
     */
    fun init() {
        log.trace("Initializing...")
        discordApi.init()
        // subscribers.forEach(eventBus::register)
        eventBus.register(this)
    }

    /**
     * Handle [StopBotEvent]s.
     *
     * @param event [StopBotEvent]
     */
    @Subscribe
    fun onStopBotEvent(event: StopBotEvent) {
        log.trace("Stopping {{ gracefully: {} }}", event.isGraceful)

        if (event.isGraceful) {
            discordApi.close()
        } else {
            exitProcess(1)
        }
    }

    /**
     * Run the bot.
     */
    override fun run() {
        log.trace("Running...")
        discordApi.run()
    }

    /* *************************************** Private utility functions **************************************** */
}
