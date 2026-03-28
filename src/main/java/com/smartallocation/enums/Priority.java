package com.smartallocation.enums;

public enum Priority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
