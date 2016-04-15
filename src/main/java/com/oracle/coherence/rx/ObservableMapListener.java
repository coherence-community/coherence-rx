/*
 * File: ObservableMapListener.java
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

import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MultiplexingMapListener;
import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.subscriptions.Subscriptions;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * Observable implementation of a Coherence MapListener.
 * <p>
 * This is a 'hot' Observable which will start receiving events as soon as it's
 * registered with a cache via {@link NamedCache#addMapListener} method.
 *
 * @author Aleksandar Seovic  2015.02.22
 */
public class ObservableMapListener<K, V> extends MultiplexingMapListener<K, V> implements MapListener<K, V>
{
    // ---- data members ----------------------------------------------------

    /**
     * A set of active subscribers.
     */
    protected Set<Subscriber<? super MapEvent<? extends K, ? extends V>>> m_subscribers = new CopyOnWriteArraySet<>();


    // ---- MultiplexingMapListener methods ---------------------------------

    @Override
    public void onMapEvent(MapEvent<K, V> evt)
    {
        m_subscribers.forEach(s -> s.onNext(evt));
    }


    // ---- helper methods --------------------------------------------------

    /**
     * Return an {@link Observable} for this MapListener.
     *
     * @return an Observable based on this MapListener
     */
    public Observable<MapEvent<? extends K, ? extends V>> toObservable()
    {
        return Observable.create(new ToObservableMapListener<>(this));
    }


    /**
     * Register {@link Subscriber} from this MapListener.
     *
     * @param subscriber  the subscriber to register
     */
    protected void subscribe(Subscriber<? super MapEvent<? extends K, ? extends V>> subscriber)
    {
        m_subscribers.add(subscriber);
    }


    /**
     * Unregister {@link Subscriber} from this MapListener.
     *
     * @param subscriber  the subscriber to unregister
     */
    protected void unsubscribe(Subscriber<? super MapEvent<? extends K, ? extends V>> subscriber)
    {
        m_subscribers.remove(subscriber);
    }


    public static void main(String[] args) throws InterruptedException
    {
        WrapperNamedCache<Integer, String>     cache    = new WrapperNamedCache<>(new HashMap<>(), "test");

        ObservableMapListener<Integer, String> listener = new ObservableMapListener<>();

        cache.addMapListener(listener);

        ConnectableObservable<MapEvent<? extends Integer, ? extends String>> hot = listener.toObservable().publish();

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

        observable.groupBy(MapEvent::getKey)
        .subscribe(o -> o.buffer(1, TimeUnit.SECONDS).subscribe(System.out::println));
        Thread.sleep(2000);
    }


    // ---- inner class: ToObservableMapListener ----------------------------

    static class ToObservableMapListener<K, V> implements Observable.OnSubscribe<MapEvent<? extends K, ? extends V>>
    {
        private ObservableMapListener<K, V> m_listener;


        ToObservableMapListener(ObservableMapListener<K, V> listener)
        {
            m_listener = listener;
        }


        public void call(Subscriber<? super MapEvent<? extends K, ? extends V>> subscriber)
        {
            subscriber.add(Subscriptions.create(() -> m_listener.unsubscribe(subscriber)));

            if (!subscriber.isUnsubscribed())
            {
                m_listener.subscribe(subscriber);
            }
        }
    }
}
