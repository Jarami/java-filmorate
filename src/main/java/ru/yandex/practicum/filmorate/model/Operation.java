package ru.yandex.practicum.filmorate.model;

public enum Operation {
    REMOVE("remove"), ADD("add"), UPDATE("update");
    private final String title;

    Operation(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}