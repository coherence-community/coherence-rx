package com.oracle.coherence.rx.examples.cold;


import com.tangosol.util.UUID;

import java.io.Serializable;

/**
 * Created by timmiddleton on 22/04/16.
 */
public class Trade implements Serializable
    {
     /**
     * The unique identifier for the {@link Trade}.
     */
    private UUID id;

    /**
     * The symbol (ticker code) of the equity for the {@link Trade}
     */
    private String symbol;

    /**
     * The number of shares for the {@link Trade}.
     */
    private int amount;

    /**
     * The price at which the shares in the {@link Trade} were acquired.
     */
    private double price;


    /**
     * The standard constructor for a {@link Trade}.
     *
     * @param symbol  symbol (ticker code) of the {@link Trade}
     * @param amount  number of shares (quantity) for the {@link Trade}
     * @param price   price of the shares
     */
    public Trade(String symbol,
                 int    amount,
                 double price)
    {
        this.id     = new UUID();
        this.symbol = symbol;
        this.amount = amount;
        this.price  = price;
    }


    /**
     * Obtain the unique identifier for the {@link Trade}.
     *
     * @return the identifier
     */
    public UUID getId()
    {
        return id;
    }


    /**
     * Obtain the symbol (ticker code) of the equity (stock) for the {@link Trade}.
     *
     * @return the symbol
     */
    public String getSymbol()
    {
        return symbol;
    }


    /**
     * Obtain the value at which the shares were acquired for the {@link Trade}.
     *
     * @return the price
     */
    public double getPrice()
    {
        return price;
    }


    /**
     * Obtain the number of shares acquired for the {@link Trade}.
     *
     * @return the amount
     */
    public int getAmount()
    {
        return amount;
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


    /**
     * Set the price of the Position.
     *
     * @param price the new price.
     */
    public void setPrice(double price)
    {
        this.price = price;
    }

    @Override
    public String toString()
        {
        return "Trade{" +
               "id=" + id +
               ", symbol='" + symbol + '\'' +
               ", amount=" + amount +
               ", price=" + price +
               '}';
        }
    }
