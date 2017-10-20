package com.mattdolan.layercache

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

/**
 * Fetcher is a special kind of cache that is just used to retrieve data. It is not possible to cache any values
 * and so implements no-op for set and evict. An example would be a network fetcher.
 */
interface Fetcher<Key : Any, Value : Any> : Cache<Key, Value> {
    /**
     * No-op as Cache is a Fetcher
     */
    @Deprecated("set does nothing on a Fetcher")
    override fun set(key: Key, value: Value): Deferred<Unit> = async(CommonPool) {}

    /**
     * No-op as Cache is a Fetcher
     */
    @Deprecated("evict does nothing on a Fetcher")
    override fun evict(key: Key): Deferred<Unit> = async(CommonPool) {}

    /**
     * No-op as Cache is a Fetcher
     */
    @Deprecated("evictAll does nothing on a Fetcher")
    override fun evictAll(): Deferred<Unit> = async(CommonPool) {}
    
    @Deprecated("Use mapValues(transform) on a Fetcher", ReplaceWith("mapValues(transform)"))
    override fun <MappedValue : Any> mapValues(transform: (Value) -> MappedValue,
                                               inverseTransform: (MappedValue) -> Value): Fetcher<Key, MappedValue> {
        return mapValues(transform)
    }

    override fun <MappedKey : Any> mapKeys(transform: (MappedKey) -> Key): Fetcher<MappedKey, Value> {
        @Suppress("EmptyClassBlock")
        return object : Fetcher<MappedKey, Value>, MapKeysCache<Key, Value, MappedKey>(this@Fetcher, transform) {}
    }

    override fun <MappedKey : Any> mapKeys(transform: OneWayTransform<MappedKey, Key>): Fetcher<MappedKey, Value> =
            mapKeys(transform::transform)

    override fun reuseInflight(): Fetcher<Key, Value> {
        @Suppress("EmptyClassBlock")
        return object : Fetcher<Key, Value>, ReuseInflightCache<Key, Value>(this) {}
    }
}