package datalayer;

import datalayer.dto.RunHistoryData;
import datalayer.dto.RunRecordData;
import domain.model.GameSession;
import domain.model.GameStatistics;
import domain.model.GameStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryService
{
    private final JsonRunHistoryRepository repository;

    public HistoryService(
            JsonRunHistoryRepository repository)
    {
        this.repository =
                repository;
    }

    public void recordRun(
            GameSession session)
            throws IOException
    {
        GameStatistics statistics =
                session.getStatistics();

        RunRecordData record =
                new RunRecordData(
                        statistics.getTreasure(),
                        statistics.getReachedLevel(),
                        statistics.getDefeatedEnemies(),
                        statistics.getFoodEaten(),
                        statistics.getElixirsDrunk(),
                        statistics.getScrollsRead(),
                        statistics.getHitsDealt(),
                        statistics.getHitsReceived(),
                        statistics.getCellsWalked(),
                        session.getStatus()
                                == GameStatus.WON
                );

        RunHistoryData history =
                repository.load();

        List<RunRecordData> runs =
                new ArrayList<>(
                        history.runs()
                );

        runs.add(record);

        repository.save(
                new RunHistoryData(runs)
        );
    }

    public List<RunRecordData> getBestRuns()
            throws IOException
    {
        List<RunRecordData> runs =
                new ArrayList<>(
                        repository.load()
                                .runs()
                );

        runs.sort(
                Comparator
                        .comparingInt(
                                RunRecordData::treasure
                        )
                        .reversed()
                        .thenComparing(
                                Comparator
                                        .comparingInt(
                                                RunRecordData::reachedLevel
                                        )
                                        .reversed()
                        )
        );

        return List.copyOf(runs);
    }
}