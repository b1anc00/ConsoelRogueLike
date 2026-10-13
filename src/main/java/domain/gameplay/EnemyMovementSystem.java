package domain.gameplay;

import domain.model.Enemy;
import domain.model.Level;
import domain.model.Player;
import domain.model.Position;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnemyMovementSystem
{
    private final PathFinder pathFinder;

    public EnemyMovementSystem(
            MovementSystem movementSystem)
    {
        this.pathFinder =
                new PathFinder(movementSystem);
    }

    public boolean isAdjacent(
            Enemy enemy,
            Player player)
    {
        Position enemyPosition =
                enemy.getPosition();

        Position playerPosition =
                player.getPosition();

        int distance =
                Math.abs(
                        enemyPosition.x()
                                - playerPosition.x()
                )
                        + Math.abs(
                        enemyPosition.y()
                                - playerPosition.y()
                );

        return distance == 1;
    }

    public boolean moveTowardPlayer(
            Enemy enemy,
            Player player,
            Level level)
    {
        Set<Position> blocked =
                getOccupiedPositions(
                        level,
                        enemy
                );

        List<Position> path =
                pathFinder.findPath(
                        level,
                        enemy.getPosition(),
                        player.getPosition(),
                        blocked
                );

        if (path.size() < 2)
        {
            return false;
        }

        int distance =
                path.size() - 1;

        if (distance > enemy.getHostility())
        {
            return false;
        }

        Position next =
                path.get(1);

        if (next.equals(
                player.getPosition()))
        {
            return false;
        }

        enemy.setPosition(next);

        return true;
    }

    private Set<Position> getOccupiedPositions(
            Level level,
            Enemy currentEnemy)
    {
        Set<Position> result =
                new HashSet<>();

        for (Enemy enemy : level.getEnemies())
        {
            if (enemy != currentEnemy
                    && enemy.isAlive())
            {
                result.add(
                        enemy.getPosition()
                );
            }
        }

        return result;
    }
}