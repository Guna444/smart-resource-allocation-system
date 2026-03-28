package com.sras.service;

public final class Validation {
    private Validation() {}

    public static String requireNonBlank(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }
        return s.trim();
    }

    public static int requireInt(String s, int min, int max) {
        String v = requireNonBlank(s);
        try {
            int n = Integer.parseInt(v);
            if (n < min || n > max) throw new IllegalArgumentException("Number out of range.");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number.");
        }
    }

    public static long requireLong(String s, long min, long max, long defaultIfBlank) {
        if (s == null || s.trim().isEmpty()) return defaultIfBlank;
        String v = s.trim();
        try {
            long n = Long.parseLong(v);
            if (n < min || n > max) throw new IllegalArgumentException("Number out of range.");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number.");
        }
    }

    public static <E extends Enum<E>> E parseEnum(String raw, Class<E> enumClass) {
        String v = requireNonBlank(raw).toUpperCase();
        try {
            return Enum.valueOf(enumClass, v);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid value. Expected one of: " + java.util.Arrays.toString(enumClass.getEnumConstants()));
        }
    }
}