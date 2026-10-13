package domain.model;

public class Enemy extends Creature
{
    private EnemyType type;
    private int hostility;
    private boolean firstAttackMiss;
    private int restTurns;
    private boolean guaranteedHit;
    private boolean visible;
    private boolean inCombat;
    private int snakeHorizontalDirection;

    public Enemy(
            Position position,
            int maxHealth,
            int health,
            int agility,
            int strength,
            EnemyType type,
            int hostility)
    {
        super(position, maxHealth, health, agility, strength);

        this.type = type;
        this.hostility = hostility;
        this.firstAttackMiss = type == EnemyType.VAMPIRE;
        this.restTurns = 0;
        this.guaranteedHit = false;
        this.visible = true;
        this.inCombat = false;
        this.snakeHorizontalDirection = 1;
    }

    public void restoreRuntimeState(
            boolean firstAttackMiss,
            int restTurns,
            boolean guaranteedHit,
            boolean visible,
            boolean inCombat,
            int snakeHorizontalDirection)
    {
        this.firstAttackMiss =
                firstAttackMiss;

        this.restTurns =
                restTurns;

        this.guaranteedHit =
                guaranteedHit;

        this.visible =
                visible;

        this.inCombat =
                inCombat;

        this.snakeHorizontalDirection =
                snakeHorizontalDirection;
    }

    public boolean isFirstAttackMissPending()
    {
        return firstAttackMiss;
    }

    public int getRestTurns()
    {
        return restTurns;
    }

    public boolean isGuaranteedHitPending()
    {
        return guaranteedHit;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean isInCombat()
    {
        return inCombat;
    }

    public void enterCombat()
    {
        inCombat = true;
        visible = true;
    }

    public void setVisible(boolean visible)
    {
        if (!inCombat)
        {
            this.visible = visible;
        }
    }

    public void toggleVisibility()
    {
        if (!inCombat)
        {
            visible = !visible;
        }
    }

    public int getSnakeHorizontalDirection()
    {
        return snakeHorizontalDirection;
    }

    public void flipSnakeHorizontalDirection()
    {
        snakeHorizontalDirection *= -1;
    }

    public EnemyType getType()
    {
        return type;
    }

    public void setType(EnemyType type)
    {
        this.type = type;
    }

    public int getHostility()
    {
        return hostility;
    }

    public void setHostility(int hostility)
    {
        this.hostility = hostility;
    }

    public boolean consumeFirstAttackMiss()
    {
        if (!firstAttackMiss)
        {
            return false;
        }

        firstAttackMiss = false;
        return true;
    }

    public boolean isResting() { return restTurns > 0; }

    public void rest(int turns)
    {
        if (turns > restTurns)
        {
            restTurns = turns;
        }
    }

    public void tickRest()
    {
        if (restTurns > 0)
        {
            restTurns--;

            if (restTurns == 0 && type == EnemyType.OGRE)
            {
                guaranteedHit = true;
            }
        }
    }

    public boolean consumeGuaranteedHit()
    {
        if (!guaranteedHit)
        {
            return false;
        }

        guaranteedHit = false;
        return true;
    }
}