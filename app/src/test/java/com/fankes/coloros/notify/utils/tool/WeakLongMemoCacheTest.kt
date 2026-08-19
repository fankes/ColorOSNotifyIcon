/*
 * ColorOSNotifyIcon - Optimize notification icons for ColorOS and adapt to native notification icon specifications.
 * Copyright (C) 20174 Fankes Studio(qzmmcn@163.com)
 * https://github.com/fankes/ColorOSNotifyIcon
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 */
package com.fankes.coloros.notify.utils.tool

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.lang.ref.WeakReference

class WeakLongMemoCacheTest {

    private enum class Decision { KEEP_ORIGINAL, REPLACE }

    @Test
    fun sameKeyAndSignatureHitsCache() {
        val cache = WeakLongMemoCache<Any, Decision>()
        val key = Any()
        cache.put(key, 1L, Decision.REPLACE)

        assertEquals(Decision.REPLACE, cache.get(key, 1L))
        assertEquals(1, cache.sizeForTest())
    }

    @Test
    fun replacementDecisionIsReusedWithoutProducer() {
        val cache = WeakLongMemoCache<Any, Any>()
        val key = Any()
        val replacement = Any()
        var producerCalls = 0

        fun resolve(): Any = cache.get(key, 7L) ?: run {
            producerCalls++
            replacement.also { cache.put(key, 7L, it) }
        }

        assertSame(replacement, resolve())
        assertSame(replacement, resolve())
        assertEquals(1, producerCalls)
    }

    @Test
    fun negativeDecisionIsCached() {
        val cache = WeakLongMemoCache<Any, Decision>()
        val key = Any()
        var producerCalls = 0

        fun resolve(): Decision = cache.get(key, 11L) ?: run {
            producerCalls++
            Decision.KEEP_ORIGINAL.also { cache.put(key, 11L, it) }
        }

        assertEquals(Decision.KEEP_ORIGINAL, resolve())
        assertEquals(Decision.KEEP_ORIGINAL, resolve())
        assertEquals(1, producerCalls)
    }

    @Test
    fun signatureChangeMissesAndRecomputes() {
        val cache = WeakLongMemoCache<Any, Decision>()
        val key = Any()
        cache.put(key, 1L, Decision.KEEP_ORIGINAL)

        assertNull(cache.get(key, 2L))
        cache.put(key, 2L, Decision.REPLACE)
        assertEquals(Decision.REPLACE, cache.get(key, 2L))
    }

    @Test
    fun clearInvalidatesCachedDecision() {
        val cache = WeakLongMemoCache<Any, Decision>()
        val key = Any()
        cache.put(key, 3L, Decision.REPLACE)

        cache.clear()

        assertNull(cache.get(key, 3L))
        assertEquals(0, cache.sizeForTest())
    }

    @Test
    fun weakKeyDoesNotPreventGarbageCollection() {
        val cache = WeakLongMemoCache<Any, Decision>()
        val keyReference = putTemporaryKey(cache)

        repeat(50) {
            if (keyReference.get() == null) return@repeat
            System.gc()
            System.runFinalization()
            ByteArray(64 * 1024)
        }

        assertNull("Weak cache must not keep NotificationEntry-like keys alive", keyReference.get())
        assertEquals(0, cache.sizeForTest())
    }

    private fun putTemporaryKey(cache: WeakLongMemoCache<Any, Decision>): WeakReference<Any> {
        val key = Any()
        cache.put(key, 5L, Decision.REPLACE)
        return WeakReference(key)
    }
}
