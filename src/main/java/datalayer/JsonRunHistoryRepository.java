package datalayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import datalayer.dto.RunHistoryData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class JsonRunHistoryRepository
{
    private final Gson gson;
    private final Path historyPath;

    public JsonRunHistoryRepository(
            Path historyPath)
    {
        this.historyPath =
                historyPath;

        this.gson =
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
    }

    public RunHistoryData load()
            throws IOException
    {
        if (!Files.exists(historyPath))
        {
            return new RunHistoryData(
                    new ArrayList<>()
            );
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             historyPath,
                             StandardCharsets.UTF_8))
        {
            RunHistoryData data =
                    gson.fromJson(
                            reader,
                            RunHistoryData.class
                    );

            if (data == null
                    || data.runs() == null)
            {
                return new RunHistoryData(
                        new ArrayList<>()
                );
            }

            return data;
        }
    }

    public void save(
            RunHistoryData data)
            throws IOException
    {
        Path parent =
                historyPath.getParent();

        if (parent != null)
        {
            Files.createDirectories(
                    parent
            );
        }

        try (Writer writer =
                     Files.newBufferedWriter(
                             historyPath,
                             StandardCharsets.UTF_8))
        {
            gson.toJson(
                    data,
                    writer
            );
        }
    }

    public void delete()
            throws IOException
    {
        Files.deleteIfExists(
                historyPath
        );
    }
}