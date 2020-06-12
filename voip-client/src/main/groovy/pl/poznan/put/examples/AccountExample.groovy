package pl.poznan.put.examples

import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import pl.poznan.put.VoipHttpClient

import java.util.concurrent.ThreadLocalRandom

@Slf4j
@PackageScope
class AccountExample {

    @SuppressWarnings("DuplicatedCode")
    static void main(String[] args) {
        VoipHttpClient voipHttpClient = new VoipHttpClient("127.0.0.1", "8080")
        log.info("login success: " + voipHttpClient.login("test", "test"))
        log.info("login success: " + voipHttpClient.login("", "test"))
        log.info("login success: " + voipHttpClient.login("test", ""))

        log.info("register success: " + voipHttpClient.register("test", "test"))
        int randomNumber = ThreadLocalRandom.current().nextInt()
        log.info("register success: " + voipHttpClient.register("test${randomNumber}", "test"))
    }

}
