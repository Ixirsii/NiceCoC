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

package tech.ixirsii.data

import arrow.core.Either
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War

/**
 * Clan War League Season for a clan.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
class ClanWarLeagueSeason(
    private val war1: War?,
    private val war2: War?,
    private val war3: War?,
    private val war4: War?,
    private val war5: War?,
    private val war6: War?,
    private val war7: War?,
) {
    /**
     * Currently active CWL war if any.
     */
    val activeWar: Option<War> =
        if (war1?.state == State.WAR || war1?.state == State.IN_WAR) {
            war1.some()
        } else if (war2?.state == State.WAR || war2?.state == State.IN_WAR) {
            war2.some()
        } else if (war3?.state == State.WAR || war3?.state == State.IN_WAR) {
            war3.some()
        } else if (war4?.state == State.WAR || war4?.state == State.IN_WAR) {
            war4.some()
        } else if (war5?.state == State.WAR || war5?.state == State.IN_WAR) {
            war5.some()
        } else if (war6?.state == State.WAR || war6?.state == State.IN_WAR) {
            war6.some()
        } else if (war7?.state == State.WAR || war7?.state == State.IN_WAR) {
            war7.some()
        } else {
            none()
        }

    /**
     * Get [ClanWarLeagueSeason] from a group of rounds.
     *
     * @param clanTag Tag of the clan to build [ClanWarLeagueSeason] for.
     * @param warRoundEithers List of war rounds.
     */
    constructor(clanTag: String, warRoundEithers: List<List<Either<ClashAPIError, War>>>) : this(
        getWar(clanTag, warRoundEithers.getOrNull(0)),
        getWar(clanTag, warRoundEithers.getOrNull(1)),
        getWar(clanTag, warRoundEithers.getOrNull(2)),
        getWar(clanTag, warRoundEithers.getOrNull(3)),
        getWar(clanTag, warRoundEithers.getOrNull(4)),
        getWar(clanTag, warRoundEithers.getOrNull(5)),
        getWar(clanTag, warRoundEithers.getOrNull(6)),
    )

    /* *************************************** Private utility functions **************************************** */

    private companion object {
        private fun getWar(clanTag: String, warEithers: List<Either<ClashAPIError, War>>?): War? =
            warEithers?.find { either: Either<ClashAPIError, War> ->
                either.isRight { it.clan?.tag == clanTag || it.opponent?.tag == clanTag }
            }?.getOrNull()
    }
}
