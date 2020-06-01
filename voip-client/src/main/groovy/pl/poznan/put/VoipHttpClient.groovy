package pl.poznan.put

import groovy.util.logging.Slf4j
import org.json.JSONObject
import pl.poznan.put.structures.*

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Slf4j
class VoipHttpClient {

    String serverAddress

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build()

    VoipHttpClient(String serverAddress, String serverPort) throws ConnectException {
        this.serverAddress = serverAddress + ":" + serverPort
        checkConnection()
    }

    private boolean checkConnection() {
        log.info("checking connection")
        HttpRequest request = buildGetRequest("/")
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.statusCode() == 200
    }

    private HttpRequest buildGetRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(new URI("http://${serverAddress}${endpoint}"))
                .setHeader("User-Agent", "Java 11 HttpClient")
                .GET()
                .build()
    }

    PhoneCallResponse startCall(String username, String targetUsername) {
        JSONObject body = new PhoneCallRequest(sourceUsername: username, targetUsername: targetUsername).toJSON()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://127.0.0.1:8080/phone-call/start-call"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build()

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return PhoneCallResponse.parseJSON(response.body())
    }

    Map<String, UserStatus> getUserList(String username) {
        log.info("getting user list")
        HttpRequest request = buildGetRequest("/account/user-list")
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        log.info("received: " + response.body())
        Map<String, UserStatus> result = UserListResponse.parseJSON(response.body()).userList
        result.remove(username)
        return result
    }

    private HttpResponse<String> accountPost(String username, String password, String endpoint) {
        JSONObject body = new LoginRequest(username: username, password: password).toJSON()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://${serverAddress}/account/${endpoint}"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build()
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response
    }

    LoginResponse login(String username, String password) {
        log.info("logging in")
        HttpResponse<String> response = accountPost(username, password.digest("SHA-512"), "login")
        if (response == null) {
            return null
        }
        return LoginResponse.parseJSON(response.body())
    }

    void logout(String username) {
        log.info("logging out")
        JSONObject body = new LoginRequest(username: username, password: null).toJSON()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://${serverAddress}/account/logout"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build()
        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    boolean register(String username, String password) {
        log.info('registering account')
        HttpResponse<String> response = accountPost(username, password.digest("SHA-512"), "register")
        return response.statusCode() == 201
    }

    PasswordPolicy getPasswordPolicy() {
        log.info("getting user list")
        HttpRequest request = buildGetRequest("/account/password-policy")
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        log.info("received: " + response.body())
        return PasswordPolicy.parseJSON(response.body())
    }

}
