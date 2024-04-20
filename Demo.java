package com.a;
import java.sql.*;
import java.util.Scanner;

public class Demo {
   // private  String DB_URL = "j3306/bank_db";
    //private static final String DB_USER = "root";
    //private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("if you are new use type yes to create an account");
        String conform=sc.nextLine();
        if(conform.equals("yes")) {
        	System.out.println("Enter your 'Name', 'CustomerId', and 'Password' to create your Bank account:");
        	String name = sc.nextLine();
            int customerId = sc.nextInt();
            sc.nextLine();
            String password = sc.nextLine();
            BankAccount obj = new BankAccount(name, customerId, password);
        	obj.createUser();
        	obj.menu();
        }
        else {
        System.out.println("Enter your 'Name', 'CustomerId', and 'Password' to access your Bank account:");
        String name = sc.nextLine();
        int customerId = sc.nextInt();
        sc.nextLine();
        String password = sc.nextLine();
        BankAccount obj1 = new BankAccount(name, customerId, password);
        if (obj1.validateUser()) {
            obj1.menu();
        } else {
            System.out.println("Invalid customer ID or password. Exiting...");
        }
       }
    }
}

class BankAccount {
    double bal;
    double prevTrans;
    String customerName;
    int customerId;
    String password;

    BankAccount(String customerName, int customerId, String password) {
        this.customerName = customerName;
        this.customerId = customerId;
        this.password = password;
    }

    boolean validateUser() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bhargavi", "root", "root");
             PreparedStatement stmt = conn.prepareStatement("SELECT password, balance FROM Cdetails WHERE customer_id = ?")) {
            stmt.setInt(1, customerId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                double balance = resultSet.getDouble("balance");
                if (password.equals(storedPassword)) {
                    bal = balance; // Update the balance after successful login
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    void createUser() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bhargavi","root","root");
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Cdetails (customer_id, customer_name, password, balance) VALUES (?,?,?,?)")) {
            stmt.setInt(1, customerId);
            stmt.setString(2, customerName);
            stmt.setString(3, password);
            stmt.setDouble(4, bal);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void deposit(double amount) {
        if (amount != 0) {
            bal += amount;
            prevTrans = amount;
            updateBalanceInDatabase();
        }
    }

    void withdraw(double amt) {
        if (amt != 0 && bal >= amt) {
            bal -= amt;
            prevTrans = -amt;
            updateBalanceInDatabase();
        } else if (bal < amt) {
            System.out.println("Bank balance insufficient");
        }
    }

    void getPreviousTrans() {
        if (prevTrans > 0) {
            System.out.println("Deposited: " + prevTrans);
        } else if (prevTrans < 0) {
            System.out.println("Withdrawn: " + Math.abs(prevTrans));
        } else {
            System.out.println("No transaction occurred");
        }
    }

    void menu() {
        char option;
        Scanner sc = new Scanner(System.in);
        System.out.println("Your ID: " + customerId);
        System.out.println("\n");
        System.out.println("a) Check Balance");
        System.out.println("b) Deposit Amount");
        System.out.println("c) Withdraw Amount");
        System.out.println("d) Previous Transaction");
        System.out.println("e) exit");
        

        do {
            System.out.println("****************");
            System.out.println("Choose an option");
            option = sc.next().charAt(0);
            System.out.println("\n");

            switch (option) {
                case 'a':
                    System.out.println("......................");
                    System.out.println("Balance =" + bal);
                    System.out.println("......................");
                    System.out.println("\n");
                    break;
                case 'b':
                    System.out.println("......................");
                    System.out.println("Enter an amount to deposit:");
                    System.out.println("......................");
                    double amt = sc.nextDouble();
                    deposit(amt);
                    saveTransaction("Deposit", amt);
                    System.out.println("\n");
                    break;
                case 'c':
                    System.out.println("......................");
                    System.out.println("Enter an amount to withdraw:");
                    System.out.println("......................");
                    double amtW = sc.nextDouble();
                    withdraw(amtW);
                    saveTransaction("Withdrawal", amtW);
                    System.out.println("\n");
                    break;
                case 'd':
                    System.out.println("......................");
                    System.out.println("Previous Transaction:");
                    getPreviousTrans();
                    System.out.println("......................");
                    System.out.println("\n");
                    break;
                
                case 'e':
                    System.out.println("......................");
                    break;
                default:
                    System.out.println("Choose a correct option to proceed");
                    break;
            }

        } while (option != 'e');

        System.out.println("Thank you for using our banking services");
    }

    private void saveTransaction(String transactionType, double amount) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bhargavi","root","root");
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO transactions (customer_id, transaction_type, amount, avaliable_balance) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, customerId);
            stmt.setString(2, transactionType);
            stmt.setDouble(3, amount);
            stmt.setDouble(4, bal);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateBalanceInDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bhargavi", "root", "root");
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Cdetails SET balance = ? WHERE customer_id = ?")) {
            stmt.setDouble(1, bal);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
