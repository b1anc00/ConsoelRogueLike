package domain.model;

public class TemporaryEffect
{
    private final StatType statType;
    private final int amount;
    private int remainingTurns;

    public TemporaryEffect(StatType statType, int amount, int remainingTurns)
    {
        this.statType = statType;
        this.amount = amount;
        this.remainingTurns = remainingTurns;
    }

    public StatType getStatType()
    {
        return statType;
    }

    public int getAmount()
    {
        return amount;
    }

    public int getRemainingTurns()
    {
        return remainingTurns;
    }

    public boolean tick()
    {
        if (remainingTurns > 0)
        {
            remainingTurns--;
        }

        return remainingTurns == 0;
    }
}