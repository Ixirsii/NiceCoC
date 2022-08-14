package com.nicecoc.logging

import org.slf4j.Logger

/**
 * Utility interface to "inject" a [Logger] into classes which need it.
 *
 * @author Ryan Porterfield
 */
interface Logging {
    val log: Logger
}
