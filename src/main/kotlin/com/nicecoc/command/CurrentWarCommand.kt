package com.nicecoc.command

import com.lycoon.clashapi.core.ClashAPI
import com.lycoon.clashapi.models.war.War
import com.lycoon.clashapi.models.war.enums.WarState
import com.nicecoc.data.blue
import com.nicecoc.data.green
import com.nicecoc.data.lightBlue
import com.nicecoc.data.orange
import com.nicecoc.data.red
import com.nicecoc.data.yellow
import com.nicecoc.logging.Logging
import com.nicecoc.logging.LoggingImpl
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.Color
import discord4j.rest.util.Image
import org.koin.core.annotation.Single

/**
 * Command to get info about the current war.
 *
 * @author Ryan Porterfield
 */
@Single
class CurrentWarCommand(
    private val clashAPI: ClashAPI,
) : Command, Logging by LoggingImpl<CurrentWarCommand>() {
    /** [ApplicationCommandRequest] to register the command with Discord APIs. */
    override val request: ApplicationCommandRequest
        get() = ApplicationCommandRequest.builder()
            .name(name)
            .description("Get current war information")
            .build()

    /** Command name. */
    override val name: String = "current_war"

    /** Midwest Warrior Clan tag */
    private val clanTag: String = "2Q82UJVY"

    /**
     * Function which can be registered to listen to chat input interaction events.
     *
     * @param event Chat command event.
     */
    override fun listener(event: ChatInputInteractionEvent) {
        if (event.commandName == name) {
            event.deferReply().subscribe()

            val color: Color
            val description: String
            // TODO: Catch exceptions
            val title: String = clashAPI.getClan(clanTag).name
            val war: War = clashAPI.getCurrentWar(clanTag)

            war.clan?.name

            when (war.state) {
                WarState.CLAN_NOT_FOUND, WarState.ACCESS_DENIED -> {
                    color = red
                    description = "Error getting war status"
                }

                WarState.WAR, WarState.IN_WAR -> {
                    color = yellow
                    description = "War day against ${war.opponent?.name}"
                }

                WarState.PREPARATION -> {
                    color = green
                    description = "Prep day against ${war.opponent?.name}"
                }

                WarState.IN_MATCHMAKING, WarState.ENTER_WAR, WarState.MATCHED -> {
                    color = lightBlue
                    description = "Searching for war"
                }

                WarState.NOT_IN_WAR -> {
                    color = blue
                    description = "No active war"
                }

                WarState.ENDED -> {
                    color = orange
                    description = "War has ended"
                }
            }

            val embed: EmbedCreateSpec = EmbedCreateSpec.builder()
                .author(
                    event.client.self.block()?.username ?: "",
                    "https://github.com/Ixirsii/NiceCoC",
                    event.client.self.block()?.avatarUrl ?: "",
                )
                .color(color)
                .title(title)
                .description(description)
                .build()

            event.editReply().withEmbeds(embed).subscribe()
        }
    }
}
