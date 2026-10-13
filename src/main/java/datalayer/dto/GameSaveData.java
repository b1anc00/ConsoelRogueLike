package datalayer.dto;

import java.util.List;

public record GameSaveData(
        int currentLevelNumber,
        String status,
        PlayerData player,
        LevelData level,
        StatisticsData statistics,
        List<TemporaryEffectData> activeEffects)
{
    public record PositionData(
            int x,
            int y)
    {
    }

    public record PlayerData(
            PositionData position,
            int maxHealth,
            int health,
            int agility,
            int strength,
            int sleepTurns,
            ItemData currentWeapon,
            BackpackData backpack)
    {
    }

    public record BackpackData(
            List<ItemData> food,
            List<ItemData> elixirs,
            List<ItemData> scrolls,
            List<ItemData> weapons,
            int treasureAmount)
    {
    }

    public record LevelData(
            int levelNumber,
            List<RoomData> rooms,
            List<CorridorData> corridors,
            int startRoomIndex,
            int exitRoomIndex,
            List<EnemyData> enemies,
            List<GroundItemData> groundItems)
    {
    }

    public record RoomData(
            int x,
            int y,
            int width,
            int height)
    {
    }

    public record CorridorData(
            int fromRoomIndex,
            int toRoomIndex,
            List<PositionData> cells)
    {
    }

    public record EnemyData(
            String type,
            PositionData position,
            int maxHealth,
            int health,
            int agility,
            int strength,
            int hostility,
            boolean firstAttackMiss,
            int restTurns,
            boolean guaranteedHit,
            boolean visible,
            boolean inCombat,
            int snakeHorizontalDirection)
    {
    }

    public record GroundItemData(
            PositionData position,
            ItemData item)
    {
    }

    public record ItemData(
            String type,
            String subtype,
            String statType,
            int amount,
            int healthRestore,
            int strength,
            int cost)
    {
    }

    public record TemporaryEffectData(
            String statType,
            int amount,
            int remainingTurns)
    {
    }

    public record StatisticsData(
            int treasure,
            int reachedLevel,
            int defeatedEnemies,
            int foodEaten,
            int elixirsDrunk,
            int scrollsRead,
            int hitsDealt,
            int hitsReceived,
            int cellsWalked)
    {
    }
}