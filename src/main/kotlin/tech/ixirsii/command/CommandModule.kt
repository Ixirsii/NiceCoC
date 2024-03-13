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

package tech.ixirsii.command

import arrow.core.Option
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import tech.ixirsii.klash.client.ClashAPI

/**
 * Koin module for bot commands.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@ComponentScan("tech.ixirsii.command")
@Module
class CommandModule {
    /**
     * Singleton provider for all bot commands.
     *
     * @return Map of command names to commands.
     */
    @Single
    fun commands(currentWarCommandOption: Option<CurrentWarCommand>): Map<String, Command> {
        val commands = mutableMapOf<String, Command>()

        currentWarCommandOption.onSome { currentWarCommand: CurrentWarCommand ->
            commands[currentWarCommand.name] = currentWarCommand
        }

        return commands
    }

    /**
     * Singleton provider for the current war command.
     *
     * @return Option of CurrentWarCommand.
     */
    @Single
    fun currentWarCommand(clashAPIOption: Option<ClashAPI>): Option<CurrentWarCommand> =
        clashAPIOption.map { clashAPI: ClashAPI -> CurrentWarCommand(clashAPI) }
}