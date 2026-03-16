package com.todoapp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskManager - Core library class for managing to-do tasks.
 * Provides full CRUD operations and filtering utilities.
 */
public class TaskManager {

    private List<Task> tasks;

    public TaskManager() {
        this.tasks = new ArrayList<>();
    }

    // ──────────────────────────────────────────────
    // ADD
    // ──────────────────────────────────────────────

    /**
     * Add a new task with a given title and priority.
     */
    public Task addTask(String title, Task.Priority priority) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        Task task = new Task(title.trim(), priority);
        tasks.add(task);
        return task;
    }

    /**
     * Add a task with default MEDIUM priority.
     */
    public Task addTask(String title) {
        return addTask(title, Task.Priority.MEDIUM);
    }

    // ──────────────────────────────────────────────
    // READ
    // ──────────────────────────────────────────────

    /**
     * Get all tasks.
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Get only pending (not completed) tasks.
     */
    public List<Task> getPendingTasks() {
        return tasks.stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Get only completed tasks.
     */
    public List<Task> getCompletedTasks() {
        return tasks.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks filtered by priority.
     */
    public List<Task> getTasksByPriority(Task.Priority priority) {
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Find a task by its ID.
     */
    public Task findById(int id) {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // ──────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────

    /**
     * Mark a task as complete by ID.
     */
    public boolean markComplete(int id) {
        Task task = findById(id);
        if (task != null) {
            task.setCompleted(true);
            return true;
        }
        return false;
    }

    /**
     * Mark a task as incomplete (undo) by ID.
     */
    public boolean markIncomplete(int id) {
        Task task = findById(id);
        if (task != null) {
            task.setCompleted(false);
            return true;
        }
        return false;
    }

    /**
     * Update the title of a task by ID.
     */
    public boolean updateTitle(int id, String newTitle) {
        Task task = findById(id);
        if (task != null && newTitle != null && !newTitle.trim().isEmpty()) {
            task.setTitle(newTitle.trim());
            return true;
        }
        return false;
    }

    /**
     * Update the priority of a task by ID.
     */
    public boolean updatePriority(int id, Task.Priority priority) {
        Task task = findById(id);
        if (task != null) {
            task.setPriority(priority);
            return true;
        }
        return false;
    }

    // ──────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────

    /**
     * Delete a task by ID.
     */
    public boolean deleteTask(int id) {
        return tasks.removeIf(t -> t.getId() == id);
    }

    /**
     * Delete all completed tasks.
     */
    public int clearCompleted() {
        int before = tasks.size();
        tasks.removeIf(Task::isCompleted);
        return before - tasks.size();
    }

    /**
     * Delete all tasks.
     */
    public void clearAll() {
        tasks.clear();
    }

    // ──────────────────────────────────────────────
    // STATS
    // ──────────────────────────────────────────────

    public int totalCount()     { return tasks.size(); }
    public int pendingCount()   { return (int) tasks.stream().filter(t -> !t.isCompleted()).count(); }
    public int completedCount() { return (int) tasks.stream().filter(Task::isCompleted).count(); }

    /**
     * Print a summary of all tasks to console.
     */
    public void printSummary() {
        System.out.println("─────────────────────────────────────────");
        System.out.printf("  Total: %d  |  Pending: %d  |  Done: %d%n",
                totalCount(), pendingCount(), completedCount());
        System.out.println("─────────────────────────────────────────");
    }

    /**
     * Print all tasks in a given list.
     */
    public void printTasks(List<Task> list) {
        if (list.isEmpty()) {
            System.out.println("  (no tasks)");
            return;
        }
        for (Task t : list) {
            System.out.println("  " + t);
        }
    }
}
