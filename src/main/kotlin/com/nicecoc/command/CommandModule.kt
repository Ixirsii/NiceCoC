package com.nicecoc.command

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@ComponentScan("com.nicecoc.command")
@Module
class CommandModule {
    @Single
    fun commands(currentWarCommand: CurrentWarCommand): Map<String, Command> = mapOf(
        currentWarCommand.name to currentWarCommand,
    )
}
