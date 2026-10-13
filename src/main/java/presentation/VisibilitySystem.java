package presentation;

import domain.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VisibilitySystem
{
    private static final int VIEW_RADIUS = 5;

    private final int mapWidth;
    private final int mapHeight;

    private final Set<Position> exploredPositions =
            new HashSet<>();

    private final Set<Position> visiblePositions =
            new HashSet<>();

    private Level currentLevel;

    public VisibilitySystem(int mapWidth, int mapHeight)
    {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void updateVisibility(Level level, Player player)
    {
        if (currentLevel != level)
        {
            clear();
            currentLevel = level;
        }

        visiblePositions.clear();

        Position playerPosition =
                player.getPosition();

        Room currentRoom =
                level.getRoomAt(
                        playerPosition.x(),
                        playerPosition.y()
                );

        if (currentRoom != null)
        {
            revealRoom(currentRoom);
        }

        revealNearby(level, playerPosition);

        visiblePositions.add(playerPosition);
        exploredPositions.addAll(visiblePositions);
    }

    private void revealRoom(Room room)
    {
        for (int y = room.getY();
             y < room.getY() + room.getHeight();
             y++)
        {
            for (int x = room.getX();
                 x < room.getX() + room.getWidth();
                 x++)
            {
                Position position = new Position(x, y);

                if (isInsideMap(position))
                {
                    visiblePositions.add(position);
                }
            }
        }
    }

    private void revealNearby(Level level, Position origin)
    {
        for (int dy = -VIEW_RADIUS;
             dy <= VIEW_RADIUS;
             dy++)
        {
            for (int dx = -VIEW_RADIUS;
                 dx <= VIEW_RADIUS;
                 dx++)
            {
                if (dx * dx + dy * dy
                        > VIEW_RADIUS * VIEW_RADIUS)
                {
                    continue;
                }

                Position target = new Position(
                        origin.x() + dx,
                        origin.y() + dy
                );

                if (!isInsideMap(target))
                {
                    continue;
                }

                if (hasLineOfSight(origin, target, level))
                {
                    visiblePositions.add(target);
                }
            }
        }
    }

    private boolean hasLineOfSight(
            Position from,
            Position to,
            Level level)
    {
        int x0 = from.x();
        int y0 = from.y();

        int x1 = to.x();
        int y1 = to.y();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int error = dx - dy;

        int x = x0;
        int y = y0;

        while (true)
        {
            Position current = new Position(x, y);

            if (x == x1 && y == y1)
            {
                return true;
            }

            if (!current.equals(from)
                    && !isTransparent(current, level))
            {
                return false;
            }

            int doubledError = 2 * error;

            if (doubledError > -dy)
            {
                error -= dy;
                x += sx;
            }

            if (doubledError < dx)
            {
                error += dx;
                y += sy;
            }
        }
    }

    private boolean isTransparent(Position position, Level level)
    {
        for (Corridor corridor : level.getCorridors())
        {
            if (corridor.contains(position))
            {
                return true;
            }
        }

        for (Room room : level.getRooms())
        {
            if (room.contains(position))
            {
                return !isRoomBorder(room, position);
            }
        }

        return false;
    }

    private boolean isRoomBorder(Room room, Position position)
    {
        int x = position.x();
        int y = position.y();

        return x == room.getX()
                || x == room.getX() + room.getWidth() - 1
                || y == room.getY()
                || y == room.getY() + room.getHeight() - 1;
    }

    private boolean isInsideMap(Position position)
    {
        return position.x() >= 0
                && position.x() < mapWidth
                && position.y() >= 0
                && position.y() < mapHeight;
    }

    public boolean isVisible(
            Position position,
            Level level,
            Player player)
    {
        return currentLevel == level
                && visiblePositions.contains(position);
    }

    public boolean isExplored(Position position)
    {
        return exploredPositions.contains(position);
    }

    public Collection<Position> getExploredPositions()
    {
        return Collections.unmodifiableSet(exploredPositions);
    }

    public void restoreExploredPositions(
            Collection<Position> positions)
    {
        exploredPositions.clear();
        exploredPositions.addAll(positions);
    }

    public void clear()
    {
        exploredPositions.clear();
        visiblePositions.clear();
        currentLevel = null;
    }
}