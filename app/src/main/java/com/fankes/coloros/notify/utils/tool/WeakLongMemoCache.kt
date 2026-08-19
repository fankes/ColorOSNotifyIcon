/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 20174 Fankes Studio(qzmmcn@163.com)
 * https://github.com/fankes/ColorOSNotifyIcon
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 * <p>
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 */
package com.fankes.coloros.notify.utils.tool

import java.util.WeakHashMap

/**
 * A weak-key memoization cache with a cheap [Long] source signature.
 *
 * Access is synchronized because [WeakHashMap] is not thread-safe. Only cheap lookup/store/clear
 * operations hold the monitor; callers keep expensive value production outside the lock.
 */
internal class WeakLongMemoCache<K : Any, V : Any> {

    private class Entry<V>(val signature: Long, val value: V)

    private val lock = Any()
    private val entries = WeakHashMap<K, Entry<V>>()

    /** Returns a cached value for [key] only when its source [signature] still matches. */
    fun get(key: K, signature: Long): V? = synchronized(lock) {
        entries[key]?.takeIf { it.signature == signature }?.value
    }

    /** Stores [value] for [key] and its current source [signature]. */
    fun put(key: K, signature: Long, value: V) = synchronized(lock) {
        entries[key] = Entry(signature, value)
    }

    /** Clears all memoized values, for example after preferences or package state changes. */
    fun clear() = synchronized(lock) { entries.clear() }

    /** Visible for local unit tests; querying size also lets [WeakHashMap] expunge stale keys. */
    internal fun sizeForTest() = synchronized(lock) { entries.size }
}
