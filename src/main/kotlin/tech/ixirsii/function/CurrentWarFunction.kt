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

package tech.ixirsii.function

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import org.koin.core.annotation.Single
import reactor.core.publisher.Mono
import tech.ixirsii.command.CurrentWarCommand
import tech.ixirsii.data.ClanWarLeagueSeason
import tech.ixirsii.data.RED
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl

/**
 * Logic for [CurrentWarCommand].
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Single
class CurrentWarFunction(
    private val clashAPI: ClashAPI,
    private val clanWarLeagueFunction: ClanWarLeagueFunction,
) : GetEmbedsFunction<Option<War>>(), Logging by LoggingImpl<CurrentWarFunction>() {
    /**
     * Get [EmbedCreateSpec]s detailing the current war status.
     *
     * @return [EmbedCreateSpec]s detailing the current war status.
     */
    fun getCurrentWarEmbeds(clanTag: String, userMono: Mono<User>): Mono<List<EmbedCreateSpec>> {
        log.trace("Getting current war")

        val warMono: Mono<Option<War>> = clashAPI.leagueGroup(clanTag)
            .flatMap { groupEither: Either<ClashAPIError, ClanWarLeagueGroup> ->
                // Ignore errors from the league group endpoint, as the API throws 404 errors after CWL has "expired".
                val group: ClanWarLeagueGroup = groupEither.getOrElse { ClanWarLeagueGroup() }

                if (group.state == ClanWarLeagueGroup.State.WAR) {
                    clanWarLeagueFunction.getLeagueWars(clanTag, group)
                        .map { season: ClanWarLeagueSeason -> season.activeWar }
                } else {
                    clashAPI.currentWar(clanTag).map { warEither: Either<ClashAPIError, War> ->
                        Option.fromNullable(warEither.getOrNull())
                    }
                }
            }

        return getEmbeds(clanTag, warMono, userMono)
    }

    override fun getEmbeds(clanTag: String, data: Option<War>, user: User): List<EmbedCreateSpec> {
        log.trace("Getting war status embeds for war data {}", data)

        return data.map { war: War ->
            getEmbeds(clanTag, war, user)
        }.getOrElse {
            listOf(buildEmbed(user, RED, ERROR, "Error getting war status"))
        }
    }

    /* *************************************** Private utility functions **************************************** */
}
