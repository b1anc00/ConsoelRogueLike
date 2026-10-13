package domain.model;

public class Weapon extends Item
{
    private int strength;

    public Weapon(String subtype, int strength)
    {
        super(ItemType.WEAPON, subtype);
        this.strength = strength;
    }

    public int getStrength()
    {
        return strength;
    }

    public void setStrength(int strength)
    {
        this.strength = strength;
    }
}