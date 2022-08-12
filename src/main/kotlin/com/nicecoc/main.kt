package com.nicecoc

import com.nicecoc.api.ApiModule
import com.nicecoc.listener.ListenerModule
import com.nicecoc.module.Discord4JModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() {
    startKoin {
        modules(ApiModule().module, Discord4JModule().module, ListenerModule().module)
    }

    NiceCoC().run()
}
