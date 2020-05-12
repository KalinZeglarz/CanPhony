package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import org.apache.groovy.sql.extensions.SqlExtensions
import pl.poznan.put.structures.LoginRequest

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.ThreadLocalRandom

@Slf4j
class DatabaseManager {
    private final static String dbUrl = "jdbc:sqlite:${System.getProperty("user.dir")}${File.separator}canphony.db"

    private DatabaseManager() {}

    static boolean checkAccount(LoginRequest loginRequest) {
        return checkAccount(loginRequest.username, loginRequest.password)
    }

    static boolean checkAccount(String username, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String query = "select 1 from accounts where username='${username}' and password='${password}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next()
            }
        }
    }

    static boolean accountExists(String username) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String query = "select 1 from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next()
            }
        }
    }

    static boolean createAccount(LoginRequest loginRequest) throws SQLException {
        return createAccount(loginRequest.username, loginRequest.password)
    }

    static boolean createAccount(String username, String password) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String query = "insert into accounts (username, password) " +
                    "values ('${username}','${password}')"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        } catch (SQLException ignored) {
            return false
        }
        return true
    }

    static boolean updateUserAddress(String username, String ipAddress) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String query = "update accounts set ipv4_address='${ipAddress}' where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            return prepareStatement.execute()
        }
    }

    static String getUserAddress(String username) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String query = "select ipv4_address from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next()
                return resultSet.getString(1)
            }
        }
    }

}
