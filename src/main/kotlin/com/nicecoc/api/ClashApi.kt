package com.nicecoc.api

import com.lycoon.clashapi.core.ClashAPI
import com.lycoon.clashapi.models.clan.Clan
import com.lycoon.clashapi.models.clan.ClanMember
import com.lycoon.clashapi.models.clan.ClanRanking
import com.lycoon.clashapi.models.clan.ClanVersusRanking
import com.lycoon.clashapi.models.common.Label
import com.lycoon.clashapi.models.common.Location
import com.lycoon.clashapi.models.league.League
import com.lycoon.clashapi.models.league.LeagueSeason
import com.lycoon.clashapi.models.player.Player
import com.lycoon.clashapi.models.player.PlayerRanking
import com.lycoon.clashapi.models.player.PlayerVersusRanking
import com.lycoon.clashapi.models.war.War
import com.lycoon.clashapi.models.war.WarlogEntry
import com.lycoon.clashapi.models.warleague.WarLeague
import com.lycoon.clashapi.models.warleague.WarLeagueGroup
import org.koin.core.annotation.Single

/**
 * Wrapper for [ClashAPI].
 *
 * @author Ryan Porterfield
 */
@Single
class ClashApi(private val clashAPI: ClashAPI) {
    /** No Type 2.0 Clan tag */
    val clanTag: String = "2PUVOROPR"

    /**
     * Get clan details.
     */
    fun getClan(): Clan = clashAPI.getClan(clanTag)

    /**
     * Get clan labels.
     */
    fun getClanLabels(): List<Label> = clashAPI.getClanLabels()

    /**
     * Get clan members.
     */
    fun getClanMembers(): List<ClanMember> = clashAPI.getClanMembers(clanTag)

    /**
     * Get clan rankings for a location.
     *
     * @param locationId Location to get rankings from.
     */
    fun getClanRankings(locationId: String): List<ClanRanking> = clashAPI.getClanRankings(locationId)

    /**
     * Get clan versus rankings for a location.
     *
     * @param locationId Location to get rankings from.
     */
    fun getClanVersusRankings(locationId: String): List<ClanVersusRanking> = clashAPI.getClanVersusRankings(locationId)

    /**
     * Get current war information.
     */
    fun getCurrentWar(): War = clashAPI.getCurrentWar(clanTag)

    /**
     * Get information for a specific League.
     *
     * @param leagueId League ID.
     */
    fun getLeague(leagueId: String): League = clashAPI.getLeague(leagueId)

    /**
     * Get rankings for a specific League in a specific season.
     *
     * @param leagueId League ID.
     * @param seasonId League season ID.
     */
    fun getLeagueSeasonRankings(leagueId: String, seasonId: String): List<PlayerRanking> =
        clashAPI.getLeagueSeasonRankings(leagueId, seasonId)

    /**
     * List seasons for a specific League.
     *
     * @param leagueId League ID.
     */
    fun getLeagueSeasons(leagueId: String): List<LeagueSeason> = clashAPI.getLeagueSeasons(leagueId)

    /**
     * List all Leagues.
     */
    fun getLeagues(): List<League> = clashAPI.getLeagues()

    /**
     * Get location information by ID.
     *
     * @param locationId Location ID.
     */
    fun getLocation(locationId: String): Location = clashAPI.getLocation(locationId)

    /**
     * List all locations.
     */
    fun getLocations(): List<Location> = clashAPI.getLocations()

    /**
     * Get player by ID.
     *
     * @param playerTag Player ID.
     */
    fun getPlayer(playerTag: String): Player = clashAPI.getPlayer(playerTag)

    /**
     * List all player labels.
     */
    fun getPlayerLabels(): List<Label> = clashAPI.getPlayerLabels()

    /**
     * Get player rankings for a specific location.
     *
     * @param locationId Location ID.
     */
    fun getPlayerRankings(locationId: String): List<PlayerRanking> = clashAPI.getPlayerRankings(locationId)

    /**
     * Get player versus rankings for a specific location.
     *
     * @param locationId Location ID.
     */
    fun getPlayerVersusRankings(locationId: String): List<PlayerVersusRanking> =
        clashAPI.getPlayerVersusRankings(locationId)

    /**
     *
     */
    fun getWarLeague(leagueId: String): WarLeague = clashAPI.getWarLeague(leagueId)

    fun getWarLeagueGroup(): WarLeagueGroup = clashAPI.getWarLeagueGroup(clanTag)

    fun getWarLeagueWar(warTag: String): War = clashAPI.getWarLeagueWar(warTag)

    fun getWarLeagues(): List<WarLeague> = clashAPI.getWarLeagues()

    fun getWarlog(): List<WarlogEntry> = clashAPI.getWarlog(clanTag)

    fun isVerifiedPlayer(playerTag: String, token: String): Boolean = clashAPI.isVerifiedPlayer(playerTag, token)
}
