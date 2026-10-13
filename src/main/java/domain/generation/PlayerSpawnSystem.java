package domain.generation;

import domain.model.Position;
import domain.model.Room;

import java.util.Random;

public class PlayerSpawnSystem
{
    private final Random random;

    public PlayerSpawnSystem()
    {
        this.random =
                new Random();
    }

    public PlayerSpawnSystem(
            Random random)
    {
        this.random =
                random;
    }

    public Position getRandomPosition(
            Room room)
    {
        int minX =
                room.getX() + 1;

        int maxX =
                room.getX()
                        + room.getWidth()
                        - 2;

        int minY =
                room.getY() + 1;

        int maxY =
                room.getY()
                        + room.getHeight()
                        - 2;

        int x =
                minX
                        + random.nextInt(
                        maxX - minX + 1
                );

        int y =
                minY
                        + random.nextInt(
                        maxY - minY + 1
                );

        return new Position(
                x,
                y
        );
    }
}