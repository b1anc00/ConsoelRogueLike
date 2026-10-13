package domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameSession
{
    private final Player player;
    private final GameStatistics statistics;
    private final List<TemporaryEffect> activeEffects;

    private int currentLevelNumber;
    private GameStatus status;
    private Level currentLevel;

    public GameSession(Player player)
    {
        this.player = player;
        this.statistics = new GameStatistics();
        this.activeEffects = new ArrayList<>();

        this.currentLevelNumber = 1;
        this.status = GameStatus.RUNNING;
    }

    public void restoreState(
            int currentLevelNumber,
            GameStatus status)
    {
        this.currentLevelNumber =
                currentLevelNumber;

        this.status =
                status;
    }

    public Level getCurrentLevel() { return currentLevel; }

    public void setCurrentLevel(Level currentLevel) { this.currentLevel = currentLevel; }

    public Player getPlayer()
    {
        return player;
    }

    public GameStatistics getStatistics()
    {
        return statistics;
    }

    public int getCurrentLevelNumber()
    {
        return currentLevelNumber;
    }

    public GameStatus getStatus()
    {
        return status;
    }

    public List<TemporaryEffect> getActiveEffects()
    {
        return Collections.unmodifiableList(
                activeEffects
        );
    }

    public void addTemporaryEffect(TemporaryEffect effect)
    {
        activeEffects.add(effect);
    }

    public void removeTemporaryEffect(TemporaryEffect effect)
    {
        activeEffects.remove(effect);
    }

    public void nextLevel()
    {
        if (currentLevelNumber >= 21)
        {
            status = GameStatus.WON;
            return;
        }

        currentLevelNumber++;

        statistics.setReachedLevel(currentLevelNumber);
    }

    public void lose()
    {
        status = GameStatus.LOST;
    }

    public boolean isRunning()
    {
        return status == GameStatus.RUNNING;
    }
}