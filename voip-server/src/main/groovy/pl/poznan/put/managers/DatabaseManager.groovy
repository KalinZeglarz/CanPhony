package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import pl.poznan.put.structures.LoginRequest


import java.sql.*

@Slf4j
class DatabaseManager {
    private static final String DB_PATH = "canphony.db"
    private static final String DDL_PATH = "/db/sqlite-ddl.sql"
    private static final String DB_URL = "jdbc:sqlite:${System.getProperty("user.dir")}${File.separator}${DB_PATH}"

    private DatabaseManager() {}

    static boolean checkAccount(LoginRequest loginRequest) {
        return checkAccount(loginRequest.username, loginRequest.password)
    }

    static void createDatabaseIfNotExists() throws FileNotFoundException {
        if (new File(DB_PATH).exists()) {
            log.info("database found, using existing database")
            return
        }
        String ddl = null
        try {
            Resource resource = new ClassPathResource(DDL_PATH)
            ddl = resource.getFile().getText()
        } catch (IOException | NullPointerException e) {
            log.error("ddl file not found, database setup failed")
            e.printStackTrace()
            System.exit(1)
        }
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement preparedStatement = conn.prepareStatement(ddl)
            preparedStatement.execute()
        }
        log.info('database setup success')
    }

    static boolean checkAccount(String username, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select 1 from accounts where username='${username}' and password='${password}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next()
            }
        }
    }

    static boolean accountExists(String username) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
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
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
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
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "update accounts set ipv4_address='${ipAddress}' where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            return prepareStatement.execute()
        }
    }

    static String getUserAddress(String username) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select ipv4_address from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next()
                return resultSet.getString(1)
            }
        }
    }

    static Set<String> getUserList() {
        Set<String> result = []
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select username from accounts"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString(1))
                }
            }
        }
        return result
    }
}
