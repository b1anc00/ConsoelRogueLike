import domain.gameplay.*;
import domain.model.*;
import domain.generation.*;

import datalayer.JsonSaveRepository;
import datalayer.SaveService;
import java.nio.file.Files;
import java.nio.file.Path;
import domain.gameplay.GameSessionFactory;
import domain.generation.GraphValidator;
import domain.generation.LevelGenerator;
import datalayer.GameSaveMapper;
import datalayer.dto.GameSaveData;
import datalayer.GamePersistenceService;
import datalayer.HistoryService;
import datalayer.JsonRunHistoryRepository;
import datalayer.dto.RunRecordData;
import application.GameApplicationService;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DomainIntegrationTest
{
    private static int passed = 0;
    private static int failed = 0;

    @Test
    void runAllTests() throws Exception
    {
        main(new String[0]);
    }

    public static void main(String[] args) throws Exception
    {
        passed = 0;
        failed = 0;

        testBackpackLimit();
        testItems();
        testWeapon();
        testVampire();
        testSnakeMage();
        testOgre();
        testReward();
        testLevels();
        testLevelGeneration();
        testEnemyChase();
        testEnemyPatterns();
        testGhostVisibility();
        testFullTurn();
        testLevelProgression();
        testGameVictory();
        testInventoryActions();
        testGameSaveMapping();
        testGameSaveRoundTrip();
        testJsonSaveLoad();
        testRunHistory();
        testGamePersistence();
        testGameSessionFactory();
        testApplicationLifecycle();
        testApplicationSaveAndContinue();
        testFullCampaignLifecycle();
        testSpecialStateSaveRoundTrip();
        testContinueWithoutSave();

        System.out.println();
        System.out.println("====================");
        System.out.println("PASSED: " + passed);
        System.out.println("FAILED: " + failed);
        System.out.println("====================");

        if (failed > 0)
        {
            throw new AssertionError(failed + " integration test(s) failed");
        }
    }

    private static void testContinueWithoutSave()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== CONTINUE WITHOUT SAVE ==="
        );

        Path savePath =
                Path.of(
                        "build",
                        "test-data",
                        "missing-save.json"
                );

        Path historyPath =
                Path.of(
                        "build",
                        "test-data",
                        "missing-history.json"
                );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);

        GameApplicationService application =
                new GameApplicationService(
                        new GameSessionFactory(),
                        new GamePersistenceService(
                                new SaveService(
                                        new JsonSaveRepository(
                                                savePath
                                        )
                                ),
                                new HistoryService(
                                        new JsonRunHistoryRepository(
                                                historyPath
                                        )
                                )
                        )
                );

        check(
                "Continue returns false without save",
                !application.continueGame()
        );

        check(
                "Session remains null without save",
                application.getSession()
                        == null
        );
    }

    private static void testSpecialStateSaveRoundTrip()
    {
        System.out.println();
        System.out.println(
                "=== SPECIAL STATE SAVE ROUND TRIP ==="
        );

        Room room =
                new Room(
                        1,
                        1,
                        20,
                        20
                );

        Level level =
                new Level(
                        8,
                        List.of(room),
                        List.of()
                );

        level.setStartRoom(room);
        level.setExitRoom(room);

        Player player =
                new Player(
                        new Position(5, 5),
                        120,
                        90,
                        15,
                        18
                );

        player.sleep(2);

        Enemy vampire =
                new Enemy(
                        new Position(7, 5),
                        60,
                        42,
                        12,
                        10,
                        EnemyType.VAMPIRE,
                        9
                );

        vampire.consumeFirstAttackMiss();

        Enemy ghost =
                new Enemy(
                        new Position(8, 6),
                        25,
                        25,
                        13,
                        5,
                        EnemyType.GHOST,
                        4
                );

        ghost.setVisible(false);

        Enemy ogre =
                new Enemy(
                        new Position(10, 10),
                        90,
                        70,
                        3,
                        17,
                        EnemyType.OGRE,
                        6
                );

        ogre.rest(2);

        Enemy snake =
                new Enemy(
                        new Position(12, 12),
                        35,
                        30,
                        16,
                        8,
                        EnemyType.SNAKE_MAGE,
                        9
                );

        snake.flipSnakeHorizontalDirection();

        level.addEnemy(vampire);
        level.addEnemy(ghost);
        level.addEnemy(ogre);
        level.addEnemy(snake);

        GameSession original =
                new GameSession(player);

        original.restoreState(
                8,
                GameStatus.RUNNING
        );

        original.setCurrentLevel(level);

        GameSaveMapper mapper =
                new GameSaveMapper();

        GameSession restored =
                mapper.fromData(
                        mapper.toData(original)
                );

        check(
                "Player sleep state restored",
                restored.getPlayer()
                        .isSleeping()
        );

        check(
                "Player sleep turns restored",
                restored.getPlayer()
                        .getSleepTurns()
                        == 2
        );

        Enemy restoredVampire =
                restored.getCurrentLevel()
                        .getEnemies()
                        .get(0);

        Enemy restoredGhost =
                restored.getCurrentLevel()
                        .getEnemies()
                        .get(1);

        Enemy restoredOgre =
                restored.getCurrentLevel()
                        .getEnemies()
                        .get(2);

        Enemy restoredSnake =
                restored.getCurrentLevel()
                        .getEnemies()
                        .get(3);

        check(
                "Vampire first miss state restored",
                !restoredVampire
                        .isFirstAttackMissPending()
        );

        check(
                "Ghost visibility restored",
                !restoredGhost.isVisible()
        );

        check(
                "Ghost combat state restored",
                !restoredGhost.isInCombat()
        );

        check(
                "Ogre rest turns restored",
                restoredOgre.getRestTurns()
                        == 2
        );

        check(
                "Snake direction restored",
                restoredSnake
                        .getSnakeHorizontalDirection()
                        == -1
        );
    }

    private static void testFullCampaignLifecycle()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== FULL 21 LEVEL CAMPAIGN ==="
        );

        Path savePath =
                Path.of(
                        "build",
                        "test-data",
                        "campaign-save.json"
                );

        Path historyPath =
                Path.of(
                        "build",
                        "test-data",
                        "campaign-history.json"
                );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);

        GamePersistenceService persistence =
                new GamePersistenceService(
                        new SaveService(
                                new JsonSaveRepository(
                                        savePath
                                )
                        ),
                        new HistoryService(
                                new JsonRunHistoryRepository(
                                        historyPath
                                )
                        )
                );

        GameApplicationService application =
                new GameApplicationService(
                        new GameSessionFactory(),
                        persistence
                );

        application.newGame();

        check(
                "Campaign starts at level 1",
                application.getSession()
                        .getCurrentLevelNumber()
                        == 1
        );

        for (int expectedLevel = 1;
             expectedLevel <= 21;
             expectedLevel++)
        {
            GameSession session =
                    application.getSession();

            Level level =
                    session.getCurrentLevel();

            check(
                    "Level "
                            + expectedLevel
                            + " has 9 rooms",
                    level.getRooms()
                            .size()
                            == 9
            );

            check(
                    "Level "
                            + expectedLevel
                            + " number is correct",
                    level.getLevelNumber()
                            == expectedLevel
            );

            Room exitRoom =
                    level.getExitRoom();

            Position exitPosition =
                    new Position(
                            exitRoom.getCenterX(),
                            exitRoom.getCenterY()
                    );

            Position beforeExit =
                    new Position(
                            exitPosition.x() - 1,
                            exitPosition.y()
                    );

            Enemy enemy =
                    level.getEnemyAt(
                            beforeExit
                    );

            if (enemy != null)
            {
                level.removeEnemy(
                        enemy
                );
            }

            GroundItem item =
                    level.getGroundItemAt(
                            beforeExit
                    );

            if (item != null)
            {
                level.removeGroundItem(
                        item
                );
            }

            session.getPlayer()
                    .setPosition(
                            beforeExit
                    );

            PlayerActionResult result =
                    application.move(
                            1,
                            0
                    );

            check(
                    "Exit detected on level "
                            + expectedLevel,
                    result.getType()
                            == PlayerActionType.LEVEL_EXIT
            );

            if (expectedLevel < 21)
            {
                check(
                        "Campaign advances to level "
                                + (expectedLevel + 1),
                        application
                                .getSession()
                                .getCurrentLevelNumber()
                                == expectedLevel + 1
                );

                check(
                        "Campaign remains running after level "
                                + expectedLevel,
                        application
                                .getSession()
                                .getStatus()
                                == GameStatus.RUNNING
                );

                check(
                        "Save exists after level "
                                + expectedLevel,
                        Files.exists(
                                savePath
                        )
                );

                Room newStartRoom =
                        application
                                .getSession()
                                .getCurrentLevel()
                                .getStartRoom();

                check(
                        "Player spawned inside start room on level "
                                + (expectedLevel + 1),
                        newStartRoom
                                .containsInterior(
                                        application
                                                .getSession()
                                                .getPlayer()
                                                .getPosition()
                                )
                );
            }
        }

        check(
                "Campaign ends with victory",
                application.getSession()
                        .getStatus()
                        == GameStatus.WON
        );

        check(
                "Campaign never creates level 22",
                application.getSession()
                        .getCurrentLevelNumber()
                        == 21
        );

        check(
                "Save removed after victory",
                !Files.exists(
                        savePath
                )
        );

        List<RunRecordData> runs =
                application.getBestRuns();

        check(
                "Victory recorded exactly once",
                runs.size() == 1
        );

        check(
                "Recorded run is a victory",
                runs.get(0)
                        .won()
        );

        check(
                "Recorded run reached level 21",
                runs.get(0)
                        .reachedLevel()
                        == 21
        );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);
    }

    private static void testApplicationSaveAndContinue()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== APPLICATION SAVE / CONTINUE ==="
        );

        Path savePath =
                Path.of(
                        "build",
                        "test-data",
                        "continue-save.json"
                );

        Path historyPath =
                Path.of(
                        "build",
                        "test-data",
                        "continue-history.json"
                );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);

        GamePersistenceService firstPersistence =
                new GamePersistenceService(
                        new SaveService(
                                new JsonSaveRepository(
                                        savePath
                                )
                        ),
                        new HistoryService(
                                new JsonRunHistoryRepository(
                                        historyPath
                                )
                        )
                );

        GameApplicationService firstApplication =
                new GameApplicationService(
                        new GameSessionFactory(),
                        firstPersistence
                );

        firstApplication.newGame();

        GameSession session =
                firstApplication.getSession();

        session.getPlayer()
                .getBackpack()
                .addTreasure(123);

        Level level =
                session.getCurrentLevel();

        Room exitRoom =
                level.getExitRoom();

        Position exitPosition =
                new Position(
                        exitRoom.getCenterX(),
                        exitRoom.getCenterY()
                );

        Position beforeExit =
                new Position(
                        exitPosition.x() - 1,
                        exitPosition.y()
                );

        Enemy enemy =
                level.getEnemyAt(
                        beforeExit
                );

        if (enemy != null)
        {
            level.removeEnemy(
                    enemy
            );
        }

        GroundItem groundItem =
                level.getGroundItemAt(
                        beforeExit
                );

        if (groundItem != null)
        {
            level.removeGroundItem(
                    groundItem
            );
        }

        session.getPlayer()
                .setPosition(
                        beforeExit
                );

        PlayerActionResult result =
                firstApplication.move(
                        1,
                        0
                );

        check(
                "Application detects level exit",
                result.getType()
                        == PlayerActionType.LEVEL_EXIT
        );

        check(
                "Application advances to level 2",
                firstApplication
                        .getSession()
                        .getCurrentLevelNumber()
                        == 2
        );

        check(
                "Level exit creates save",
                Files.exists(
                        savePath
                )
        );

        check(
                "Running game was not added to history",
                firstApplication
                        .getBestRuns()
                        .isEmpty()
        );

        GamePersistenceService secondPersistence =
                new GamePersistenceService(
                        new SaveService(
                                new JsonSaveRepository(
                                        savePath
                                )
                        ),
                        new HistoryService(
                                new JsonRunHistoryRepository(
                                        historyPath
                                )
                        )
                );

        GameApplicationService secondApplication =
                new GameApplicationService(
                        new GameSessionFactory(),
                        secondPersistence
                );

        boolean continued =
                secondApplication
                        .continueGame();

        check(
                "Continue finds saved session",
                continued
        );

        check(
                "Continued game restores level 2",
                secondApplication
                        .getSession()
                        .getCurrentLevelNumber()
                        == 2
        );

        check(
                "Continued game restores treasure",
                secondApplication
                        .getSession()
                        .getPlayer()
                        .getBackpack()
                        .getTreasureAmount()
                        == 123
        );

        check(
                "Continued game restores level",
                secondApplication
                        .getSession()
                        .getCurrentLevel()
                        != null
                        && secondApplication
                        .getSession()
                        .getCurrentLevel()
                        .getRooms()
                        .size()
                        == 9
        );

        Room restoredStartRoom =
                secondApplication
                        .getSession()
                        .getCurrentLevel()
                        .getStartRoom();

        check(
                "Continued player is inside saved start room",
                restoredStartRoom
                        .containsInterior(
                                secondApplication
                                        .getSession()
                                        .getPlayer()
                                        .getPosition()
                        )
        );

        secondApplication.newGame();

        check(
                "Starting new game removes old save",
                !secondApplication
                        .hasSavedGame()
        );

        check(
                "New game starts again at level 1",
                secondApplication
                        .getSession()
                        .getCurrentLevelNumber()
                        == 1
        );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);
    }

    private static void testApplicationLifecycle()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== APPLICATION LIFECYCLE ==="
        );

        Path savePath =
                Path.of(
                        "build",
                        "test-data",
                        "application-save.json"
                );

        Path historyPath =
                Path.of(
                        "build",
                        "test-data",
                        "application-history.json"
                );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);

        SaveService saveService =
                new SaveService(
                        new JsonSaveRepository(
                                savePath
                        )
                );

        HistoryService historyService =
                new HistoryService(
                        new JsonRunHistoryRepository(
                                historyPath
                        )
                );

        GamePersistenceService persistence =
                new GamePersistenceService(
                        saveService,
                        historyService
                );

        GameApplicationService application =
                new GameApplicationService(
                        new GameSessionFactory(),
                        persistence
                );

        application.newGame();

        check(
                "Application creates new game",
                application.getSession()
                        != null
        );

        check(
                "Application creates gameplay service",
                application.getGameplay()
                        != null
        );

        check(
                "New game starts on level 1",
                application.getSession()
                        .getCurrentLevelNumber()
                        == 1
        );

        application.getSession()
                .lose();

        application.move(
                1,
                0
        );

        check(
                "Lost run added to history",
                application.getBestRuns()
                        .size()
                        == 1
        );

        check(
                "Lost run is not marked as won",
                !application.getBestRuns()
                        .get(0)
                        .won()
        );

        application.move(
                1,
                0
        );

        check(
                "Finished run is not recorded twice",
                application.getBestRuns()
                        .size()
                        == 1
        );

        Files.deleteIfExists(
                savePath
        );

        Files.deleteIfExists(
                historyPath
        );
    }

    private static void testGameSessionFactory()
    {
        System.out.println();
        System.out.println(
                "=== GAME SESSION FACTORY ==="
        );

        GameSessionFactory factory =
                new GameSessionFactory();

        GameSession session =
                factory.createNewSession();

        check(
                "New session is running",
                session.isRunning()
        );

        check(
                "New session starts at level 1",
                session.getCurrentLevelNumber()
                        == 1
        );

        check(
                "First level exists",
                session.getCurrentLevel()
                        != null
        );

        check(
                "First level has 9 rooms",
                session.getCurrentLevel()
                        .getRooms()
                        .size()
                        == 9
        );

        check(
                "First level is populated",
                !session.getCurrentLevel()
                        .getEnemies()
                        .isEmpty()
                        || !session.getCurrentLevel()
                        .getGroundItems()
                        .isEmpty()
        );

        Room startRoom =
                session.getCurrentLevel()
                        .getStartRoom();

        check(
                "Player starts inside start room",
                startRoom.containsInterior(
                        session.getPlayer()
                                .getPosition()
                )
        );

        check(
                "Start room contains no enemies",
                session.getCurrentLevel()
                        .getEnemies()
                        .stream()
                        .noneMatch(
                                enemy ->
                                        startRoom.contains(
                                                enemy.getPosition()
                                        )
                        )
        );

        check(
                "Player has initial health",
                session.getPlayer()
                        .getHealth()
                        == 100
        );
    }

    private static void testGamePersistence()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== GAME PERSISTENCE ==="
        );

        Path savePath =
                Path.of(
                        "build",
                        "test-data",
                        "persistence-save.json"
                );

        Path historyPath =
                Path.of(
                        "build",
                        "test-data",
                        "persistence-history.json"
                );

        Files.deleteIfExists(savePath);
        Files.deleteIfExists(historyPath);

        SaveService saveService =
                new SaveService(
                        new JsonSaveRepository(
                                savePath
                        )
                );

        HistoryService historyService =
                new HistoryService(
                        new JsonRunHistoryRepository(
                                historyPath
                        )
                );

        GamePersistenceService persistence =
                new GamePersistenceService(
                        saveService,
                        historyService
                );

        Room room =
                new Room(
                        1,
                        1,
                        10,
                        10
                );

        Level level =
                new Level(
                        3,
                        List.of(room),
                        List.of()
                );

        level.setStartRoom(room);
        level.setExitRoom(room);

        Player player =
                new Player(
                        new Position(4, 4),
                        100,
                        80,
                        10,
                        15
                );

        GameSession session =
                new GameSession(player);

        session.restoreState(
                3,
                GameStatus.RUNNING
        );

        session.setCurrentLevel(level);

        session.getStatistics()
                .restore(
                        250,
                        3,
                        5,
                        2,
                        1,
                        1,
                        15,
                        7,
                        180
                );

        persistence.saveAfterLevel(
                session
        );

        check(
                "Persistence creates active save",
                persistence.hasSavedSession()
        );

        GameSession loaded =
                persistence.loadLastSession();

        check(
                "Persistence loads saved session",
                loaded != null
                        && loaded
                        .getCurrentLevelNumber()
                        == 3
        );

        session.restoreState(
                3,
                GameStatus.LOST
        );

        persistence.finishRun(
                session
        );

        check(
                "Finished run removes active save",
                !persistence.hasSavedSession()
        );

        List<RunRecordData> runs =
                persistence.getBestRuns();

        check(
                "Finished run added to history",
                runs.size() == 1
        );

        check(
                "History contains run treasure",
                runs.get(0)
                        .treasure()
                        == 250
        );

        check(
                "History contains failed run",
                !runs.get(0)
                        .won()
        );

        Files.deleteIfExists(
                historyPath
        );
    }

    private static void testRunHistory()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== RUN HISTORY ==="
        );

        Path historyPath =
                Path.of(
                        "build",
                        "test-data",
                        "history.json"
                );

        Files.deleteIfExists(
                historyPath
        );

        JsonRunHistoryRepository repository =
                new JsonRunHistoryRepository(
                        historyPath
                );

        HistoryService historyService =
                new HistoryService(
                        repository
                );

        Player firstPlayer =
                new Player(
                        new Position(1, 1),
                        100,
                        0,
                        10,
                        10
                );

        GameSession firstSession =
                new GameSession(
                        firstPlayer
                );

        firstSession.restoreState(
                7,
                GameStatus.LOST
        );

        firstSession.getStatistics()
                .restore(
                        300,
                        7,
                        10,
                        3,
                        2,
                        1,
                        20,
                        12,
                        250
                );

        historyService.recordRun(
                firstSession
        );

        Player secondPlayer =
                new Player(
                        new Position(1, 1),
                        100,
                        100,
                        10,
                        10
                );

        GameSession secondSession =
                new GameSession(
                        secondPlayer
                );

        secondSession.restoreState(
                21,
                GameStatus.WON
        );

        secondSession.getStatistics()
                .restore(
                        900,
                        21,
                        40,
                        8,
                        6,
                        7,
                        100,
                        35,
                        1200
                );

        historyService.recordRun(
                secondSession
        );

        List<RunRecordData> runs =
                historyService.getBestRuns();

        check(
                "History contains both runs",
                runs.size() == 2
        );

        check(
                "Runs sorted by treasure",
                runs.get(0)
                        .treasure()
                        == 900
        );

        check(
                "Winning run stored",
                runs.get(0)
                        .won()
        );

        check(
                "Failed run stored",
                !runs.get(1)
                        .won()
        );

        check(
                "Full statistics stored",
                runs.get(0)
                        .cellsWalked()
                        == 1200
                        && runs.get(0)
                        .defeatedEnemies()
                        == 40
        );

        repository.delete();

        check(
                "History file deleted after test",
                !Files.exists(
                        historyPath
                )
        );
    }

    private static void testJsonSaveLoad()
            throws Exception
    {
        System.out.println();
        System.out.println(
                "=== JSON SAVE / LOAD ==="
        );

        Path savePath =
                Path.of(
                        "build",
                        "test-data",
                        "save.json"
                );

        Files.deleteIfExists(
                savePath
        );

        Room room =
                new Room(
                        1,
                        1,
                        10,
                        10
                );

        Level level =
                new Level(
                        5,
                        List.of(room),
                        List.of()
                );

        level.setStartRoom(room);
        level.setExitRoom(room);

        Player player =
                new Player(
                        new Position(4, 4),
                        150,
                        91,
                        13,
                        22
                );

        player.getBackpack()
                .addFood(
                        new Food(
                                "TEST_FOOD",
                                30
                        )
                );

        player.getBackpack()
                .addTreasure(777);

        player.setCurrentWeapon(
                new Weapon(
                        "TEST_AXE",
                        11
                )
        );

        Enemy enemy =
                new Enemy(
                        new Position(6, 6),
                        60,
                        33,
                        12,
                        10,
                        EnemyType.VAMPIRE,
                        9
                );

        enemy.consumeFirstAttackMiss();

        level.addEnemy(enemy);

        GameSession original =
                new GameSession(player);

        original.restoreState(
                5,
                GameStatus.RUNNING
        );

        original.setCurrentLevel(
                level
        );

        original.getStatistics()
                .restore(
                        777,
                        5,
                        12,
                        4,
                        3,
                        2,
                        30,
                        11,
                        350
                );

        JsonSaveRepository repository =
                new JsonSaveRepository(
                        savePath
                );

        SaveService saveService =
                new SaveService(
                        repository
                );

        saveService.save(
                original
        );

        check(
                "Save file was created",
                Files.exists(savePath)
        );

        check(
                "SaveService detects save",
                saveService.hasSave()
        );

        GameSession restored =
                saveService.load();

        check(
                "Loaded session is not null",
                restored != null
        );

        check(
                "Loaded level number",
                restored.getCurrentLevelNumber()
                        == 5
        );

        check(
                "Loaded player health",
                restored.getPlayer()
                        .getHealth()
                        == 91
        );

        check(
                "Loaded player max health",
                restored.getPlayer()
                        .getMaxHealth()
                        == 150
        );

        check(
                "Loaded player strength",
                restored.getPlayer()
                        .getStrength()
                        == 22
        );

        check(
                "Loaded treasure",
                restored.getPlayer()
                        .getBackpack()
                        .getTreasureAmount()
                        == 777
        );

        check(
                "Loaded equipped weapon",
                restored.getPlayer()
                        .getCurrentWeapon()
                        != null
                        && restored.getPlayer()
                        .getCurrentWeapon()
                        .getStrength()
                        == 11
        );

        check(
                "Loaded enemy",
                restored.getCurrentLevel()
                        .getEnemies()
                        .size()
                        == 1
        );

        check(
                "Loaded vampire state",
                !restored.getCurrentLevel()
                        .getEnemies()
                        .get(0)
                        .isFirstAttackMissPending()
        );

        check(
                "Loaded statistics",
                restored.getStatistics()
                        .getCellsWalked()
                        == 350
        );

        saveService.deleteSave();

        check(
                "Save file can be deleted",
                !Files.exists(savePath)
        );
    }

    private static void testGameSaveRoundTrip()
    {
        System.out.println();
        System.out.println("=== GAME SAVE ROUND TRIP ===");

        Room firstRoom =
                new Room(
                        1,
                        1,
                        8,
                        8
                );

        Room secondRoom =
                new Room(
                        15,
                        1,
                        8,
                        8
                );

        List<Position> cells =
                List.of(
                        new Position(5, 5),
                        new Position(6, 5),
                        new Position(7, 5),
                        new Position(8, 5),
                        new Position(9, 5),
                        new Position(10, 5),
                        new Position(11, 5),
                        new Position(12, 5),
                        new Position(13, 5),
                        new Position(14, 5),
                        new Position(15, 5)
                );

        Corridor corridor =
                new Corridor(
                        cells,
                        firstRoom,
                        secondRoom
                );

        Level level =
                new Level(
                        4,
                        List.of(
                                firstRoom,
                                secondRoom
                        ),
                        List.of(corridor)
                );

        level.setStartRoom(firstRoom);
        level.setExitRoom(secondRoom);

        Player player =
                new Player(
                        new Position(4, 4),
                        120,
                        73,
                        14,
                        19
                );

        player.getBackpack()
                .addFood(
                        new Food(
                                "TEST_FOOD",
                                25
                        )
                );

        player.getBackpack()
                .addTreasure(123);

        Weapon weapon =
                new Weapon(
                        "TEST_SWORD",
                        7
                );

        player.setCurrentWeapon(
                weapon
        );

        Enemy vampire =
                new Enemy(
                        new Position(17, 4),
                        60,
                        41,
                        12,
                        10,
                        EnemyType.VAMPIRE,
                        9
                );

        vampire.consumeFirstAttackMiss();

        level.addEnemy(vampire);

        level.addGroundItem(
                new GroundItem(
                        new Scroll(
                                "TEST_SCROLL",
                                StatType.STRENGTH,
                                4
                        ),
                        new Position(18, 5)
                )
        );

        GameSession original =
                new GameSession(player);

        original.restoreState(
                4,
                GameStatus.RUNNING
        );

        original.setCurrentLevel(
                level
        );

        original.addTemporaryEffect(
                new TemporaryEffect(
                        StatType.AGILITY,
                        3,
                        5
                )
        );

        original.getStatistics()
                .restore(
                        123,
                        4,
                        6,
                        2,
                        1,
                        3,
                        20,
                        8,
                        150
                );

        GameSaveMapper mapper =
                new GameSaveMapper();

        GameSaveData data =
                mapper.toData(original);

        GameSession restored =
                mapper.fromData(data);

        check(
                "Restored level number",
                restored.getCurrentLevelNumber()
                        == 4
        );

        check(
                "Restored player health",
                restored.getPlayer()
                        .getHealth()
                        == 73
        );

        check(
                "Restored player position",
                restored.getPlayer()
                        .getPosition()
                        .equals(
                                new Position(4, 4)
                        )
        );

        check(
                "Restored treasure",
                restored.getPlayer()
                        .getBackpack()
                        .getTreasureAmount()
                        == 123
        );

        check(
                "Restored equipped weapon",
                restored.getPlayer()
                        .getCurrentWeapon()
                        != null
                        && restored.getPlayer()
                        .getCurrentWeapon()
                        .getStrength()
                        == 7
        );

        check(
                "Restored rooms",
                restored.getCurrentLevel()
                        .getRooms()
                        .size()
                        == 2
        );

        check(
                "Restored corridor",
                restored.getCurrentLevel()
                        .getCorridors()
                        .size()
                        == 1
        );

        check(
                "Restored enemy",
                restored.getCurrentLevel()
                        .getEnemies()
                        .size()
                        == 1
        );

        check(
                "Restored vampire first miss state",
                !restored.getCurrentLevel()
                        .getEnemies()
                        .get(0)
                        .isFirstAttackMissPending()
        );

        check(
                "Restored ground item",
                restored.getCurrentLevel()
                        .getGroundItems()
                        .size()
                        == 1
        );

        check(
                "Restored temporary effect",
                restored.getActiveEffects()
                        .size()
                        == 1
                        && restored.getActiveEffects()
                        .get(0)
                        .getRemainingTurns()
                        == 5
        );

        check(
                "Restored statistics",
                restored.getStatistics()
                        .getCellsWalked()
                        == 150
        );
    }

    private static void testGameSaveMapping()
    {
        System.out.println();
        System.out.println("=== GAME SAVE MAPPING ===");

        LevelGenerator generator =
                new LevelGenerator();

        Level level =
                generator.generate(
                        3,
                        80,
                        25
                );

        LevelPopulator populator =
                new LevelPopulator();

        populator.populate(level);

        Room startRoom =
                level.getStartRoom();

        Player player =
                new Player(
                        new Position(
                                startRoom.getCenterX(),
                                startRoom.getCenterY()
                        ),
                        120,
                        80,
                        14,
                        17
                );

        player.getBackpack()
                .addFood(
                        new Food(
                                "TEST_FOOD",
                                25
                        )
                );

        player.getBackpack()
                .addWeapon(
                        new Weapon(
                                "TEST_WEAPON",
                                8
                        )
                );

        GameSession session =
                new GameSession(player);

        session.nextLevel();
        session.nextLevel();

        session.setCurrentLevel(level);

        GameSaveMapper mapper =
                new GameSaveMapper();

        GameSaveData data =
                mapper.toData(session);

        check(
                "Save contains correct level number",
                data.currentLevelNumber() == 3
        );

        check(
                "Save contains player health",
                data.player().health() == 80
        );

        check(
                "Save contains player max health",
                data.player().maxHealth() == 120
        );

        check(
                "Save contains player strength",
                data.player().strength() == 17
        );

        check(
                "Save contains backpack food",
                data.player()
                        .backpack()
                        .food()
                        .size() == 1
        );

        check(
                "Save contains backpack weapon",
                data.player()
                        .backpack()
                        .weapons()
                        .size() == 1
        );

        check(
                "Save contains all rooms",
                data.level()
                        .rooms()
                        .size() == 9
        );

        check(
                "Save contains corridors",
                !data.level()
                        .corridors()
                        .isEmpty()
        );

        check(
                "Save contains start room",
                data.level()
                        .startRoomIndex() >= 0
        );

        check(
                "Save contains exit room",
                data.level()
                        .exitRoomIndex() >= 0
        );

        check(
                "Save contains generated enemies",
                data.level()
                        .enemies()
                        .size()
                        == level.getEnemies()
                        .size()
        );

        check(
                "Save contains generated items",
                data.level()
                        .groundItems()
                        .size()
                        == level.getGroundItems()
                        .size()
        );
    }

    private static void testInventoryActions()
    {
        System.out.println();
        System.out.println("=== INVENTORY ACTIONS ===");

        Room room =
                new Room(
                        0,
                        0,
                        12,
                        12
                );

        Level level =
                new Level(
                        1,
                        List.of(room),
                        List.of()
                );

        Player player =
                new Player(
                        new Position(5, 5),
                        100,
                        50,
                        10,
                        10
                );

        GameSession session =
                new GameSession(player);

        session.setCurrentLevel(level);

        GameplayService gameplay =
                new GameplayService(session);

        Food food =
                new Food(
                        "TEST_FOOD",
                        20
                );

        player.getBackpack()
                .addFood(food);

        InventoryActionResult foodResult =
                gameplay.performUseFood(0);

        check(
                "Food can be used",
                foodResult.getType()
                        == InventoryActionType.FOOD_USED
        );

        check(
                "Food restores health",
                player.getHealth() == 70
        );

        check(
                "Used food disappears from backpack",
                player.getBackpack()
                        .getFood()
                        .isEmpty()
        );

        check(
                "Food statistic increases",
                session.getStatistics()
                        .getFoodEaten() == 1
        );

        Weapon firstWeapon =
                new Weapon(
                        "OLD_WEAPON",
                        5
                );

        Weapon secondWeapon =
                new Weapon(
                        "NEW_WEAPON",
                        10
                );

        player.getBackpack()
                .addWeapon(firstWeapon);

        InventoryActionResult firstEquip =
                gameplay.performEquipWeapon(0);

        check(
                "First weapon can be equipped",
                firstEquip.getType()
                        == InventoryActionType.WEAPON_EQUIPPED
        );

        check(
                "Player holds first weapon",
                player.getCurrentWeapon()
                        == firstWeapon
        );

        player.getBackpack()
                .addWeapon(secondWeapon);

        InventoryActionResult secondEquip =
                gameplay.performEquipWeapon(0);

        check(
                "Weapon can be replaced",
                secondEquip.getType()
                        == InventoryActionType.WEAPON_EQUIPPED
        );

        check(
                "Player holds new weapon",
                player.getCurrentWeapon()
                        == secondWeapon
        );

        check(
                "Old weapon was dropped on floor",
                level.getGroundItems()
                        .size() == 1
                        && level.getGroundItems()
                        .get(0)
                        .getItem()
                        == firstWeapon
        );

        Position droppedPosition =
                level.getGroundItems()
                        .get(0)
                        .getPosition();

        int dropDistance =
                Math.abs(
                        droppedPosition.x()
                                - player.getPosition().x()
                )
                        + Math.abs(
                        droppedPosition.y()
                                - player.getPosition().y()
                );

        check(
                "Old weapon was dropped on adjacent cell",
                dropDistance == 1
        );

        InventoryActionResult unequip =
                gameplay.performUnequipWeapon();

        check(
                "Weapon can be unequipped",
                unequip.getType()
                        == InventoryActionType.WEAPON_UNEQUIPPED
        );

        check(
                "Player has no equipped weapon",
                player.getCurrentWeapon() == null
        );

        check(
                "Unequipped weapon returns to backpack",
                player.getBackpack()
                        .getWeapons()
                        .contains(secondWeapon)
        );
    }

    private static void testGameVictory()
    {
        System.out.println();
        System.out.println("=== GAME VICTORY ===");

        Room room =
                new Room(
                        0,
                        0,
                        10,
                        10
                );

        Level finalLevel =
                new Level(
                        21,
                        List.of(room),
                        List.of()
                );

        finalLevel.setStartRoom(room);
        finalLevel.setExitRoom(room);

        Player player =
                new Player(
                        new Position(4, 5),
                        100,
                        100,
                        10,
                        10
                );

        GameSession session =
                new GameSession(player);

        for (int i = 0; i < 20; i++)
        {
            session.nextLevel();
        }

        session.setCurrentLevel(
                finalLevel
        );

        check(
                "Session reached level 21",
                session.getCurrentLevelNumber()
                        == 21
        );

        check(
                "Game runs before final exit",
                session.isRunning()
        );

        GameplayService gameplay =
                new GameplayService(session);

        PlayerActionResult result =
                gameplay.performPlayerMove(
                        1,
                        0
                );

        check(
                "Final exit is detected",
                result.getType()
                        == PlayerActionType.LEVEL_EXIT
        );

        check(
                "Game becomes won",
                session.getStatus()
                        == GameStatus.WON
        );

        check(
                "Level does not become 22",
                session.getCurrentLevelNumber()
                        == 21
        );
    }

    private static void testLevelProgression()
    {
        System.out.println();
        System.out.println("=== LEVEL PROGRESSION ===");

        Room room =
                new Room(
                        0,
                        0,
                        10,
                        10
                );

        Level firstLevel =
                new Level(
                        1,
                        List.of(room),
                        List.of()
                );

        firstLevel.setStartRoom(room);
        firstLevel.setExitRoom(room);

        Player player =
                new Player(
                        new Position(4, 5),
                        100,
                        100,
                        10,
                        10
                );

        GameSession session =
                new GameSession(player);

        session.setCurrentLevel(
                firstLevel
        );

        GameplayService gameplay =
                new GameplayService(session);

        PlayerActionResult result =
                gameplay.performPlayerMove(
                        1,
                        0
                );

        check(
                "Exit is detected",
                result.getType()
                        == PlayerActionType.LEVEL_EXIT
        );

        check(
                "Session advances to level 2",
                session.getCurrentLevelNumber()
                        == 2
        );

        check(
                "New level was generated",
                session.getCurrentLevel()
                        != firstLevel
        );

        check(
                "Generated level has number 2",
                session.getCurrentLevel()
                        .getLevelNumber()
                        == 2
        );

        check(
                "New level contains 9 rooms",
                session.getCurrentLevel()
                        .getRooms()
                        .size()
                        == 9
        );

        Room startRoom =
                session.getCurrentLevel()
                        .getStartRoom();

        check(
                "Player moved to new start room",
                startRoom.containsInterior(
                        player.getPosition()
                )
        );

        check(
                "Game continues after level 1",
                session.isRunning()
        );

        check(
                "New start position has no enemy",
                session.getCurrentLevel()
                        .getEnemyAt(
                                player.getPosition()
                        ) == null
        );
    }

    private static void testFullTurn()
    {
        System.out.println();
        System.out.println("=== FULL TURN ===");

        Room room =
                new Room(
                        0,
                        0,
                        20,
                        20
                );

        Level level =
                new Level(
                        1,
                        List.of(room),
                        List.of()
                );

        Player player =
                new Player(
                        new Position(5, 5),
                        100,
                        100,
                        10,
                        10
                );

        Enemy enemy =
                new Enemy(
                        new Position(7, 5),
                        50,
                        50,
                        10,
                        10,
                        EnemyType.ZOMBIE,
                        10
                );

        level.addEnemy(enemy);

        GameSession session =
                new GameSession(player);

        session.setCurrentLevel(level);

        GameplayService gameplay =
                new GameplayService(session);

        Position oldEnemyPosition =
                enemy.getPosition();

        PlayerActionResult result =
                gameplay.performPlayerMove(
                        0,
                        1
                );

        check(
                "Player action succeeds",
                result.getType()
                        == PlayerActionType.MOVED
        );

        check(
                "Player moved",
                player.getPosition()
                        .equals(
                                new Position(5, 6)
                        )
        );

        check(
                "Enemy received its turn",
                !enemy.getPosition()
                        .equals(oldEnemyPosition)
        );

        int walkedBeforeBlocked =
                session.getStatistics()
                        .getCellsWalked();

        player.setPosition(
                new Position(1, 1)
        );

        level.removeEnemy(enemy);

        Enemy ogre =
                new Enemy(
                        new Position(2, 1),
                        50,
                        50,
                        0,
                        1,
                        EnemyType.OGRE,
                        10
                );

        level.addEnemy(ogre);

        PlayerActionResult blocked =
                gameplay.performPlayerMove(
                        -1,
                        0
                );

        check(
                "Wall movement is blocked",
                blocked.getType()
                        == PlayerActionType.BLOCKED
        );

        check(
                "Blocked movement does not count as walked cell",
                session.getStatistics()
                        .getCellsWalked()
                        == walkedBeforeBlocked
        );

        check(
                "Blocked movement gives adjacent enemy a turn",
                ogre.isResting()
        );
    }

    private static void testGhostVisibility()
    {
        System.out.println();
        System.out.println("=== GHOST VISIBILITY ===");

        Player player =
                new Player(
                        new Position(0, 0),
                        100,
                        100,
                        100,
                        10
                );

        Enemy ghost =
                new Enemy(
                        new Position(1, 0),
                        50,
                        50,
                        5,
                        5,
                        EnemyType.GHOST,
                        5
                );

        ghost.setVisible(false);

        check(
                "Ghost can be invisible outside combat",
                !ghost.isVisible()
        );

        CombatSystem combatSystem =
                new CombatSystem();

        combatSystem.attack(
                player,
                ghost
        );

        check(
                "Ghost becomes visible when combat starts",
                ghost.isVisible()
                        && ghost.isInCombat()
        );
    }

    private static void testEnemyPatterns()
    {
        System.out.println();
        System.out.println("=== ENEMY PATTERNS ===");

        Room room =
                new Room(
                        0,
                        0,
                        20,
                        20
                );

        Level level =
                new Level(
                        1,
                        List.of(room),
                        List.of()
                );

        Player player =
                new Player(
                        new Position(18, 18),
                        100,
                        100,
                        10,
                        10
                );

        MovementSystem movementSystem =
                new MovementSystem();

        EnemyBehaviorSystem behavior =
                new EnemyBehaviorSystem(
                        movementSystem,
                        new Random(123)
                );

        Enemy ghost =
                new Enemy(
                        new Position(5, 5),
                        30,
                        30,
                        10,
                        5,
                        EnemyType.GHOST,
                        1
                );

        level.addEnemy(ghost);

        Position oldGhostPosition =
                ghost.getPosition();

        behavior.moveByPattern(
                ghost,
                player,
                level
        );

        check(
                "Ghost teleports to another position",
                !ghost.getPosition()
                        .equals(oldGhostPosition)
        );

        check(
                "Ghost remains inside room",
                room.containsInterior(
                        ghost.getPosition()
                )
        );

        level.removeEnemy(ghost);

        Enemy ogre =
                new Enemy(
                        new Position(10, 10),
                        100,
                        100,
                        5,
                        15,
                        EnemyType.OGRE,
                        1
                );

        level.addEnemy(ogre);

        Position oldOgrePosition =
                ogre.getPosition();

        behavior.moveByPattern(
                ogre,
                player,
                level
        );

        int ogreDistance =
                Math.abs(
                        oldOgrePosition.x()
                                - ogre.getPosition().x()
                )
                        + Math.abs(
                        oldOgrePosition.y()
                                - ogre.getPosition().y()
                );

        check(
                "Ogre moves two cells",
                ogreDistance == 2
        );

        level.removeEnemy(ogre);

        Enemy snake =
                new Enemy(
                        new Position(10, 10),
                        40,
                        40,
                        15,
                        8,
                        EnemyType.SNAKE_MAGE,
                        1
                );

        level.addEnemy(snake);

        Position oldSnakePosition =
                snake.getPosition();

        behavior.moveByPattern(
                snake,
                player,
                level
        );

        int snakeDx =
                Math.abs(
                        oldSnakePosition.x()
                                - snake.getPosition().x()
                );

        int snakeDy =
                Math.abs(
                        oldSnakePosition.y()
                                - snake.getPosition().y()
                );

        check(
                "Snake Mage moves diagonally",
                snakeDx == 1
                        && snakeDy == 1
        );
    }

    private static void testEnemyChase()
    {
        System.out.println();
        System.out.println("=== ENEMY CHASE ===");

        Room room =
                new Room(
                        0,
                        0,
                        12,
                        10
                );

        Level level =
                new Level(
                        1,
                        List.of(room),
                        List.of()
                );

        Player player =
                new Player(
                        new Position(8, 5),
                        100,
                        100,
                        10,
                        10
                );

        MovementSystem movementSystem =
                new MovementSystem();

        EnemyMovementSystem enemyMovementSystem =
                new EnemyMovementSystem(
                        movementSystem
                );

        Enemy chasingEnemy =
                new Enemy(
                        new Position(3, 5),
                        50,
                        50,
                        10,
                        10,
                        EnemyType.ZOMBIE,
                        10
                );

        level.addEnemy(chasingEnemy);

        boolean chased =
                enemyMovementSystem
                        .moveTowardPlayer(
                                chasingEnemy,
                                player,
                                level
                        );

        check(
                "Enemy inside hostility range starts chasing",
                chased
        );

        check(
                "Enemy moves one cell toward player",
                chasingEnemy
                        .getPosition()
                        .equals(
                                new Position(4, 5)
                        )
        );

        level.removeEnemy(chasingEnemy);

        Enemy passiveEnemy =
                new Enemy(
                        new Position(2, 3),
                        50,
                        50,
                        10,
                        10,
                        EnemyType.ZOMBIE,
                        1
                );

        level.addEnemy(passiveEnemy);

        Position oldPosition =
                passiveEnemy.getPosition();

        boolean passiveChased =
                enemyMovementSystem
                        .moveTowardPlayer(
                                passiveEnemy,
                                player,
                                level
                        );

        check(
                "Enemy outside hostility range does not start chasing",
                !passiveChased
        );

        check(
                "Chase system does not move enemy outside hostility range",
                passiveEnemy
                        .getPosition()
                        .equals(oldPosition)
        );
    }

    private static void testLevelGeneration()
    {
        System.out.println();
        System.out.println("=== LEVEL GENERATION ===");

        LevelGenerator generator =
                new LevelGenerator();

        GraphValidator validator =
                new GraphValidator();

        boolean correctRoomCount = true;
        boolean startAndExitExist = true;
        boolean startAndExitDifferent = true;
        boolean graphConnected = true;
        boolean corridorsExist = true;
        boolean corridorCellsRecognized = true;
        boolean roomsInsideMap = true;
        boolean uniqueRooms = true;

        int generations = 500;

        for (int i = 0; i < generations; i++)
        {
            Level level =
                    generator.generate(1);

            if (level.getRooms().size() != 9)
            {
                correctRoomCount = false;
            }

            if (level.getStartRoom() == null
                    || level.getExitRoom() == null)
            {
                startAndExitExist = false;
            }

            if (level.getStartRoom()
                    == level.getExitRoom())
            {
                startAndExitDifferent = false;
            }

            if (!validator.isConnected(
                    level.getRooms(),
                    level.getCorridors()))
            {
                graphConnected = false;
            }

            if (level.getCorridors().isEmpty())
            {
                corridorsExist = false;
            }

            Set<Room> roomSet =
                    new HashSet<>(
                            level.getRooms()
                    );

            if (roomSet.size() != 9)
            {
                uniqueRooms = false;
            }

            for (Room room : level.getRooms())
            {
                if (room.getX() < 0
                        || room.getY() < 0
                        || room.getX()
                        + room.getWidth() > 80
                        || room.getY()
                        + room.getHeight() > 25)
                {
                    roomsInsideMap = false;
                }
            }

            for (Corridor corridor :
                    level.getCorridors())
            {
                for (Position position :
                        corridor.getCells())
                {
                    if (!level.isCorridor(
                            position.x(),
                            position.y()))
                    {
                        corridorCellsRecognized =
                                false;
                    }
                }
            }
        }

        check(
                "Every level contains 9 rooms",
                correctRoomCount
        );

        check(
                "Start and exit rooms always exist",
                startAndExitExist
        );

        check(
                "Start and exit rooms are different",
                startAndExitDifferent
        );

        check(
                "Room graph is always connected",
                graphConnected
        );

        check(
                "Generated levels contain corridors",
                corridorsExist
        );

        check(
                "Corridor cells are recognized by Level",
                corridorCellsRecognized
        );

        check(
                "All rooms stay inside map bounds",
                roomsInsideMap
        );

        check(
                "All 9 rooms are unique",
                uniqueRooms
        );
    }

    private static void testBackpackLimit()
    {
        System.out.println();
        System.out.println("=== BACKPACK ===");

        Backpack backpack = new Backpack();

        for (int i = 0; i < 9; i++)
        {
            backpack.addFood(
                    new Food("FOOD_" + i, 10)
            );
        }

        boolean tenthAdded =
                backpack.addFood(
                        new Food("EXTRA_FOOD", 10)
                );

        check(
                "Backpack stores maximum 9 food",
                backpack.getFood().size() == 9
        );

        check(
                "Tenth food is rejected",
                !tenthAdded
        );
    }

    private static void testItems()
    {
        System.out.println();
        System.out.println("=== ITEMS ===");

        Player player = new Player(
                new Position(0, 0),
                100,
                50,
                10,
                10
        );

        GameSession session =
                new GameSession(player);

        GameplayService gameplay =
                new GameplayService(session);

        gameplay.pickUpItem(
                new Food("APPLE", 20)
        );

        gameplay.pickUpItem(
                new Scroll(
                        "STRENGTH_SCROLL",
                        StatType.STRENGTH,
                        5
                )
        );

        gameplay.pickUpItem(
                new Elixir(
                        "AGILITY_ELIXIR",
                        StatType.AGILITY,
                        7
                )
        );

        boolean foodUsed =
                gameplay.useFood(0);

        check(
                "Food was used",
                foodUsed
        );

        check(
                "Food restores health",
                player.getHealth() == 70
        );

        check(
                "Food statistics updated",
                session.getStatistics()
                        .getFoodEaten() == 1
        );

        boolean scrollUsed =
                gameplay.useScroll(0);

        check(
                "Scroll was used",
                scrollUsed
        );

        check(
                "Scroll permanently increases strength",
                player.getStrength() == 15
        );

        check(
                "Scroll statistics updated",
                session.getStatistics()
                        .getScrollsRead() == 1
        );

        boolean elixirUsed =
                gameplay.useElixir(0, 2);

        check(
                "Elixir was used",
                elixirUsed
        );

        check(
                "Elixir increases agility",
                player.getAgility() == 17
        );

        check(
                "Elixir statistics updated",
                session.getStatistics()
                        .getElixirsDrunk() == 1
        );

        gameplay.finishTurn();

        check(
                "Elixir active after first turn",
                player.getAgility() == 17
        );

        gameplay.finishTurn();

        check(
                "Elixir expires after second turn",
                player.getAgility() == 10
        );
    }

    private static void testWeapon()
    {
        System.out.println();
        System.out.println("=== WEAPON ===");

        Player player = new Player(
                new Position(0, 0),
                100,
                100,
                10,
                10
        );

        GameplayService gameplay =
                new GameplayService(
                        new GameSession(player)
                );

        Weapon sword =
                new Weapon("SWORD", 5);

        gameplay.pickUpItem(sword);

        WeaponEquipResult equipResult =
                gameplay.equipWeapon(0);

        check(
                "Weapon equipped",
                equipResult.isSuccess()
                        && player.getCurrentWeapon() == sword
        );

        check(
                "Equipped weapon removed from backpack",
                player.getBackpack()
                        .getWeapons()
                        .isEmpty()
        );

        boolean unequipped =
                gameplay.unequipWeapon();

        check(
                "Weapon unequipped",
                unequipped
                        && player.getCurrentWeapon() == null
        );

        check(
                "Unequipped weapon returned to backpack",
                player.getBackpack()
                        .getWeapons()
                        .size() == 1
        );
    }

    private static void testVampire()
    {
        System.out.println();
        System.out.println("=== VAMPIRE ===");

        Player player = new Player(
                new Position(0, 0),
                100,
                100,
                100,
                20
        );

        Enemy vampire = new Enemy(
                new Position(1, 0),
                100,
                100,
                5,
                10,
                EnemyType.VAMPIRE,
                10
        );

        GameplayService gameplay =
                new GameplayService(
                        new GameSession(player)
                );

        AttackResult first =
                gameplay.playerAttack(vampire);

        check(
                "First attack against vampire always misses",
                !first.isHit()
        );

        int attempts = 0;
        AttackResult result;

        do
        {
            result =
                    gameplay.playerAttack(vampire);

            attempts++;
        }
        while (!result.isHit()
                && vampire.isAlive()
                && attempts < 100);

        check(
                "Vampire can be hit after first attack",
                result.isHit()
        );

        Player victim = new Player(
                new Position(0, 0),
                100,
                100,
                5,
                5
        );

        Enemy attackingVampire = new Enemy(
                new Position(1, 0),
                100,
                100,
                100,
                1,
                EnemyType.VAMPIRE,
                10
        );

        GameplayService vampireGameplay =
                new GameplayService(
                        new GameSession(victim)
                );

        AttackResult vampireAttack;

        attempts = 0;

        do
        {
            vampireAttack =
                    vampireGameplay.enemyAttack(
                            attackingVampire
                    );

            attempts++;
        }
        while (!vampireAttack.isHit()
                && attempts < 100);

        check(
                "Vampire attack reduces maximum health",
                victim.getMaxHealth() < 100
        );

        check(
                "Vampire result reports max health damage",
                vampireAttack.getMaxHealthDamage() > 0
        );
    }

    private static void testSnakeMage()
    {
        System.out.println();
        System.out.println("=== SNAKE MAGE ===");

        Player player = new Player(
                new Position(0, 0),
                10000,
                10000,
                5,
                5
        );

        Enemy snakeMage = new Enemy(
                new Position(1, 0),
                100,
                100,
                100,
                1,
                EnemyType.SNAKE_MAGE,
                10
        );

        GameSession session =
                new GameSession(player);

        GameplayService gameplay =
                new GameplayService(session);

        boolean slept = false;

        for (int i = 0; i < 200; i++)
        {
            AttackResult result =
                    gameplay.enemyAttack(snakeMage);

            if (result.isSleepApplied())
            {
                slept = true;
                break;
            }
        }

        check(
                "Snake Mage can apply sleep",
                slept
        );

        if (slept)
        {
            boolean firstTurn =
                    gameplay.beginPlayerTurn();

            boolean secondTurn =
                    gameplay.beginPlayerTurn();

            check(
                    "Sleeping player skips next turn",
                    !firstTurn
            );

            check(
                    "Player can act after skipped turn",
                    secondTurn
            );
        }
    }

    private static void testOgre()
    {
        System.out.println();
        System.out.println("=== OGRE ===");

        Player player = new Player(
                new Position(0, 0),
                1000,
                1000,
                1000,
                5
        );

        Enemy ogre = new Enemy(
                new Position(1, 0),
                100,
                100,
                0,
                10,
                EnemyType.OGRE,
                10
        );

        GameplayService gameplay =
                new GameplayService(
                        new GameSession(player)
                );

        gameplay.enemyAttack(ogre);

        check(
                "Ogre rests after attack",
                ogre.isResting()
        );

        boolean duringRest =
                gameplay.beginEnemyTurn(ogre);

        check(
                "Ogre skips rest turn",
                !duringRest
        );

        boolean afterRest =
                gameplay.beginEnemyTurn(ogre);

        check(
                "Ogre can act after rest",
                afterRest
        );

        AttackResult guaranteed =
                gameplay.enemyAttack(ogre);

        check(
                "Ogre attack after rest is guaranteed",
                guaranteed.isHit()
        );
    }

    private static void testReward()
    {
        System.out.println();
        System.out.println("=== REWARD ===");

        Player player = new Player(
                new Position(0, 0),
                100,
                100,
                100,
                100
        );

        Enemy zombie = new Enemy(
                new Position(1, 0),
                1,
                1,
                1,
                1,
                EnemyType.ZOMBIE,
                1
        );

        GameSession session =
                new GameSession(player);

        GameplayService gameplay =
                new GameplayService(session);

        int attempts = 0;

        while (zombie.isAlive()
                && attempts < 100)
        {
            gameplay.playerAttack(zombie);
            attempts++;
        }

        check(
                "Enemy was killed",
                !zombie.isAlive()
        );

        check(
                "Defeated enemy statistic updated",
                session.getStatistics()
                        .getDefeatedEnemies() == 1
        );

        int backpackTreasure =
                player.getBackpack()
                        .getTreasureAmount();

        int statisticsTreasure =
                session.getStatistics()
                        .getTreasure();

        check(
                "Treasure reward received",
                backpackTreasure > 0
        );

        check(
                "Treasure statistics match backpack",
                backpackTreasure
                        == statisticsTreasure
        );

        check(
                "Successful hit statistic updated",
                session.getStatistics()
                        .getHitsDealt() > 0
        );
    }

    private static void testLevels()
    {
        System.out.println();
        System.out.println("=== LEVELS ===");

        Player player = new Player(
                new Position(0, 0),
                100,
                100,
                10,
                10
        );

        GameSession session =
                new GameSession(player);

        GameplayService gameplay =
                new GameplayService(session);

        for (int i = 0; i < 20; i++)
        {
            gameplay.nextLevel();
        }

        check(
                "Player reaches level 21",
                session.getCurrentLevelNumber() == 21
        );

        check(
                "Game still running on level 21",
                session.getStatus()
                        == GameStatus.RUNNING
        );

        gameplay.nextLevel();

        check(
                "Exit from level 21 wins the game",
                session.getStatus()
                        == GameStatus.WON
        );

        check(
                "Level number never becomes 22",
                session.getCurrentLevelNumber() == 21
        );

        check(
                "Reached level statistic is 21",
                session.getStatistics()
                        .getReachedLevel() == 21
        );
    }

    private static void check(
            String name,
            boolean condition)
    {
        if (condition)
        {
            System.out.println(
                    "[PASS] " + name
            );

            passed++;
        }
        else
        {
            System.out.println(
                    "[FAIL] " + name
            );

            failed++;
        }
    }
}