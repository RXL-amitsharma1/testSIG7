package com.rxlogix.config

public enum NotificationLevel {
    INFO("Information"),
    WARN("Warning"),
    ERROR("Error")

    String name

    NotificationLevel(String name) {
        this.name = name
    }

}