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

import arrow.core.Either
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.Color
import org.koin.core.annotation.Single
import reactor.core.publisher.Mono
import reactor.util.function.Tuple3
import tech.ixirsii.data.BLUE
import tech.ixirsii.data.GREEN
import tech.ixirsii.data.LIGHT_BLUE
import tech.ixirsii.data.ORANGE
import tech.ixirsii.data.RED
import tech.ixirsii.data.YELLOW
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.clan.Clan
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl

/**
 * Command to get info about the current war.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Single
class CurrentWarCommand(private val clashAPI: ClashAPI) : Command, Logging by LoggingImpl<CurrentWarCommand>() {
    /**
     * [ApplicationCommandRequest] to register the command with Discord APIs.
     */
    override val request: ApplicationCommandRequest
        get() = ApplicationCommandRequest.builder()
            .name(name)
            .description("Get current war information")
            .build()

    /**
     * Command name.
     */
    override val name: String = "current_war"

    /**
     * Midwest Warrior Clan tag.
     */
    private val clanTag: String = "2Q82UJVY"

    /**
     * Function which can be registered to listen to chat input interaction events.
     *
     * @param event Chat command event.
     */
    override fun listener(event: ChatInputInteractionEvent): Mono<Any> {
        if (event.commandName != name) {
            return Mono.empty()
        }

        return event.deferReply().then(
            CurrentWarEvent(clashAPI.clan(clanTag), event.client.self, clashAPI.currentWar(clanTag))
                .buildEmbed()
                .map { embed: EmbedCreateSpec ->
                    event.editReply().withEmbeds(embed)
                }
        )
    }

    /* *************************************** Private utility functions **************************************** */

    private class CurrentWarEvent(
        private val clanMono: Mono<Either<ClashAPIError, Clan>>,
        private val userMono: Mono<User>,
        private val warMono: Mono<Either<ClashAPIError, War>>,
    ) {

        fun buildEmbed(): Mono<EmbedCreateSpec> {
            return Mono.zip(clanMono, userMono, warMono)
                .map { tuple: Tuple3<Either<ClashAPIError, Clan>, User, Either<ClashAPIError, War>> ->
                    val color: Color
                    val description: String
                    val opponent: String = tuple.t3.map { war: War -> war.opponent?.name }.getOrNull() ?: "Unknown"

                    when (tuple.t3.map { war: War -> war.state }) {
                        Either.Right(State.WAR), Either.Right(State.IN_WAR) -> {
                            color = YELLOW
                            description = "War day against $opponent"
                        }

                        Either.Right(State.PREPARATION) -> {
                            color = GREEN
                            description = "Prep day against $opponent"
                        }

                        Either.Right(State.IN_MATCHMAKING),
                        Either.Right(State.ENTER_WAR),
                        Either.Right(State.MATCHED) -> {
                            color = LIGHT_BLUE
                            description = "Searching for war"
                        }

                        Either.Right(State.NOT_IN_WAR) -> {
                            color = BLUE
                            description = "No active war"
                        }

                        Either.Right(State.ENDED) -> {
                            color = ORANGE
                            description = "War has ended"
                        }

                        else -> {
                            color = RED
                            description = "Error getting war status"
                        }
                    }

                    EmbedCreateSpec.builder()
                        .author(tuple.t2.username, "https://github.com/Ixirsii/NiceCoC", tuple.t2.avatarUrl)
                        .color(color)
                        .description(description)
                        .title(tuple.t1.map { clan: Clan -> clan.name }.getOrNull() ?: "Clan name")
                        .build()
                }
        }
    }
}
