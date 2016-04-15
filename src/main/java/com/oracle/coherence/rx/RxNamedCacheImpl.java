/*
 * File: RxNamedCacheImpl.java
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

import com.tangosol.net.AsyncNamedCache;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import rx.Observable;

import java.util.Collection;
import java.util.Map;

/**
 * Reactive Extensions (RxJava) {@link NamedCache} API implementation.
 *
 * @author Aleksandar Seovic  2015.12.30
 */
@SuppressWarnings("Convert2MethodRef")
public class RxNamedCacheImpl<K, V> implements RxNamedCache<K, V>
{
    // ---- data members ----------------------------------------------------

    /**
     * The wrapped AsyncNamedCache instance to delegate invocations to.
     */
    protected final AsyncNamedCache<K, V> m_cache;


    // ---- constructors ----------------------------------------------------

    /**
     * Construct RxNamedCache instance.
     *
     * @param cache  the wrapped NamedCache to delegate invocations to
     */
    protected RxNamedCacheImpl(AsyncNamedCache<K, V> cache)
    {
        m_cache = cache;
    }


    // ---- RxNamedCache interface ------------------------------------------

    @Override
    public <R> Observable<R> invoke(K                                    key,
                                    InvocableMap.EntryProcessor<K, V, R> processor)
    {
        return Observable.create(s -> m_cache.invoke(key, processor).handle((r, t) -> {
                if (!s.isUnsubscribed())
                {
                    if (t == null)
                    {
                        s.onNext(r);
                        s.onCompleted();
                    }
                    else
                    {
                        s.onError(t);
                    }
                }

                return null;
            }));
    }


    @Override
    public <R> Observable<? extends Map.Entry<? extends K, ? extends R>> invokeAll(Collection<? extends K> collKeys,
                                                                                   InvocableMap.EntryProcessor<K, V,
                                                                                   R>                      processor)
    {
        return Observable.create(s -> m_cache.invokeAll(collKeys,
                                                        processor,
                                                        entry -> s.onNext(entry)).handle((r, t) -> {
                                                                if (!s.isUnsubscribed())
                                                                {
                                                                    if (t == null)
                                                                    {
                                                                        s.onCompleted();
                                                                    }
                                                                    else
                                                                    {
                                                                        s.onError(t);
                                                                    }
                                                                }

                                                                return null;
                                                            }));
    }


    @Override
    public <R> Observable<? extends Map.Entry<? extends K, ? extends R>> invokeAll(Filter filter,
                                                                                   InvocableMap.EntryProcessor<K, V,
                                                                                   R>     processor)
    {
        return Observable.create(s -> m_cache.invokeAll(filter, processor, entry -> s.onNext(entry)).handle((r, t) -> {
                if (!s.isUnsubscribed())
                {
                    if (t == null)
                    {
                        s.onCompleted();
                    }
                    else
                    {
                        s.onError(t);
                    }
                }

                return null;
            }));
    }


    @Override
    public <R> Observable<R> aggregate(Collection<? extends K>                               collKeys,
                                       InvocableMap.EntryAggregator<? super K, ? super V, R> aggregator)
    {
        return Observable.create(s -> m_cache.aggregate(collKeys, aggregator).handle((r, t) -> {
                if (!s.isUnsubscribed())
                {
                    if (t == null)
                    {
                        s.onNext(r);
                        s.onCompleted();
                    }
                    else
                    {
                        s.onError(t);
                    }
                }

                return null;
            }));
    }


    @Override
    public <R> Observable<R> aggregate(Filter                                                filter,
                                       InvocableMap.EntryAggregator<? super K, ? super V, R> aggregator)
    {
        return Observable.create(s -> m_cache.aggregate(filter, aggregator).handle((r, t) -> {
                if (!s.isUnsubscribed())
                {
                    if (t == null)
                    {
                        s.onNext(r);
                        s.onCompleted();
                    }
                    else
                    {
                        s.onError(t);
                    }
                }

                return null;
            }));
    }


    @Override
    public Observable<Void> putAll(Map<? extends K, ? extends V> map)
    {
        return Observable.create(s -> m_cache.putAll(map).handle(
            (r, t) -> {
                if (!s.isUnsubscribed())
                {
                    if (t == null)
                    {
                        s.onCompleted();
                    }
                    else
                    {
                        s.onError(t);
                    }
                }

                return null;
            }));
    }
}
