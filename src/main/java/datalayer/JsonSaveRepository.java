package datalayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import datalayer.dto.GameSaveData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonSaveRepository
{
    private final Gson gson;
    private final Path savePath;

    public JsonSaveRepository(
            Path savePath)
    {
        this.savePath = savePath;

        this.gson =
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
    }

    public void save(
            GameSaveData data)
            throws IOException
    {
        Path parent =
                savePath.getParent();

        if (parent != null)
        {
            Files.createDirectories(parent);
        }

        try (Writer writer =
                     Files.newBufferedWriter(
                             savePath,
                             StandardCharsets.UTF_8))
        {
            gson.toJson(
                    data,
                    writer
            );
        }
    }

    public GameSaveData load()
            throws IOException
    {
        if (!exists())
        {
            return null;
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             savePath,
                             StandardCharsets.UTF_8))
        {
            return gson.fromJson(
                    reader,
                    GameSaveData.class
            );
        }
    }

    public boolean exists()
    {
        return Files.exists(savePath);
    }

    public void delete()
            throws IOException
    {
        Files.deleteIfExists(
                savePath
        );
    }

    public Path getSavePath()
    {
        return savePath;
    }
}