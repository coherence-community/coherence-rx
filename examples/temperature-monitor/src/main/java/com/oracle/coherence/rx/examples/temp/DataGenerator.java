/*
 * File: DataGenerator.java
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


import com.tangosol.net.NamedCache;
import com.tangosol.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import java.util.Random;

import static com.oracle.coherence.rx.examples.temp.Utilities.getDeviceName;
import static com.oracle.coherence.rx.examples.temp.Utilities.getTextField;

/**
 * A class that displays a GUI to generate mocked temperature
 * readings for devices and insert them into the "device-readings" cache.
 * <p>
 *
 * @author Tim Middleton  2016.04.27
 */
public class DataGenerator
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Construct a DataGenerator instance.
     *
     * @param cache  cache to use to insert data into
     */
    public DataGenerator(NamedCache<UUID, DeviceReading> cache)
        {
        this.cache = cache;
        }

    // ---- DataGenerator methods -------------------------------------------

    /**
     * Initalize the DataGenerator.
     */
    public void init()
        {
        showGUI();
        }

    /**
     * Build and show the GUI.
     */
    private void showGUI()
        {
        JFrame frmMain = new JFrame("Coherence-Rx Demo - Data Generator");
        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent pneContent = (JComponent) frmMain.getContentPane();

        JPanel pnlMain = new JPanel(new BorderLayout());

        // create the header panel
        JPanel pnlDetail = new JPanel(new GridLayout(1, 3));

        pnlMain.add(pnlDetail, BorderLayout.NORTH);
        pnlMain.add(new JLabel(" "), BorderLayout.CENTER);

        // add the gauges
        for (int i = 0; i < Utilities.GAUGES; i++)
            {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(5, 5, 5, 5));

            atxtDeviceId[i] = getTextField(7, JTextField.CENTER);
            atxtDeviceId[i].setText(getDeviceName(i));
            panel.add(atxtDeviceId[i]);

            anCurrentTemp[i]  = Utilities.INITIAL_TEMP;
            atxtDeviceTemp[i] = getTextField(7, JTextField.CENTER);
            atxtDeviceTemp[i].setText(Integer.toString(anCurrentTemp[i]));
            panel.add(atxtDeviceTemp[i]);

            asldTemp[i] = new JSlider(JSlider.VERTICAL, Utilities.MIN_TEMP, Utilities.MAX_TEMP, Utilities.INITIAL_TEMP);
            asldTemp[i].setMajorTickSpacing(10);
            asldTemp[i].setMinorTickSpacing(5);
            asldTemp[i].setPaintTicks(true);
            asldTemp[i].setPaintLabels(true);
            asldTemp[i].setPreferredSize(new Dimension(40,260));

            // final required for lambda usage
            final int index = i;

            asldTemp[i].addChangeListener(event ->
                {
                int nValue = ((JSlider)event.getSource()).getValue();

                anCurrentTemp[index] = nValue;
                atxtDeviceTemp[index].setText(String.valueOf(nValue));
                });

            panel.add(asldTemp[i]);
            pnlDetail.add(panel);
        }

        // Create the action panel
        JPanel pnlAction = new JPanel(new FlowLayout());

        btnControl = new JButton(START);

        // listener to start and stop the "emitting" of data
        btnControl.addActionListener( event ->
            {
            if (START.equals(btnControl.getText()))
                {
                btnControl.setText(STOP);

                // create new action every 1 second to update cache
                timer = new Timer(1000, action ->
                    {
                    for (int j = 0; j < Utilities.GAUGES; j++)
                        {
                        if (chkRandomize.isSelected())
                            {
                            // randomly change the temp
                            int nValue = RANDOM.nextInt(3) - 1;

                            // -1 = down, 0 = same, 1 = up
                            int newValue = asldTemp[j].getValue() + nValue;
                            if (newValue > Utilities.MAX_TEMP)
                                {
                                nValue = -2;
                                }
                            else if (newValue < Utilities.MIN_TEMP)
                                {
                                nValue = 2;
                                }
                            asldTemp[j].setValue(asldTemp[j].getValue() + nValue);
                            }

                        DeviceReading reading = new DeviceReading(getDeviceName(j), anCurrentTemp[j]);
                        cache.put(reading.getId(), reading);
                        }
                    });
                timer.start();
                }
            else
                {
                // stop the timer
                btnControl.setText(START);
                timer.stop();
                timer = null;
               }
            });

        pnlAction.add(btnControl);

        chkRandomize = new JCheckBox("Randomize Temperature");
        chkRandomize.setMnemonic(KeyEvent.VK_R);
        chkRandomize.setSelected(false);
        pnlAction.add(chkRandomize);

        pnlMain.add(pnlAction, BorderLayout.SOUTH);

        pneContent.add(pnlMain);
        pneContent.setOpaque(true);
        pneContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        frmMain.setContentPane(pneContent);

        frmMain.pack();
        frmMain.setVisible(true);
        }

    // ---- constants -------------------------------------------------------

    /**
     * Button description to start emitting data.
     */
    private static final String START = "Start Emitting";

    /**
     * Button description to stop emitting data.
     */
    private static final String STOP = "Stop Emitting" ;

    // ---- data members ----------------------------------------------------

    /**
     * An array of {@link JSlider}s representing the temperature.
     */
    private JSlider[] asldTemp = new JSlider[Utilities.GAUGES];

    /**
     * An array of {@link JTextField}s representing the device id's.
     */
    private JTextField[] atxtDeviceId = new JTextField[Utilities.GAUGES];

    /**
     * An array of {@link JTextField}s representing the devices temperature.
     */
    private JTextField[] atxtDeviceTemp = new JTextField[Utilities.GAUGES];

    /**
     * An array of int's representing the numeric current temperature.
     */
    private int[] anCurrentTemp = new int[Utilities.GAUGES];

    /**
     * Button to control the emitting (inserting) of data.
     */
    private JButton btnControl;

    /**
     * Check box to randomize the temperature data.
     */
    private JCheckBox chkRandomize;

    /**
     * Random number generator.
     */
    private final Random RANDOM = new Random();

    /**
     * Cache to use to insert into.
     */
    private NamedCache<UUID, DeviceReading> cache;

    /**
     * Timer used to periodically insert data.
     */
    private Timer timer = null;
    }