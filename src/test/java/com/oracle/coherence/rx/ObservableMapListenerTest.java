/*
 * File: ObservableMapListenerTest.java
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
import com.oracle.tools.junit.SessionBuilders;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.java.options.SystemProperty;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.MapEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Unit tests for ObservableMapListener class.
 *
 * @author Aleksandar Seovic  2016.04.20
 */
@SuppressWarnings("unchecked")
public class ObservableMapListenerTest
    {
    @ClassRule
    public static final CoherenceClusterOrchestration ORCHESTRATION =
            new CoherenceClusterOrchestration().withOptions(
                    SystemProperty.of("coherence.nameservice.address", LocalPlatform.get().getLoopbackAddress().getHostAddress())
            );

    protected <K, V> NamedCache<K, V> getNamedCache()
        {
        ConfigurableCacheFactory cacheFactory = ORCHESTRATION.getSessionFor(SessionBuilders.storageDisabledMember());
        NamedCache               cache        = cacheFactory.ensureCache("test", null);

        cache.clear();

        return cache;
        }

    @Test
    public void testObservableMapListener() throws Exception
        {
        NamedCache<Integer, String> cache = getNamedCache();

        List<MapEvent<Integer, String>> expected = new ArrayList<>(5);
        expected.add(new MapEvent<>(cache, MapEvent.ENTRY_INSERTED, 1, null, "one"));
        expected.add(new MapEvent<>(cache, MapEvent.ENTRY_INSERTED, 2, null, "two"));
        expected.add(new MapEvent<>(cache, MapEvent.ENTRY_INSERTED, 3, null, "three"));
        expected.add(new MapEvent<>(cache, MapEvent.ENTRY_UPDATED, 2, "two", "TWO"));
        expected.add(new MapEvent<>(cache, MapEvent.ENTRY_DELETED, 3, "three", null));

        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger nIndex = new AtomicInteger(0);

        ObservableMapListener<Integer, String> listener = ObservableMapListener.create();
        listener.subscribe(evt ->
                           {
                           latch.countDown();
                           System.out.println(evt);
                           assertEvent(expected.get(nIndex.getAndIncrement()), evt);
                           });

        cache.addMapListener(listener);

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        cache.put(2, "TWO");
        cache.remove(3);

        latch.await();
        }

    private void assertEvent(MapEvent<Integer, String> expected, MapEvent<Integer, String> actual)
        {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getOldValue(), actual.getOldValue());
        assertEquals(expected.getNewValue(), actual.getNewValue());
        }
    }
