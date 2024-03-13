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

package tech.ixirsii.module

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.google.common.eventbus.EventBus
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import tech.ixirsii.data.Config
import tech.ixirsii.klash.client.ClashAPI
import tech.ixirsii.listener.DiscordListener
import tech.ixirsii.logging.Logging
import tech.ixirsii.logging.LoggingImpl
import java.io.File

/**
 * Koin module for bot dependencies.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Module
class NiceCoCModule : Logging by LoggingImpl<NiceCoCModule>() {

    /**
     * Singleton provider for [ClashAPI].
     *
     * @return [ClashAPI] singleton.
     */
    @Single
    fun clashAPI(configOption: Option<Config>): Option<ClashAPI> = configOption.map { config: Config ->
        ClashAPI(config.clashOfClansToken)
    }

    /**
     * Singleton provider for [Config].
     *
     * @return [Config] singleton.
     */
    @Single
    fun config(userConfigFile: File, yamlMapper: ObjectMapper): Option<Config> = if (userConfigFile.exists()) {
        yamlMapper.readValue(userConfigFile, Config::class.java).some()
    } else {
        log.warn("User config file does not exist at {}", userConfigFile.absolutePath)

        none()
    }

    /**
     * Singleton provider for [EventBus].
     *
     * @return [EventBus] singleton.
     */
    @Single
    fun eventBus(): EventBus = EventBus()

    /**
     * Singleton provider for [GatewayDiscordClient].
     *
     * @return [GatewayDiscordClient] singleton.
     */
    @Single
    fun gatewayDiscordClient(
        configOption: Option<Config>,
        discordListener: DiscordListener
    ): Option<GatewayDiscordClient> = configOption.map { config: Config ->
        val client: GatewayDiscordClient =
            DiscordClient.create(config.discordToken)
                .gateway()
                .withEventDispatcher { it.on(ReadyEvent::class.java).doOnNext(discordListener::readyEventListener) }
                .login()
                .block()!!

        client.eventDispatcher.on(ChatInputInteractionEvent::class.java, discordListener::chatInputInteractionListener)
            .subscribe()

        client
    }

    /**
     * Singleton provider for the default config.yaml resource path.
     *
     * @return the default config.yaml resource path.
     */
    @Named("resourceFilePath")
    @Single
    fun resourceFilePath(): String = CONFIG_FILE_NAME

    /**
     * Singleton provider for the user's config.yaml file.
     *
     * @return the user's config.yaml file.
     */
    @Single
    fun userConfigFile(@Named("userConfigFilePath") path: String): File = File(path)

    /**
     * Singleton provider for the user's config.yaml file path.
     *
     * @return the user's config.yaml file path.
     */
    @Named("userConfigFilePath")
    @Single
    fun userConfigFilePath(): String = CONFIG_DIRECTORY + CONFIG_FILE_NAME

    /**
     * Singleton provider for [ObjectMapper].
     *
     * @return [ObjectMapper] singleton.
     */
    @Single
    fun yamlMapper(): ObjectMapper = ObjectMapper(YAMLFactory())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        .registerModule(Jdk8Module())
        .registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullToEmptyCollection, true)
                .configure(KotlinFeature.NullToEmptyMap, true)
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .build()
        )

    /* *************************************** Private utility functions **************************************** */

    private companion object {
        /**
         * Bot configuration directory.
         */
        const val CONFIG_DIRECTORY = "config/"

        /**
         * Default config resource file path.
         */
        private const val CONFIG_FILE_NAME: String = "config.yaml"
    }
}
