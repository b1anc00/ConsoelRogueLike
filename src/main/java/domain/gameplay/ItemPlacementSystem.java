package domain.gameplay;

import domain.model.Enemy;
import domain.model.Level;
import domain.model.Player;
import domain.model.Position;
import domain.model.Room;

public class ItemPlacementSystem
{
    private final MovementSystem movementSystem;

    public ItemPlacementSystem(
            MovementSystem movementSystem)
    {
        this.movementSystem = movementSystem;
    }

    public Position findFreeAdjacentPosition(
            Level level,
            Player player)
    {
        int[][] directions =
                {
                        {1, 0},
                        {-1, 0},
                        {0, 1},
                        {0, -1}
                };

        Position current =
                player.getPosition();

        for (int[] direction : directions)
        {
            Position target =
                    new Position(
                            current.x()
                                    + direction[0],
                            current.y()
                                    + direction[1]
                    );

            if (!movementSystem.isWalkable(
                    level,
                    target))
            {
                continue;
            }

            Enemy enemy =
                    level.getEnemyAt(target);

            if (enemy != null)
            {
                continue;
            }

            if (level.getGroundItemAt(target)
                    != null)
            {
                continue;
            }

            Room exitRoom =
                    level.getExitRoom();

            if (exitRoom != null)
            {
                Position exitPosition =
                        new Position(
                                exitRoom.getCenterX(),
                                exitRoom.getCenterY()
                        );

                if (target.equals(exitPosition))
                {
                    continue;
                }
            }

            return target;
        }

        return null;
    }
}