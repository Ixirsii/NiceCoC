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

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import reactor.core.publisher.Mono
import tech.ixirsii.data.Clan
import tech.ixirsii.function.CurrentWarFunction
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl
import kotlin.jvm.optionals.getOrElse

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
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(CLAN_OPTION_NAME)
                    .description("Clan to get war info for")
                    .type(ApplicationCommandOption.Type.STRING.value)
                    .addChoice(
                        ApplicationCommandOptionChoiceData.builder()
                            .name(Clan.MIDWEST_WARRIOR.clanName)
                            .value(Clan.MIDWEST_WARRIOR.tag)
                            .build()
                    )
                    .addChoice(
                        ApplicationCommandOptionChoiceData.builder()
                            .name(Clan.JJK.clanName)
                            .value(Clan.JJK.tag)
                            .build()
                    )
                    .required(false)
                    .build()
            )
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
        .then(
            Mono.just(
                event.getOption(CLAN_OPTION_NAME)
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .getOrElse { Clan.MIDWEST_WARRIOR.tag }
            )
        )
        .flatMap { clanTag: String -> CurrentWarFunction(clanTag, clashAPI).getCurrentWarEmbeds(event.client.self) }
        .flatMap { embeds: List<EmbedCreateSpec> -> event.editReply().withEmbedsOrNull(embeds) }

    /* *************************************** Private utility functions **************************************** */

    private companion object {
        private const val CLAN_OPTION_NAME = "clan"
    }
}
