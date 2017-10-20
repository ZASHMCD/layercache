package com.mattdolan.layercache

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class CacheShould {

    lateinit var cache: Cache<String, String>

    @Before
    fun before() {

        cache = object : Cache<String, String> {
            override fun get(key: String): Deferred<String?> {
                return async(CommonPool) {
                    delay(500, TimeUnit.MILLISECONDS)

                    "value"
                }
            }

            override fun set(key: String, value: String): Deferred<Unit> {
                return async(CommonPool) {
                    delay(500, TimeUnit.MILLISECONDS)
                }
            }

            override fun evict(key: String): Deferred<Unit> {
                return async(CommonPool) {
                    delay(500, TimeUnit.MILLISECONDS)
                }
            }

            override fun evictAll(): Deferred<Unit> {
                return async(CommonPool) {
                    delay(500, TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    @Test(expected = CancellationException::class)
    fun `throw exception on get when job cancelled`() {
        runBlocking {
            // given we call get
            val job = cache.get("key")

            // when we cancel the job
            assertTrue(job.cancel())

            // then the job is cancelled and exception returned
            job.await()
        }
    }


    @Test(expected = CancellationException::class)
    fun `throw exception on set when job cancelled`() {
        runBlocking {
            // given we call set
            val job = cache.set("key", "value")

            // when we cancel the job
            assertTrue(job.cancel())

            // then the job is cancelled and exception returned
            job.await()
        }
    }

    @Test(expected = CancellationException::class)
    fun `throw exception on evict when job cancelled`() {
        runBlocking {
            // given we call evict
            val job = cache.evict("key")

            // when we cancel the job
            assertTrue(job.cancel())

            // then the job is cancelled and exception returned
            job.await()
        }
    }

    @Test
    fun `execute onSuccess when job completes as expected`() {
        runBlocking {
            // given we call get
            val job = cache.get("key")

            var called = false
            job.onSuccess { called = true }
            job.onFailure { fail() }
            job.onCompletion { fail() }

            // when we wait for the job
            job.await()

            delay(100, TimeUnit.MILLISECONDS)

            // then onSuccess is executed
            assertEquals(true, called)
        }
    }

    @Test
    fun `onFailure`() {
        runBlocking {
            cache = object : Cache<String, String> {
                override fun get(key: String): Deferred<String?> {
                    return async(CommonPool) {
                        throw Exception("Forced failure")
                    }
                }

                override fun set(key: String, value: String): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }

                override fun evict(key: String): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }

                override fun evictAll(): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }
            }


            // given we call evict
            val job = cache.get("key")

            job.onSuccess { println("Result $it") }
            job.onFailure { println("Exception $it") }

            // when we cancel the job
            //assertTrue(job.cancel())

            // then the job is cancelled and exception returned
            job.join()

            yield()
        }
    }

    @Test
    fun `onFailure cancelled`() {
        runBlocking {
            cache = object : Cache<String, String> {
                override fun get(key: String): Deferred<String?> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                        "value"
                    }
                }

                override fun set(key: String, value: String): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }

                override fun evict(key: String): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }

                override fun evictAll(): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }
            }


            // given we call evict
            val job = cache.get("key")

            job.onSuccess { result -> println("Result $result") }
            job.onFailure { exception -> println("Exception $exception") }
            job.onCancel { exception -> println("Cancelled exception $exception") }

            // when we cancel the job
            assertTrue(job.cancel())

            // then the job is cancelled and exception returned
            job.join()

            yield()
        }
    }


    @Test
    fun `onCompletion cancelled`() {
        runBlocking {
            cache = object : Cache<String, String> {
                override fun get(key: String): Deferred<String?> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                        "value"
                    }
                }

                override fun set(key: String, value: String): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }

                override fun evict(key: String): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }

                override fun evictAll(): Deferred<Unit> {
                    return async(CommonPool) {
                        delay(500, TimeUnit.MILLISECONDS)
                    }
                }
            }


            // given we call evict
            val job = cache.get("key")

            job.onCompletion {
                when (it) {
                    is DeferredResult.Success -> println("Result ${it.value}")
                    is DeferredResult.Failure -> println("Exception ${it.exception}")
                    is DeferredResult.Cancelled -> println("Cancelled exception ${it.exception}")
                }
            }

            job.onSuccess { value -> println("Result $value") }
            job.onFailure { exception -> println("Exception $exception") }
            job.onCancel { exception -> println("Cancelled exception $exception") }

            // when we cancel the job
            assertTrue(job.cancel())

            // then the job is cancelled and exception returned
            job.join()

            yield()
        }
    }

}