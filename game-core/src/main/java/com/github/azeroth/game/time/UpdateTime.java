package game;


public class UpdateTime {
    private final int[] updateTimeDataTable = new int[500];
    private int averageUpdateTime;
    private int totalUpdateTime;
    private int updateTimeTableIndex;
    private int maxUpdateTime;
    private int maxUpdateTimeOfLastTable;
    private int maxUpdateTimeOfCurrentTable;

    private int recordedTime;

    public final int getAverageUpdateTime() {
        return averageUpdateTime;
    }

    public final int getTimeWeightedAverageUpdateTime() {
        int sum = 0, weightsum = 0;

        for (var diff : updateTimeDataTable) {
            sum += diff * diff;
            weightsum += diff;
        }

        if (weightsum == 0) {
            return 0;
        }

        return sum / weightsum;
    }

    public final int getMaxUpdateTime() {
        return maxUpdateTime;
    }

    public final int getMaxUpdateTimeOfCurrentTable() {
        return Math.max(maxUpdateTimeOfCurrentTable, maxUpdateTimeOfLastTable);
    }

    public final int getLastUpdateTime() {
        return _updateTimeDataTable[_updateTimeTableIndex != 0 ? _updateTimeTableIndex - 1 : updateTimeDataTable.length - 1];
    }

    public final void updateWithDiff(int diff) {
        totalUpdateTime = _totalUpdateTime - _updateTimeDataTable[_updateTimeTableIndex] + diff;
        _updateTimeDataTable[_updateTimeTableIndex] = diff;

        if (diff > maxUpdateTime) {
            maxUpdateTime = diff;
        }

        if (diff > maxUpdateTimeOfCurrentTable) {
            maxUpdateTimeOfCurrentTable = diff;
        }

        if (++updateTimeTableIndex >= updateTimeDataTable.length) {
            updateTimeTableIndex = 0;
            maxUpdateTimeOfLastTable = maxUpdateTimeOfCurrentTable;
            maxUpdateTimeOfCurrentTable = 0;
        }


        if (_updateTimeDataTable[ ^ 1] !=0)
        {
            averageUpdateTime = (int) (_totalUpdateTime / updateTimeDataTable.length);
        }
		else if (updateTimeTableIndex != 0) {
            averageUpdateTime = _totalUpdateTime / updateTimeTableIndex;
        }
    }

    public final void recordUpdateTimeReset() {
        recordedTime = System.currentTimeMillis();
    }

    public final void recordUpdateTimeDuration(String text, int minUpdateTime) {
        var thisTime = System.currentTimeMillis();
        var diff = time.GetMSTimeDiff(recordedTime, thisTime);

        if (diff > minUpdateTime) {
            Log.outInfo(LogFilter.misc, String.format("Recored Update Time of %1$s: %2$s.", text, diff));
        }

        recordedTime = thisTime;
    }
}
