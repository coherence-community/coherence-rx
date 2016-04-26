package com.oracle.coherence.rx.examples.cold;

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

/**
 * Created by timmiddleton on 26/04/16.
 */
public class Driver
{
    private final static String[] SYMBOLS  = {"ORCL", "MSFT", "GOOG", "AAPL", "YHOO", "EMC"};
    private final static float    MIN_FACTOR             = 0.65f;
    private final static float    MAX_FACTOR             = 1.35f;
    private final static double   INITIAL_PRICE          = 50;


    public static void main(String ... args) throws InterruptedException
    {
        System.setProperty("coherence.distributed.localstorage", "true");

        NamedCache<UUID, Trade> cache = CacheFactory.getTypedCache("trades",
                TypeAssertion.withTypes(UUID.class, Trade.class));
        cache.addIndex(Trade::getSymbol, true, null);

        RxNamedCache<UUID, Trade> rxCache = rx(cache);

        cache.clear();
        createPositions(cache, 100);

        // get count of total positions
        rxCache.size()
               .subscribe(size -> System.out.println("Size is " + size ));

        // display all the trades
        rxCache.entrySet()
               .map(entry -> entry.getValue())
               .subscribe(trade -> System.out.println("All Trades: " + trade));

        // display only trades from ORCL using filter
        rxCache.entrySet()
               .map(entry -> entry.getValue())
               .filter(trade -> trade.getSymbol().equals("ORCL"))
               .subscribe( trade -> System.out.println("ORCL Trades: " + trade));

        // get total value of ORCL trades using coherence filter in entrySet
        MathObservable.averageDouble(rxCache.entrySet(equal(Trade::getSymbol, "ORCL"))
                                            .map(entry -> entry.getValue().getPurchaseValue()))
                      .subscribe(total -> System.out.println("Total ORCL trades: " + total));

        // sleep for enough time to let values complete
        System.out.println("Sleeping 10");
        Thread.sleep(10000L);

        System.exit(0);
    }

    /**
     * Create "count" positions in the cache at the current price.
     *
     * @param count the number of entries to add
     */
    public static void createPositions( NamedCache<UUID, Trade> tradesCache, int count)
    {
        Random               random = new Random();
        HashMap<UUID, Trade> trades = new HashMap<>();

        for (int i = 0; i < count; i++)
        {
            // create a random position
            String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            int    amount = random.nextInt(1000) + 1;
            double price  = random.nextFloat() * (MAX_FACTOR - MIN_FACTOR) + MIN_FACTOR * INITIAL_PRICE;

            Trade  trade  = new Trade(symbol, amount, price);

            trades.put(trade.getId(), trade);
        }

        tradesCache.putAll(trades);

    }

}
