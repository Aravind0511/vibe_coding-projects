public class Student {
    private String name;
    private double mathsMarks;
    private double scienceMarks;
    private double englishMarks;

    public Student(String name, double mathsMarks, double scienceMarks, double englishMarks) {
        this.name = name;
        this.mathsMarks = mathsMarks;
        this.scienceMarks = scienceMarks;
        this.englishMarks = englishMarks;
    }

    public String getName() { return name; }
    public double getMathsMarks() { return mathsMarks; }
    public double getScienceMarks() { return scienceMarks; }
    public double getEnglishMarks() { return englishMarks; }

    public double getAverage() {
        return (mathsMarks + scienceMarks + englishMarks) / 3.0;
    }

    public String getGrade() {
        double avg = getAverage();
        if (avg >= 90) return "O  (Outstanding)";
        if (avg >= 80) return "A+ (Excellent)";
        if (avg >= 70) return "B  (Good)";
        if (avg >= 60) return "C  (Average)";
        if (avg >= 50) return "D  (Below Average)";
        return              "F  (Fail)";
    }

    @Override
    public String toString() {
        return String.format("%-20s | Maths: %5.1f | Science: %5.1f | English: %5.1f | Avg: %5.1f | Grade: %s",
                name, mathsMarks, scienceMarks, englishMarks, getAverage(), getGrade());
    }
}
