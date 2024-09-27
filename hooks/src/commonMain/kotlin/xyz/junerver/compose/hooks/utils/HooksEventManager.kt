package xyz.junerver.compose.hooks.utils

import kotlin.reflect.KClass

/*
  Description:
  Author: Junerver
  Date: 2024/8/5-8:39
  Email: junerver@gmail.com
  Version: v1.0
*/
object HooksEventManager {
    private val subscriberMap = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()
    private val aliasSubscriberMap =
        mutableMapOf<String, MutableList<(Any?) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : Any> register(clazz: KClass<*>, subscriber: (T) -> Unit): () -> Unit {
        subscriberMap.getOrPut(clazz) { mutableListOf() }.add(subscriber as (Any) -> Unit)
        return {
            subscriberMap[clazz]?.remove(subscriber)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T> post(event: T & Any, clazz: KClass<*>) {
        subscriberMap[clazz]?.forEach { (it as (T) -> Unit).invoke(event) }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T> register(alias: String, subscriber: (T) -> Unit): () -> Unit {
        aliasSubscriberMap.getOrPut(alias) { mutableListOf() }.add(subscriber as (Any?) -> Unit)
        return {
            aliasSubscriberMap[alias]?.remove(subscriber)
        }
    }

    internal fun <T> post(alias: String, event: T) {
        aliasSubscriberMap[alias]?.forEach { (it as (T?) -> Unit).invoke(event) }
    }

    inline fun <reified T : Any> register(noinline subscriber: (T) -> Unit): () -> Unit = register(T::class, subscriber)

    inline fun <reified T : Any> post(event: T) = post(event, T::class)
}
