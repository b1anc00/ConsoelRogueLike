package domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Level {
    private final int levelNumber;
    private final List<Room> rooms;
    private final List<Corridor> corridors;
    private Room startRoom;
    private Room exitRoom;
    private final List<Enemy> enemies;
    private final List<GroundItem> groundItems;

    public Level(int levelNumber, List<Room> rooms, List<Corridor> corridors) {
        this.levelNumber = levelNumber;
        this.rooms = new ArrayList<>(rooms);
        this.corridors = new ArrayList<>(corridors);
        this.enemies = new ArrayList<>();
        this.groundItems = new ArrayList<>();
    }

    public List<Enemy> getEnemies()
    {
        return Collections.unmodifiableList(enemies);
    }

    public List<GroundItem> getGroundItems()
    {
        return Collections.unmodifiableList(groundItems);
    }

    public void addEnemy(Enemy enemy)
    {
        enemies.add(enemy);
    }

    public void removeEnemy(Enemy enemy)
    {
        enemies.remove(enemy);
    }

    public void addGroundItem(GroundItem groundItem)
    {
        groundItems.add(groundItem);
    }

    public void removeGroundItem(GroundItem groundItem)
    {
        groundItems.remove(groundItem);
    }

    public Enemy getEnemyAt(Position position)
    {
        for (Enemy enemy : enemies)
        {
            if (enemy.isAlive()
                    && enemy.getPosition().equals(position))
            {
                return enemy;
            }
        }

        return null;
    }

    public GroundItem getGroundItemAt(Position position)
    {
        for (GroundItem groundItem : groundItems)
        {
            if (groundItem.getPosition().equals(position))
            {
                return groundItem;
            }
        }

        return null;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Corridor> getCorridors() {
        return Collections.unmodifiableList(corridors);
    }

    public Room getStartRoom() {
        return startRoom;
    }

    public void setStartRoom(Room startRoom) {
        this.startRoom = startRoom;
        startRoom.setStartRoom(true);
    }

    public Room getExitRoom() {
        return exitRoom;
    }

    public void setExitRoom(Room exitRoom) {
        this.exitRoom = exitRoom;
        exitRoom.setExitRoom(true);
    }

    public Room getRoomAt(int x, int y) {
        for (Room room : rooms) {
            if (room.contains(x, y)) {
                return room;
            }
        }
        return null;
    }

    public boolean isCorridor(int x, int y) {
        Position pos = new Position(x, y);
        for (Corridor corridor : corridors) {
            if (corridor.contains(pos)) {
                return true;
            }
        }
        return false;
    }
}