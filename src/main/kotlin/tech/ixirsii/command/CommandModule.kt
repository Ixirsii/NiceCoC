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

import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import tech.ixirsii.command.Command.Companion.CLAN_OPTION_NAME
import tech.ixirsii.data.Clan

/**
 * Koin module for bot commands.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@ComponentScan("tech.ixirsii.command")
@Module
class CommandModule {
    /**
     * @return Clan option for commands.
     */
    @Single
    fun clanOption(): ApplicationCommandOptionData = ApplicationCommandOptionData.builder()
        .name(CLAN_OPTION_NAME)
        .description("Clan to get war info for")
        .type(ApplicationCommandOption.Type.STRING.value)
        .addChoice(
            ApplicationCommandOptionChoiceData.builder()
                .name(Clan.MIDWEST_WARRIOR.clanName)
                .value(Clan.MIDWEST_WARRIOR.tag)
                .build()
        )
        .addChoice(
            ApplicationCommandOptionChoiceData.builder()
                .name(Clan.JJK.clanName)
                .value(Clan.JJK.tag)
                .build()
        )
        .required(false)
        .build()

    /**
     * @return Map of command names to commands.
     */
    @Single
    fun commands(
        clanWarLeagueCommand: ClanWarLeagueCommand,
        currentWarCommand: CurrentWarCommand,
    ): Map<String, Command> {
        return mapOf(
            clanWarLeagueCommand.name to clanWarLeagueCommand,
            currentWarCommand.name to currentWarCommand
        )
    }
}
