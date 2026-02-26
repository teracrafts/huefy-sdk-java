package com.huefy.utils;

public class ConsoleLogger implements Logger {
    @Override
    public void log(LogLevel level, String message) {
        System.out.printf("[%s] [Huefy] %s%n", level, message);
    }
}
