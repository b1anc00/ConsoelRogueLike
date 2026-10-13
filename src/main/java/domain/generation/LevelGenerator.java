package domain.generation;

import domain.model.Corridor;
import domain.model.Level;
import domain.model.Room;
import domain.model.Position;
import java.util.List;
import java.util.Random;

public class LevelGenerator {
    private final RoomGenerator roomGenerator;
    private final CorridorGenerator corridorGenerator;
    private final GraphValidator graphValidator;
    private final Random random;

    private static final int DEFAULT_MAP_WIDTH = 80;
    private static final int DEFAULT_MAP_HEIGHT = 25;

    public LevelGenerator() {
        this.random = new Random();
        this.roomGenerator = new RoomGenerator(random);
        this.corridorGenerator = new CorridorGenerator(random);
        this.graphValidator = new GraphValidator();
    }

    public LevelGenerator(Random random) {
        this.random = random;
        this.roomGenerator = new RoomGenerator(random);
        this.corridorGenerator = new CorridorGenerator(random);
        this.graphValidator = new GraphValidator();
    }

    public Level generate(int levelNumber) {
        return generate(levelNumber, DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
    }

    public Level generate(int levelNumber, int mapWidth, int mapHeight) {

        List<Room> rooms = roomGenerator.generate(mapWidth, mapHeight);

        List<Corridor> corridors = corridorGenerator.generate(rooms);

        if (!graphValidator.isConnected(rooms, corridors)) {
            throw new IllegalStateException("Граф комнат не является связным после генерации!");
        }

        Level level = new Level(levelNumber, rooms, corridors);

        Room startRoom = selectRandomRoom(rooms);
        Room exitRoom;
        do {
            exitRoom = selectRandomRoom(rooms);
        } while (exitRoom == startRoom);

        level.setStartRoom(startRoom);
        level.setExitRoom(exitRoom);

        return level;
    }

    private Room selectRandomRoom(List<Room> rooms) {
        int index = random.nextInt(rooms.size());
        return rooms.get(index);
    }
}