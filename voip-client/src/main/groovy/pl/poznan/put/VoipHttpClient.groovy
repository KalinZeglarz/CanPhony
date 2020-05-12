package pl.poznan.put

import groovy.util.logging.Slf4j
import org.json.JSONObject
import pl.poznan.put.structures.LoginRequest
import pl.poznan.put.structures.LoginResponse
import pl.poznan.put.structures.PhoneCallRequest
import pl.poznan.put.structures.PhoneCallResponse

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Slf4j
class VoipHttpClient {

    String serverAddress
    String username

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build()

    VoipHttpClient(String serverAddress) throws ConnectException {
        this.serverAddress = serverAddress
        checkConnection()
    }

    private boolean checkConnection() {
        log.info("checking connection")
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://${serverAddress}/"))
                .setHeader("User-Agent", "Java 11 HttpClient")
                .GET()
                .build()

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.statusCode() == 200
    }

    PhoneCallResponse startCall(String targetUsername) {
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

    private HttpResponse<String> accountPost(String username, String password, String endpoint) {
        JSONObject body = new LoginRequest(username: username, password: password).toJSON()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://${serverAddress}/account/${endpoint}"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build()
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 201) {
            return null
        }
        return response
    }

    LoginResponse login(String username, String password) {
        HttpResponse<String> response = accountPost(username, password, "login")
        if (response == null) {
            return null
        }
        return LoginResponse.parseJSON(response.body())
    }

    boolean register(String username, String password) {
        log.info('registering account')
        HttpResponse<String> response = accountPost(username, password, "register")
        if (response == null) {
            return false
        }
        return true
    }

}
