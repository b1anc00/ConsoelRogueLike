package domain.model;

public abstract class Item
{
    private final ItemType type;
    private String subtype;

    public Item(ItemType type, String subtype)
    {
        this.type = type;
        this.subtype = subtype;
    }

    public ItemType getType()
    {
        return type;
    }

    public String getSubtype()
    {
        return subtype;
    }

    public void setSubtype(String subtype)
    {
        this.subtype = subtype;
    }
}