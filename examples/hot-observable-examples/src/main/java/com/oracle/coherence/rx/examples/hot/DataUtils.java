package com.oracle.coherence.rx.examples.hot;

import javax.swing.JTextField;

/**
 * Utility class for UI components.
 *
 * @author Tim Middleton
 */
public class DataUtils
{

    /**
     * Device name prefix.
     */
    private   static final String DEVICE  = "Device" ;

    /**
     * Minimum temperature to display on slider.
     */
    protected static final int MIN_TEMP     = 0;

    /**
     * Max temperature to display on slider.
     */
    protected static final int MAX_TEMP     = 100;

   /**
    * Max temperature to display on slider.
    */
    protected static final int INITIAL_TEMP = 50;

    /**
     * Number of gauges to disoplay.
     */
    protected static final int GAUGES = 3;

    /**
     * Create a {@link JTextField} with the specified width and specified
     * alignment.
     *
     * @param  width  the width for the {@link JTextField}
     * @param  align  either {@link JTextField}.RIGHT or LEFT
     *
     * @return the newly created text field
     */
    protected JTextField getTextField(int width, int align)
        {
        JTextField textField = new JTextField();

        textField.setEditable(false);
        textField.setColumns(width);
        textField.setHorizontalAlignment(align);

        textField.setOpaque(true);

        return textField;
        }

    /**
     * Return the device name given an array index.
     *
     * @param i  array index
     *
     * @return the device name
     */
    protected String getDeviceName(int i)
    {
        return DEVICE + '-' + i;
    }

    /**
     * Return the device array index given a name.
     *
     * @param deviceName  the name of the device
     *
     * @return the device array index
     */
    protected  int getDeviceIndex(String deviceName)
        {
        String[] parts = deviceName.split("-");
        return Integer.parseInt(parts[1]);
        }
    }
