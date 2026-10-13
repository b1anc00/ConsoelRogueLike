package domain.generation;

import domain.model.Room;
import domain.model.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomGenerator {
    private final Random random;

    public RoomGenerator() {
        this.random = new Random();
    }

    public RoomGenerator(Random random) {
        this.random = random;
    }

    public List<Room> generate(int mapWidth, int mapHeight) {
        List<Room> rooms = new ArrayList<>();
        
        int sectionWidth = mapWidth / 3;
        int sectionHeight = mapHeight / 3;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                int sectionX = col * sectionWidth;
                int sectionY = row * sectionHeight;
                

                int roomWidth = random.nextInt(3) + 4;
                int roomHeight = random.nextInt(3) + 4;
                

                int marginX = random.nextInt(2) + 1;
                int marginY = random.nextInt(2) + 1;
                

                int maxOffsetX = sectionWidth - roomWidth - marginX;
                int maxOffsetY = sectionHeight - roomHeight - marginY;
                
                int offsetX = random.nextInt(Math.max(1, maxOffsetX));
                int offsetY = random.nextInt(Math.max(1, maxOffsetY));
                

                int roomX = sectionX + marginX + offsetX;
                int roomY = sectionY + marginY + offsetY;
                
                rooms.add(new Room(roomX, roomY, roomWidth, roomHeight));
            }
        }
        
        return rooms;
    }
}