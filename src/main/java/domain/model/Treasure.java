package domain.model;

public class Treasure extends Item
{
    private int cost;

    public Treasure(String subtype, int cost)
    {
        super(ItemType.TREASURE, subtype);
        this.cost = cost;
    }

    public int getCost()
    {
        return cost;
    }

    public void setCost(int cost)
    {
        this.cost = cost;
    }
}