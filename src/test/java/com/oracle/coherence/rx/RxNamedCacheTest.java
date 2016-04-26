/*
 * File: RxNamedCacheTest.java
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates.
 *
 * You may not use this file except in compliance with the Universal Permissive
 * License (UPL), Version 1.0 (the "License.")
 *
 * You may obtain a copy of the License at https://opensource.org/licenses/UPL.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.oracle.coherence.rx;


import com.oracle.tools.junit.CoherenceClusterOrchestration;
import com.oracle.tools.junit.SessionBuilder;
import com.oracle.tools.junit.SessionBuilders;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.java.options.SystemProperty;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.InvocableMap;
import com.tangosol.util.ValueExtractor;

import com.tangosol.util.aggregator.LongSum;

import com.tangosol.util.extractor.IdentityExtractor;

import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.GreaterFilter;

import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.oracle.coherence.rx.RxNamedCache.rx;

import static org.junit.Assert.*;


/**
 * Unit tests for RxNamedCacheImpl class.
 *
 * @author Aleksandar Seovic  2015.02.25
 */
@SuppressWarnings("unchecked")
public class RxNamedCacheTest
    {
    @ClassRule
    public static final CoherenceClusterOrchestration ORCHESTRATION =
            new CoherenceClusterOrchestration()
                    .withOptions(
                            SystemProperty.of("coherence.nameservice.address", LocalPlatform.get().getLoopbackAddress().getHostAddress())
                    );

    protected static final GreaterFilter GREATER_THAN_1 = new GreaterFilter<>(IdentityExtractor.INSTANCE, 1);
    protected static final GreaterFilter GREATER_THAN_2 = new GreaterFilter<>(IdentityExtractor.INSTANCE, 2);

    protected <K, V> NamedCache<K, V> getNamedCache()
        {
        ConfigurableCacheFactory cacheFactory = ORCHESTRATION.getSessionFor(SessionBuilders.storageDisabledMember());
        NamedCache               cache        = cacheFactory.ensureCache("test", null);

        cache.clear();

        return cache;
        }

    @Test
    public void testGet()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");

        assertEquals("one", rx(cache).get(1).toBlocking().single());
        assertEquals(null, rx(cache).get(2).toBlocking().single());
        }


    @Test
    public void testGetAll()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        assertEquals(Arrays.asList("one", "two"),
                     rx(cache).getAll(Arrays.asList(1, 2, 5))
                             .map(Map.Entry::getValue)
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testPut()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        rx(cache).put(1, "one").toBlocking().singleOrDefault(null);

        assertEquals("one", cache.get(1));
        }

    @Test
    public void testPutAll()
        {
        NamedCache<Integer, String> cache = getNamedCache();
        Map<Integer, String>        map   = new HashMap<>();

        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");

        rx(cache).putAll(map).toBlocking().singleOrDefault(null);

        assertEquals("one", cache.get(1));
        assertEquals("two", cache.get(2));
        assertEquals("three", cache.get(3));
        }

    @Test
    public void testRemove()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");

        assertEquals("one", rx(cache).remove(1).toBlocking().single());
        assertEquals(null, cache.get(1));
        }

    @Test
    public void testRemoveAll()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        rx(cache).removeAll(Arrays.asList(2, 3)).toBlocking().singleOrDefault(null);

        assertNull(cache.get(2));
        assertNull(cache.get(3));
        }

    @Test
    public void testRemoveAllWithFilter()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        rx(cache).removeAll(AlwaysFilter.INSTANCE).toBlocking().singleOrDefault(null);

        assertNull(cache.get(1));
        assertNull(cache.get(2));
        assertNull(cache.get(3));
        }

    @Test
    public void testKeySet()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        assertEquals(Arrays.asList(1, 2, 3),
                     rx(cache).keySet()
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testEntrySet()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        assertEquals(Arrays.asList("one", "three", "two"),
                     rx(cache).entrySet()
                             .map(Map.Entry::getValue)
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testValues()
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        assertEquals(Arrays.asList("one", "three", "two"),
                     rx(cache).values()
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testInvoke()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(2, 2);

        assertEquals(4, (int) rx(cache).invoke(2, square()).toBlocking().single());
        }

    @Test
    public void testInvokeAll()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        assertEquals(Arrays.asList(1, 4, 9),
                     rx(cache).invokeAll(square())
                             .map(Map.Entry::getValue)
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testInvokeAllWithKeySet()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        assertEquals(Arrays.asList(1, 4),
                     rx(cache).invokeAll(Arrays.asList(1, 2), square())
                             .map(Map.Entry::getValue)
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testInvokeAllWithFilter()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        assertEquals(Arrays.asList(4, 9),
                     rx(cache).invokeAll(GREATER_THAN_1, square())
                             .map(Map.Entry::getValue)
                             .toSortedList()
                             .toBlocking()
                             .single());
        }

    @Test
    public void testAggregate()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        assertEquals(6L,
                     (long) rx(cache).aggregate(new LongSum<>(ValueExtractor.identity()))
                             .toBlocking()
                             .single());
        }

    @Test
    public void testAggregateWithKeySet()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        assertEquals(3L,
                     (long) rx(cache).aggregate(Arrays.asList(1, 2), new LongSum<>(ValueExtractor.identity()))
                             .toBlocking()
                             .single());
        }

    @Test
    public void testAggregateWithFilter()
        {
        NamedCache<Integer, Integer> cache = getNamedCache();

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);

        assertEquals(5L,
                     (long) rx(cache).aggregate(GREATER_THAN_1, new LongSum<>(ValueExtractor.identity()))
                             .toBlocking()
                             .single());
        }

    // ---- Map methods -----------------------------------------------------

    @Test
    public void testSizeContainsClearIsEmpty() throws Exception
        {
        NamedCache<Integer, String> cache = getNamedCache();

        assertEquals(0, (int) rx(cache).size().toBlocking().single());
        assertTrue(rx(cache).isEmpty().toBlocking().single());
        assertFalse(rx(cache).containsKey(1).toBlocking().single());

        cache.put(1, "one");
        cache.put(2, "two");

        assertEquals(2, (int) rx(cache).size().toBlocking().single());
        assertFalse(rx(cache).isEmpty().toBlocking().single());
        assertTrue(rx(cache).containsKey(1).toBlocking().single());

        rx(cache).clear().toBlocking().singleOrDefault(null);
        assertEquals(0, (int) rx(cache).size().toBlocking().single());
        assertTrue(rx(cache).isEmpty().toBlocking().single());
        assertFalse(rx(cache).containsKey(1).toBlocking().single());
        }

    @Test
    public void testGetOrDefault() throws Exception
        {
        NamedCache<Integer, String> cache = getNamedCache();

        cache.put(2, "two");

        assertEquals("one", rx(cache).getOrDefault(1, "one").toBlocking().single());
        assertEquals("two", rx(cache).getOrDefault(2, "TWO").toBlocking().single());
        }

    @Test
    public void testPutIfAbsent() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        assertNull(rx(cache).putIfAbsent("1", 1).toBlocking().single());
        assertEquals(1, (int) rx(cache).putIfAbsent("1", 100).toBlocking().single());
        cache.put("2", null);
        assertNull(rx(cache).putIfAbsent("2", 2).toBlocking().single());
        assertEquals(2, cache.size());
        assertEquals(2, (int) cache.get("2"));
        }

    @Test
    public void testRemoveMatching() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);

        assertFalse(rx(cache).remove("1", 2).toBlocking().single());
        assertTrue(rx(cache).remove("2", 2).toBlocking().single());

        assertEquals(1, cache.size());
        assertTrue(cache.containsKey("1"));
        assertFalse(cache.containsKey("2"));
        }

    @Test
    public void testReplace() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", null);

        assertEquals(1, (int) rx(cache).replace("1", 100).toBlocking().single());
        assertNull(rx(cache).replace("2", 200).toBlocking().single());
        assertNull(rx(cache).replace("3", 300).toBlocking().single());

        assertEquals(2, cache.size());
        assertFalse(cache.containsKey("3"));
        }

    @Test
    public void testReplaceWithValueCheck() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", null);
        cache.put("3", null);

        assertTrue(rx(cache).replace("1", 1, 100).toBlocking().single());
        assertFalse(rx(cache).replace("2", 2, 200).toBlocking().single());
        assertTrue(rx(cache).replace("3", null, 300).toBlocking().single());
        assertFalse(rx(cache).replace("4", 4, 400).toBlocking().single());

        assertEquals(100, (int) cache.get("1"));
        assertNull(cache.get("2"));
        assertEquals(300, (int) cache.get("3"));
        assertFalse(cache.containsKey("4"));
        }

    @Test
    public void testComputeIfAbsent() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("five", 5);
        assertEquals(1, (int) rx(cache).computeIfAbsent("1", Integer::parseInt).toBlocking().single());
        assertEquals(5, (int) rx(cache).computeIfAbsent("five", Integer::parseInt).toBlocking().single());
        assertNull(rx(cache).computeIfAbsent("null", (k) -> null).toBlocking().single());
        }

    @Test
    public void testComputeIfPresent() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);

        assertEquals(2, (int) rx(cache).computeIfPresent("1", (k, v) -> v + v).toBlocking().single());
        assertEquals(4, (int) rx(cache).computeIfPresent("2", (k, v) -> v * v).toBlocking().single());
        assertNull(rx(cache).computeIfPresent("1", (k, v) -> null).toBlocking().single());
        assertNull(rx(cache).computeIfPresent("3", (k, v) -> v * v).toBlocking().single());

        assertEquals(4, (int) cache.get("2"));
        assertEquals(1, cache.size());
        assertFalse(cache.containsKey("1"));
        }

    @Test
    public void testCompute() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);

        assertEquals(2, (int) rx(cache).compute("1", (k, v) -> v + v).toBlocking().single());
        assertNull(rx(cache).compute("2", (k, v) -> null).toBlocking().single());
        assertFalse(cache.containsKey("2"));
        }

    @Test
    public void testMerge() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);

        assertEquals(2, (int) rx(cache).merge("1", 1, (v1, v2) -> v1 + v2).toBlocking().single());
        assertEquals(3, (int) rx(cache).merge("2", 1, (v1, v2) -> v1 + v2).toBlocking().single());
        assertEquals(1, (int) rx(cache).merge("3", 1, (v1, v2) -> v1 + v2).toBlocking().single());
        assertNull(rx(cache).merge("1", 1, (v1, v2) -> null).toBlocking().single());

        assertFalse(cache.containsKey("1"));
        assertTrue(cache.containsKey("3"));
        }

    @Test
    public void testReplaceAll() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);
        cache.put("3", 3);

        rx(cache).replaceAll((k, v) -> v * v).toBlocking().singleOrDefault(null);
        assertEquals(1, (int) cache.get("1"));
        assertEquals(4, (int) cache.get("2"));
        assertEquals(9, (int) cache.get("3"));
        }

    @Test
    public void testReplaceAllWithKeySet() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);
        cache.put("3", 3);

        rx(cache).replaceAll(Arrays.asList("1", "3"), (k, v) -> v * v).toBlocking().singleOrDefault(null);
        assertEquals(1, (int) cache.get("1"));
        assertEquals(2, (int) cache.get("2"));
        assertEquals(9, (int) cache.get("3"));
        }

    @Test
    public void testReplaceAllWithFilter() throws Exception
        {
        NamedCache<String, Integer> cache = getNamedCache();

        cache.put("1", 1);
        cache.put("2", 2);
        cache.put("3", 3);

        rx(cache).replaceAll(GREATER_THAN_2, (k, v) -> v * v).toBlocking().singleOrDefault(null);
        assertEquals(1, (int) cache.get("1"));
        assertEquals(2, (int) cache.get("2"));
        assertEquals(9, (int) cache.get("3"));
        }

    // ---- helpers ---------------------------------------------------------

    public static InvocableMap.EntryProcessor<Integer, Integer, Integer> square()
        {
        return entry -> entry.getValue() * entry.getValue();
        }
    }
