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

import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import tech.ixirsii.logging.Logging

/**
 * Base class for functions that build embeds.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
abstract class GetEmbedsFunction<T> : Logging {

    /**
     * Get a mono of list of embeds for a command response.
     *
     * @param clanTag Clan tag.
     * @param dataMono Mono containing the data that will be used to build the embeds.
     * @param userMono Mono containing the user/post author that will be used to set embed author.
     * @return mono of list of embeds for a command response.
     */
    protected fun getEmbeds(
        clanTag: String,
        dataMono: Mono<T>,
        userMono: Mono<User>
    ): Mono<List<EmbedCreateSpec>> {
        log.trace("Asynchronously building war status embed")

        return Mono.zip(dataMono, userMono)
            .map { tuple: Tuple2<T, User> -> getEmbeds(clanTag, tuple.t1, tuple.t2) }
    }

    /**
     * Get list of embeds for a command response.
     *
     * @param clanTag Clan tag.
     * @param data Data that will be used to build the embeds.
     * @param user User/post author that will be used to set embed author.
     */
    protected abstract fun getEmbeds(clanTag: String, data: T, user: User): List<EmbedCreateSpec>
}
