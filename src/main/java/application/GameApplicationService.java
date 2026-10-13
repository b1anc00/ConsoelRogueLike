package application;

import datalayer.GamePersistenceService;
import datalayer.HistoryService;
import datalayer.JsonRunHistoryRepository;
import datalayer.JsonSaveRepository;
import datalayer.SaveService;
import datalayer.dto.RunRecordData;
import domain.gameplay.GameSessionFactory;
import domain.gameplay.GameplayService;
import domain.gameplay.InventoryActionResult;
import domain.gameplay.PlayerActionResult;
import domain.gameplay.PlayerActionType;
import domain.model.GameSession;
import domain.model.GameStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class GameApplicationService
{
    private final GameSessionFactory sessionFactory;
    private final GamePersistenceService persistence;

    private GameSession session;
    private GameplayService gameplay;
    private boolean runFinished;

    public GameApplicationService(
            GameSessionFactory sessionFactory,
            GamePersistenceService persistence)
    {
        this.sessionFactory =
                sessionFactory;

        this.persistence =
                persistence;

        this.runFinished =
                false;
    }

    public static GameApplicationService createDefault()
    {
        SaveService saveService =
                new SaveService(
                        new JsonSaveRepository(
                                Path.of(
                                        "data",
                                        "save.json"
                                )
                        )
                );

        HistoryService historyService =
                new HistoryService(
                        new JsonRunHistoryRepository(
                                Path.of(
                                        "data",
                                        "history.json"
                                )
                        )
                );

        GamePersistenceService persistence =
                new GamePersistenceService(
                        saveService,
                        historyService
                );

        return new GameApplicationService(
                new GameSessionFactory(),
                persistence
        );
    }

    public void newGame()
            throws IOException
    {
        persistence.deleteSavedSession();

        session =
                sessionFactory
                        .createNewSession();

        gameplay =
                new GameplayService(
                        session
                );

        runFinished =
                false;
    }

    public boolean continueGame()
            throws IOException
    {
        GameSession loaded =
                persistence
                        .loadLastSession();

        if (loaded == null)
        {
            return false;
        }

        session =
                loaded;

        gameplay =
                new GameplayService(
                        session
                );

        runFinished =
                false;

        return true;
    }

    public PlayerActionResult move(
            int deltaX,
            int deltaY)
            throws IOException
    {
        ensureGameStarted();

        PlayerActionResult result =
                gameplay.performPlayerMove(
                        deltaX,
                        deltaY
                );

        processSessionState(
                result
        );

        return result;
    }

    public InventoryActionResult useFood(
            int index)
            throws IOException
    {
        ensureGameStarted();

        InventoryActionResult result =
                gameplay.performUseFood(
                        index
                );

        processSessionState(null);

        return result;
    }

    public InventoryActionResult useElixir(
            int index)
            throws IOException
    {
        ensureGameStarted();

        InventoryActionResult result =
                gameplay.performUseElixir(
                        index
                );

        processSessionState(null);

        return result;
    }

    public InventoryActionResult useScroll(
            int index)
            throws IOException
    {
        ensureGameStarted();

        InventoryActionResult result =
                gameplay.performUseScroll(
                        index
                );

        processSessionState(null);

        return result;
    }

    public InventoryActionResult equipWeapon(
            int index)
            throws IOException
    {
        ensureGameStarted();

        InventoryActionResult result =
                gameplay.performEquipWeapon(
                        index
                );

        processSessionState(null);

        return result;
    }

    public InventoryActionResult unequipWeapon()
            throws IOException
    {
        ensureGameStarted();

        InventoryActionResult result =
                gameplay.performUnequipWeapon();

        processSessionState(null);

        return result;
    }

    public GameSession getSession()
    {
        return session;
    }

    public GameplayService getGameplay()
    {
        return gameplay;
    }

    public boolean hasSavedGame()
    {
        return persistence
                .hasSavedSession();
    }

    public List<RunRecordData> getBestRuns()
            throws IOException
    {
        return persistence
                .getBestRuns();
    }

    private void processSessionState(
            PlayerActionResult actionResult)
            throws IOException
    {
        if (runFinished)
        {
            return;
        }

        GameStatus status =
                session.getStatus();

        if (status == GameStatus.WON
                || status == GameStatus.LOST)
        {
            persistence.finishRun(
                    session
            );

            runFinished =
                    true;

            return;
        }

        if (actionResult != null
                && actionResult.getType()
                == PlayerActionType.LEVEL_EXIT)
        {
            persistence.saveAfterLevel(
                    session
            );
        }
    }

    private void ensureGameStarted()
    {
        if (session == null
                || gameplay == null)
        {
            throw new IllegalStateException(
                    "Game session has not been started"
            );
        }
    }
}