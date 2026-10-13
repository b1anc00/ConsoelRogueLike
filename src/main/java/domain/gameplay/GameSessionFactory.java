package domain.gameplay;

import domain.generation.LevelGenerator;
import domain.generation.LevelPopulator;
import domain.generation.PlayerSpawnSystem;
import domain.model.GameSession;
import domain.model.Level;
import domain.model.Player;
import domain.model.Position;
import domain.model.Room;

public class GameSessionFactory
{
    private static final int INITIAL_MAX_HEALTH = 100;
    private static final int INITIAL_HEALTH = 100;
    private static final int INITIAL_AGILITY = 10;
    private static final int INITIAL_STRENGTH = 10;

    private final LevelGenerator levelGenerator;
    private final LevelPopulator levelPopulator;
    private final PlayerSpawnSystem playerSpawnSystem;

    public GameSessionFactory()
    {
        this.levelGenerator =
                new LevelGenerator();

        this.levelPopulator =
                new LevelPopulator();

        this.playerSpawnSystem =
                new PlayerSpawnSystem();
    }

    public GameSession createNewSession()
    {
        Level level =
                levelGenerator.generate(
                        1,
                        80,
                        25
                );

        levelPopulator.populate(
                level
        );

        Room startRoom =
                level.getStartRoom();

        Position startPosition =
                playerSpawnSystem
                        .getRandomPosition(
                                startRoom
                        );

        Player player =
                new Player(
                        startPosition,
                        INITIAL_MAX_HEALTH,
                        INITIAL_HEALTH,
                        INITIAL_AGILITY,
                        INITIAL_STRENGTH
                );

        GameSession session =
                new GameSession(
                        player
                );

        session.setCurrentLevel(
                level
        );

        return session;
    }
}