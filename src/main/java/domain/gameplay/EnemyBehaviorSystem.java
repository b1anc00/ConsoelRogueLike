package domain.gameplay;

import domain.model.Enemy;
import domain.model.EnemyType;
import domain.model.Level;
import domain.model.Player;
import domain.model.Position;
import domain.model.Room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EnemyBehaviorSystem
{
    private final MovementSystem movementSystem;
    private final Random random;

    public EnemyBehaviorSystem(
            MovementSystem movementSystem)
    {
        this(
                movementSystem,
                new Random()
        );
    }

    public EnemyBehaviorSystem(
            MovementSystem movementSystem,
            Random random)
    {
        this.movementSystem = movementSystem;
        this.random = random;
    }

    public boolean moveByPattern(
            Enemy enemy,
            Player player,
            Level level)
    {
        return switch (enemy.getType())
        {
            case ZOMBIE, VAMPIRE ->
                    moveRandomCardinal(
                            enemy,
                            player,
                            level
                    );

            case GHOST ->
                    teleportGhost(
                            enemy,
                            player,
                            level
                    );

            case OGRE ->
                    moveOgre(
                            enemy,
                            player,
                            level
                    );

            case SNAKE_MAGE ->
                    moveSnakeMage(
                            enemy,
                            player,
                            level
                    );
        };
    }

    private boolean moveRandomCardinal(
            Enemy enemy,
            Player player,
            Level level)
    {
        int[][] directions =
                {
                        {1, 0},
                        {-1, 0},
                        {0, 1},
                        {0, -1}
                };

        int start =
                random.nextInt(
                        directions.length
                );

        Set<Position> blocked =
                getBlockedPositions(
                        level,
                        enemy,
                        player
                );

        for (int i = 0;
             i < directions.length;
             i++)
        {
            int index =
                    (start + i)
                            % directions.length;

            Position current =
                    enemy.getPosition();

            Position target =
                    new Position(
                            current.x()
                                    + directions[index][0],
                            current.y()
                                    + directions[index][1]
                    );

            if (isFree(
                    level,
                    target,
                    blocked))
            {
                enemy.setPosition(target);
                return true;
            }
        }

        return false;
    }

    private boolean teleportGhost(
            Enemy enemy,
            Player player,
            Level level)
    {
        Position current =
                enemy.getPosition();

        Room room =
                level.getRoomAt(
                        current.x(),
                        current.y()
                );

        if (room == null)
        {
            return moveRandomCardinal(
                    enemy,
                    player,
                    level
            );
        }

        Set<Position> blocked =
                getBlockedPositions(
                        level,
                        enemy,
                        player
                );

        List<Position> candidates =
                new ArrayList<>();

        for (int y =
             room.getY() + 1;
             y < room.getY()
                     + room.getHeight() - 1;
             y++)
        {
            for (int x =
                 room.getX() + 1;
                 x < room.getX()
                         + room.getWidth() - 1;
                 x++)
            {
                Position position =
                        new Position(x, y);

                if (!position.equals(current)
                        && !blocked.contains(position))
                {
                    candidates.add(position);
                }
            }
        }

        if (candidates.isEmpty())
        {
            return false;
        }

        Position target =
                candidates.get(
                        random.nextInt(
                                candidates.size()
                        )
                );

        enemy.setPosition(target);

        if (!enemy.isInCombat()
                && random.nextDouble() < 0.35)
        {
            enemy.toggleVisibility();
        }

        return true;
    }

    private boolean moveOgre(
            Enemy enemy,
            Player player,
            Level level)
    {
        int[][] directions =
                {
                        {1, 0},
                        {-1, 0},
                        {0, 1},
                        {0, -1}
                };

        int start =
                random.nextInt(
                        directions.length
                );

        Set<Position> blocked =
                getBlockedPositions(
                        level,
                        enemy,
                        player
                );

        Position current =
                enemy.getPosition();

        for (int i = 0;
             i < directions.length;
             i++)
        {
            int index =
                    (start + i)
                            % directions.length;

            int dx =
                    directions[index][0];

            int dy =
                    directions[index][1];

            Position middle =
                    new Position(
                            current.x() + dx,
                            current.y() + dy
                    );

            Position target =
                    new Position(
                            current.x() + dx * 2,
                            current.y() + dy * 2
                    );

            if (isFree(
                    level,
                    middle,
                    blocked)
                    && isFree(
                    level,
                    target,
                    blocked))
            {
                enemy.setPosition(target);
                return true;
            }
        }

        return false;
    }

    private boolean moveSnakeMage(
            Enemy enemy,
            Player player,
            Level level)
    {
        Set<Position> blocked =
                getBlockedPositions(
                        level,
                        enemy,
                        player
                );

        Position current =
                enemy.getPosition();

        int dx =
                enemy.getSnakeHorizontalDirection();

        int firstDy =
                random.nextBoolean()
                        ? 1
                        : -1;

        int[] verticalDirections =
                {
                        firstDy,
                        -firstDy
                };

        for (int dy : verticalDirections)
        {
            Position target =
                    new Position(
                            current.x() + dx,
                            current.y() + dy
                    );

            if (isFree(
                    level,
                    target,
                    blocked))
            {
                enemy.setPosition(target);
                enemy.flipSnakeHorizontalDirection();

                return true;
            }
        }

        enemy.flipSnakeHorizontalDirection();

        dx =
                enemy.getSnakeHorizontalDirection();

        for (int dy : verticalDirections)
        {
            Position target =
                    new Position(
                            current.x() + dx,
                            current.y() + dy
                    );

            if (isFree(
                    level,
                    target,
                    blocked))
            {
                enemy.setPosition(target);
                enemy.flipSnakeHorizontalDirection();

                return true;
            }
        }

        return false;
    }

    private boolean isFree(
            Level level,
            Position position,
            Set<Position> blocked)
    {
        return movementSystem.isWalkable(
                level,
                position
        )
                && !blocked.contains(position);
    }

    private Set<Position> getBlockedPositions(
            Level level,
            Enemy currentEnemy,
            Player player)
    {
        Set<Position> result =
                new HashSet<>();

        result.add(
                player.getPosition()
        );

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