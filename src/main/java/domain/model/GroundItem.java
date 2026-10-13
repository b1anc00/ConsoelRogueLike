package domain.model;

public class GroundItem
{
    private final Item item;
    private final Position position;

    public GroundItem(Item item, Position position)
    {
        this.item = item;
        this.position = position;
    }

    public Item getItem()
    {
        return item;
    }

    public Position getPosition()
    {
        return position;
    }
}