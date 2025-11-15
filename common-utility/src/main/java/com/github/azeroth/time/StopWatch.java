package com.github.azeroth.time;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StopWatch {

    public static final int SPECIAL_INIT_ACTIVE_MOVER_TIME_SYNC_COUNTER = 0xFFFFFFFF;
    public static final int SPECIAL_RESUME_COMMS_TIME_SYNC_COUNTER      = 0xFFFFFFFE;


    private final Map<Integer, Long> taskTime = new HashMap<>();


    public void start(int task) throws IllegalStateException {
        if (taskTime.containsKey(task)) {
            throw new IllegalStateException("Task " + task + " is already started");
        }
        taskTime.put(task, System.currentTimeMillis());
    }

    public Duration stop(int task) throws IllegalStateException {
        if (!taskTime.containsKey(task)) {
            throw new IllegalStateException("Task " + task + " is not started");
        }
        long startTime = taskTime.remove(task);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        return Duration.ofMillis(duration);
    }

    public void reset(int task) {
        taskTime.remove(task);
    }

    public void resetAll() {
        taskTime.clear();
    }

}
