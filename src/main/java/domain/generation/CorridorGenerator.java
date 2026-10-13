package domain.generation;

import domain.model.Corridor;
import domain.model.Level;
import domain.model.Room;
import domain.model.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CorridorGenerator {
    private final Random random;
    private final GraphValidator validator;

    public CorridorGenerator() {
        this.random = new Random();
        this.validator = new GraphValidator();
    }

    public CorridorGenerator(Random random) {
        this.random = random;
        this.validator = new GraphValidator();
    }

    public List<Corridor> generate(List<Room> rooms) {
        List<Corridor> corridors = new ArrayList<>();

        List<Room> shuffledRooms = new ArrayList<>(rooms);
        Collections.shuffle(shuffledRooms, random);

        for (int i = 0; i < shuffledRooms.size() - 1; i++) {
            Room from = shuffledRooms.get(i);
            Room to = shuffledRooms.get(i + 1);
            corridors.add(createCorridor(from, to));
        }

        int extraConnections = random.nextInt(3) + 2; 
        for (int i = 0; i < extraConnections; i++) {
            int fromIdx = random.nextInt(rooms.size());
            int toIdx = random.nextInt(rooms.size());

            if (fromIdx != toIdx) {
                Room from = rooms.get(fromIdx);
                Room to = rooms.get(toIdx);

                if (!corridorExists(corridors, from, to)) {
                    corridors.add(createCorridor(from, to));
                }
            }
        }

        if (!validator.isConnected(rooms, corridors)) {
            throw new IllegalStateException("Сгенерированный граф комнат не является связным!");
        }

        return corridors;
    }

    private Corridor createCorridor(Room from, Room to) {
        int x1 = from.getCenterX();
        int y1 = from.getCenterY();
        int x2 = to.getCenterX();
        int y2 = to.getCenterY();

        List<Position> cells = new ArrayList<>();

        if (random.nextBoolean()) {
            addHorizontalSegment(cells, x1, x2, y1);
            addVerticalSegment(cells, y1, y2, x2);
        } else {
            addVerticalSegment(cells, y1, y2, x1);
            addHorizontalSegment(cells, x1, x2, y2);
        }

        return new Corridor(cells, from, to);
    }

    private void addHorizontalSegment(List<Position> cells, int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            Position pos = new Position(x, y);
            if (!cells.contains(pos)) {
                cells.add(pos);
            }
        }
    }

    private void addVerticalSegment(List<Position> cells, int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
            Position pos = new Position(x, y);
            if (!cells.contains(pos)) {
                cells.add(pos);
            }
        }
    }

    private boolean corridorExists(List<Corridor> corridors, Room from, Room to) {
        for (Corridor c : corridors) {
            if ((c.getFrom() == from && c.getTo() == to) ||
                (c.getFrom() == to && c.getTo() == from)) {
                return true;
            }
        }
        return false;
    }
}