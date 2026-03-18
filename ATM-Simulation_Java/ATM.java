import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ATM {

    private Map<String, Account> accounts = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);

    public ATM() {
        // Pre-loaded demo accounts
        accounts.put("1001", new Account("1001", "1234", "Aravind Kumar",  50000.00));
        accounts.put("1002", new Account("1002", "5678", "Priya Sharma",   30000.00));
        accounts.put("1003", new Account("1003", "9999", "Karthik Raj",    75000.00));
    }

    public void start() {
        printBanner();
        while (true) {
            Account account = login();
            if (account != null) {
                runSession(account);
            }
            System.out.print("\nReturn to main screen? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("\nThank you for using our ATM. Goodbye!\n");
                break;
            }
        }
        scanner.close();
    }

    private Account login() {
        System.out.println("\n" + "─".repeat(38));
        System.out.println("  Please insert your card (Account No)");
        System.out.println("─".repeat(38));

        int attempts = 0;
        while (attempts < 3) {
            System.out.print("  Account Number : ");
            String accNo = scanner.nextLine().trim();

            Account account = accounts.get(accNo);
            if (account == null) {
                System.out.println("  Account not found. Try again.\n");
                attempts++;
                continue;
            }

            System.out.print("  Enter PIN       : ");
            String pin = scanner.nextLine().trim();

            if (account.validatePin(pin)) {
                System.out.println("\n  Welcome, " + account.getHolderName() + "!");
                return account;
            } else {
                attempts++;
                int left = 3 - attempts;
                if (left > 0) {
                    System.out.println("  Incorrect PIN. " + left + " attempt(s) remaining.\n");
                } else {
                    System.out.println("  Card blocked due to too many failed attempts.\n");
                }
            }
        }
        return null;
    }

    private void runSession(Account account) {
        boolean active = true;
        while (active) {
            printMenu();
            System.out.print("  Choose option : ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": checkBalance(account);  break;
                case "2": deposit(account);       break;
                case "3": withdraw(account);      break;
                case "4": changePin(account);     break;
                case "5":
                    System.out.println("\n  Session ended. Card ejected. Thank you!\n");
                    active = false;
                    break;
                default:
                    System.out.println("  Invalid option. Please choose 1–5.");
            }
        }
    }

    private void checkBalance(Account account) {
        System.out.println("\n  ┌─────────────────────────────┐");
        System.out.printf ("  │  Account : %-18s│%n", account.getAccountNumber());
        System.out.printf ("  │  Name    : %-18s│%n", account.getHolderName());
        System.out.printf ("  │  Balance : ₹ %-16.2f│%n", account.getBalance());
        System.out.println("  └─────────────────────────────┘");
    }

    private void deposit(Account account) {
        System.out.print("\n  Enter deposit amount: ₹ ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (account.deposit(amount)) {
                System.out.printf("  ✔ ₹%.2f deposited successfully.%n", amount);
                System.out.printf("  New Balance: ₹%.2f%n", account.getBalance());
            } else {
                System.out.println("  ✘ Invalid amount. Must be greater than 0.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Invalid input. Please enter a valid number.");
        }
    }

    private void withdraw(Account account) {
        System.out.print("\n  Enter withdrawal amount: ₹ ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (account.withdraw(amount)) {
                System.out.printf("  ✔ ₹%.2f dispensed. Please collect your cash.%n", amount);
                System.out.printf("  Remaining Balance: ₹%.2f%n", account.getBalance());
            } else if (amount > account.getBalance()) {
                System.out.println("  ✘ Insufficient funds.");
                System.out.printf("  Available Balance: ₹%.2f%n", account.getBalance());
            } else {
                System.out.println("  ✘ Invalid amount.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Invalid input. Please enter a valid number.");
        }
    }

    private void changePin(Account account) {
        System.out.print("\n  Enter current PIN : ");
        String oldPin = scanner.nextLine().trim();
        System.out.print("  Enter new PIN (4 digits): ");
        String newPin = scanner.nextLine().trim();
        System.out.print("  Confirm new PIN   : ");
        String confirm = scanner.nextLine().trim();

        if (!newPin.equals(confirm)) {
            System.out.println("  ✘ PINs do not match. Try again.");
            return;
        }
        if (!newPin.matches("\\d{4}")) {
            System.out.println("  ✘ PIN must be exactly 4 digits.");
            return;
        }
        if (account.changePin(oldPin, newPin)) {
            System.out.println("  ✔ PIN changed successfully.");
        } else {
            System.out.println("  ✘ Incorrect current PIN.");
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║         JAVA ATM SIMULATION          ║");
        System.out.println("  ║         Secure Banking System        ║");
        System.out.println("  ╚══════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Demo Accounts:");
        System.out.println("  Account: 1001  PIN: 1234  (Aravind Kumar)");
        System.out.println("  Account: 1002  PIN: 5678  (Priya Sharma)");
        System.out.println("  Account: 1003  PIN: 9999  (Karthik Raj)");
    }

    private void printMenu() {
        System.out.println("\n  ┌───────────────────────┐");
        System.out.println("  │      ATM SERVICES     │");
        System.out.println("  ├───────────────────────┤");
        System.out.println("  │  1. Check Balance     │");
        System.out.println("  │  2. Deposit           │");
        System.out.println("  │  3. Withdraw          │");
        System.out.println("  │  4. Change PIN        │");
        System.out.println("  │  5. Exit / Eject Card │");
        System.out.println("  └───────────────────────┘");
    }
}
