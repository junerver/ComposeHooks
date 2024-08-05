package xyz.junerver.compose.hooks.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/*
  Description:
  Author: Junerver
  Date: 2024/8/5-8:39
  Email: junerver@gmail.com
  Version: v1.0
*/
@PublishedApi
internal object EventManager {
    private val subscriberMap = ConcurrentHashMap<KClass<*>, CopyOnWriteArrayList<(Any) -> Unit>>()
    private val aliasSubscriberMap =
        ConcurrentHashMap<String, CopyOnWriteArrayList<(Any?) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : Any> register(clazz: KClass<*>, subscriber: (T) -> Unit): () -> Unit {
        subscriberMap.computeIfAbsent(clazz) { CopyOnWriteArrayList() }
            .add(subscriber as (Any) -> Unit)
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
        aliasSubscriberMap.computeIfAbsent(alias) { CopyOnWriteArrayList() }
            .add(subscriber as (Any?) -> Unit)
        return {
            aliasSubscriberMap[alias]?.remove(subscriber)
        }
    }

    internal fun <T> post(alias: String, event: T) {
        aliasSubscriberMap[alias]?.forEach { (it as (T?) -> Unit).invoke(event) }
    }
}
