package com.oracle.coherence.rx;


import com.oracle.tools.deferred.Eventually;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;

import com.tangosol.util.InvocableMap;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.LongSum;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.GreaterFilter;

import java.util.Arrays;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.deferred;

import static org.hamcrest.CoreMatchers.is;

import static com.oracle.coherence.rx.RxNamedCache.rx;


/**
 * Unit tests for RxNamedCache class.
 *
 * @author Aleksandar Seovic  2015.02.25
 */
@SuppressWarnings("unchecked")
public class RxNamedCacheTest
    {
    protected <K, V> NamedCache<K, V> getNamedCache()
        {
        NamedCache<K, V> cache = CacheFactory.getTypedCache("test", TypeAssertion.withoutTypeChecking());
        cache.clear();
        return cache;
        }

    @Test
    public void testInvoke()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(2, 2);

        AtomicInteger result = new AtomicInteger(0);
        rx(cache).invoke(2, square())
                 .subscribe(result::set, Throwable::printStackTrace);

        Eventually.assertThat(deferred(result), is(4));
        }

    @Test
    public void testInvokeAll()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        AtomicInteger result = new AtomicInteger(0);
        rx(cache).invokeAll(square())
                 .subscribe(e -> System.out.println(e + ", result = " +
                                                    result.accumulateAndGet(e.getValue(), (a, b) -> a + b)));

        Eventually.assertThat(deferred(result), is(14));
        }

    @Test
    public void testInvokeAllWithKeySet()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        AtomicInteger result = new AtomicInteger(0);
        rx(cache).invokeAll(Arrays.asList(1, 2), square())
                 .subscribe(e -> System.out.println(e + ", result = " +
                                                    result.accumulateAndGet(e.getValue(), (a, b) -> a + b)));

        Eventually.assertThat(deferred(result), is(5));
        }

    @Test
    public void testInvokeAllWithFilter()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        AtomicInteger result = new AtomicInteger(0);
        rx(cache).invokeAll(GREATER_THAN_1, square())
                 .subscribe(e -> System.out.println(e + ", result = " +
                                                    result.accumulateAndGet(e.getValue(), (a, b) -> a + b)));

        Eventually.assertThat(deferred(result), is(13));
        }

    @Test
    public void testAggregate()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        AtomicLong result = new AtomicLong(0);
        rx(cache).aggregate(new LongSum<>(ValueExtractor.identity()))
                 .subscribe(result::set);

        Eventually.assertThat(deferred(result), is(6L));
        }

    @Test
    public void testAggregateWithKeySet()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        AtomicLong result = new AtomicLong(0);
        rx(cache).aggregate(Arrays.asList(1, 2), new LongSum<>(ValueExtractor.identity()))
                 .subscribe(result::set);

        Eventually.assertThat(deferred(result), is(3L));
        }

    @Test
    public void testAggregateWithFilter()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        AtomicLong result = new AtomicLong(0);
        rx(cache).aggregate(GREATER_THAN_1, new LongSum<>(ValueExtractor.identity()))
                 .subscribe(result::set);

        Eventually.assertThat(deferred(result), is(5L));
        }


    // ---- helpers ---------------------------------------------------------

    public static InvocableMap.EntryProcessor<Integer, Integer, Integer> square()
        {
        return entry -> entry.getValue() * entry.getValue();
        }

    protected static final GreaterFilter GREATER_THAN_1 = new GreaterFilter<>(IdentityExtractor.INSTANCE, 1);
    }
