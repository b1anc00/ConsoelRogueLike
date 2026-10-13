package domain.model;

public abstract class Creature
{
    private Position position;
    private int maxHealth;
    private int health;
    private int agility;
    private int strength;

    public Creature(Position position, int maxHealth, int health, int agility, int strength)
    {
        this.position = position;
        this.maxHealth = maxHealth;
        this.health = health;
        this.agility = agility;
        this.strength = strength;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    public int getMaxHealth()
    {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth)
    {
        this.maxHealth = maxHealth;
    }

    public int getHealth()
    {
        return health;
    }

    public void setHealth(int health)
    {
        this.health = health;
    }

    public int getAgility()
    {
        return agility;
    }

    public void setAgility(int agility)
    {
        this.agility = agility;
    }

    public int getStrength()
    {
        return strength;
    }

    public void setStrength(int strength)
    {
        this.strength = strength;
    }

    public boolean isAlive()
    {
        return health > 0;
    }
}