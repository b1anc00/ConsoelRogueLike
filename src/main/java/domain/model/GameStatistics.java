package domain.model;

public class GameStatistics
{
    private int treasure;
    private int reachedLevel;
    private int defeatedEnemies;
    private int foodEaten;
    private int elixirsDrunk;
    private int scrollsRead;
    private int hitsDealt;
    private int hitsReceived;
    private int cellsWalked;

    public GameStatistics()
    {
        treasure = 0;
        reachedLevel = 1;
        defeatedEnemies = 0;
        foodEaten = 0;
        elixirsDrunk = 0;
        scrollsRead = 0;
        hitsDealt = 0;
        hitsReceived = 0;
        cellsWalked = 0;
    }

    public void restore(
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
        this.treasure =
                treasure;

        this.reachedLevel =
                reachedLevel;

        this.defeatedEnemies =
                defeatedEnemies;

        this.foodEaten =
                foodEaten;

        this.elixirsDrunk =
                elixirsDrunk;

        this.scrollsRead =
                scrollsRead;

        this.hitsDealt =
                hitsDealt;

        this.hitsReceived =
                hitsReceived;

        this.cellsWalked =
                cellsWalked;
    }

    public void addTreasure(int amount)
    {
        treasure += amount;
    }

    public void setReachedLevel(int reachedLevel)
    {
        this.reachedLevel = reachedLevel;
    }

    public void incrementDefeatedEnemies()
    {
        defeatedEnemies++;
    }

    public void incrementFoodEaten()
    {
        foodEaten++;
    }

    public void incrementElixirsDrunk()
    {
        elixirsDrunk++;
    }

    public void incrementScrollsRead()
    {
        scrollsRead++;
    }

    public void incrementHitsDealt()
    {
        hitsDealt++;
    }

    public void incrementHitsReceived()
    {
        hitsReceived++;
    }

    public void incrementCellsWalked()
    {
        cellsWalked++;
    }

    public int getTreasure()
    {
        return treasure;
    }

    public int getReachedLevel()
    {
        return reachedLevel;
    }

    public int getDefeatedEnemies()
    {
        return defeatedEnemies;
    }

    public int getFoodEaten()
    {
        return foodEaten;
    }

    public int getElixirsDrunk()
    {
        return elixirsDrunk;
    }

    public int getScrollsRead()
    {
        return scrollsRead;
    }

    public int getHitsDealt()
    {
        return hitsDealt;
    }

    public int getHitsReceived()
    {
        return hitsReceived;
    }

    public int getCellsWalked()
    {
        return cellsWalked;
    }
}