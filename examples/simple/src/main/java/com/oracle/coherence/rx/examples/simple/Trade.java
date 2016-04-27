/*
 * File: Trade.java
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

package com.oracle.coherence.rx.examples.simple;


import com.tangosol.util.UUID;

import java.io.Serializable;


/**
 * An immutable class to represent a single position in a financial market for an equity (stock).
 *
 * @author Tim Middleton  2016.04.27
 */
public class Trade implements Serializable
    {
    // ----- constructors ----------------------------------------------------

    /**
     * The standard constructor for a {@link Trade}.
     *
     * @param sSymbol  symbol (ticker code) of the {@link Trade}
     * @param nAmount  number of shares (quantity) for the {@link Trade}
     * @param nPrice   price of the shares
     */
    public Trade(String sSymbol,
                 int    nAmount,
                 double nPrice)
        {
        f_id      = new UUID();
        f_sSymbol = sSymbol;
        f_nAmount = nAmount;
        f_nPrice  = nPrice;
        }

    // ----- accessors -------------------------------------------------------

    /**
     * Obtain the unique identifier for the {@link Trade}.
     *
     * @return the identifier
     */
    public UUID getId()
        {
        return f_id;
        }

    /**
     * Obtain the symbol (ticker code) of the equity (stock) for the {@link Trade}.
     *
     * @return the symbol
     */
    public String getSymbol()
        {
        return f_sSymbol;
        }

    /**
     * Obtain the value at which the shares were acquired for the {@link Trade}.
     *
     * @return the price
     */
    public double getPrice()
        {
        return f_nPrice;
        }

    /**
     * Obtain the number of shares acquired for the {@link Trade}.
     *
     * @return the amount
     */
    public int getAmount()
        {
        return f_nAmount;
        }


    /**
     * Obtain the original purchase value of the {@link Trade}. (value = price * quantity)
     *
     * @return the value
     */
    public double getPurchaseValue()
        {
        return getAmount() * getPrice();
        }

    // ----- Object methods --------------------------------------------------

    @Override
    public String toString()
        {
        return "Trade{" +
               "id=" + f_id +
               ", symbol='" + f_sSymbol + '\'' +
               ", amount=" + f_nAmount +
               ", price=" + f_nPrice +
               '}';
        }

   // ----- data members ---------------------------------------------------

   /**
    * The unique identifier for the {@link Trade}.
    */
   private final UUID f_id;

    /**
     * The symbol (ticker code) of the equity for the {@link Trade}
     */
   private final String f_sSymbol;

    /**
     * The number of shares for the {@link Trade}.
     */
    private final int f_nAmount;

    /**
     * The price at which the shares in the {@link Trade} were acquired.
     */
    private final double f_nPrice;
    }