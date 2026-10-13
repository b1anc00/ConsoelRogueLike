package datalayer.dto;

import java.util.List;

public record RunHistoryData(
        List<RunRecordData> runs)
{
}