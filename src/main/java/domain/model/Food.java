package domain.model;

public class Food extends Item
{
    private int healthRestore;

    public Food(String subtype, int healthRestore)
    {
        super(ItemType.FOOD, subtype);
        this.healthRestore = healthRestore;
    }

    public int getHealthRestore()
    {
        return healthRestore;
    }

    public void setHealthRestore(int healthRestore)
    {
        this.healthRestore = healthRestore;
    }
}