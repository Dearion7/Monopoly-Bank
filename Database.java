import java.sql.*;

public class Database {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://145.24.222.106/Monopoly-bank";

    // Database credentials
    static final String USER = "jacobCuperus";
    static final String PASS = "CsiEUq7%^903";

    Connection connection;
    PreparedStatement preparedStatement;

    Database() {
        System.out.println("Starting...");
        connection = null;
    }

    private Connection connect() {
        try {
            Class.forName(JDBC_DRIVER);

            System.out.println("Connecting to a selected database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");
            return connection;
        } catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return null;
    }

    private Connection close() {
        connection = connect();
        try {
            connection.close();
            return connection;
        } catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return null;
    }

    int checkSaldo(String iban, int pin) {
        try {
            connection = connect();

            preparedStatement = connection.prepareStatement("SELECT Balance FROM Accounts WHERE  IBAN = ? AND PIN = ?");
            preparedStatement.setString(1, iban);
            preparedStatement.setInt(2, pin);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int balance = resultSet.getInt("Balance");
                return balance;
            }
            resultSet.close();
            preparedStatement.close();
            close();
        } catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        } finally{
            //finally block used to close resources
            try{
                if(connection!=null)
                    connection.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return -1;
    }

    void withdraw(String iban, int pin, int amount) {
        try {
            connection = connect();

            System.out.println("Creating statement...");
            preparedStatement = connection.prepareStatement("UPDATE Accounts " + "SET Balance = CONCAT(Balance-?) WHERE IBAN = ? AND PIN = ?");
            preparedStatement.setInt(1, amount);
            preparedStatement.setString(2, iban);
            preparedStatement.setInt(3, pin);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("INSERT INTO Transactions (IBANSender, IBANReceiver, TransactionType ,Amount)" + "VALUES (?, ?, 'Withdraw', ?)");
            preparedStatement.setString(1, iban);
            preparedStatement.setString(2, iban);
            preparedStatement.setInt(3, amount);
            preparedStatement.executeUpdate();
            System.out.println("Statement success");
            preparedStatement.close();
            close();
        } catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        } finally{
            //finally block used to close resources
            try{
                if(connection!=null)
                    connection.close();
            } catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    void deposit(String iban, int pin, int amount) {
        try {
            connection = connect();

            System.out.println("Creating statement...");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Accounts " + "SET Balance = CONCAT(Balance+?) WHERE IBAN = ? AND PIN = ?");
            preparedStatement.setInt(1, amount);
            preparedStatement.setString(2, iban);
            preparedStatement.setInt(3, pin);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("INSERT INTO Transactions (IBANSender, IBANReceiver, TransactionType ,Amount)" + "VALUES (?, ?, 'Deposit', ?)");
            preparedStatement.setString(1, iban);
            preparedStatement.setString(2, iban);
            preparedStatement.setInt(3, amount);
            preparedStatement.executeUpdate();
            System.out.println("Statement success");
            preparedStatement.close();
            close();

        } catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        } finally{
            //finally block used to close resources
            try{
                if(connection!=null)
                    connection.close();
            } catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    boolean checkIban(String iban) {
        try {
            connection = connect();

            preparedStatement = connection.prepareStatement("SELECT IBAN FROM Accounts WHERE  IBAN = ?");
            preparedStatement.setString(1, iban);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String ibanCheck = resultSet.getString("IBAN");
                if (ibanCheck.equals(iban)) {
                    return true;
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    boolean checkPin(String iban, int pin) {
        try {
            connection = connect();

            preparedStatement = connection.prepareStatement("SELECT PIN FROM Accounts WHERE IBAN = ? AND PIN = ?");
            preparedStatement.setString(1, iban);
            preparedStatement.setInt(2, pin);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int pinCheck = resultSet.getInt("PIN");
                if (pinCheck == pin) {
                    return true;
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    void updateAttempts(int attempts, String iban) {
        try {
            connection = connect();

            preparedStatement = connection.prepareStatement("UPDATE Accounts " + "SET PINattempts = ? WHERE IBAN = ?");
            preparedStatement.setInt(1, attempts);
            preparedStatement.setString(2, iban);
            preparedStatement.executeUpdate();
            close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    boolean checkBlocked(String iban) {
        try {
            connection = connect();

            preparedStatement = connection.prepareStatement("SELECT Blocked FROM Accounts WHERE IBAN = ?");
            preparedStatement.setString(1, iban);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String blockedCheck = resultSet.getString("Blocked");
                if (blockedCheck.equals("No")) {
                    return false;
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return true;
    }

    void blockedCard(String iban) {
        try {
            connection = connect();

            preparedStatement = connection.prepareStatement("UPDATE Accounts " + "SET Blocked = 'Yes' WHERE IBAN = ?");
            preparedStatement.setString(1, iban);
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
