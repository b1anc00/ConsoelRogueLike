package datalayer;

import datalayer.dto.RunRecordData;
import domain.model.GameSession;
import domain.model.GameStatus;

import java.io.IOException;
import java.util.List;

public class GamePersistenceService
{
    private final SaveService saveService;
    private final HistoryService historyService;

    public GamePersistenceService(
            SaveService saveService,
            HistoryService historyService)
    {
        this.saveService =
                saveService;

        this.historyService =
                historyService;
    }

    public void saveAfterLevel(
            GameSession session)
            throws IOException
    {
        if (session.getStatus()
                != GameStatus.RUNNING)
        {
            return;
        }

        saveService.save(
                session
        );
    }

    public void finishRun(
            GameSession session)
            throws IOException
    {
        if (session.getStatus()
                == GameStatus.RUNNING)
        {
            throw new IllegalStateException(
                    "Cannot finish a running session"
            );
        }

        historyService.recordRun(
                session
        );

        saveService.deleteSave();
    }

    public GameSession loadLastSession()
            throws IOException
    {
        return saveService.load();
    }

    public boolean hasSavedSession()
    {
        return saveService.hasSave();
    }

    public List<RunRecordData> getBestRuns()
            throws IOException
    {
        return historyService.getBestRuns();
    }

    public void deleteSavedSession()
            throws IOException
    {
        saveService.deleteSave();
    }
}