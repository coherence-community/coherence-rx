package com.oracle.coherence.rx.examples.hot;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;
import com.tangosol.util.Base;
import com.tangosol.util.UUID;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by timmiddleton on 21/04/16.
 */
public class DeviceReading implements Serializable
    {
    private UUID   id;
    private String deviceId;
    private long   ts;
    private int    reading;

    @Override
    public String toString()
        {
        return "DeviceReading{" +
               "id=" + id +
               ", deviceId='" + deviceId + '\'' +
               ", ts=" + new Date(ts) +
               ", reading=" + reading +
               '}';
        }

    public static NamedCache<UUID, DeviceReading> getCache()
        {
        return CacheFactory.getTypedCache("device-readings",
                TypeAssertion.withTypes(UUID.class, DeviceReading.class));
        }

    public DeviceReading(String deviceId,int reading)
        {
        this.id  = new UUID();
        this.deviceId = deviceId;
        this.ts = Base.getLastSafeTimeMillis();
        this.reading = reading;
        }

    public UUID getId()
        {
        return id;
        }

    public void setId(UUID id)
        {
        this.id = id;
        }

    public String getDeviceId()
        {
        return deviceId;
        }

    public long getTs()
        {
        return ts;
        }

    public int getReading()
        {
        return reading;
        }

    }
