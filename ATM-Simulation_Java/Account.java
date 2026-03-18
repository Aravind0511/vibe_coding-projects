public class Account {
    private String accountNumber;
    private String pin;
    private String holderName;
    private double balance;

    public Account(String accountNumber, String pin, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.holderName = holderName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName()    { return holderName; }
    public double getBalance()       { return balance; }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        return true;
    }

    public boolean changePin(String oldPin, String newPin) {
        if (!validatePin(oldPin)) return false;
        if (newPin == null || newPin.length() != 4) return false;
        this.pin = newPin;
        return true;
    }
}
