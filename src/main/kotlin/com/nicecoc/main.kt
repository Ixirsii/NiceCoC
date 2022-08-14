package com.nicecoc

import com.nicecoc.api.ApiModule
import com.nicecoc.listener.ListenerModule
import com.nicecoc.module.NiceCoCModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

/**
 * Main function/program entry point.
 *
 * Registers Koin modules and starts the bot.
 */
fun main() {
    startKoin {
        modules(ApiModule().module, NiceCoCModule().module, ListenerModule().module)
    }

    NiceCoCBot().use {
        it.init()
        it.run()
    }
}
