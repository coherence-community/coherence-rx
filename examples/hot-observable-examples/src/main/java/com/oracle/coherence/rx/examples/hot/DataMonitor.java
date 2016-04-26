package com.oracle.coherence.rx.examples.hot;

import com.oracle.coherence.rx.ObservableMapListener;

import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.UUID;
import rx.observables.ConnectableObservable;

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

/**
 * Created by timmiddleton on 22/04/16.
 */
public class DataMonitor extends DataUtils
{
    private JTextField txtLastWarning;

    private JTextField[] avgLast15Seconds = new JTextField[GAUGES];
    private JTextField[] avgLast30Seconds = new JTextField[GAUGES];
    private JTextField[] avgLast60Seconds = new JTextField[GAUGES];
    private JTextField[] tempTrend        = new JTextField[GAUGES];
    private JTextField[] deviceTemp       = new JTextField[GAUGES];

    private JTextField   processed;

    private ObservableMapListener<UUID, DeviceReading> listener;
    private NamedCache<UUID, DeviceReading> cache;

    private static final String FORMAT = "%5.1f";

    protected AtomicLong counter = new AtomicLong(0);

    public static void main(String ... args)
    {
    /*
        LocalPlatform   platform = LocalPlatform.get();

        // start a storage disabled client

        CoherenceCacheServer server = LocalPlatform.get()
                   .launch(CoherenceCacheServer.class,
                           LocalHost.only(),
                           Logging.at(2),
                           ClusterName.of(CLUSTER_NAME));


        DataMonitor monitor  = new DataMonitor(DeviceReading.getCache());

        monitor.showGUI();
        monitor.addRxObservers();  */
    }

    public DataMonitor(NamedCache<UUID, DeviceReading> cache)
    {
        this.cache = cache;
    }

    public void init()
        {
        showGUI();
        addRxObservers();
        }

    private void addRxObservers()
    {
        // add the ObservableMapListeners to the cache
        listener = ObservableMapListener.create();
        cache.addMapListener(listener);

        // create a "hot" observable
        ConnectableObservable<MapEvent<? extends UUID, ? extends DeviceReading>> hot = listener.publish();

        for (int i = 0; i < GAUGES; i++)
            {
            final int index = i;
            final String device = getDeviceName(i);

            // get the current temperature
            hot.map(entry -> entry.getNewEntry().getValue())
                    .subscribe(value ->
                    {
                        System.out.println(value);
                        updateTemp(value);
                        processed.setText(String.format("%,d", counter.incrementAndGet()));
                    });

            // get an average of the last 15 seconds of readings for each device
            hot.map(entry -> entry.getNewEntry().getValue())
                .filter(reading -> reading.getDeviceId().equals(device))
                .map(value -> value.getReading())
                .buffer(15, TimeUnit.SECONDS)
                .subscribe(list -> avgLast15Seconds[index].setText(String.format(FORMAT, getAverage(list))));

            // get an average of the last 30 seconds of readings for each device
            hot.map(entry -> entry.getNewEntry().getValue())
                .filter(reading -> reading.getDeviceId().equals(device))
                .map(value -> value.getReading())
                .buffer(30, TimeUnit.SECONDS)
                .subscribe(list -> avgLast30Seconds[index].setText(String.format(FORMAT, getAverage(list))));

            // get an average of the last 60 seconds of readings for each device
            hot.map(entry -> entry.getNewEntry().getValue())
                .filter(reading -> reading.getDeviceId().equals(device))
                .map(value -> value.getReading())
                .buffer(60, TimeUnit.SECONDS)
                .subscribe(list -> updateTrends(index, list));
            }

        // instruct the hot Observable to start publishing
        hot.connect();

    }

    private void showGUI()
    {
        JFrame frame = new JFrame("Coherence-Rx Demo - Data Monitor");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter()
            {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                if (cache != null && listener != null)
                {
                    cache.removeMapListener(listener);
                }
                System.exit(0);
            }
            });

        JComponent contentPane = (JComponent) frame.getContentPane();

        JPanel pnlMain = new JPanel(new BorderLayout());

        // create the header panel
        JPanel pnlHeader = new JPanel(new GridLayout(9, 4));

        // row 1 - headers
        pnlHeader.add(new JLabel("Device"));
        for (int i = 0 ; i < GAUGES; i++)
        {
            pnlHeader.add(new JLabel(getDeviceName(i),  SwingConstants.CENTER));
        }

        // row 2 - current temp
        pnlHeader.add(new JLabel("Current Temp  "));
        for (int i = 0 ; i < GAUGES; i++)
        {
            deviceTemp[i]  = getTextField(7, JTextField.CENTER);
            pnlHeader.add(deviceTemp[i]);
        }

        blankLine(pnlHeader);

        // row 3 - last15 seconds average
        pnlHeader.add(new JLabel("15 Second Average  "));
        for (int i = 0 ; i < GAUGES; i++)
        {
            avgLast15Seconds[i] = getTextField(7, JTextField.CENTER);
            pnlHeader.add(avgLast15Seconds[i]);
        }

        // row 4 - last30 seconds average
        pnlHeader.add(new JLabel("30 Second Average"));
        for (int i = 0 ; i < GAUGES; i++)
        {
            avgLast30Seconds[i] = getTextField(7, JTextField.CENTER);
            pnlHeader.add(avgLast30Seconds[i]);
        }

        // row 5 - last60 seconds average
        pnlHeader.add(new JLabel("60 Second Average"));
        for (int i = 0 ; i < GAUGES; i++)
        {
            avgLast60Seconds[i] = getTextField(7, JTextField.CENTER);
            pnlHeader.add(avgLast60Seconds[i]);
        }

        // row 6 - 60 seconds trend
        pnlHeader.add(new JLabel("60 Second Trend"));
        for (int i = 0 ; i < GAUGES; i++)
        {
            tempTrend[i] = getTextField(7, JTextField.CENTER);
            pnlHeader.add(tempTrend[i]);
        }

        // row 7 items processed
        blankLine(pnlHeader);
        pnlHeader.add(new JLabel(""));
        pnlHeader.add(new JLabel("Processed"));
        processed = getTextField(7, JTextField.CENTER);
        pnlHeader.add(processed);
        pnlHeader.add(new JLabel(""));

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(new JLabel(" "), BorderLayout.CENTER);

        // create footer
        JPanel pnlFooter = new JPanel(new FlowLayout());

        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        contentPane.add(pnlMain);
        contentPane.setOpaque(true);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        frame.setContentPane(contentPane);

        frame.pack();
        frame.setVisible(true);
    }

    private static float getAverage(List<Integer> values)
        {
        int total = 0;
        if (values == null || values.size() == 0)
            {
            return total;
            }
        else
            {
            for (float value : values)
                {
                total += value;
                }
            return total * 1.0f / values.size();
            }
        }

    private void updateTemp(DeviceReading reading)
    {
        int temp = reading.getReading();
        int deviceNumber = getDeviceIndex(reading.getDeviceId());
        deviceTemp[deviceNumber].setText(String.format(FORMAT, temp * 1.0f));

        // set the color to indicate the temp level
        if (temp > 85)
        {
            deviceTemp[deviceNumber].setBackground(Color.red);
            deviceTemp[deviceNumber].setForeground(Color.white);
        }
        else if (temp > 60)
        {
            deviceTemp[deviceNumber].setBackground(Color.orange);
            deviceTemp[deviceNumber].setForeground(Color.white);
        }
        else
        {
            deviceTemp[deviceNumber].setBackground(Color.white);
            deviceTemp[deviceNumber].setForeground(Color.black);
        }

    }

    private void blankLine(JPanel panel)
    {
        for (int i = 0; i < GAUGES + 1 ; i++)
        {
            panel.add(new JLabel(""));
        }
    }

    private void updateTrends(int deviceIndex, List<Integer> values )
    {
        // update the given average value
        avgLast60Seconds[deviceIndex].setText(String.format(FORMAT, getAverage(values)));

        // determine the trend
        int firstValue = values.get(0);
        int lastValue  = values.get(values.size() - 1);
        float percent  = ((float)lastValue - (float)firstValue) / firstValue * 100.0f;

        String text = firstValue == lastValue ? "Steady" :
                      (firstValue > lastValue  ? "Falling" : "Rising") + " (" + String.format("%3.1f%%", percent) + ")";

        tempTrend[deviceIndex].setText(text);
    }

}

