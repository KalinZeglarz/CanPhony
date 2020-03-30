package pl.poznan.put

import groovy.util.logging.Log
import pl.poznan.put.subpub.Message

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Log
class VoipHttpClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build()

    Message startCall() {
        log.info("starting call")
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://127.0.0.1:8080/phone-call/start-call"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build()

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        log.info("status code: " + response.statusCode())
        log.info(response.body())
        return Message.parseJSON(response.body())
    }

    void endCall() {
        log.info("ending call")
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://127.0.0.1:8080/phone-call/end-call"))
                .setHeader("User-Agent", "Java 11 HttpClient")
                .build()

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        log.info("status code : " + response.statusCode())
        log.info(response.body())
    }

    static void main(String[] args) {
        VoipHttpClient client = new VoipHttpClient()
        client.startCall()
        sleep(20000)
        client.endCall()
    }

}
