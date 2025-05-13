package org.bibletranslationtools.docscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.koin.android.ext.android.inject
import org.slf4j.LoggerFactory


class MainActivity : ComponentActivity() {
    val directoryProvider: DirectoryProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLogback()

        setContent {
            App()
        }
    }

    private fun configureLogback() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        lc.stop()

        // setup FileAppender
        val fileEncoder = PatternLayoutEncoder()
        fileEncoder.context = lc
        fileEncoder.pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        fileEncoder.start()

        val fileAppender = FileAppender<ILoggingEvent>()
        fileAppender.context = lc
        fileAppender.file = directoryProvider.logFile.toString()
        fileAppender.encoder = fileEncoder
        fileAppender.start()

        // setup LogcatAppender
        val logcatEncoder = PatternLayoutEncoder()
        logcatEncoder.context = lc
        logcatEncoder.pattern = "[%thread] %msg%n"
        logcatEncoder.start()

        val logcatAppender = LogcatAppender()
        logcatAppender.context = lc
        logcatAppender.encoder = logcatEncoder
        logcatAppender.start()

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.addAppender(fileAppender)
        root.addAppender(logcatAppender)
    }
}
