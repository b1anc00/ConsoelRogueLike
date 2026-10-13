package domain.model;

public class Player extends Creature
{
    private Weapon currentWeapon;
    private final Backpack backpack;
    private int sleepTurns;

    public Player(
            Position position,
            int maxHealth,
            int health,
            int agility,
            int strength)
    {
        super(position, maxHealth, health, agility, strength);
        this.sleepTurns = 0;
        this.currentWeapon = null;
        this.backpack = new Backpack();
    }

    public Weapon getCurrentWeapon()
    {
        return currentWeapon;
    }

    public void setCurrentWeapon(Weapon currentWeapon)
    {
        this.currentWeapon = currentWeapon;
    }

    public Backpack getBackpack()
    {
        return backpack;
    }

    public int getSleepTurns(){ return sleepTurns; }

    public boolean isSleeping() { return sleepTurns > 0; }

    public void sleep(int turns)
    {
        if (turns > sleepTurns)
        {
            sleepTurns = turns;
        }
    }

    public void tickSleep()
    {
        if (sleepTurns > 0)
        {
            sleepTurns--;
        }
    }
}