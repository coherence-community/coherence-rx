/*
 * File: DeviceReading.java
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


import com.tangosol.util.Base;
import com.tangosol.util.UUID;

import java.io.Serializable;

import java.util.Date;


/**
 * An immutable class to represent a temperature reading from a device.
 *
 * @author Tim Middleton  2016.04.27
 */
public class DeviceReading implements Serializable
    {
    // ----- constructors ----------------------------------------------------

    /**
     * Construct a new ReviceReading.
     *
     * @param nDeviceId     Device Id to record reading for
     * @param nTemperature  temperature reading to record
     */
    public DeviceReading(String nDeviceId, int nTemperature)
        {
        f_id  = new UUID();
        f_nDeviceId = nDeviceId;
        f_ltdTimestamp = Base.getLastSafeTimeMillis();
        f_nTemperature = nTemperature;
        }

    // ----- accessors -------------------------------------------------------

    /**
     * Obtain the unique identifier for the DeviceReading.
     *
     * @return the identifier
     */
    public UUID getId()
        {
        return f_id;
        }

    /**
     * Obtain the device identifier for the DeviceReading.
     *
     * @return the device identifier
     */
    public String getDeviceId()
        {
        return f_nDeviceId;
        }

    /**
     * Obtain the time-stamp for the DeviceReading.
     *
     * @return the time-stamp
     */
    public long getTimeStamp()
        {
        return f_ltdTimestamp;
        }

    /**
     * Obtain the reading for the DeviceReading.
     *
     * @return the reading
     */
    public int getTemperature()
        {
        return f_nTemperature;
        }

    // ----- Object methods -------------------------------------------------

    @Override
    public String toString()
        {
        return "DeviceReading{" +
               "id=" + f_id +
               ", deviceId='" + f_nDeviceId + '\'' +
               ", ts=" + new Date(f_ltdTimestamp) +
               ", temp=" + f_nTemperature +
               '}';
        }

    // ----- data members ---------------------------------------------------

    /**
     * The unique identifier.
     */
    private final UUID f_id;

    /**
     * The device id.
     */
    private final String f_nDeviceId;

    /**
     * The time the reading was recorded.
     */
    private final long f_ltdTimestamp;

    /**
     * The temperature reading recorded.
     */
    private final int f_nTemperature;
    }
