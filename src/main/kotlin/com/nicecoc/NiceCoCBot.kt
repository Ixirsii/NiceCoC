package com.nicecoc

import kotlin.system.exitProcess
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.nicecoc.api.DiscordApi
import com.nicecoc.event.StopBotEvent
import com.nicecoc.listener.DiscordListener
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Main bot class.
 *
 * @author Ryan Porterfield
 */
class NiceCoCBot : AutoCloseable, KoinComponent, Logging by LoggingImpl<NiceCoCBot>(), Runnable {
    /** Discord API interface. */
    private val discordApi: DiscordApi by inject()
    /** Internal event bus. */
    private val eventBus: EventBus by inject()
    /** Internal event bus subscribers. */
//    private val subscribers: List<Any> by inject()

    /** While `true`, the bot will continue running. When `false` the bot will try to shut down gracefully. */
    private var running: Boolean = true

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
//        subscribers.forEach(eventBus::register)
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
            running = false
        } else {
            exitProcess(0)
        }
    }

    /**
     * Run the bot.
     */
    override fun run() {
        log.trace("Running...")
        discordApi.run()
    }

    /* ***************************************** Private utility functions ****************************************** */
}
