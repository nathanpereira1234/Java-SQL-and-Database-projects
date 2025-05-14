package com.lab;

import java.sql.*;

public class BankingSystem {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            // establish the connection to MySQL
            conn = getDatabaseConnection();
            // check connection is not null before using it
            if (conn != null) {
                // Start a transaction
                conn.setAutoCommit(false);
                createDatabase(conn);
                createTables(conn);

                Customer customer1 = new Customer(0, "John Doe", "123 Main St");
                createCustomerAccount(conn, customer1, 500.00);
                Customer customer2 = new Customer(0, "Jane Smith", "456 Oak St");
                createCustomerAccount(conn, customer2, 1000.00);

                customer1.setAddress("456 New Address");
                updateCustomerDetails(conn, customer1);

                viewAllCustomers(conn);
                deleteCustomerAccount(conn, 1);

                conn.commit();  // Commit transaction
            }
        } catch (SQLException e) {
            System.err.println("Main SQLException: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback if there's any error
                    System.err.println("Transaction rolled back due to an error.");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
        } finally {
            // close the connection if it was successfully opened
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Restore auto-commit
                    conn.close();
                    System.out.println("Auto-commit restored and connection closed.");
                } catch (SQLException e) {
                    System.err.println("Error closing the database connection: " + e.getMessage());
                }
            }
        }
    }

    public static void createCustomerAccount(Connection conn, Customer customer, double initialBalance) {
        String insertCustomerSQL = "INSERT INTO customers (name, address) VALUES (?, ?)";
        String insertAccountSQL = "INSERT INTO accounts (customer_id, balance, account_type) VALUES (?, ?, ?)";

        try (PreparedStatement customerStmt = conn.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS)) {
            customerStmt.setString(1, customer.getName());
            customerStmt.setString(2, customer.getAddress());
            customerStmt.executeUpdate();

            ResultSet generatedKeys = customerStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int customerId = generatedKeys.getInt(1);
                customer.setId(customerId);
            } else {
                throw new SQLException("Failed to retrieve customer ID.");
            }

            try (PreparedStatement accountStmt = conn.prepareStatement(insertAccountSQL)) {
                accountStmt.setInt(1, customer.getId());
                accountStmt.setDouble(2, initialBalance);
                accountStmt.setString(3, "SAVINGS");
                accountStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Account created successfully for " + customer.getName());
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("Transaction rolled back due to an error.");
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error creating customer account: " + e.getMessage());
        }
    }

    public static void updateCustomerDetails(Connection conn, Customer customer) {
        String updateSQL = "UPDATE customers SET address = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, customer.getAddress());
            pstmt.setInt(2, customer.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Customer " + customer.getName() + " details updated successfully.");
            } else {
                System.out.println("Customer not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating customer details: " + e.getMessage());
        }
    }

    public static void deleteCustomerAccount(Connection conn, int accountId) {
        String getCustomerIdSQL = "SELECT customer_id FROM accounts WHERE id = ?";
        String deleteAccountSQL = "DELETE FROM accounts WHERE id = ?";
        String countAccountsSQL = "SELECT COUNT(*) FROM accounts WHERE customer_id = ?";
        String deleteCustomerSQL = "DELETE FROM customers WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            int customerId = -1;

            try (PreparedStatement getCustomerStmt = conn.prepareStatement(getCustomerIdSQL)) {
                getCustomerStmt.setInt(1, accountId);
                ResultSet rs = getCustomerStmt.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                } else {
                    System.out.println("Account not found.");
                    return;
                }
            }

            try (PreparedStatement deleteAccountStmt = conn.prepareStatement(deleteAccountSQL)) {
                deleteAccountStmt.setInt(1, accountId);
                int rowsDeleted = deleteAccountStmt.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Account deleted successfully.");
                } else {
                    System.out.println("No account deleted.");
                    return;
                }
            }

            try (PreparedStatement countAccountsStmt = conn.prepareStatement(countAccountsSQL)) {
                countAccountsStmt.setInt(1, customerId);
                ResultSet rs = countAccountsStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement deleteCustomerStmt = conn.prepareStatement(deleteCustomerSQL)) {
                        deleteCustomerStmt.setInt(1, customerId);
                        deleteCustomerStmt.executeUpdate();
                        System.out.println("Customer deleted as they had no remaining accounts.");
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("Transaction rolled back due to an error.");
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error deleting customer account: " + e.getMessage());
        }
    }

    public static void viewAllCustomers(Connection conn) {
        String selectSQL = "SELECT c.name, c.address, a.id AS account_id, a.balance " +
                           "FROM customers c " +
                           "JOIN accounts a ON c.id = a.customer_id";

        try (PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String address = rs.getString("address");
                int accountId = rs.getInt("account_id");
                double balance = rs.getDouble("balance");

                System.out.printf("Customer: %s, Address: %s, Account: %d, Balance: %.2f%n",
                        name, address, accountId, balance);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving customer details: " + e.getMessage());
        }
    }

    public static void createDatabase(Connection conn) throws SQLException {
        String query = "CREATE DATABASE IF NOT EXISTS BankDB";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
        System.out.println("Database 'BankDB' created successfully (if it didn’t exist).");
    }

    public static void createTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("USE BankDB");

            // Drop tables to reset (optional in development)
            stmt.executeUpdate("DROP TABLE IF EXISTS accounts");
            stmt.executeUpdate("DROP TABLE IF EXISTS customers");

            String createCustomersTableSQL = "CREATE TABLE customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "address VARCHAR(255))";

            String createAccountsTableSQL = "CREATE TABLE accounts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "customer_id INT," +
                    "balance DOUBLE," +
                    "account_type VARCHAR(50)," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE)";

            stmt.execute(createCustomersTableSQL);
            stmt.execute(createAccountsTableSQL);

            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    public static Connection getDatabaseConnection() {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String password = "password";

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Connection failed SQLException: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Connection failed Exception: " + e.getMessage());
            return null;
        }
    }
}
