package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import pl.poznan.put.PasswordHash
import pl.poznan.put.structures.AccountStatus
import pl.poznan.put.structures.PasswordPolicy
import pl.poznan.put.structures.UserStatus
import pl.poznan.put.structures.api.CallHistoryResponse
import pl.poznan.put.structures.api.LoginRequest
import pl.poznan.put.structures.api.PasswordChangeRequest

import java.sql.*
import java.time.Duration
import java.time.LocalDateTime

import static pl.poznan.put.structures.UserStatus.INACTIVE

@Slf4j
class DatabaseManager {
    private static final String DB_PATH = "canphony.db"
    private static final String DDL_PATH = "/db/sqlite-ddl.sql"
    private static final String DB_URL = "jdbc:sqlite:${System.getProperty("user.dir")}${File.separator}${DB_PATH}"

    private DatabaseManager() {}

    static AccountStatus checkAccount(LoginRequest loginRequest) {
        return checkAccount(loginRequest.username, loginRequest.password)
    }

    static void createDatabaseIfNotExists() throws FileNotFoundException, SQLException {
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
            for (String scriptLine in ddl.split(";")) {
                if (scriptLine.isEmpty() || scriptLine.trim().isEmpty()) {
                    continue
                }
                PreparedStatement preparedStatement = conn.prepareStatement(scriptLine)
                preparedStatement.execute()
            }
        }
        PasswordPolicy defaultPasswordPolicy = new PasswordPolicy(
                minPasswordLength: 4,
                numberOfUppercaseCharacters: 0,
                numberOfLowercaseCharacters: 0,
                numberOfNumericCharacters: 0,
                numberOfSpecialCharacters: 0,
                specialCharacters: "!@#\$%^&*_-"
        )
        setPasswordPolicy(defaultPasswordPolicy)

        log.info("database setup success")
    }

    static AccountStatus checkAccount(String username, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select password from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return AccountStatus.NOT_EXISTS
                }
                if (!PasswordHash.validatePassword(password, resultSet.getString(1))) {
                    return AccountStatus.INCORRECT_PASSWORD
                }
            }
        }
        return AccountStatus.SUCCESS
    }

    static boolean createAccount(LoginRequest loginRequest) throws SQLException {
        return createAccount(loginRequest.username, loginRequest.password)
    }

    static boolean createAccount(String username, String password) {
        password = PasswordHash.createHash(password)
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "insert into accounts (username, password, status) " +
                    "values ('${username}','${password}','${INACTIVE.toString()}')"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        } catch (SQLException ignored) {
            return false
        }
        return true
    }

    static void updateUserAddress(String username, String ipAddress) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "update accounts set ipv4_address='${ipAddress}' where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        }
    }

    static void updateUserPassword(PasswordChangeRequest request) {
        updateUserPassword(request.username, request.password, request.newPassword)
    }

    static void updateUserPassword(String username, String currentPassword, String newPassword) {
        currentPassword = PasswordHash.createHash(currentPassword)
        newPassword = PasswordHash.createHash(newPassword)
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "update accounts set password='${newPassword}' where username='${username}' " +
                    "and password='${currentPassword}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        }
    }

    static void setUserStatus(String username, UserStatus userStatus) {
        log.info("updating user ${username} status to ${userStatus.toString()}")
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "update accounts set status='${userStatus.toString()}' where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        }
    }

    static String getUserAddress(String username) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select ipv4_address from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next()
                return resultSet.getString('ipv4_address')
            }
        }
    }

    static PasswordPolicy getPasswordPolicy() {
        PasswordPolicy result = new PasswordPolicy()
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement prepareStatement = conn.prepareStatement("select * from password_policy")
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.setField(resultSet.getString(2), resultSet.getString(3))
                }
            }
        }
        log.info("retrieved password policy: " + result.toMap())
        return result
    }

    static Map<String, UserStatus> getUserList() {
        Map<String, UserStatus> result = new HashMap<>()
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select username, status from accounts"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    String userStatusText = resultSet.getString(2)
                    UserStatus userStatus = null
                    if (userStatusText != null) {
                        userStatus = UserStatus.valueOf(userStatusText)
                    }

                    result.put(resultSet.getString("username"), userStatus)
                }
            }
        }
        return result
    }

    private static setPasswordPolicy(PasswordPolicy passwordPolicy) throws SQLException {
        Map<String, String> passwordPolicyMap = passwordPolicy.toMap()
        for (Map.Entry<String, String> entry in passwordPolicyMap) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String query = "insert into password_policy (policy_name, policy_value) " +
                        "values ('${entry.getKey()}','${entry.getValue()}')"
                PreparedStatement prepareStatement = conn.prepareStatement(query)
                prepareStatement.execute()
            }
        }
    }

    static UserStatus getUserStatus(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select status from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next()
                return UserStatus.valueOf(resultSet.getString("status"))
            }
        }
    }

    static void addCall(String sourceUsername, String targetUsername) {
        LocalDateTime date = LocalDateTime.now()
        int userId = getUserId(sourceUsername)
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "insert into calls (id_account, call_date, username) " +
                    "values (${userId},'${date.toString()}','${targetUsername}')"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        } catch (SQLException ignored) {
        }
    }

    static void updateLatestCallDuration(String username) {
        LocalDateTime latestCallDate
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select call_date from calls where username='${username}' " +
                    "and id_call=(select max(id_call) from calls where username='${username}')"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next()
                latestCallDate = LocalDateTime.parse(resultSet.getString("call_date"))
            }
        }

        float duration = Duration.between(latestCallDate, LocalDateTime.now()).getSeconds()
        log.info("updating latest ${username} call duration to ${duration}")
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "update calls set duration=${duration} where username='${username}' " +
                    "and id_call=(select max(id_call) from calls where username='${username}')"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            prepareStatement.execute()
        }
    }

    static CallHistoryResponse getUserCallHistory(String username) {
        CallHistoryResponse result = new CallHistoryResponse()
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select username, call_date, duration from calls where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.usernames.add(resultSet.getString('username'))
                    result.dates.add(resultSet.getString('call_date'))
                    result.durations.add(resultSet.getInt('duration').toString())
                }
            }
        }
        return result
    }

    private static int getUserId(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "select id_account from accounts where username='${username}'"
            PreparedStatement prepareStatement = conn.prepareStatement(query)
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                resultSet.next()
                return resultSet.getInt("id_account")
            }
        }
    }

}
