/*
 * File: Utilities.java
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

import javax.swing.JTextField;


/**
 * Utility class for UI components.
 *
 * @author Tim Middleton  2016.04.27
 */
public class Utilities
    {
    // ---- Utilities methods -----------------------------------------------

    /**
     * Create a {@link JTextField} with the specified nWidth and specified
     * nAlignment.
     *
     * @param  nWidth  the width for the {@link JTextField}
     * @param  nAlign  either {@link JTextField}.RIGHT or LEFT
     *
     * @return the newly created text field
     */
    protected static JTextField getTextField(int nWidth, int nAlign)
        {
        JTextField textField = new JTextField();

        textField.setEditable(false);
        textField.setColumns(nWidth);
        textField.setHorizontalAlignment(nAlign);

        textField.setOpaque(true);

        return textField;
        }

    /**
     * Obtain the device name given an array index.
     *
     * @param nIndex  array index
     *
     * @return the device name
     */
    protected static String getDeviceName(int nIndex)
        {
        return DEVICE + '-' + nIndex;
        }

    /**
     * Obtain the device array index given a device name.
     *
     * @param sDeviceName  the name of the device
     *
     * @return the device array index
     */
    protected static int getDeviceIndex(String sDeviceName)
        {
        String[] parts = sDeviceName.split("-");
        if (parts == null || parts.length != 2)
            {
            throw new IllegalArgumentException("Invalid device name");
            }

        return Integer.parseInt(parts[1]);
        }

    // ---- constants -------------------------------------------------------

    /**
     * Device name prefix.
     */
    private static final String DEVICE  = "Device" ;

    /**
     * Minimum temperature to display on slider.
     */
    protected static final int MIN_TEMP = 0;

    /**
     * Max temperature to display on slider.
     */
    protected static final int MAX_TEMP = 100;

   /**
    * Max temperature to display on slider.
    */
    protected static final int INITIAL_TEMP = 50;

    /**
     * Number of gauges to display.
     */
    protected static final int GAUGES = 3;
    }