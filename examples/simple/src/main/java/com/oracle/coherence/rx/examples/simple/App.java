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

import java.util.Map;
import rx.observables.MathObservable;

import java.util.HashMap;
import java.util.Random;

import static com.oracle.coherence.rx.RxNamedCache.rx;

import static com.tangosol.net.cache.TypeAssertion.withTypes;

import static com.tangosol.util.Filters.equal;
import static com.tangosol.util.Filters.less;


/**
 * Driver for "simple" Coherence-Rx example.
 * <p>
 * This example shows how to use Reactive Extensions (RxJava) programming model
 * via CoherenceRx to access Coherence caches via {@link RxNamedCache} interface.
 * <p>
 * These examples demonstrate "cold" Observables.
 *
 * @author Tim Middleton  2016.04.27
 */
public class App
    {

    // ---- App methods -----------------------------------------------------

    public static void main(String[] asArgs) throws InterruptedException
        {
        System.setProperty("coherence.distributed.localstorage", "true");

        NamedCache<UUID, Trade> cache =
                CacheFactory.getTypedCache("trades", withTypes(UUID.class, Trade.class));

        cache.addIndex(Trade::getSymbol, true, null);
        cache.addIndex(Trade::getPrice, true, null);

        RxNamedCache<UUID, Trade> rxCache = rx(cache);

        cache.clear();
        createPositions(cache, 100);

        // get count of total positions
        rxCache.size()
               .toBlocking()
               .subscribe(size -> System.out.println("Size is " + size));

        // display all the trades
        rxCache.entrySet()
               .map(Map.Entry::getValue)
               .toBlocking()
               .subscribe(System.out::println);

        // display only trades from ORCL using filter
        rxCache.entrySet(equal(Trade::getSymbol, "ORCL"))
               .map(Map.Entry::getValue)
               .toBlocking()
               .subscribe(trade -> System.out.println("ORCL trade: " + trade));

        // get total value of ORCL trades using Coherence filter in values() call
        MathObservable.averageDouble(rxCache.values(equal(Trade::getSymbol, "ORCL"))
                                            .map(trade -> trade.getPurchaseValue())
                                            .toBlocking())
                      .subscribe(total -> System.out.printf("Average Purchase Value of ORCL trades: $%10.2f\n", total)));

        // get number trades with purchase price < $30
        rxCache.keySet(less(Trade::getPrice, 30d))
               .count()
               .toBlocking()
               .subscribe(result -> System.out.println("Number of trades purchased below $30.00 is " + result));
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