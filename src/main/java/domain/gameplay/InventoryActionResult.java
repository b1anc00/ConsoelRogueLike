package domain.gameplay;

import domain.model.Item;
import domain.model.Position;

public class InventoryActionResult
{
    private final InventoryActionType type;
    private final Item item;
    private final Position droppedPosition;

    public InventoryActionResult(
            InventoryActionType type)
    {
        this(type, null, null);
    }

    public InventoryActionResult(
            InventoryActionType type,
            Item item)
    {
        this(type, item, null);
    }

    public InventoryActionResult(
            InventoryActionType type,
            Item item,
            Position droppedPosition)
    {
        this.type = type;
        this.item = item;
        this.droppedPosition = droppedPosition;
    }

    public InventoryActionType getType()
    {
        return type;
    }

    public Item getItem()
    {
        return item;
    }

    public Position getDroppedPosition()
    {
        return droppedPosition;
    }
}