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
import arrow.core.getOrElse
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import org.koin.core.annotation.Single
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.ixirsii.command.ClanWarLeagueCommand
import tech.ixirsii.data.ClanWarLeagueSeason
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.cwl.ClanWarLeagueRound
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl

/**
 * Logic for [ClanWarLeagueCommand].
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Single
class ClanWarLeagueFunction(private val clashAPI: ClashAPI) :
    GetEmbedsFunction<ClanWarLeagueSeason>(),
    Logging by LoggingImpl<ClanWarLeagueFunction>() {

    /**
     * Get [EmbedCreateSpec]s detailing the clan war league status.
     *
     * @return [EmbedCreateSpec]s detailing the clan war league status.
     */
    fun getCWLEmbeds(clanTag: String, userMono: Mono<User>): Mono<List<EmbedCreateSpec>> {
        log.trace("Getting CWL embeds")

        val seasonMono: Mono<ClanWarLeagueSeason> = clashAPI.leagueGroup(clanTag)
            .flatMap { groupEither: Either<ClashAPIError, ClanWarLeagueGroup> ->
                // Ignore errors from the league group endpoint, as the API throws 404 errors after CWL has "expired".
                val group: ClanWarLeagueGroup = groupEither.getOrElse { ClanWarLeagueGroup() }

                getLeagueWars(clanTag, group)
            }

        return getEmbeds(clanTag, seasonMono, userMono)
    }

    /**
     * Get the Clan War League wars for clan [clanTag] in the given group.
     *
     * @param clanTag The clan tag to get wars for.
     * @param group The CWL group to get wars for.
     * @return the Clan War League wars for clan [clanTag] in the given group.
     */
    fun getLeagueWars(clanTag: String, group: ClanWarLeagueGroup): Mono<ClanWarLeagueSeason> {
        log.trace("Getting CWL wars")

        return Flux.fromIterable(
            group.rounds.map { round: ClanWarLeagueRound ->
                Flux.merge(
                    round.warTags.filter { tag: String -> tag != "#0" }.map { tag: String -> clashAPI.leagueWar(tag) }
                )
            }
        )
            .flatMap { flux: Flux<Either<ClashAPIError, War>> -> flux.collectList() }
            .collectList()
            .map { warRounds: List<List<Either<ClashAPIError, War>>> ->
                ClanWarLeagueSeason(clanTag, warRounds)
            }
    }

    override fun getEmbeds(clanTag: String, data: ClanWarLeagueSeason, user: User): List<EmbedCreateSpec> {
        log.trace("Building CWL war embeds")

        return data.wars.flatMap { war: War -> getEmbeds(clanTag, war, user) }
    }

    /* *************************************** Private utility functions **************************************** */
}
