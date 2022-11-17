package com.nicecoc.command

import com.lycoon.clashapi.models.war.War
import com.nicecoc.api.ClashApi
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
    private val clashApi: ClashApi
): Command, Logging by LoggingImpl<CurrentWarCommand>() {
    /** [ApplicationCommandRequest] to register the command with Discord APIs. */
    override val request: ApplicationCommandRequest
        get() = ApplicationCommandRequest.builder()
            .name(name)
            .description("Get current war information")
            .build()

    /** Command name. */
    override val name: String = "current_war"

    // TODO: Move these somewhere else
    /** Material theme red 600. */
    private val red = Color.of(0xE53935)
    /** Material theme yellow 500. */
    private val yellow = Color.of(0xFFEB3B)
    /** Material theme green 500. */
    private val green = Color.of(0x4CAF50)

    /**
     * Function which can be registered to listen to chat input interaction events.
     *
     * @param event Chat command event.
     */
    override fun listener(event: ChatInputInteractionEvent) {
        if (event.commandName == "current_war") {
            event.deferReply()

            val war: War = clashApi.getCurrentWar()

            val applicationInfo: ApplicationInfo? = event.client.applicationInfo.block()
            val color: Color
            val title: String

            when (war.state) {
                "active" -> {
                    color = green
                    title = "War day against ${war.opponent?.name}"
                }

                "preparation" -> {
                    color = yellow
                    title = "Prep day against ${war.opponent?.name}"
                }

                else -> {
                    color = red
                    title = "No active war"
                }
            }

            val embed: EmbedCreateSpec = EmbedCreateSpec.builder()
                .author(applicationInfo?.name ?: "", "https://bitbucket.org/phrionhaus/nicecoc/src/main/", applicationInfo?.getIcon(Image.Format.PNG)?.block()?.dataUri ?: "")
                .color(color)
                .title(title)
                .build()

            event.editReply().withEmbeds(embed).subscribe()
        }
    }
}
