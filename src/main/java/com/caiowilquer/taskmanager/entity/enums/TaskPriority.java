package com.caiowilquer.taskmanager.entity.enums;

public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    TaskPriority(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
