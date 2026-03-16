package com.todoapp;

public class Task {

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    private static int counter = 1;

    private int id;
    private String title;
    private boolean completed;
    private Priority priority;

    public Task(String title, Priority priority) {
        this.id = counter++;
        this.title = title;
        this.priority = priority;
        this.completed = false;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public Priority getPriority() { return priority; }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setTitle(String title) { this.title = title; }
    public void setPriority(Priority priority) { this.priority = priority; }

    @Override
    public String toString() {
        String status = completed ? "[x]" : "[ ]";
        return String.format("[%d] %s %s  |  Priority: %s", id, status, title, priority);
    }
}
