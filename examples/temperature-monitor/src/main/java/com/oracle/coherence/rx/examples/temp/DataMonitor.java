/*
 * File: DataMonitor.java
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

import com.oracle.coherence.rx.ObservableMapListener;

import com.tangosol.net.NamedCache;
import com.tangosol.util.UUID;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.oracle.coherence.rx.examples.temp.Utilities.getDeviceIndex;
import static com.oracle.coherence.rx.examples.temp.Utilities.getDeviceName;
import static com.oracle.coherence.rx.examples.temp.Utilities.getTextField;

/**
 * A class that displays a GUI to monitor temperature
 * readings for devices and use "Hot" Observables to extra and
 * display data from them.
 * <p>
 *
 * @author Tim Middleton  2016.04.27
 */
public class DataMonitor
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Construct a DataMonitor instance.
     *
     * @param cache  cache retrieve data from
     */
    public DataMonitor(NamedCache<UUID, DeviceReading> cache)
        {
        f_cache = cache;
        }

    // ---- DataMonitor methods ---------------------------------------------

    /**
     * Initalize the DataMonitor.
     */
    public void init()
        {
        showGUI();
        addRxObservers();
        }

    /**
     * Add the RX Observers and subscribe to display the required data.
     */
    private void addRxObservers()
        {
        // create a new ObservableMapLister which is a hot observable
        m_listener = ObservableMapListener.create();

        // subscribe to get the current temperature
        m_listener.map(entry -> entry.getNewEntry().getValue())
                .subscribe(value ->
                    {
                    updateTemp(value);
                    txtProcessed.setText(String.format("%,d", f_counter.incrementAndGet()));
                    });

        // add subscribers for each of the gauges
        for (int i = 0; i < Utilities.GAUGES; i++)
            {
            final int nIndex = i;
            final String device = getDeviceName(i);

            // get an average of the last 15 seconds of readings for each device
            m_listener.map(entry -> entry.getNewEntry().getValue())
                .filter(reading -> reading.getDeviceId().equals(device))
                .map(DeviceReading::getReading)
                .buffer(15, TimeUnit.SECONDS)
                .subscribe(list -> atxtAvgLast15Seconds[nIndex].setText(String.format(FORMAT, getAverage(list))));

            // get an average of the last 30 seconds of readings for each device
            m_listener.map(entry -> entry.getNewEntry().getValue())
                .filter(reading -> reading.getDeviceId().equals(device))
                .map(DeviceReading::getReading)
                .buffer(30, TimeUnit.SECONDS)
                .subscribe(list -> atxtAvgLast30Seconds[nIndex].setText(String.format(FORMAT, getAverage(list))));

            // get an average of the last 60 seconds of readings for each device
            m_listener.map(entry -> entry.getNewEntry().getValue())
                .filter(reading -> reading.getDeviceId().equals(device))
                .map(DeviceReading::getReading)
                .buffer(60, TimeUnit.SECONDS)
                .subscribe(list -> updateTrends(nIndex, list));
            }

        // adding the listener will tigger the above subscriptions
        f_cache.addMapListener(m_listener);
        }

    /**
     * Build and show the GUI.
     */
    private void showGUI()
        {
        JFrame frmMain = new JFrame("Coherence-Rx Demo - Data Monitor");
        frmMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // remove the map listener on exit
        frmMain.addWindowListener(new WindowAdapter()
            {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                if (f_cache != null && m_listener != null)
                {
                    f_cache.removeMapListener(m_listener);
                }
                System.exit(0);
            }
            });

        JComponent pneContent = (JComponent) frmMain.getContentPane();

        JPanel pnlMain = new JPanel(new BorderLayout());

        // create the header panel
        JPanel pnlDetail = new JPanel(new GridLayout(9, 4));

        // row 1 - headers
        pnlDetail.add(new JLabel("Device"));
        for (int i = 0 ; i < Utilities.GAUGES; i++)
            {
            pnlDetail.add(new JLabel(getDeviceName(i), SwingConstants.CENTER));
            }

        // row 2 - current temp
        pnlDetail.add(new JLabel("Current Temp  "));
        for (int i = 0 ; i < Utilities.GAUGES; i++)
            {
            atxtDeviceTemp[i]  = getTextField(7, JTextField.CENTER);
            pnlDetail.add(atxtDeviceTemp[i]);
            }

        // row 3 - blank line
        blankLine(pnlDetail);

        // row 4 - last15 seconds average
        pnlDetail.add(new JLabel("15 Second Average  "));
        for (int i = 0 ; i < Utilities.GAUGES; i++)
            {
            atxtAvgLast15Seconds[i] = getTextField(7, JTextField.CENTER);
            pnlDetail.add(atxtAvgLast15Seconds[i]);
            }

        // row 5 - last30 seconds average
        pnlDetail.add(new JLabel("30 Second Average"));
        for (int i = 0 ; i < Utilities.GAUGES; i++)
            {
            atxtAvgLast30Seconds[i] = getTextField(7, JTextField.CENTER);
            pnlDetail.add(atxtAvgLast30Seconds[i]);
            }

        // row 6 - last60 seconds average
        pnlDetail.add(new JLabel("60 Second Average"));
        for (int i = 0 ; i < Utilities.GAUGES; i++)
            {
            atxtAvgLast60Seconds[i] = getTextField(7, JTextField.CENTER);
            pnlDetail.add(atxtAvgLast60Seconds[i]);
            }

        // row 7 - 60 seconds trend
        pnlDetail.add(new JLabel("60 Second Trend"));
        for (int i = 0 ; i < Utilities.GAUGES; i++)
            {
            atxtTempTrend[i] = getTextField(7, JTextField.CENTER);
            pnlDetail.add(atxtTempTrend[i]);
            }

        // row 8 - blank line

        blankLine(pnlDetail);
        // row 8 - items txtProcessed

        pnlDetail.add(new JLabel(""));
        pnlDetail.add(new JLabel("Readings txtProcessed"));
        txtProcessed = getTextField(7, JTextField.CENTER);
        pnlDetail.add(txtProcessed);
        pnlDetail.add(new JLabel(""));

        pnlMain.add(pnlDetail, BorderLayout.NORTH);
        pnlMain.add(new JLabel(" "), BorderLayout.CENTER);

        // create footer
        JPanel pnlFooter = new JPanel(new FlowLayout());

        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        pneContent.add(pnlMain);
        pneContent.setOpaque(true);
        pneContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        frmMain.setContentPane(pneContent);
        frmMain.pack();
        frmMain.setVisible(true);
        }

    /**
     * Obtain the average of a {@link List} of {@link Integer}s.
     *
     * @param listValues  list of values to average
     *
     * @return the calculated average
     */
    private float getAverage(List<Integer> listValues)
        {
        int total = 0;
        if (listValues == null || listValues.size() == 0)
            {
            return total;
            }
        else
            {
            for (float value : listValues)
                {
                total += value;
                }
            return total * 1.0f / listValues.size();
            }
        }

    /**
     * Update the device temperature and change colors to indicate
     * temperature severity.
     *
     * @param reading  the current reading from the cache
     */
    private void updateTemp(DeviceReading reading)
        {
        int temp = reading.getReading();
        int deviceNumber = getDeviceIndex(reading.getDeviceId());
        atxtDeviceTemp[deviceNumber].setText(String.format(FORMAT, temp * 1.0f));

        // set the color to indicate the temp level
        if (temp > 85)
            {
            atxtDeviceTemp[deviceNumber].setBackground(Color.red);
            atxtDeviceTemp[deviceNumber].setForeground(Color.white);
            }
        else if (temp > 60)
            {
            atxtDeviceTemp[deviceNumber].setBackground(Color.orange);
            atxtDeviceTemp[deviceNumber].setForeground(Color.white);
            }
        else
            {
            atxtDeviceTemp[deviceNumber].setBackground(Color.white);
            atxtDeviceTemp[deviceNumber].setForeground(Color.black);
            }
        }

    /**
     * Add a blank line to the {@link GridLayout} panel.
     *
     * @param panel  panel to add to
     */
    private void blankLine(JPanel panel)
        {
        for (int i = 0; i < Utilities.GAUGES + 1 ; i++)
            {
            panel.add(new JLabel(""));
            }
        }

    /**
     * Update the temperature trends to indicate what happened over the last time period
     * covered by the list.
     *
     * @param nDeviceIndex  the array index to update
     * @param listValues    list of values to analyze
     */
    private void updateTrends(int nDeviceIndex, List<Integer> listValues )
        {
        if (listValues == null)
            {
            throw new IllegalArgumentException("list of values must not be null");
            }

        // update the given average value
        atxtAvgLast60Seconds[nDeviceIndex].setText(String.format(FORMAT, getAverage(listValues)));

        // determine the trend
        int   nFirstValue = listValues.get(0);
        int   nLastValue  = listValues.get(listValues.size() - 1);
        float nPercent  = ((float)nLastValue - (float)nFirstValue) / nFirstValue * 100.0f;

        String text = nFirstValue == nLastValue ? "Steady" :
                      (nFirstValue > nLastValue  ? "Falling" : "Rising") +
                      " (" + String.format("%3.1f%%", nPercent) + ")";

        atxtTempTrend[nDeviceIndex].setText(text);
        }

    // ---- constants -------------------------------------------------------

    /**
     * String format for the temperature averages.
     */
    private static final String FORMAT = "%5.1f";

    // ---- data members ----------------------------------------------------

    /**
     * An array of {@link JTextField}s representing the devices temperature averaged
     * over the last 15 seconds.
     */
    private JTextField[] atxtAvgLast15Seconds = new JTextField[Utilities.GAUGES];

    /**
     * An array of {@link JTextField}s representing the devices temperature averaged
     * over the last 30 seconds.
     */
    private JTextField[] atxtAvgLast30Seconds = new JTextField[Utilities.GAUGES];

    /**
     * An array of {@link JTextField}s representing the devices temperature averaged
     * over the last 60 seconds.
     */
    private JTextField[] atxtAvgLast60Seconds = new JTextField[Utilities.GAUGES];

    /**
     * An array of {@link JTextField}s representing the devices temperature trend
     * over the last 15 seconds.
     */
    private JTextField[] atxtTempTrend = new JTextField[Utilities.GAUGES];

    /**
     * An array of {@link JTextField}s representing the devices current temperature.
     */
    private JTextField[] atxtDeviceTemp = new JTextField[Utilities.GAUGES];

    /**
     * Indicates the number of readings txtProcessed.
     */
    private JTextField txtProcessed;

    /**
     * Hot {@ObservableMapListener} added the the cache.
     */
    private ObservableMapListener<UUID, DeviceReading> m_listener;

    /**
     * Cache to read data from.
     */
    private final NamedCache<UUID, DeviceReading> f_cache;

    /**
     * Thread safe counter for recording number of readings.
     */
    protected final AtomicLong f_counter = new AtomicLong(0);
    }