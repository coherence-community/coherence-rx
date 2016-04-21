package com.oracle.coherence.rx;


import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.WrapperNamedCache;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

import java.util.HashMap;
import java.util.Set;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;

import rx.observables.ConnectableObservable;

import rx.subscriptions.Subscriptions;


/**
 * Observable implementation of a Coherence MapListener.
 * <p>
 * This is a 'hot' Observable which will start receiving events as soon as it's
 * registered with a cache via {@link NamedCache#addMapListener} method.
 *
 * @author Aleksandar Seovic  2015.02.22
 */
public class ObservableMapListener<K, V>
        extends Observable<MapEvent<? extends K, ? extends V>>
        implements MapListener<K, V>
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Create ObservableMapListener instance.
     *
     * @return an ObservableMapListener
     */
    public static <K, V> ObservableMapListener<K, V> create()
        {
        Set<Subscriber<? super MapEvent<? extends K, ? extends V>>> subscribers = new CopyOnWriteArraySet<>();
        return new ObservableMapListener<>(subscriber ->
                       {
                       subscriber.add(Subscriptions.create(() -> subscribers.remove(subscriber)));

                       if (!subscriber.isUnsubscribed())
                           {
                           subscribers.add(subscriber);
                           }
                       }, subscribers);
        }

    /**
     * Construct ObservableMapListener instance.
     *
     * @param onSubscribe  the function to execute when {@link #subscribe(Subscriber)} is called
     * @param subscribers  the set of registered subscribers
     */
    protected ObservableMapListener(Observable.OnSubscribe<MapEvent<? extends K, ? extends V>> onSubscribe,
                                    Set<Subscriber<? super MapEvent<? extends K, ? extends V>>> subscribers)
        {
        super(onSubscribe);

        m_subscribers = subscribers;
        }

    // ---- MapListener methods ---------------------------------------------

    public void entryInserted(MapEvent<K, V> evt)
        {
        onMapEvent(evt);
        }

    public void entryUpdated(MapEvent<K, V> evt)
        {
        onMapEvent(evt);
        }

    public void entryDeleted(MapEvent<K, V> evt)
        {
        onMapEvent(evt);
        }

    private void onMapEvent(MapEvent<K, V> evt)
        {
        m_subscribers.forEach(s -> s.onNext(evt));
        }

    // ---- data members ----------------------------------------------------

    /**
     * A set of active subscribers.
     */
    protected Set<Subscriber<? super MapEvent<? extends K, ? extends V>>> m_subscribers;

    public static void main(String[] args) throws InterruptedException
        {
        WrapperNamedCache<Integer, String> cache = new WrapperNamedCache<>(new HashMap<>(), "test");

        ObservableMapListener<Integer, String> listener = ObservableMapListener.create();
        cache.addMapListener(listener);

        ConnectableObservable<MapEvent<? extends Integer, ? extends String>> hot = listener.publish();
        hot.connect();
        Thread.sleep(600);

        ConnectableObservable<MapEvent<? extends Integer, ? extends String>> observable = hot.replay();
        observable.connect();
        observable.subscribe(System.out::println);

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        cache.put(2, "TWO");
        cache.remove(3);

        Thread.sleep(600);

        observable.groupBy(MapEvent::getKey).subscribe(o -> o.buffer(1, TimeUnit.SECONDS).subscribe(System.out::println));
        Thread.sleep(2000);
        }
    }
