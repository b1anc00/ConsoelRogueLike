package domain.gameplay;

import domain.model.Item;

public class PlayerActionResult
{
    private final PlayerActionType type;
    private final AttackResult attackResult;
    private final Item item;

    public PlayerActionResult(PlayerActionType type)
    {
        this(type, null, null);
    }

    public PlayerActionResult(
            PlayerActionType type,
            AttackResult attackResult)
    {
        this(type, attackResult, null);
    }

    public PlayerActionResult(
            PlayerActionType type,
            Item item)
    {
        this(type, null, item);
    }

    private PlayerActionResult(
            PlayerActionType type,
            AttackResult attackResult,
            Item item)
    {
        this.type = type;
        this.attackResult = attackResult;
        this.item = item;
    }

    public PlayerActionType getType()
    {
        return type;
    }

    public AttackResult getAttackResult()
    {
        return attackResult;
    }

    public Item getItem()
    {
        return item;
    }
}