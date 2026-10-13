package domain.gameplay;

import domain.model.Creature;
import domain.model.Enemy;
import domain.model.EnemyType;
import domain.model.Player;
import domain.model.Weapon;

import java.util.Random;

public class CombatSystem
{
    private static final double BASE_HIT_CHANCE = 0.75;
    private static final double AGILITY_MODIFIER = 0.03;

    private static final double MIN_HIT_CHANCE = 0.10;
    private static final double MAX_HIT_CHANCE = 0.95;

    private static final int VAMPIRE_MAX_HEALTH_DAMAGE = 5;

    private static final double SNAKE_MAGE_SLEEP_CHANCE = 0.25;

    private final Random random;

    public CombatSystem()
    {
        random = new Random();
    }

    public AttackResult attack(
            Creature attacker,
            Creature defender)
    {
        if (!attacker.isAlive() || !defender.isAlive())
        {
            return new AttackResult(false, 0, false);
        }
        markCombat(attacker);
        markCombat(defender);
        if (defender instanceof Enemy enemy)
        {
            if (enemy.getType() == EnemyType.VAMPIRE
                    && enemy.consumeFirstAttackMiss())
            {
                applyPostAttackState(attacker);

                return new AttackResult(
                        false,
                        0,
                        false
                );
            }
        }

        if (!isHit(attacker, defender))
        {
            applyPostAttackState(attacker);

            return new AttackResult(false, 0, false);
        }

        int damage = calculateDamage(attacker);

        defender.setHealth(
                defender.getHealth() - damage
        );

        boolean killed = !defender.isAlive();

        int maxHealthDamage = 0;
        boolean sleepApplied = false;

        if (!killed
                && attacker instanceof Enemy enemy
                && defender instanceof Player player)
        {
            if (enemy.getType() == EnemyType.VAMPIRE)
            {
                maxHealthDamage =
                        applyVampireEffect(player);
            }

            if (enemy.getType() == EnemyType.SNAKE_MAGE)
            {
                sleepApplied =
                        applySnakeMageEffect(player);
            }
        }

        applyPostAttackState(attacker);
        return new AttackResult(
                true,
                damage,
                killed,
                maxHealthDamage,
                sleepApplied
        );
    }

    private boolean isHit(
            Creature attacker,
            Creature defender)
    {
        if (attacker instanceof Enemy enemy
                && enemy.getType() == EnemyType.OGRE
                && enemy.consumeGuaranteedHit())
        {
            return true;
        }

        int agilityDifference =
                attacker.getAgility()
                        - defender.getAgility();

        double hitChance =
                BASE_HIT_CHANCE
                        + agilityDifference
                        * AGILITY_MODIFIER;

        hitChance =
                Math.max(
                        MIN_HIT_CHANCE,
                        hitChance
                );

        hitChance =
                Math.min(
                        MAX_HIT_CHANCE,
                        hitChance
                );

        return random.nextDouble() < hitChance;
    }

    private int calculateDamage(Creature attacker)
    {
        int damage = attacker.getStrength();

        if (attacker instanceof Player player)
        {
            Weapon weapon =
                    player.getCurrentWeapon();

            if (weapon != null)
            {
                damage += weapon.getStrength();
            }
        }

        return Math.max(1, damage);
    }

    private int applyVampireEffect(Player player)
    {
        int oldMaxHealth =
                player.getMaxHealth();

        int newMaxHealth =
                Math.max(
                        1,
                        oldMaxHealth
                                - VAMPIRE_MAX_HEALTH_DAMAGE
                );

        player.setMaxHealth(newMaxHealth);

        if (player.getHealth() > newMaxHealth)
        {
            player.setHealth(newMaxHealth);
        }

        return oldMaxHealth - newMaxHealth;
    }

    private boolean applySnakeMageEffect(Player player)
    {
        if (random.nextDouble()
                >= SNAKE_MAGE_SLEEP_CHANCE)
        {
            return false;
        }

        player.sleep(1);

        return true;
    }

    private void applyPostAttackState(Creature attacker)
    {
        if (attacker instanceof Enemy enemy
                && enemy.getType() == EnemyType.OGRE)
        {
            enemy.rest(1);
        }
    }

    private void markCombat(Creature creature)
    {
        if (creature instanceof Enemy enemy)
        {
            enemy.enterCombat();
        }
    }
}