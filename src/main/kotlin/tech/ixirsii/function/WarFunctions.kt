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

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.ixirsii.data.BLUE
import tech.ixirsii.data.GREEN
import tech.ixirsii.data.LIGHT_BLUE
import tech.ixirsii.data.ORANGE
import tech.ixirsii.data.RED
import tech.ixirsii.data.YELLOW
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.klash.types.war.WarClan
import tech.ixirsii.klash.types.war.WarClanMember
import tech.ixirsii.klash.types.war.WarClanMemberAttack
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.max

/**
 * Error string.
 */
internal const val ERROR = "Error"

/**
 * String format for attack percentages.
 */
private const val ATTACK_PERCENTAGE_FORMAT = "%.1f%%"

/**
 * NiceCoC GitHub URL.
 */
private const val AUTHOR_URL = "https://github.com/Ixirsii/NiceCoC"

/**
 * Clash of Clans logo URL.
 */
private const val CLASH_LOGO_URL = "https://i.imgur.com/S95pJ9o.png"

/**
 * Maximum number of fields Discord supports in an embed.
 */
private const val MAX_EMBED_FIELDS = 25

/**
 * Unknown clan name.
 */
private const val UNKNOWN = "Unknown"

/**
 * File logger instance.
 */
@Suppress("detekt:PropertyName")
private val log: Logger = LoggerFactory.getLogger("WarFunctions")

/**
 * Helper function to build an embed.
 *
 * @param user The user/post author that will be used to set embed author.
 * @param color The embed color.
 * @param title The embed title.
 * @param description The embed description.
 * @return The built embed.
 */
internal fun buildEmbed(user: User, color: Color, title: String, description: String): EmbedCreateSpec {
    log.trace("Building embed {}", title)

    return EmbedCreateSpec.builder()
        .author(user.username, AUTHOR_URL, user.avatarUrl)
        .color(color)
        .title(title)
        .description(description)
        .thumbnail(CLASH_LOGO_URL)
        .build()
}

/**
 * Get the embeds for a clan war.
 *
 * @param clanTag The clan tag to get embeds for.
 * @param war The clan war to get embeds for.
 * @param user The user/post author that will be used to set embed author.
 * @return The list of embeds for a clan war.
 */
internal fun getEmbeds(clanTag: String, war: War, user: User): List<EmbedCreateSpec> {
    log.trace("Getting war status embed fields for war {}", war)

    return when (war.state) {
        State.NOT_IN_WAR -> {
            listOf(buildEmbed(user, BLUE, "Not in war", "War has ended"))
        }

        State.IN_MATCHMAKING, State.ENTER_WAR, State.MATCHED -> {
            listOf(buildEmbed(user, LIGHT_BLUE, description = "Searching for opponents", title = "Matchmaking"))
        }

        State.PREPARATION -> {
            val clanName: String = getClanName(clanTag, war)
            val opponentName: String = getOpponentName(clanTag, war)
            val timeUntilWar: Duration = Duration.between(ZonedDateTime.now(), war.warStartTime)

            listOf(
                buildEmbed(
                    user,
                    GREEN,
                    "\"$clanName\" VS \"$opponentName\" | Prep Day",
                    "$timeUntilWar remaining in prep day",
                )
            )
        }

        State.WAR, State.IN_WAR -> {
            val clanName: String = getClanName(clanTag, war)
            val opponentName: String = getOpponentName(clanTag, war)
            val timeLeftInWar: Duration = Duration.between(ZonedDateTime.now(), war.endTime)
            val warStats = getWarStats(clanTag, war)

            listOf(
                buildEmbed(
                    user,
                    YELLOW,
                    "\"$clanName\" VS \"$opponentName\" | War Day",
                    "$warStats\n$timeLeftInWar remaining in war",
                )
            ) + buildWarMemberEmbeds(clanTag, YELLOW, user, war)
        }

        State.ENDED -> {
            val clanName: String = getClanName(clanTag, war)
            val opponentName: String = getOpponentName(clanTag, war)
            val warStats = getWarStats(clanTag, war)

            listOf(
                buildEmbed(user, ORANGE, "\"$clanName\" VS \"$opponentName\" | War Ended", warStats)
            ) + buildWarMemberEmbeds(clanTag, ORANGE, user, war)
        }

        State.CLAN_NOT_FOUND -> {
            listOf(buildEmbed(user, RED, ERROR, "Clan not found"))
        }

        State.ACCESS_DENIED -> {
            listOf(buildEmbed(user, RED, ERROR, "Clan war log is not public"))
        }
    }
}

/* ******************************************* Private utility functions ******************************************** */

/**
 * Helper function to build a list of member attack embeds.
 *
 * @param clanTag The clan tag to get embeds for.
 * @param color The embed color.
 * @param user The user/post author that will be used to set embed author.
 * @param war The clan war to get embeds for.
 * @return The list of member attack embeds.
 */
private fun buildWarMemberEmbeds(clanTag: String, color: Color, user: User, war: War): List<EmbedCreateSpec> {
    log.trace("Building member attack embeds")

    return getMemberFields(clanTag, war).chunked(MAX_EMBED_FIELDS) { fields: List<EmbedCreateFields.Field> ->
        EmbedCreateSpec.builder()
            .author(user.username, AUTHOR_URL, user.avatarUrl)
            .color(color)
            .addAllFields(fields)
            .title("Member attack status")
            .thumbnail(getOpponentBadge(clanTag, war) ?: CLASH_LOGO_URL)
            .build()
    }
}

/**
 * Get [Option]<[WarClanMemberAttack]>s for a member in a clan war.
 *
 * @param member The clan war member to get [Option]<[WarClanMemberAttack]>s for.
 * @param attacksPerMember The number of attacks per member.
 * @return A list of [attacksPerMember] # elements of [Option]<[WarClanMemberAttack]>.
 */
private fun getAttackOptions(member: WarClanMember, attacksPerMember: Int): List<Option<WarClanMemberAttack>> =
    (0 until attacksPerMember).map { index: Int -> Option.fromNullable(member.attacks.getOrNull(index)) }

/**
 * Get the clan matching [clanTag] in [war].
 *
 * @param clanTag The clan tag to match.
 * @param war The war to search.
 * @return The clan if found, otherwise none.
 */
private fun getClan(clanTag: String, war: War): Option<WarClan> {
    log.trace("Getting clan")

    return if (war.clan?.tag?.endsWith(clanTag) == true) {
        Option.fromNullable(war.clan)
    } else if (war.opponent?.tag?.endsWith(clanTag) == true) {
        Option.fromNullable(war.opponent)
    } else {
        none()
    }
}

/**
 * Get the name of the clan matching [clanTag] in [war].
 *
 * @param clanTag The clan tag to match.
 * @param war The war to search.
 * @return The clan name if found, otherwise "Unknown".
 */
private fun getClanName(clanTag: String, war: War): String {
    log.trace("Getting clan name")

    return getClan(clanTag, war).map { clan: WarClan -> clan.name }.getOrElse { UNKNOWN }
}

/**
 * Get the formatted destruction percentage for a clan war.
 *
 * @param clanOption The [Option]<[WarClan]> to get the destruction percentage for.
 * @return The formatted destruction percentage.
 */
private fun getDestructionPercentage(clanOption: Option<WarClan>): String =
    ATTACK_PERCENTAGE_FORMAT.format(clanOption.map { it.destructionPercentage }.getOrElse { 0.0 })

/**
 * Get the member fields for a clan war.
 *
 * @param clanTag The clan tag to get member fields for.
 * @param war The clan war to get member fields for.
 * @return A list of [EmbedCreateFields.Field]s for a clan war.
 */
private fun getMemberFields(clanTag: String, war: War): List<EmbedCreateFields.Field> {
    val memberFields: MutableList<EmbedCreateFields.Field> = ArrayList()

    getClan(clanTag, war).onSome { warClan: WarClan ->
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

/**
 * Get the clan that does not match [clanTag] in [war].
 *
 * @param clanTag The clan tag to (not) match.
 * @param war The war to search.
 * @return The opponent clan if found, otherwise none.
 */
private fun getOpponent(clanTag: String, war: War): Option<WarClan> {
    log.trace("Getting opponent")

    return if (war.clan?.tag?.endsWith(clanTag) == true) {
        Option.fromNullable(war.opponent)
    } else if (war.opponent?.tag?.endsWith(clanTag) == true) {
        Option.fromNullable(war.clan)
    } else {
        none()
    }
}

/**
 * Get the badge URL of the clan that does not match [clanTag] in [war].
 *
 * @param clanTag The clan tag to (not) match.
 * @param war The war to search.
 * @return The opponent clan badge URL if found, otherwise null.
 */
private fun getOpponentBadge(clanTag: String, war: War): String? {
    log.trace("Getting opponent large badge URL")

    return getOpponent(clanTag, war).map { clan: WarClan -> clan.badgeURLs?.large }.getOrNull()
}

/**
 * Get the name of the clan that does not match [clanTag] in [war].
 *
 * @param clanTag The clan tag to (not) match.
 * @param war The war to search.
 * @return The opponent clan name if found, otherwise "Unknown".
 */
private fun getOpponentName(clanTag: String, war: War): String {
    log.trace("Getting opponent name")

    return getOpponent(clanTag, war).map { clan: WarClan -> clan.name }.getOrElse { UNKNOWN }
}

/**
 * Get the formatted stars for a clan war.
 *
 * @param stars The number of stars.
 * @return The formatted stars.
 */
private fun getStars(stars: Int): String = when (stars) {
    0 -> "☆☆☆"
    1 -> "☆☆★"
    2 -> "☆★★"
    else -> "★★★"
}

/**
 * Get the formatted war stats for a clan war.
 *
 * @param clanTag The clan tag to get war stats for.
 * @param war The clan war to get war stats for.
 * @return The formatted war stats.
 */
private fun getWarStats(clanTag: String, war: War): String {
    log.trace("Getting war stats")

    val clanOption: Option<WarClan> = getClan(clanTag, war)
    val opponentOption: Option<WarClan> = getOpponent(clanTag, war)
    val friendlyStars: Int = clanOption.map { warClan: WarClan -> warClan.stars }.getOrElse { 0 }
    val friendlyDestruction: String = getDestructionPercentage(clanOption)
    val opponentStars: Int = opponentOption.map { warClan: WarClan -> warClan.stars }.getOrElse { 0 }
    val opponentDestruction: String = getDestructionPercentage(opponentOption)
    val friendlyStats = "$friendlyStars ★ ($friendlyDestruction)"
    val opponentStats = "$opponentStars ★ ($opponentDestruction)"

    return "$friendlyStats | $opponentStats"
}
