package datalayer;

import datalayer.dto.GameSaveData;
import domain.model.GameSession;

import java.io.IOException;

public class SaveService
{
    private final GameSaveMapper mapper;
    private final JsonSaveRepository repository;

    public SaveService(
            JsonSaveRepository repository)
    {
        this.mapper =
                new GameSaveMapper();

        this.repository =
                repository;
    }

    public void save(
            GameSession session)
            throws IOException
    {
        GameSaveData data =
                mapper.toData(
                        session
                );

        repository.save(data);
    }

    public GameSession load()
            throws IOException
    {
        GameSaveData data =
                repository.load();

        if (data == null)
        {
            return null;
        }

        return mapper.fromData(data);
    }

    public boolean hasSave()
    {
        return repository.exists();
    }

    public void deleteSave()
            throws IOException
    {
        repository.delete();
    }
}