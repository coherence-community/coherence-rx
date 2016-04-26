package com.oracle.coherence.rx.examples.hot;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.coherence.options.ClusterName;
import com.oracle.tools.runtime.coherence.options.LocalHost;
import com.oracle.tools.runtime.coherence.options.Logging;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
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

/**
 * A class that displays a GUI to generate mocked temperature
 * readings for a device and insert them into the "device-readings" cache.
 * <p>
 *
 * @author Tim Middleton
 */
public class DataGenerator extends DataUtils
{
    /**
     *
     */
    private static final String START   = "Start Emitting" ;
    private static final String STOP    = "Stop Emitting" ;

    private JSlider[]    tempSlider  = new JSlider[GAUGES];
    private JTextField[] deviceId    = new JTextField[GAUGES];
    private JTextField[] deviceTemp  = new JTextField[GAUGES];
    private int[]        currentTemp = new int[GAUGES];

    private JButton     btnControl;
    private JCheckBox   randomize;
    private JComponent  contentPane;

    private final Random RANDOM = new Random();

    private NamedCache<UUID, DeviceReading> cache;

    private Timer timer = null;

    public DataGenerator(NamedCache<UUID, DeviceReading> cache)
    {
        this.cache = cache;
    }

    public void init()
        {
        showGUI();
        }

    private void showGUI()
    {
        JFrame frame = new JFrame("Coherence-Rx Demo - Data Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // TODO remove listener on exit

        contentPane= (JComponent) frame.getContentPane();

        JPanel pnlMain = new JPanel(new BorderLayout());

        // create the header panel
        JPanel pnlHeader = new JPanel(new GridLayout(1, 3));

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(new JLabel(" "), BorderLayout.CENTER);

        // add 3 gauges
        for (int i = 0; i < GAUGES; i++)
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(5, 5, 5, 5));

            deviceId[i] =  getTextField(7, JTextField.CENTER);
            deviceId[i].setText(getDeviceName(i));
            panel.add(deviceId[i]);

            currentTemp[i] = INITIAL_TEMP;
            deviceTemp[i]  = getTextField(7, JTextField.CENTER);
            deviceTemp[i].setText(Integer.toString(currentTemp[i]));
            panel.add(deviceTemp[i]);

            tempSlider[i] = new JSlider(JSlider.VERTICAL, MIN_TEMP, MAX_TEMP, INITIAL_TEMP);
            tempSlider[i].setMajorTickSpacing(10);
            tempSlider[i].setMinorTickSpacing(5);
            tempSlider[i].setPaintTicks(true);
            tempSlider[i].setPaintLabels(true);
            tempSlider[i].setPreferredSize(new Dimension(40,260));

            final int index = i;

            tempSlider[i].addChangeListener(e ->
            {
                JSlider source = (JSlider)e.getSource();
                int value = source.getValue();
                currentTemp[index] = value;
                deviceTemp[index].setText(String.valueOf(value));
            });

            panel.add(tempSlider[i]);
            pnlHeader.add(panel);
        }

        // buttons
        JPanel pnlAction = new JPanel(new FlowLayout());

        btnControl = new JButton(START);

        btnControl.addActionListener( e ->
        {
            if (START.equals(btnControl.getText()))
            {
                btnControl.setText(STOP);
                timer = new Timer(1000, action ->
                {
                    for (int j = 0; j < GAUGES; j++)
                    {
                        if (randomize.isSelected())
                        {
                            // randomly change the temp
                            int value = RANDOM.nextInt(3) - 1;

                            // -1 = down, 0 = same, 1 = up
                            int newValue = tempSlider[j].getValue() + value;
                            if (newValue > MAX_TEMP)
                            {
                               value = -2;
                            }
                            else if (newValue < MIN_TEMP)
                            {
                               value = 2;
                            }
                            tempSlider[j].setValue(tempSlider[j].getValue() + value);
                        }

                        DeviceReading reading = new DeviceReading(getDeviceName(j), currentTemp[j]);
                        cache.put(reading.getId(), reading);
                    }
                });
                timer.start();
            }
            else
            {
                btnControl.setText(START);
                timer.stop();
                timer = null;
           }
        });

        pnlAction.add(btnControl);

        randomize = new JCheckBox("Randomize Temperature");
        randomize.setMnemonic(KeyEvent.VK_R);
        randomize.setSelected(false);
        pnlAction.add(randomize);

        pnlMain.add(pnlAction, BorderLayout.SOUTH);

        contentPane.add(pnlMain);
        contentPane.setOpaque(true);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        frame.setContentPane(contentPane);

        frame.pack();
        frame.setVisible(true);
    }
}