package domain.gameplay;

import domain.model.Enemy;
import domain.model.GameSession;

import java.util.Random;

public class RewardSystem
{
    private final Random random;

    public RewardSystem()
    {
        random = new Random();
    }

    public int giveEnemyReward(GameSession session, Enemy enemy)
    {
        int treasure = calculateTreasure(enemy);

        session.getPlayer()
                .getBackpack()
                .addTreasure(treasure);

        session.getStatistics()
                .addTreasure(treasure);

        session.getStatistics()
                .incrementDefeatedEnemies();

        return treasure;
    }

    private int calculateTreasure(Enemy enemy)
    {
        int baseReward =
                enemy.getHostility()
                        + enemy.getStrength()
                        + enemy.getAgility()
                        + enemy.getMaxHealth() / 10;

        int minimumReward =
                Math.max(1, baseReward / 2);

        int maximumReward =
                Math.max(minimumReward, baseReward);

        return random.nextInt(
                maximumReward - minimumReward + 1
        ) + minimumReward;
    }
}