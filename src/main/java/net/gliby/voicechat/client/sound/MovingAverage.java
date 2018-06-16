package net.gliby.voicechat.client.sound;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage
{
    private final Queue<BigDecimal> window = new LinkedList<>();
    private final int period;
    private BigDecimal sum;

    public MovingAverage(int period)
    {
        this.sum = BigDecimal.ZERO;

        if (period < 0)
        {
            System.err.println("Period must be a positive integer");
        }
        this.period = period;
    }

    public void add(int val)
    {
        BigDecimal num = new BigDecimal(val);
        this.sum = this.sum.add(num);
        this.window.add(num);

        if (this.window.size() > this.period)
        {
            this.sum = this.sum.subtract(this.window.remove());
        }
    }

    public BigDecimal getAverage()
    {
        if (this.window.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        else {
            BigDecimal divisor = BigDecimal.valueOf((long)this.window.size());
            return this.sum.divide(divisor, 2, RoundingMode.HALF_UP);
        }
    }
}