package ru.yandex.practicum.filmorate.model;

public enum EventType {
    LIKE("LIKE"), REVIEW("REVIEW"), FRIEND("FRIEND");
    private final String title;

    EventType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}