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

import arrow.core.Option
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.common.io.Resources
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import tech.ixirsii.api.DiscordApi
import tech.ixirsii.data.Config
import tech.ixirsii.event.StopBotEvent
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

/**
 * Main bot class.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
class NiceCoCBot : AutoCloseable, KoinComponent, Logging by LoggingImpl<NiceCoCBot>(), Runnable {
    /**
     * Bot configuration file.
     */
    private val configFile: File by inject()

    /**
     * Bot configuration.
     */
    private val configOption: Option<Config> by inject(named("configOption"))

    /**
     * Discord API interface.
     */
    private val discordApiOption: Option<DiscordApi> by inject(named("discordAPIOption"))

    /**
     * Internal event bus.
     */
    private val eventBus: EventBus by inject()

    /**
     * Default config resource file.
     */
    private val resourceFilePath: String by inject(named("resourceFilePath"))

    /**
     * Internal event bus subscribers.
     */
    // private val subscribers: List<Any> by inject()

    /**
     * Clean up resources.
     */
    override fun close() {
        log.trace("Cleaning up bot")
        discordApiOption.onSome { discordApi: DiscordApi -> discordApi.close() }
    }

    /**
     * Initialize the bot.
     */
    fun init() {
        log.trace("Initializing bot")
        configOption.onNone { generateUserConfig(configFile, resourceFilePath) }
            .onSome {
                // subscribers.forEach(eventBus::register)
                eventBus.register(this)
            }
        discordApiOption.onSome { discordApi: DiscordApi -> discordApi.init() }
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
            discordApiOption.onSome { discordApi: DiscordApi -> discordApi.close() }
        } else {
            exitProcess(1)
        }
    }

    /**
     * Run the bot.
     */
    override fun run() {
        log.trace("Running bot")

        discordApiOption.onSome { discordApi: DiscordApi -> discordApi.run() }
    }

    /* *************************************** Private utility functions **************************************** */

    /**
     * Write default configuration file.
     *
     * @param configFile User's config [File].
     * @param resourceFilePath File path to the default config resource file.
     */
    private fun generateUserConfig(configFile: File, resourceFilePath: String) {
        log.trace("Generating new user config file")

        try {
            if (!configFile.parentFile.exists()) {
                configFile.parentFile.mkdirs()
            }

            configFile.writeBytes(Resources.toByteArray(Resources.getResource(resourceFilePath)))

            log.info(
                "Generated new user config file at \"{}\". Please customize your configuration then restart the bot",
                configFile.absolutePath
            )
        } catch (ex: IllegalArgumentException) {
            log.error("Failed to get resource {}", resourceFilePath, ex)
        } catch (ex: IOException) {
            log.error(
                "Encountered exception while trying to write new user config file to \"{}\"",
                configFile.absolutePath,
                ex
            )
        }
    }
}
