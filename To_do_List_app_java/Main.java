package com.todoapp;

import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the To-Do List Console App.
 * Uses TaskManager as the core library.
 */
public class Main {

    private static TaskManager manager = new TaskManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║       Java To-Do List App        ║");
        System.out.println("╚══════════════════════════════════╝");

        // Preload some sample tasks
        manager.addTask("Set up Java project structure", Task.Priority.HIGH);
        manager.addTask("Write TaskManager library", Task.Priority.HIGH);
        manager.addTask("Test all features", Task.Priority.MEDIUM);
        manager.addTask("Write README documentation", Task.Priority.LOW);

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addTask();         break;
                case "2": viewAll();         break;
                case "3": viewPending();     break;
                case "4": viewCompleted();   break;
                case "5": markDone();        break;
                case "6": deleteTask();      break;
                case "7": updateTask();      break;
                case "8": clearCompleted();  break;
                case "9": filterPriority();  break;
                case "0": running = false;   break;
                default:
                    System.out.println("  ⚠ Invalid option. Try again.");
            }
        }

        System.out.println("\nGoodbye! Stay productive! 👋");
        scanner.close();
    }

    // ──────────────────────────────────────────────
    // MENU
    // ──────────────────────────────────────────────

    static void printMenu() {
        System.out.println("\n┌──────────────────────────────────┐");
        System.out.println("│           MAIN MENU              │");
        System.out.println("├──────────────────────────────────┤");
        System.out.println("│  1. Add task                     │");
        System.out.println("│  2. View all tasks               │");
        System.out.println("│  3. View pending tasks           │");
        System.out.println("│  4. View completed tasks         │");
        System.out.println("│  5. Mark task as done            │");
        System.out.println("│  6. Delete a task                │");
        System.out.println("│  7. Update a task                │");
        System.out.println("│  8. Clear completed tasks        │");
        System.out.println("│  9. Filter by priority           │");
        System.out.println("│  0. Exit                         │");
        System.out.println("└──────────────────────────────────┘");
        System.out.print("  Choose an option: ");
    }

    // ──────────────────────────────────────────────
    // ACTIONS
    // ──────────────────────────────────────────────

    static void addTask() {
        System.out.print("  Enter task title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("  ⚠ Title cannot be empty.");
            return;
        }
        System.out.print("  Priority (1=LOW, 2=MEDIUM, 3=HIGH): ");
        Task.Priority priority = parsePriority(scanner.nextLine().trim());
        Task task = manager.addTask(title, priority);
        System.out.println("  ✔ Added: " + task);
    }

    static void viewAll() {
        System.out.println("\n  ── All Tasks ──");
        manager.printTasks(manager.getAllTasks());
        manager.printSummary();
    }

    static void viewPending() {
        System.out.println("\n  ── Pending Tasks ──");
        manager.printTasks(manager.getPendingTasks());
    }

    static void viewCompleted() {
        System.out.println("\n  ── Completed Tasks ──");
        manager.printTasks(manager.getCompletedTasks());
    }

    static void markDone() {
        System.out.print("  Enter task ID to mark done: ");
        int id = parseInt(scanner.nextLine());
        if (id < 0) return;
        boolean success = manager.markComplete(id);
        System.out.println(success ? "  ✔ Task #" + id + " marked as complete." : "  ⚠ Task not found.");
    }

    static void deleteTask() {
        System.out.print("  Enter task ID to delete: ");
        int id = parseInt(scanner.nextLine());
        if (id < 0) return;
        boolean success = manager.deleteTask(id);
        System.out.println(success ? "  ✔ Task #" + id + " deleted." : "  ⚠ Task not found.");
    }

    static void updateTask() {
        System.out.print("  Enter task ID to update: ");
        int id = parseInt(scanner.nextLine());
        if (id < 0) return;
        Task task = manager.findById(id);
        if (task == null) { System.out.println("  ⚠ Task not found."); return; }

        System.out.println("  Current: " + task);
        System.out.print("  New title (press Enter to skip): ");
        String newTitle = scanner.nextLine().trim();
        if (!newTitle.isEmpty()) manager.updateTitle(id, newTitle);

        System.out.print("  New priority (1=LOW, 2=MEDIUM, 3=HIGH, Enter to skip): ");
        String pInput = scanner.nextLine().trim();
        if (!pInput.isEmpty()) manager.updatePriority(id, parsePriority(pInput));

        System.out.println("  ✔ Updated: " + manager.findById(id));
    }

    static void clearCompleted() {
        int removed = manager.clearCompleted();
        System.out.println("  ✔ Cleared " + removed + " completed task(s).");
    }

    static void filterPriority() {
        System.out.print("  Filter by priority (1=LOW, 2=MEDIUM, 3=HIGH): ");
        Task.Priority priority = parsePriority(scanner.nextLine().trim());
        List<Task> filtered = manager.getTasksByPriority(priority);
        System.out.println("\n  ── " + priority + " Priority Tasks ──");
        manager.printTasks(filtered);
    }

    // ──────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────

    static int parseInt(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("  ⚠ Invalid ID.");
            return -1;
        }
    }

    static Task.Priority parsePriority(String input) {
        switch (input) {
            case "1": return Task.Priority.LOW;
            case "3": return Task.Priority.HIGH;
            default:  return Task.Priority.MEDIUM;
        }
    }
}
