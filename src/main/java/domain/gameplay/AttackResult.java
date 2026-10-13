package domain.gameplay;

public class AttackResult
{
    private final boolean performed;
    private final boolean hit;
    private final int damage;
    private final boolean killed;

    private final int maxHealthDamage;
    private final boolean sleepApplied;

    public AttackResult(
            boolean hit,
            int damage,
            boolean killed)
    {
        this(
                true,
                hit,
                damage,
                killed,
                0,
                false
        );
    }

    public AttackResult(
            boolean hit,
            int damage,
            boolean killed,
            int maxHealthDamage,
            boolean sleepApplied)
    {
        this(
                true,
                hit,
                damage,
                killed,
                maxHealthDamage,
                sleepApplied
        );
    }

    private AttackResult(
            boolean performed,
            boolean hit,
            int damage,
            boolean killed,
            int maxHealthDamage,
            boolean sleepApplied)
    {
        this.performed = performed;
        this.hit = hit;
        this.damage = damage;
        this.killed = killed;
        this.maxHealthDamage = maxHealthDamage;
        this.sleepApplied = sleepApplied;
    }

    public static AttackResult skipped()
    {
        return new AttackResult(
                false,
                false,
                0,
                false,
                0,
                false
        );
    }

    public boolean isPerformed()
    {
        return performed;
    }

    public boolean isHit()
    {
        return hit;
    }

    public int getDamage()
    {
        return damage;
    }

    public boolean isKilled()
    {
        return killed;
    }

    public int getMaxHealthDamage()
    {
        return maxHealthDamage;
    }

    public boolean isSleepApplied()
    {
        return sleepApplied;
    }
}