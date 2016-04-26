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

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

import java.util.Set;

import java.util.concurrent.CopyOnWriteArraySet;

import rx.Observable;
import rx.Subscriber;

import rx.subscriptions.Subscriptions;


/**
 * Observable implementation of a Coherence MapListener.
 * <p>
 * This is a 'hot' Observable which will start emitting events as soon as it's
 * registered with a cache via the {@link NamedCache#addMapListener} method.
 *
 * See <a href="http://reactivex.io/documentation/observable.html">Observable
 * documentation</a> for the explanation of 'hot' vs 'cold' observables.
 *
 * @param <K> the type of the entry keys
 * @param <V> the type of the entry values
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

    /**
    * Invoked when a map entry has been inserted.
    *
    * @param evt  the MapEvent carrying the insert information
    */
    public void entryInserted(MapEvent<K, V> evt)
        {
        onMapEvent(evt);
        }

    /**
    * Invoked when a map entry has been updated.
    *
    * @param evt  the MapEvent carrying the update information
    */
    public void entryUpdated(MapEvent<K, V> evt)
        {
        onMapEvent(evt);
        }

    /**
    * Invoked when a map entry has been removed.
    *
    * @param evt  the MapEvent carrying the delete information
    */
    public void entryDeleted(MapEvent<K, V> evt)
        {
        onMapEvent(evt);
        }

    /**
     * Invoked when any event is received.
     * <p/>
     * This method is called internally by the {@link #entryInserted},
     * {@link #entryUpdated} and {@link #entryDeleted} methods, and is responsible
     * for propagating received events to all of the subscribers of this observable.
     *
     * @param evt  the MapEvent information
     */
    protected void onMapEvent(MapEvent<K, V> evt)
        {
        m_subscribers.forEach(s -> s.onNext(evt));
        }

    // ---- data members ----------------------------------------------------

    /**
     * A set of active subscribers.
     */
    protected Set<Subscriber<? super MapEvent<? extends K, ? extends V>>> m_subscribers;
    }
