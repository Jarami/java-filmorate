package ru.yandex.practicum.filmorate.model;

public enum EventType {
    LIKE("like"), REVIEW("review"), FRIEND("friend");
    private final String title;

    EventType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}