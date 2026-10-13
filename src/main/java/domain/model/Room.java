package domain.model;

import java.util.Objects;

public class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    
    private boolean isStartRoom;
    private boolean isExitRoom;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isStartRoom = false;
        this.isExitRoom = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    public boolean isStartRoom() { return isStartRoom; }
    public void setStartRoom(boolean startRoom) { isStartRoom = startRoom; }
    
    public boolean isExitRoom() { return isExitRoom; }
    public void setExitRoom(boolean exitRoom) { isExitRoom = exitRoom; }

    public int getCenterX() {
        return x + (width / 2);
    }

    public int getCenterY() {
        return y + (height / 2);
    }

    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public boolean contains(Position position) {
        return contains(position.x(), position.y());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return x == room.x && y == room.y && width == room.width && height == room.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    public boolean containsInterior(int px, int py)
    {
        return px > x
                && px < x + width - 1
                && py > y
                && py < y + height - 1;
    }

    public boolean containsInterior(Position position)
    {
        return containsInterior(
                position.x(),
                position.y()
        );
    }
}