package com.nicecoc

import com.nicecoc.api.DiscordApi
import com.nicecoc.listener.DiscordListener
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NiceCoC : KoinComponent, Logging by LoggingImpl<NiceCoC>(), Runnable {
    private val discordApi: DiscordApi by inject()
    private val discordListener: DiscordListener by inject()

    private var running: Boolean = true

    override fun run() {
        log.trace("Running")
        log.debug("discordApi {}", discordApi)
        log.debug("discordListener {}", discordListener)

        while (running) {
            runBlocking {
                delay(1000L)
            }
        }
    }

    fun stop() {
        log.trace("Stopping")

        running = false
    }
}
