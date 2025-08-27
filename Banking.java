import java.sql.*;
import java.util.Scanner;

public class Banking {
    private static final String URL = "jdbc:mysql://localhost:3306/bank_system";
    private static final String USER = "root";
    private static final String PASSWORD = "ritesh@2008";

    private Connection conn;
    private Scanner scanner;

    public Banking() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            scanner = new Scanner(System.in);
            System.out.println(" Connected to Database Successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- LOGIN -----------------
    private boolean login(String username, String password) {
        try {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // true if user exists
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- CHECK BALANCE -----------------
    private void checkBalance(String username) {
        try {
            String sql = "SELECT balance FROM users WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println(" Current Balance: " + rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- DEPOSIT -----------------
    private void deposit(String username, double amount) {
        try {
            String sql = "UPDATE users SET balance = balance + ? WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setString(2, username);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" Deposit Successful! Amount: " + amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- WITHDRAW -----------------
    private void withdraw(String username, double amount) {
        try {
            String checkSql = "SELECT balance FROM users WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getDouble("balance") >= amount) {
                String sql = "UPDATE users SET balance = balance - ? WHERE username=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setDouble(1, amount);
                stmt.setString(2, username);
                stmt.executeUpdate();
                System.out.println(" Withdrawal Successful! Amount: " + amount);
            } else {
                System.out.println("Insufficient Balance!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- TRANSFER -----------------
    private void transfer(String sender, String receiver, double amount) {
        try {
            conn.setAutoCommit(false);

            // Check sender balance
            String checkSql = "SELECT balance FROM users WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, sender);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getDouble("balance") >= amount) {
                // Deduct from sender
                String deductSql = "UPDATE users SET balance = balance - ? WHERE username=?";
                PreparedStatement deductStmt = conn.prepareStatement(deductSql);
                deductStmt.setDouble(1, amount);
                deductStmt.setString(2, sender);
                deductStmt.executeUpdate();

                // Add to receiver
                String addSql = "UPDATE users SET balance = balance + ? WHERE username=?";
                PreparedStatement addStmt = conn.prepareStatement(addSql);
                addStmt.setDouble(1, amount);
                addStmt.setString(2, receiver);
                addStmt.executeUpdate();

                conn.commit();
                System.out.println(" Transfer Successful! Amount: " + amount);
            } else {
                System.out.println(" Transfer Failed: Insufficient Balance.");
                conn.rollback();
            }

            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ---------------- MENU -----------------
    private void start() {
        System.out.print(" Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (login(username, password)) {
            System.out.println("Login Successful!");
            int choice;
            do {
                System.out.println("\n------ Banking Menu ------");
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Transfer");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> checkBalance(username);
                    case 2 -> {
                        System.out.print("Enter deposit amount: ");
                        double dAmt = scanner.nextDouble();
                        deposit(username, dAmt);
                    }
                    case 3 -> {
                        System.out.print("Enter withdraw amount: ");
                        double wAmt = scanner.nextDouble();
                        withdraw(username, wAmt);
                    }
                    case 4 -> {
                        scanner.nextLine(); // clear buffer
                        System.out.print("Enter receiver username: ");
                        String receiver = scanner.nextLine();
                        System.out.print("Enter transfer amount: ");
                        double tAmt = scanner.nextDouble();
                        transfer(username, receiver, tAmt);
                    }
                    case 5 -> System.out.println("Exiting... Thank you!");
                    default -> System.out.println(" Invalid choice, try again.");
                }
            } while (choice != 5);
        } else {
            System.out.println(" Invalid Username/Password.");
        }
    }

    public static void main(String[] args) {
        Banking app = new Banking();
        app.start();
    }
}
