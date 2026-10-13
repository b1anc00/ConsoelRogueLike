package application;

public record RunRecordData(
    int treasure,
    int reachedLevel,
    int defeatedEnemies,
    int foodEaten,
    int elixirsDrunk,
    int scrollsRead,
    int hitsDealt,
    int hitsReceived,
    int cellsWalked,
    boolean won
) {}