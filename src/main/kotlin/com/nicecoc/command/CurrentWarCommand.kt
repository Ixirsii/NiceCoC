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
            event.deferReply()

            val war: War = clashAPI.getCurrentWar(clanTag)

            val applicationInfo: ApplicationInfo? = event.client.applicationInfo.block()
            val color: Color
            val title: String

            when (war.state) {
                WarState.CLAN_NOT_FOUND, WarState.ACCESS_DENIED -> {
                    color = red
                    title = "Error getting war status"
                }

                WarState.WAR, WarState.IN_WAR -> {
                    color = yellow
                    title = "War day against ${war.opponent?.name}"
                }

                WarState.PREPARATION -> {
                    color = green
                    title = "Prep day against ${war.opponent?.name}"
                }

                WarState.IN_MATCHMAKING, WarState.ENTER_WAR, WarState.MATCHED -> {
                    color = lightBlue
                    title = "Searching for war"
                }

                WarState.NOT_IN_WAR -> {
                    color = blue
                    title = "No active war"
                }

                WarState.ENDED -> {
                    color = orange
                    title = "War has ended"
                }
            }

            val embed: EmbedCreateSpec = EmbedCreateSpec.builder()
                .author(
                    applicationInfo?.name ?: "",
                    "https://bitbucket.org/phrionhaus/nicecoc/src/main/",
                    applicationInfo?.getIcon(Image.Format.PNG)?.block()?.dataUri ?: ""
                )
                .color(color)
                .title(title)
                .build()

            event.editReply().withEmbeds(embed).subscribe()
        }
    }
}
