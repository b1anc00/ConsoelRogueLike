package domain.gameplay;

import domain.model.Level;
import domain.model.Player;
import domain.model.Position;
import domain.model.Room;

public class MovementSystem
{
    public MovementResult movePlayer(
            Player player,
            Level level,
            int deltaX,
            int deltaY)
    {
        if (!isValidDirection(deltaX, deltaY))
        {
            return MovementResult.INVALID_DIRECTION;
        }

        Position current =
                player.getPosition();

        Position target =
                new Position(
                        current.x() + deltaX,
                        current.y() + deltaY
                );

        if (!isWalkable(level, target))
        {
            return MovementResult.BLOCKED;
        }

        player.setPosition(target);

        return MovementResult.MOVED;
    }

    public boolean isWalkable(
            Level level,
            Position position)
    {
        for (Room room : level.getRooms())
        {
            if (room.containsInterior(position))
            {
                return true;
            }
        }

        return level.isCorridor(
                position.x(),
                position.y()
        );
    }

    private boolean isValidDirection(
            int deltaX,
            int deltaY)
    {
        return Math.abs(deltaX)
                + Math.abs(deltaY) == 1;
    }
}