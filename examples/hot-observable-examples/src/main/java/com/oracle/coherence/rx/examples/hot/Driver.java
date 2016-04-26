package com.oracle.coherence.rx.examples.hot;

import com.oracle.tools.Options;
import com.oracle.tools.junit.StorageDisabledMember;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.coherence.options.ClusterName;
import com.oracle.tools.runtime.coherence.options.LocalHost;
import com.oracle.tools.runtime.coherence.options.Logging;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;
import com.tangosol.util.UUID;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.deferred.Eventually.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Driver for Coherence-Rx "hot" demo.
 */
public class Driver
    {
    /**
     * Cluster name;
     */
    protected static final String CLUSTER_NAME = "CoherenceRxDemoCluster";

    public static void main(String ... args)
    {
         int    clusterSize = 3;
         final  String CACHE = "device-readings";

        // use oracle-tools to startup 3 cache servers
        LocalPlatform platform = LocalPlatform.get();
        CoherenceClusterBuilder builder = new CoherenceClusterBuilder();

        builder.include(clusterSize,
                        platform,
                        CoherenceClusterMember.class,
                       // LocalHost.only(),
                        Logging.at(2),
                        ClusterName.of(CLUSTER_NAME));

        try
        {
            CoherenceCluster cluster = builder.build(SystemApplicationConsole.builder());

            // wait until all 3 members started
            assertThat(invoking(cluster).getClusterSize(), is(clusterSize));

            // retrieve one of the cluster members
            CoherenceClusterMember clusterMember = cluster.iterator().next();

            // ask the member for a NamedCache"proxy"
            NamedCache<UUID, DeviceReading> cache = clusterMember.getCache(CACHE, UUID.class, DeviceReading.class);

            cache.addIndex(DeviceReading::getDeviceId, true, null);

            // start the data generator UI
            new DataGenerator(cache).init();

            // create storage-disabled client and get the CCF
            Options options = clusterMember.getOptions();

            ConfigurableCacheFactory ccf = new StorageDisabledMember().build(platform, null, options.asArray());
            NamedCache<UUID, DeviceReading> clientCache = ccf.ensureTypedCache(CACHE, null, TypeAssertion.withTypes(UUID.class, DeviceReading.class));

            // start the data monitor UI
            new DataMonitor(clientCache).init();
        }
        catch (Exception e)
        {
            System.err.println("Failed to start cluster");
            e.printStackTrace();
        }

    }
}
