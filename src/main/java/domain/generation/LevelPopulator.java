package domain.generation;

import domain.model.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class LevelPopulator
{
    private final Random random;

    public LevelPopulator()
    {
        this.random = new Random();
    }

    public LevelPopulator(Random random)
    {
        this.random = random;
    }

    public void populate(Level level)
    {
        Set<Position> occupied =
                new HashSet<>();

        Position exitPosition =
                new Position(
                        level.getExitRoom().getCenterX(),
                        level.getExitRoom().getCenterY()
                );

        occupied.add(exitPosition);

        for (Room room : level.getRooms())
        {
            if (room == level.getStartRoom())
            {
                continue;
            }

            generateEnemies(
                    level,
                    room,
                    occupied
            );

            generateItem(
                    level,
                    room,
                    occupied
            );
        }
    }

    private void generateEnemies(
            Level level,
            Room room,
            Set<Position> occupied)
    {
        double chance =
                Math.min(
                        0.35
                                + level.getLevelNumber()
                                * 0.02,
                        0.80
                );

        if (random.nextDouble() > chance)
        {
            return;
        }

        int maximumExtraEnemies =
                Math.min(
                        2,
                        level.getLevelNumber() / 7
                );

        int enemyCount =
                1 + random.nextInt(
                        maximumExtraEnemies + 1
                );

        for (int i = 0; i < enemyCount; i++)
        {
            Position position =
                    findFreePosition(
                            room,
                            occupied
                    );

            if (position == null)
            {
                return;
            }

            Enemy enemy =
                    createEnemy(
                            position,
                            level.getLevelNumber()
                    );

            level.addEnemy(enemy);
            occupied.add(position);
        }
    }

    private void generateItem(
            Level level,
            Room room,
            Set<Position> occupied)
    {
        double chance =
                Math.max(
                        0.20,
                        0.60
                                - level.getLevelNumber()
                                * 0.015
                );

        if (random.nextDouble() > chance)
        {
            return;
        }

        Position position =
                findFreePosition(
                        room,
                        occupied
                );

        if (position == null)
        {
            return;
        }

        Item item = createItem();

        level.addGroundItem(
                new GroundItem(
                        item,
                        position
                )
        );

        occupied.add(position);
    }

    private Enemy createEnemy(
            Position position,
            int levelNumber)
    {
        EnemyType[] types =
                EnemyType.values();

        EnemyType type =
                types[random.nextInt(types.length)];

        double multiplier =
                1.0
                        + (levelNumber - 1)
                        * 0.08;

        int health;
        int agility;
        int strength;
        int hostility;

        switch (type)
        {
            case ZOMBIE:
                health = 50;
                agility = 4;
                strength = 9;
                hostility = 5;
                break;

            case VAMPIRE:
                health = 60;
                agility = 12;
                strength = 10;
                hostility = 9;
                break;

            case GHOST:
                health = 25;
                agility = 13;
                strength = 5;
                hostility = 4;
                break;

            case OGRE:
                health = 90;
                agility = 3;
                strength = 17;
                hostility = 6;
                break;

            case SNAKE_MAGE:
                health = 35;
                agility = 16;
                strength = 8;
                hostility = 9;
                break;

            default:
                throw new IllegalStateException(
                        "Unknown enemy type"
                );
        }

        health =
                (int) Math.round(
                        health * multiplier
                );

        strength =
                (int) Math.round(
                        strength * multiplier
                );

        return new Enemy(
                position,
                health,
                health,
                agility,
                strength,
                type,
                hostility
        );
    }

    private Item createItem()
    {
        int type =
                random.nextInt(4);

        switch (type)
        {
            case 0:
                return new Food(
                        "FOOD",
                        20 + random.nextInt(21)
                );

            case 1:
                return new Elixir(
                        "ELIXIR",
                        randomStat(),
                        3 + random.nextInt(4)
                );

            case 2:
                return new Scroll(
                        "SCROLL",
                        randomStat(),
                        2 + random.nextInt(4)
                );

            case 3:
                return new Weapon(
                        "WEAPON",
                        4 + random.nextInt(6)
                );

            default:
                throw new IllegalStateException();
        }
    }

    private StatType randomStat()
    {
        StatType[] stats =
                StatType.values();

        return stats[
                random.nextInt(stats.length)
                ];
    }

    private Position findFreePosition(
            Room room,
            Set<Position> occupied)
    {
        int interiorWidth =
                room.getWidth() - 2;

        int interiorHeight =
                room.getHeight() - 2;

        for (int attempt = 0;
             attempt < 50;
             attempt++)
        {
            int x =
                    room.getX()
                            + 1
                            + random.nextInt(
                            interiorWidth
                    );

            int y =
                    room.getY()
                            + 1
                            + random.nextInt(
                            interiorHeight
                    );

            Position position =
                    new Position(x, y);

            if (!occupied.contains(position))
            {
                return position;
            }
        }

        return null;
    }
}