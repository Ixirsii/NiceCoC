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

package tech.ixirsii.api

import discord4j.core.GatewayDiscordClient
import discord4j.rest.service.ApplicationService
import org.koin.core.annotation.Single
import reactor.core.publisher.Mono
import tech.ixirsii.command.Command
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl

/**
 * Discord API implementation.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Single
class DiscordApi(
    /**
     * Discord4J gateway client.
     */
    private val client: GatewayDiscordClient,
    /**
     * All bot commands.
     */
    private val commands: Map<String, Command>,
) : AutoCloseable, Logging by LoggingImpl<DiscordApi>(), Runnable {

    /**
     * Register bot commands.
     */
    fun init() {
        log.trace("Initializing Discord API")

        val applicationId: Long = client.restClient.applicationId.block() ?: 0L
        val applicationService: ApplicationService = client.restClient.applicationService

        commands.forEach { (_, command: Command) ->
            applicationService.createGuildApplicationCommand(applicationId, IXI_BOT_ID, command.request)
                .onErrorResume { throwable: Throwable ->
                    log.error("Failed to add command \"{}\" to IxiBot server", command.name, throwable)
                    Mono.empty()
                }
                .subscribe()
//            applicationService.createGuildApplicationCommand(applicationId, MIDWEST_WARRIOR_ID, command.request)
//                .onErrorResume { throwable: Throwable ->
//                    log.error("Failed to add command \"{}\" to Midwest Warrior server", command.name, throwable)
//                    Mono.empty()
//                }
//                .subscribe()
        }
    }

    /**
     * Close the client connection and log out.
     */
    override fun close() {
        log.trace("Closing Discord gateway")

        client.logout().block()
    }

    /**
     * Wait for the client to disconnect.
     */
    override fun run() {
        log.trace("Waiting for Discord client to disconnect")

        client.onDisconnect().block()
    }

    /* *************************************** Private utility functions **************************************** */

    private companion object {
        /**
         * IxiBot guild ID.
         */
        private const val IXI_BOT_ID: Long = 452233740408717313L

        /**
         * Midwest Warrior guild ID.
         */
//        private const val MIDWEST_WARRIOR_ID: Long = 1073848977637789707L
    }
}
