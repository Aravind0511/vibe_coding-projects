# Java To-Do List App

A console-based To-Do List application built with Java.
The core logic lives in `TaskManager.java` — a reusable library class.

## Project Structure

```
TodoApp/
├── src/
│   └── com/todoapp/
│       ├── Task.java          ← Model: represents a single task
│       ├── TaskManager.java   ← Library: all CRUD & filter logic
│       └── Main.java          ← Entry point: interactive console menu
├── lib/                       ← Place any external .jar files here
└── README.md
```

## How to Compile & Run

```bash
# Compile
javac -d out src/com/todoapp/*.java

# Run
java -cp out com.todoapp.Main
```

## Features

- Add tasks with LOW / MEDIUM / HIGH priority
- View all, pending, or completed tasks
- Mark tasks as done
- Update task title or priority
- Delete individual tasks
- Clear all completed tasks
- Filter tasks by priority
- Live stats summary

## TaskManager API (Library Usage)

```java
TaskManager manager = new TaskManager();

// Add
manager.addTask("My task", Task.Priority.HIGH);

// Read
manager.getAllTasks();
manager.getPendingTasks();
manager.getCompletedTasks();
manager.getTasksByPriority(Task.Priority.HIGH);

// Update
manager.markComplete(1);
manager.updateTitle(1, "New title");
manager.updatePriority(1, Task.Priority.LOW);

// Delete
manager.deleteTask(1);
manager.clearCompleted();
```
