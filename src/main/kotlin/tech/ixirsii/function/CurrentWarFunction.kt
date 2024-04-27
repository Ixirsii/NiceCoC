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
import arrow.core.none
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import tech.ixirsii.command.CurrentWarCommand
import tech.ixirsii.data.BLUE
import tech.ixirsii.data.ClanWarLeagueSeason
import tech.ixirsii.data.GREEN
import tech.ixirsii.data.LIGHT_BLUE
import tech.ixirsii.data.ORANGE
import tech.ixirsii.data.RED
import tech.ixirsii.data.WarEmbedInfo
import tech.ixirsii.data.YELLOW
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.cwl.ClanWarLeagueRound
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.klash.types.war.WarClan
import tech.ixirsii.klash.types.war.WarClanMember
import tech.ixirsii.klash.types.war.WarClanMemberAttack
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.max

/**
 * Logic for [CurrentWarCommand].
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
class CurrentWarFunction(
    private val clanTag: String,
    private val clashAPI: ClashAPI
) : Logging by LoggingImpl<CurrentWarFunction>() {

    /**
     * Get [EmbedCreateSpec]s detailing the current war status.
     *
     * @param userMono
     * @return [EmbedCreateSpec]s detailing the current war status.
     */
    fun getCurrentWarEmbeds(userMono: Mono<User>): Mono<List<EmbedCreateSpec>> {
        log.trace("Getting current war")

        val warMono: Mono<Option<War>> = clashAPI.leagueGroup(clanTag)
            .flatMap { groupEither: Either<ClashAPIError, ClanWarLeagueGroup> ->
                val group: ClanWarLeagueGroup = groupEither.getOrElse { ClanWarLeagueGroup() }

                if (group.state == ClanWarLeagueGroup.State.WAR) {
                    getLeagueWars(group).map { season: ClanWarLeagueSeason -> season.activeWar }
                } else {
                    clashAPI.currentWar(clanTag).map { warEither: Either<ClashAPIError, War> ->
                        Option.fromNullable(warEither.getOrNull())
                    }
                }
            }

        return buildEmbeds(userMono, warMono)
    }

    /* *************************************** Private utility functions **************************************** */

    private fun buildEmbeds(
        userMono: Mono<User>,
        warMono: Mono<Option<War>>,
    ): Mono<List<EmbedCreateSpec>> {
        log.trace("Asynchronously building war status embed")

        return Mono.zip(userMono, warMono)
            .map { tuple: Tuple2<User, Option<War>> ->
                tuple.t2.map { war: War -> buildEmbeds(tuple.t1, war) }
                    .getOrElse {
                        listOf(
                            EmbedCreateSpec.builder()
                                .author(tuple.t1.username, AUTHOR_URL, tuple.t1.avatarUrl)
                                .color(RED)
                                .title("Error getting war status")
                                .thumbnail(CLASH_LOGO_URL)
                                .build()
                        )
                    }
            }
    }

    private fun buildEmbeds(user: User, war: War): List<EmbedCreateSpec> {
        log.trace("Getting war status embed fields for war {}", war)

        return when (war.state) {
            State.WAR, State.IN_WAR -> {
                buildWarEmbeds(user, war)
            }

            State.PREPARATION -> {
                val clanName: String = getClanName(war)
                val opponentName: String = getOpponentName(war)
                val timeUntilWar: Duration = Duration.between(war.warStartTime, ZonedDateTime.now())

                buildEmbeds(
                    user,
                    GREEN,
                    "\"$clanName\" VS \"$opponentName\" | Prep Day",
                    "$timeUntilWar remaining in prep day",
                )
            }

            State.IN_MATCHMAKING, State.ENTER_WAR, State.MATCHED -> {
                buildEmbeds(user, LIGHT_BLUE, description = "Searching for opponents", title = "Matchmaking")
            }

            State.NOT_IN_WAR -> {
                buildEmbeds(user, BLUE, "Not in war", "War has ended")
            }

            State.ENDED -> {
                buildEmbeds(user, ORANGE, "War ended", "War has ended")
            }

            State.CLAN_NOT_FOUND -> {
                buildEmbeds(user, RED, ERROR, "Clan not found")
            }

            State.ACCESS_DENIED -> {
                buildEmbeds(user, RED, ERROR, "Clan war log is not public")
            }
        }
    }

    private fun buildEmbeds(user: User, color: Color, title: String, description: String): List<EmbedCreateSpec> {
        return listOf(
            EmbedCreateSpec.builder()
                .author(user.username, AUTHOR_URL, user.avatarUrl)
                .color(color)
                .title(title)
                .description(description)
                .thumbnail(CLASH_LOGO_URL)
                .build()
        )
    }

    private fun buildWarEmbeds(user: User, war: War): List<EmbedCreateSpec> {
        val memberFields: List<EmbedCreateFields.Field> = getMemberFields(war)
        val (color, description, title) = getWarEmbedInfo(war)

        return memberFields.chunked(25) { fields: List<EmbedCreateFields.Field> ->
            EmbedCreateSpec.builder()
                .author(user.username, AUTHOR_URL, user.avatarUrl)
                .color(color)
                .addAllFields(fields)
                .title(title)
                .description(description)
                .thumbnail(getOpponentBadge(war) ?: CLASH_LOGO_URL)
                .build()
        }
    }

    private fun getAttackOptions(member: WarClanMember, attacksPerMember: Int): List<Option<WarClanMemberAttack>> =
        (0 until attacksPerMember).map { index: Int -> Option.fromNullable(member.attacks.getOrNull(index)) }

    private fun getClan(war: War): Option<WarClan> {
        log.trace("Getting clan")

        return if (war.clan?.tag?.endsWith(clanTag) == true) {
            Option.fromNullable(war.clan)
        } else if (war.opponent?.tag?.endsWith(clanTag) == true) {
            Option.fromNullable(war.opponent)
        } else {
            none()
        }
    }

    private fun getClanName(war: War): String =
        getClan(war).map { clan: WarClan -> clan.name }.getOrElse { UNKNOWN }

    private fun getDestructionPercentage(clanOption: Option<WarClan>): String =
        ATTACK_PERCENTAGE_FORMAT.format(clanOption.map { it.destructionPercentage }.getOrElse { 0.0 })

    private fun getLeagueWars(group: ClanWarLeagueGroup): Mono<ClanWarLeagueSeason> {
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

    private fun getMemberFields(war: War): List<EmbedCreateFields.Field> {
        val memberFields: MutableList<EmbedCreateFields.Field> = ArrayList()

        getClan(war).onSome { warClan: WarClan ->
            warClan.members.sortedBy { member: WarClanMember -> member.mapPosition }
                .forEach { member: WarClanMember ->
                    log.trace("Building war status field for member {}", member.name)

                    val attacksPerMember: Int = max(war.attacksPerMember, 1)
                    val attacks: List<Option<WarClanMemberAttack>> = getAttackOptions(member, attacksPerMember)
                    val value: String = attacks.joinToString(separator = " | ") { option: Option<WarClanMemberAttack> ->
                        option.map { attack: WarClanMemberAttack ->
                            val stars: String = getStars(attack.stars)
                            val destruction: String = ATTACK_PERCENTAGE_FORMAT.format(attack.destructionPercentage)

                            "$destruction $stars"
                        }.getOrElse { "No attack" }
                    }

                    memberFields.add(EmbedCreateFields.Field.of(member.name, value, false))
                }
        }

        return memberFields
    }

    private fun getOpponent(war: War): Option<WarClan> {
        log.trace("Getting opponent")

        return if (war.clan?.tag?.endsWith(clanTag) == true) {
            Option.fromNullable(war.opponent)
        } else if (war.opponent?.tag?.endsWith(clanTag) == true) {
            Option.fromNullable(war.clan)
        } else {
            none()
        }
    }

    private fun getOpponentBadge(war: War): String? =
        getOpponent(war).map { clan: WarClan -> clan.badgeURLs?.large }.getOrNull()

    private fun getOpponentName(war: War): String =
        getOpponent(war).map { clan: WarClan -> clan.name }.getOrElse { UNKNOWN }

    private fun getStars(stars: Int): String = when (stars) {
        0 -> "☆☆☆"
        1 -> "☆☆★"
        2 -> "☆★★"
        else -> "★★★"
    }

    private fun getWarEmbedInfo(war: War): WarEmbedInfo {
        val clanName: String = getClanName(war)
        val clanOption: Option<WarClan> = getClan(war)
        val opponentName: String = getOpponentName(war)
        val opponentOption: Option<WarClan> = getOpponent(war)
        val friendlyStars: Int = clanOption.map { warClan: WarClan -> warClan.stars }.getOrElse { 0 }
        val friendlyDestruction: String = getDestructionPercentage(clanOption)
        val opponentStars: Int = opponentOption.map { warClan: WarClan -> warClan.stars }.getOrElse { 0 }
        val opponentDestruction: String = getDestructionPercentage(opponentOption)

        return WarEmbedInfo(
            color = YELLOW,
            description = "$friendlyStars ★ ($friendlyDestruction) | $opponentStars ★ ($opponentDestruction)",
            title = "\"$clanName\" VS \"$opponentName\" | War Day",
        )
    }

    private companion object {
        private const val ATTACK_PERCENTAGE_FORMAT = "%.1f%%"
        private const val AUTHOR_URL = "https://github.com/Ixirsii/NiceCoC"
        private const val CLASH_LOGO_URL = "https://i.imgur.com/S95pJ9o.png"
        private const val ERROR = "Error"
        private const val UNKNOWN = "Unknown"
    }
}
