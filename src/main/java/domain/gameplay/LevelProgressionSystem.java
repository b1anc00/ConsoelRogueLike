package domain.gameplay;

import domain.generation.LevelGenerator;
import domain.generation.LevelPopulator;
import domain.model.GameSession;
import domain.model.Level;
import domain.model.Position;
import domain.model.Room;
import domain.generation.PlayerSpawnSystem;

public class LevelProgressionSystem
{
    private static final int MAP_WIDTH = 80;
    private static final int MAP_HEIGHT = 25;

    private final LevelGenerator levelGenerator;
    private final LevelPopulator levelPopulator;
    private final PlayerSpawnSystem playerSpawnSystem;

    public LevelProgressionSystem()
    {
        this.levelGenerator =
                new LevelGenerator();

        this.levelPopulator =
                new LevelPopulator();

        this.playerSpawnSystem =
                new PlayerSpawnSystem();
    }

    public boolean advanceToNextLevel(
            GameSession session)
    {
        session.nextLevel();

        if (!session.isRunning())
        {
            return false;
        }

        int levelNumber =
                session.getCurrentLevelNumber();

        Level nextLevel =
                levelGenerator.generate(
                        levelNumber,
                        MAP_WIDTH,
                        MAP_HEIGHT
                );

        levelPopulator.populate(nextLevel);

        Room startRoom =
                nextLevel.getStartRoom();

        Position startPosition =
                playerSpawnSystem
                        .getRandomPosition(
                                startRoom
                        );

        session.getPlayer()
                .setPosition(startPosition);

        session.setCurrentLevel(nextLevel);

        return true;
    }
}