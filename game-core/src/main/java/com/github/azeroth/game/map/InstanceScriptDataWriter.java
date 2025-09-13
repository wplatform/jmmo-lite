package com.github.azeroth.game.map;


class InstanceScriptDataWriter {
    private final InstanceScript instance;
    private jsonObject doc = new jsonObject();

    public InstanceScriptDataWriter(InstanceScript instance) {
        instance = instance;
    }

    public final String getString() {
        try (var stream = new MemoryStream()) {

            try (var writer = new Utf8JsonWriter(stream)) {
                doc.WriteTo(writer);
            }

            return Encoding.UTF8.getString(stream.ToArray());
        }
    }


    public final void fillData() {
        fillData(true);
    }

    public final void fillData(boolean withValues) {
        doc.add("Header", instance.getHeader());

        JsonArray bossStates = new JsonArray();

        for (int bossId = 0; bossId < instance.getEncounterCount(); ++bossId) {
            bossStates.add(JsonValue.create((int) (withValues ? instance.getBossState(bossId) : EncounterState.NotStarted)));
        }

        doc.add("BossStates", bossStates);

        if (!instance.getPersistentScriptValues().isEmpty()) {
            JsonObject moreData = new jsonObject();

            for (var additionalValue : instance.getPersistentScriptValues()) {
                if (withValues) {
                    var data = additionalValue.createEvent();

                    if (data.Value instanceof Double) {
                        moreData.add(data.key, (Double) data.value);
                    } else {
                        moreData.add(data.key, (Long) data.value);
                    }
                } else {
                    moreData.add(additionalValue.getName(), null);
                }
            }

            doc.add("AdditionalData", moreData);
        }
    }

    public final void fillDataFrom(String data) {
        try {
            doc = JsonNode.parse(data).AsObject();
        } catch (JsonException e) {
            fillData(false);
        }
    }

    public final void setBossState(UpdateBossStateSaveDataEvent data) {
        var array = _doc["BossStates"].AsArray();
        array[(int) data.BossId] = data.newState.getValue();
    }

    public final void setAdditionalData(UpdateAdditionalSaveDataEvent data) {
        var jObject = _doc["AdditionalData"].AsObject();

        if (data.Value instanceof Double) {
            jObject[data.Key] = (Double) data.value;
        } else {
            jObject[data.Key] = (Long) data.value;
        }
    }
}
