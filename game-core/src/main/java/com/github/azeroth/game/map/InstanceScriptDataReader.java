package com.github.azeroth.game.map;


import java.util.Objects;

class InstanceScriptDataReader {
    private final InstanceScript instance;
    private JsonDocument doc;

    public InstanceScriptDataReader(InstanceScript instance) {
        instance = instance;
    }

    public final Result load(String data) {
		/*
		   Expected JSON

		    {
		        "Header": "HEADER_STRING_SET_BY_SCRIPT",
		        "BossStates": [0,2,0,...] // indexes are boss ids, values are EncounterState
		        "AdditionalData: { // optional
		            "ExtraKey1": 123
		            "AnotherExtraKey": 2.0
		        }
		    }
		*/

        try {
            doc = JsonDocument.parse(data);
        } catch (JsonException ex) {
            Log.outError(LogFilter.Scripts, String.format("JSON parser error %1$s at %2$s while loading data for instance %3$s [%4$s-%5$s | %6$s-%7$s]", ex.getMessage(), ex.LineNumber, getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

            return result.MalformedJson;
        }

        if (doc.RootElement.ValueKind != JsonValueKind.object) {
            Log.outError(LogFilter.Scripts, String.format("Root JSON value is not an object for instance %1$s [%2$s-%3$s | %4$s-%5$s]", getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

            return result.RootIsNotAnObject;
        }

        var result = parseHeader();

        if (result != result.Ok) {
            return result;
        }

        result = parseBossStates();

        if (result != result.Ok) {
            return result;
        }

        result = parseAdditionalData();

        if (result != result.Ok) {
            return result;
        }

        return result.Ok;
    }

    private Result parseHeader() {
        var header;

        if (!doc.RootElement.TryGetProperty("Header", out header)) {
            Log.outError(LogFilter.Scripts, String.format("Missing data header for instance %1$s [%2$s-%3$s | %4$s-%5$s]", getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

            return result.MissingHeader;
        }

        if (!Objects.equals(header.getString(), instance.getHeader())) {
            Log.outError(LogFilter.Scripts, String.format("Incorrect data header for instance %1$s [%2$s-%3$s | %4$s-%5$s], expected \"%6$s\" got \"%7$s\"", getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName(), instance.getHeader(), header.getString()));

            return result.UnexpectedHeader;
        }

        return result.Ok;
    }

    private Result parseBossStates() {
        var bossStates;

        if (!doc.RootElement.TryGetProperty("BossStates", out bossStates)) {
            Log.outError(LogFilter.Scripts, String.format("Missing boss states for instance %1$s [%2$s-%3$s | %4$s-%5$s]", getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

            return result.MissingBossStates;
        }

        if (bossStates.ValueKind != JsonValueKind.Array) {
            Log.outError(LogFilter.Scripts, String.format("Boss states is not an array for instance %1$s [%2$s-%3$s | %4$s-%5$s]", getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

            return result.BossStatesIsNotAnObject;
        }

        for (var bossId = 0; bossId < bossStates.GetArrayLength(); ++bossId) {
            if (bossId >= instance.getEncounterCount()) {
                Log.outError(LogFilter.Scripts, String.format("Boss states has entry for boss with higher id (%1$s) than number of bosses (%2$s) for instance %3$s [%4$s-%5$s | %6$s-%7$s]", bossId, instance.getEncounterCount(), getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

                return result.UnknownBoss;
            }

            var bossState = bossStates[bossId];

            if (bossState.ValueKind != JsonValueKind.Number) {
                Log.outError(LogFilter.Scripts, String.format("Boss state for boss (%1$s) is not a number for instance %2$s [%3$s-%4$s | %5$s-%6$s]", bossId, getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

                return result.BossStateIsNotAnObject;
            }

            var state = EncounterState.forValue(bossState.GetInt32());

            if (state == EncounterState.inProgress || state == EncounterState.Fail || state == EncounterState.Special) {
                state = EncounterState.NotStarted;
            }

            if (state.getValue() < EncounterState.ToBeDecided.getValue()) {
                instance.setBossState((int) bossId, state);
            }
        }

        return result.Ok;
    }

    private Result parseAdditionalData() {
        var moreData;

        if (!doc.RootElement.TryGetProperty("AdditionalData", out moreData)) {
            return result.Ok;
        }

        if (moreData.ValueKind != JsonValueKind.object) {
            Log.outError(LogFilter.Scripts, String.format("Additional data is not an object for instance %1$s [%2$s-%3$s | %4$s-%5$s]", getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

            return result.AdditionalDataIsNotAnObject;
        }

        for (var valueBase : instance.getPersistentScriptValues()) {
            var value;

            if (moreData.TryGetProperty(valueBase.getName(), out value) && value.ValueKind != JsonValueKind.Null) {
                if (value.ValueKind != JsonValueKind.Number) {
                    Log.outError(LogFilter.Scripts, String.format("Additional data second for first %1$s is not a number for instance %2$s [%3$s-%4$s | %5$s-%6$s]", valueBase.getName(), getInstanceId(), getMapId(), getMapName(), getDifficultyId(), getDifficultyName()));

                    return result.AdditionalDataUnexpectedValueType;
                }

                var doubleValue;

                if (value.TryGetDouble(out doubleValue)) {
                    valueBase.loadValue(doubleValue);
                } else {
                    valueBase.loadValue(value.GetInt64());
                }
            }
        }

        return result.Ok;
    }


    private int getInstanceId() {
        return instance.getInstance().getInstanceId();
    }


    private int getMapId() {
        return instance.getInstance().getId();
    }

    private String getMapName() {
        return instance.getInstance().getMapName();
    }


    private int getDifficultyId() {
        return (int) instance.getInstance().getDifficultyID().getValue();
    }

    private String getDifficultyName() {
        return CliDB.DifficultyStorage.get(instance.getInstance().getDifficultyID()).name;
    }

    public enum Result {
        Ok,
        MalformedJson,
        RootIsNotAnObject,
        MissingHeader,
        UnexpectedHeader,
        MissingBossStates,
        BossStatesIsNotAnObject,
        UnknownBoss,
        BossStateIsNotAnObject,
        MissingBossState,
        BossStateValueIsNotANumber,
        AdditionalDataIsNotAnObject,
        AdditionalDataUnexpectedValueType;

        public static final int SIZE = Integer.SIZE;

        public static Result forValue(int value) {
            return values()[value];
        }

        public int getValue() {
            return this.ordinal();
        }
    }
}

