import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GradeManager {

    private List<Student> students = new ArrayList<>();

    // Add a student
    public void addStudent(String name, double maths, double science, double english) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Student name cannot be empty.");
            return;
        }
        if (!isValidMark(maths) || !isValidMark(science) || !isValidMark(english)) {
            System.out.println("Error: Marks must be between 0 and 100.");
            return;
        }
        students.add(new Student(name.trim(), maths, science, english));
        System.out.println("Student \"" + name.trim() + "\" added successfully!");
    }

    // Remove a student by name
    public void removeStudent(String name) {
        boolean removed = students.removeIf(s -> s.getName().equalsIgnoreCase(name.trim()));
        if (removed) {
            System.out.println("Student \"" + name + "\" removed.");
        } else {
            System.out.println("Student \"" + name + "\" not found.");
        }
    }

    // Display all students
    public void displayAll() {
        if (students.isEmpty()) {
            System.out.println("No students in the system yet.");
            return;
        }
        System.out.println("\n" + "=".repeat(90));
        System.out.printf("%-20s | %-12s | %-13s | %-13s | %-10s | %s%n",
                "Name", "Maths", "Science", "English", "Average", "Grade");
        System.out.println("=".repeat(90));
        for (Student s : students) {
            System.out.println(s);
        }
        System.out.println("=".repeat(90));
    }

    // Display class summary
    public void displaySummary() {
        if (students.isEmpty()) {
            System.out.println("No students to summarise.");
            return;
        }

        double classAvg = students.stream()
                .mapToDouble(Student::getAverage)
                .average()
                .orElse(0);

        Optional<Student> topStudent = students.stream()
                .max(Comparator.comparingDouble(Student::getAverage));

        Optional<Student> lowestStudent = students.stream()
                .min(Comparator.comparingDouble(Student::getAverage));

        System.out.println("\n===== CLASS SUMMARY =====");
        System.out.printf("Total students  : %d%n", students.size());
        System.out.printf("Class average   : %.2f%n", classAvg);
        topStudent.ifPresent(s ->
                System.out.printf("Top scorer      : %s (%.2f)%n", s.getName(), s.getAverage()));
        lowestStudent.ifPresent(s ->
                System.out.printf("Lowest scorer   : %s (%.2f)%n", s.getName(), s.getAverage()));
        System.out.println("=========================\n");
    }

    // Search student by name
    public void searchStudent(String name) {
        Optional<Student> found = students.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name.trim()))
                .findFirst();

        if (found.isPresent()) {
            System.out.println("\nStudent found:");
            System.out.println(found.get());
        } else {
            System.out.println("Student \"" + name + "\" not found.");
        }
    }

    // Sort and display by average (descending)
    public void displaySortedByAverage() {
        if (students.isEmpty()) {
            System.out.println("No students to sort.");
            return;
        }
        System.out.println("\nStudents ranked by average (highest first):");
        System.out.println("-".repeat(90));
        students.stream()
                .sorted(Comparator.comparingDouble(Student::getAverage).reversed())
                .forEach(s -> System.out.println(s));
        System.out.println("-".repeat(90));
    }

    // Grade distribution
    public void displayGradeDistribution() {
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }
        long o  = students.stream().filter(s -> s.getAverage() >= 90).count();
        long aP = students.stream().filter(s -> s.getAverage() >= 80 && s.getAverage() < 90).count();
        long b  = students.stream().filter(s -> s.getAverage() >= 70 && s.getAverage() < 80).count();
        long c  = students.stream().filter(s -> s.getAverage() >= 60 && s.getAverage() < 70).count();
        long d  = students.stream().filter(s -> s.getAverage() >= 50 && s.getAverage() < 60).count();
        long f  = students.stream().filter(s -> s.getAverage() < 50).count();

        System.out.println("\n===== GRADE DISTRIBUTION =====");
        System.out.printf("O  (Outstanding) : %d student(s)%n", o);
        System.out.printf("A+ (Excellent)   : %d student(s)%n", aP);
        System.out.printf("B  (Good)        : %d student(s)%n", b);
        System.out.printf("C  (Average)     : %d student(s)%n", c);
        System.out.printf("D  (Below Avg)   : %d student(s)%n", d);
        System.out.printf("F  (Fail)        : %d student(s)%n", f);
        System.out.println("==============================\n");
    }

    private boolean isValidMark(double mark) {
        return mark >= 0 && mark <= 100;
    }

    public int getStudentCount() {
        return students.size();
    }
}
