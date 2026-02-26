package com.huefy.utils;

public interface Logger {
    enum LogLevel { DEBUG, INFO, WARN, ERROR }
    void log(LogLevel level, String message);
}
