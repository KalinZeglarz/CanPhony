package pl.poznan.put

import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import pl.poznan.put.structures.AccountStatus
import pl.poznan.put.structures.PasswordPolicy
import pl.poznan.put.structures.UserStatus
import pl.poznan.put.structures.api.*

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext

@Slf4j
class VoipHttpClient {

    String serverAddress

    private final HttpClient httpClient


    VoipHttpClient(String serverAddress, String serverPort) throws ConnectException {
        this.serverAddress = serverAddress + ":" + serverPort

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build()
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier()
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts)

        httpClient = HttpClients.custom()
                .setSSLSocketFactory(connectionFactory)
                .build()
        checkConnection()
    }

    private boolean checkConnection() {
        log.info("checking connection")
        HttpGet request = new HttpGet("https://${serverAddress}/")
        HttpResponse response = httpClient.execute(request)
        return response.getStatusLine().getStatusCode() == 200
    }

    PhoneCallResponse startCall(String username, String targetUsername) {
        JSONObject body = new PhoneCallRequest(sourceUsername: username, targetUsername: targetUsername).toJSON()
        HttpPost request = new HttpPost("https://${serverAddress}/phone-call/start-call")
        request.setEntity(new StringEntity(body.toString()))
        request.setHeader("Content-Type", "application/json")
        HttpResponse response = httpClient.execute(request)

        return PhoneCallResponse.parseJSON(EntityUtils.toString(response.getEntity()))
    }

    void endCall(String username, String targetUsername) {
        String queryString = "?sourceUsername=${username}&targetUsername=${targetUsername}"
        HttpDelete request = new HttpDelete("https://${serverAddress}/phone-call/end-call${queryString}")
        request.setHeader("Content-Type", "application/json")
        httpClient.execute(request)
    }

    void rejectCall(String username, String targetUsername) {
        String queryString = "?sourceUsername=${username}&targetUsername=${targetUsername}"
        HttpDelete request = new HttpDelete("https://${serverAddress}/phone-call/reject-call${queryString}")
        request.setHeader("Content-Type", "application/json")
        httpClient.execute(request)
    }

    Map<String, UserStatus> getUserList(String username) {
        log.info("getting user list")
        HttpGet request = new HttpGet("https://${serverAddress}/account/user-list")
        HttpResponse response = httpClient.execute(request)

        String responseBody = EntityUtils.toString(response.getEntity())
        log.info("received: " + responseBody)
        Map<String, UserStatus> result = UserListResponse.parseJSON(responseBody).userList
        result.remove(username)
        return result
    }

    private HttpResponse accountPost(String username, String password, String endpoint) {
        JSONObject body = new LoginRequest(username: username, password: password).toJSON()
        HttpPost request = new HttpPost("https://${serverAddress}/account/${endpoint}")
        request.setEntity(new StringEntity(body.toString()))
        request.setHeader("Content-Type", "application/json")
        return httpClient.execute(request)
    }

    LoginResponse login(String username, String password) {
        log.info("logging in")
        HttpResponse response = accountPost(username, password, "login")
        String responseBody = EntityUtils.toString(response.getEntity())
        log.info("received: " + responseBody)
        return LoginResponse.parseJSON(responseBody)
    }

    void logout(String username) {
        log.info("logging out")
        String queryString = "?username=${username}"
        HttpDelete request = new HttpDelete("https://${serverAddress}/account/logout${queryString}")
        httpClient.execute(request)
    }

    AccountStatus register(String username, String password) {
        log.info("registering account")
        HttpResponse response = accountPost(username, password, "register")
        String responseBody = EntityUtils.toString(response.getEntity())
        JSONObject responseJson = new JSONObject(responseBody)
        return AccountStatus.valueOf(responseJson.getString("message"))
    }

    AccountStatus changePassword(String username, String currentPassword, String newPassword) {
        log.info("changing password")
        JSONObject body = new PasswordChangeRequest(username: username, password: currentPassword,
                newPassword: newPassword).toJSON()
        HttpPut request = new HttpPut("https://${serverAddress}/account/change-password")
        request.setEntity(new StringEntity(body.toString()))
        request.setHeader("Content-Type", "application/json")
        HttpResponse response = httpClient.execute(request)
        if (response.statusLine.statusCode == 401) {
            return null
        }
        String responseBody = EntityUtils.toString(response.getEntity())
        JSONObject responseJson = new JSONObject(responseBody)
        return AccountStatus.valueOf(responseJson.getString("message"))
    }


    PasswordPolicy getPasswordPolicy() {
        log.info("getting password policy")
        HttpGet request = new HttpGet("https://${serverAddress}/account/password-policy")
        HttpResponse response = httpClient.execute(request)

        String responseBody = EntityUtils.toString(response.getEntity())
        log.info("received: " + responseBody)
        return PasswordPolicy.parseJSON(responseBody)
    }

    CallHistoryResponse getCallHistory(String username) {
        log.info("getting call history")
        String queryString = "?username=${username}"
        HttpGet request = new HttpGet("https://${serverAddress}/phone-call/call-history${queryString}")
        HttpResponse response = httpClient.execute(request)

        String responseBody = EntityUtils.toString(response.getEntity())
        log.info("received: " + responseBody)
        return CallHistoryResponse.parseJSON(responseBody)
    }

}
