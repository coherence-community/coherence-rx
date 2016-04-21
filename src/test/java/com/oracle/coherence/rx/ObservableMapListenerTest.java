package com.oracle.coherence.rx;


import com.oracle.tools.junit.CoherenceClusterOrchestration;
import com.oracle.tools.junit.SessionBuilder;
import com.oracle.tools.junit.SessionBuilders;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.java.options.SystemProperty;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import org.junit.ClassRule;
import org.junit.Test;


/**
 * Unit tests for ObservableMapListener class.
 *
 * @author Aleksandar Seovic  2016.04.20
 */
public class ObservableMapListenerTest
    {
    @ClassRule
    public static final CoherenceClusterOrchestration ORCHESTRATION =
            new CoherenceClusterOrchestration().withOptions(
                    SystemProperty.of("coherence.nameservice.address", LocalPlatform.get().getLoopbackAddress().getHostAddress())
            );

    public static final SessionBuilder MEMBER = SessionBuilders.storageDisabledMember();

    protected <K, V> NamedCache<K, V> getNamedCache()
        {
        ConfigurableCacheFactory cacheFactory = ORCHESTRATION.getSessionFor(MEMBER);

        NamedCache cache = cacheFactory.ensureCache("test", null);
        cache.clear();
        return cache;
        }

    @Test
    public void testGet()
        {

        }
    }
