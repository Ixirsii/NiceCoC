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
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import tech.ixirsii.data.BLUE
import tech.ixirsii.data.GREEN
import tech.ixirsii.data.LIGHT_BLUE
import tech.ixirsii.data.ORANGE
import tech.ixirsii.data.RED
import tech.ixirsii.data.YELLOW
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl

/**
 * Command to get info about the current war.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
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
     * Function which can be registered to listen to chat input interaction events.
     *
     * @param event Chat command event.
     */
    override fun listener(event: ChatInputInteractionEvent): Mono<Any> = event.deferReply()
        .then(buildEmbed(event.client.self, clashAPI.currentWar(MIDWEST_WARRIOR)))
        .flatMap { embed: EmbedCreateSpec -> event.editReply().withEmbeds(embed) }

    /* *************************************** Private utility functions **************************************** */

    private fun buildEmbed(
        userMono: Mono<User>,
        warMono: Mono<Either<ClashAPIError, War>>,
    ): Mono<EmbedCreateSpec> = Mono.zip(userMono, warMono)
        .map { tuple: Tuple2<User, Either<ClashAPIError, War>> ->
            log.trace("Building war status embed")

            val color: Color
            val description: String
            val title: String

            when (tuple.t2.map { war: War -> war.state }) {
                Either.Right(State.WAR), Either.Right(State.IN_WAR) -> {
                    val opponent: String = tuple.t2.map { war: War -> war.opponent?.name }.getOrNull() ?: "Unknown"

                    color = YELLOW
                    description = "War day against $opponent"
                    title = tuple.t2.map { war: War -> war.clan?.name }.getOrNull() ?: "Clan name"
                }

                Either.Right(State.PREPARATION) -> {
                    val opponent: String = tuple.t2.map { war: War -> war.opponent?.name }.getOrNull() ?: "Unknown"

                    color = GREEN
                    description = "Prep day against $opponent"
                    title = tuple.t2.map { war: War -> war.clan?.name }.getOrNull() ?: "Clan name"
                }

                Either.Right(State.IN_MATCHMAKING), Either.Right(State.ENTER_WAR), Either.Right(State.MATCHED) -> {
                    color = LIGHT_BLUE
                    description = "Searching for war"
                    title = "Matchmaking"
                }

                Either.Right(State.NOT_IN_WAR) -> {
                    color = BLUE
                    description = "No active war"
                    title = "Not in war"
                }

                Either.Right(State.ENDED) -> {
                    color = ORANGE
                    description = "War has ended"
                    title = "War ended"
                }

                else -> {
                    color = RED
                    description = "Error getting war status"
                    title = "Error"
                }
            }

            EmbedCreateSpec.builder()
                .author(tuple.t1.username, "https://github.com/Ixirsii/NiceCoC", tuple.t1.avatarUrl)
                .color(color)
                .description(description)
                .title(title)
                .build()
        }

    private companion object {
        /**
         * Midwest Warrior Clan tag.
         */
        private const val MIDWEST_WARRIOR: String = "2Q82UJVY"
    }
}
