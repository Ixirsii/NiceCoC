package com.nicecoc.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Implementation which injects the [Logger].
 *
 * @author Ryan Porterfield
 */
class LoggingImpl(override val log: Logger) : Logging {
    companion object {
        inline operator fun <reified T> invoke(): LoggingImpl {
            return LoggingImpl(LoggerFactory.getLogger(T::class.java))
        }
    }
}
