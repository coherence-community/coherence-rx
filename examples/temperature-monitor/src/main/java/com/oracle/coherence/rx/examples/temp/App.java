/*
 * File: App.java
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

package com.oracle.coherence.rx.examples.temp;


import com.oracle.bedrock.Options;
import com.oracle.bedrock.junit.StorageDisabledMember;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.Logging;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;
import com.oracle.bedrock.runtime.java.options.Headless;
import com.oracle.bedrock.runtime.java.options.HeapSize;

import com.oracle.coherence.rx.RxNamedCache;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;

import com.tangosol.util.UUID;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static com.oracle.bedrock.deferred.Eventually.assertThat;
import static org.hamcrest.CoreMatchers.is;


/**
 * Driver for "Temperature Monitor" Coherence-Rx example.
 * <p>
 * This example shows how to use Reactive Extensions (RxJava) programming model
 * via Coherence-Rx to access Coherence caches via {@link RxNamedCache} interface.
 * <p>
 * This examples demonstrates "Hot" Observables via two main classes:
 * <ul>
 *     <li>{@link DataGenerator} - Displays a GUI allowing random temperature values to be
 *     emitted (inserted into a cache) for 3 pseudo devices.</li>
 *     <li>{@link DataMonitor} - uses "Hot" observables to monitor data from the
 *     devices and display various information.</li>
 * </ul>
 * <p>
 * On running the App class, Oracle-Tools is used to startup 3 cluster members
 * and a storage-disabled client.
 *
 * @author Tim Middleton  2016.04.27
 */
public class App
    {
    // ----- App methods ----------------------------------------------------

    public static void main(String[] asArgs)
        {
        int nClusterSize = 3;

        // create a new builder and include what we want to build
        CoherenceClusterBuilder builder = new CoherenceClusterBuilder();
        builder.include(nClusterSize,
                        CoherenceClusterMember.class,
                        Logging.at(2),
                        HeapSize.initial(1, HeapSize.Units.GB),
                        HeapSize.maximum(1, HeapSize.Units.GB),
                        Headless.disabled(),
                        ClusterName.of(CLUSTER_NAME));

        try
            {
            // start the cluster members
            CoherenceCluster cluster = builder.build(SystemApplicationConsole.builder());

            // wait until all 3 members started
            assertThat(invoking(cluster).getClusterSize(), is(nClusterSize));

            // retrieve one of the cluster members
            CoherenceClusterMember clusterMember = cluster.iterator().next();

            // start storage-disabled client and get the CCF
            Options options = clusterMember.getOptions();
            ConfigurableCacheFactory ccf = new StorageDisabledMember().build(LocalPlatform.get(), null, options.asArray());

            // obtain named cache from the client
            NamedCache<UUID, DeviceReading> cache =
                    ccf.ensureTypedCache(CACHE, null, TypeAssertion.withTypes(UUID.class, DeviceReading.class));

            cache.addIndex(DeviceReading::getDeviceId, true, null);

            // start the data generator GUI
            new DataGenerator(cache).init();

            // start the data monitor UI
            new DataMonitor(cache).init();
            }
        catch (Exception e)
            {
            System.err.println("Failed to start cluster");
            e.printStackTrace();
            }
        }

    // ---- constants -------------------------------------------------------

    /**
     * Cluster name.
     */
    private static final String CLUSTER_NAME = "CoherenceRxDemoCluster";

    /**
     * Cache name.
     */
    private static final String CACHE = "device-readings";
    }