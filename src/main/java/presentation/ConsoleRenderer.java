package presentation;

import domain.model.*;

public class ConsoleRenderer
{
    private final int width;
    private final int height;

    public ConsoleRenderer(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void render(Level level, Player player)
    {
        char[][] map = new char[height][width];

        fillEmpty(map);
        drawRooms(map, level);
        drawCorridors(map, level);
        drawItems(map, level);
        drawEnemies(map, level);
        drawExit(map, level);
        drawPlayer(map, player);

        printMap(map);
    }

    private void drawItems(
            char[][] map,
            Level level)
    {
        for (GroundItem groundItem :
                level.getGroundItems())
        {
            char symbol;

            Item item =
                    groundItem.getItem();

            if (item instanceof Food)
            {
                symbol = '%';
            }
            else if (item instanceof Elixir)
            {
                symbol = '!';
            }
            else if (item instanceof Scroll)
            {
                symbol = '?';
            }
            else if (item instanceof Weapon)
            {
                symbol = ')';
            }
            else
            {
                symbol = '*';
            }

            Position position =
                    groundItem.getPosition();

            setCell(
                    map,
                    position.x(),
                    position.y(),
                    symbol
            );
        }
    }

    private void drawEnemies(
            char[][] map,
            Level level)
    {
        for (Enemy enemy : level.getEnemies())
        {
            if (!enemy.isAlive())
            {
                continue;
            }
            if (!enemy.isVisible())
            {
                continue;
            }
            char symbol;

            switch (enemy.getType())
            {
                case ZOMBIE:
                    symbol = 'z';
                    break;

                case VAMPIRE:
                    symbol = 'v';
                    break;

                case GHOST:
                    symbol = 'g';
                    break;

                case OGRE:
                    symbol = 'O';
                    break;

                case SNAKE_MAGE:
                    symbol = 's';
                    break;

                default:
                    symbol = '?';
            }

            Position position =
                    enemy.getPosition();

            setCell(
                    map,
                    position.x(),
                    position.y(),
                    symbol
            );
        }
    }

    private void fillEmpty(char[][] map)
    {
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                map[y][x] = ' ';
            }
        }
    }

    private void drawRooms(char[][] map, Level level)
    {
        for (Room room : level.getRooms())
        {
            for (int y = room.getY();
                 y < room.getY() + room.getHeight();
                 y++)
            {
                for (int x = room.getX();
                     x < room.getX() + room.getWidth();
                     x++)
                {
                    if (x == room.getX()
                            || x == room.getX()
                            + room.getWidth() - 1
                            || y == room.getY()
                            || y == room.getY()
                            + room.getHeight() - 1)
                    {
                        setCell(map, x, y, '#');
                    }
                    else
                    {
                        setCell(map, x, y, '.');
                    }
                }
            }
        }
    }

    private void drawCorridors(
            char[][] map,
            Level level)
    {
        for (Corridor corridor : level.getCorridors())
        {
            for (Position position : corridor.getCells())
            {
                int x = position.x();
                int y = position.y();

                if (!isInsideMap(x, y))
                {
                    continue;
                }

                if (map[y][x] == ' '
                        || map[y][x] == '#')
                {
                    map[y][x] = ':';
                }
            }
        }
    }

    private void drawExit(
            char[][] map,
            Level level)
    {
        Room exitRoom = level.getExitRoom();

        setCell(
                map,
                exitRoom.getCenterX(),
                exitRoom.getCenterY(),
                '>'
        );
    }

    private void drawPlayer(
            char[][] map,
            Player player)
    {
        Position position =
                player.getPosition();

        setCell(
                map,
                position.x(),
                position.y(),
                '@'
        );
    }

    private void printMap(char[][] map)
    {
        for (int y = 0; y < height; y++)
        {
            System.out.println(
                    new String(map[y])
            );
        }
    }

    private void setCell(
            char[][] map,
            int x,
            int y,
            char symbol)
    {
        if (isInsideMap(x, y))
        {
            map[y][x] = symbol;
        }
    }

    private boolean isInsideMap(int x, int y)
    {
        return x >= 0
                && x < width
                && y >= 0
                && y < height;
    }
}