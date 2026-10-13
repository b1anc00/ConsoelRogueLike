package domain.model;

public class Scroll extends Item
{
    private StatType statType;
    private int amount;

    public Scroll(String subtype, StatType statType, int amount)
    {
        super(ItemType.SCROLL, subtype);
        this.statType = statType;
        this.amount = amount;
    }

    public StatType getStatType()
    {
        return statType;
    }

    public void setStatType(StatType statType)
    {
        this.statType = statType;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }
}