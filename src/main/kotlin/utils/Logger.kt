package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun <T : Any> getLogger(clazz: KClass<T>): Logger =
    LoggerFactory.getLogger(clazz.java)

fun <T : Any> T.getLogger(): Logger =
    getLogger(this::class)

fun getLogger(name: String): Logger =
    LoggerFactory.getLogger(name)