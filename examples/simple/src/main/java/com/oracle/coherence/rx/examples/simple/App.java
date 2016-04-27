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

package com.oracle.coherence.rx.examples.simple;


import com.oracle.coherence.rx.RxNamedCache;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;
import com.tangosol.util.UUID;
import rx.observables.MathObservable;

import java.util.HashMap;
import java.util.Random;

import static com.oracle.coherence.rx.RxNamedCache.rx;
import static com.tangosol.util.Filters.equal;
import static com.tangosol.util.Filters.less;


/**
 * Driver for "simple" Coherence-Rx example.
 * <p>
 * This example shows how to use Reactive Extensions (RxJava) programming model
 * via Coherence-Rx to access Coherence caches via {@link RxNamedCache} interface.
 * <p>
 * These examples demonstrate "cold" Observables.
 *
 *
 * @author Tim Middleton  2016.04.27
 */
public class App
    {

    // ---- App methods -----------------------------------------------------

    public static void main(String[] asArgs) throws InterruptedException
        {
        System.setProperty("coherence.distributed.localstorage", "true");

        NamedCache<UUID, Trade> cache = CacheFactory.getTypedCache("trades",
                TypeAssertion.withTypes(UUID.class, Trade.class));

        cache.addIndex(Trade::getSymbol, true, null);
        cache.addIndex(Trade::getPrice, true, null);

        RxNamedCache<UUID, Trade> rxCache = rx(cache);

        cache.clear();
        createPositions(cache, 100);

        // get count of total positions
        rxCache.size()
               .subscribe(size -> System.out.println("Size is " + size ));

        // sleep only required to allowing us to see output separately
        sleep(5000L);

        // display all the trades
        rxCache.entrySet()
               .map(entry -> entry.getValue())
               .subscribe(System.out::println);

        // sleep only required to allowing us to see output separately
        sleep(10000L);

        // display only trades from ORCL using filter
        rxCache.entrySet(equal(Trade::getSymbol, "ORCL"))
               .map(entry -> entry.getValue())
               .subscribe(trade -> System.out.println("ORCL Trades: " + trade));

        // sleep only required to allowing us to see output separately
        sleep(5000L);

        // get total value of ORCL trades using coherence filter in entrySet
        MathObservable.averageDouble(rxCache.entrySet(equal(Trade::getSymbol, "ORCL"))
                                            .map(entry -> entry.getValue().getPurchaseValue()))
                      .subscribe(total -> System.out.println("Average Purchase Value of ORCL trades: " + String.format("$%10.2f", total)));

        // sleep only required to allowing us to see output separately
        sleep(5000L);

        // get number trades with purchase price < $30
        rxCache.keySet(less(Trade::getPrice, 30d))
               .count()
               .subscribe(result -> System.out.println("Number of trades purchased below $30.00 is " + result));

        sleep(10000L);

        System.exit(0);
    }

    /**
     * Sleep for a number of millis and display a message.
     *
     * @param ldtMillis   millis to sleep
     *
     * @throws InterruptedException
     */
    private static void sleep(long ldtMillis) throws InterruptedException
        {
        System.out.println("Sleeping " + ldtMillis + "ms");
        Thread.sleep(ldtMillis);
        }

    /**
     * Create "count" positions in the cache at the current price.
     *
     * @param nCount the number of entries to add
     */
    public static void createPositions( NamedCache<UUID, Trade> tradesCache, int nCount)
        {
        Random               random    = new Random();
        HashMap<UUID, Trade> mapTrades = new HashMap<>();

        for (int i = 0; i < nCount; i++)
            {
            String sSymbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            int    nAmount = random.nextInt(1000) + 1;
            double nPrice  = random.nextFloat() * ((MAX_FACTOR - MIN_FACTOR) + MIN_FACTOR) * INITIAL_PRICE;

            Trade  trade  = new Trade(sSymbol, nAmount, nPrice);

            mapTrades.put(trade.getId(), trade);
            }

        tradesCache.putAll(mapTrades);
        }

    // ---- constants -------------------------------------------------------

    /**
     * List of symbols to randomly select from.
     */
    private final static String[] SYMBOLS  = {"ORCL", "MSFT", "GOOG", "AAPL", "YHOO", "EMC"};

    /**
     * Minimum factor for random price.
     */
    private final static float MIN_FACTOR = 0.65f;

    /**
     * Maximum factor for random price.
     */
    private final static float MAX_FACTOR = 1.35f;

    /**
     * Initial price.
     */
    private final static double INITIAL_PRICE = 50;
}