package com.teracrafts.huefy.utils;

public class NoopLogger implements Logger {
    @Override
    public void log(LogLevel level, String message) {
        // intentionally empty
    }
}
