import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GradeManager manager = new GradeManager();

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Student Grade Management System    ║");
        System.out.println("╚══════════════════════════════════════╝");

        // Pre-load sample data
        manager.addStudent("Aravind",  92, 88, 95);
        manager.addStudent("Priya",    78, 82, 75);
        manager.addStudent("Karthik",  65, 70, 60);
        manager.addStudent("Divya",    55, 48, 52);
        manager.addStudent("Ramesh",   40, 35, 45);

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter student name: ");
                    String name = scanner.nextLine();
                    double maths   = readMark(scanner, "Maths");
                    double science = readMark(scanner, "Science");
                    double english = readMark(scanner, "English");
                    manager.addStudent(name, maths, science, english);
                    break;

                case "2":
                    manager.displayAll();
                    break;

                case "3":
                    System.out.print("Enter student name to search: ");
                    manager.searchStudent(scanner.nextLine());
                    break;

                case "4":
                    manager.displaySortedByAverage();
                    break;

                case "5":
                    manager.displaySummary();
                    break;

                case "6":
                    manager.displayGradeDistribution();
                    break;

                case "7":
                    System.out.print("Enter student name to remove: ");
                    manager.removeStudent(scanner.nextLine());
                    break;

                case "8":
                    System.out.println("Thank you for using the Grade Management System. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 8.");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n┌─────────────────────────────┐");
        System.out.println("│           MENU              │");
        System.out.println("├─────────────────────────────┤");
        System.out.println("│  1. Add student             │");
        System.out.println("│  2. View all students       │");
        System.out.println("│  3. Search student          │");
        System.out.println("│  4. Rank by average         │");
        System.out.println("│  5. Class summary           │");
        System.out.println("│  6. Grade distribution      │");
        System.out.println("│  7. Remove student          │");
        System.out.println("│  8. Exit                    │");
        System.out.println("└─────────────────────────────┘");
    }

    private static double readMark(Scanner scanner, String subject) {
        while (true) {
            System.out.print("Enter " + subject + " marks (0–100): ");
            try {
                double mark = Double.parseDouble(scanner.nextLine().trim());
                if (mark >= 0 && mark <= 100) return mark;
                System.out.println("Please enter a value between 0 and 100.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
