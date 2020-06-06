package pl.poznan.put.examples

import groovy.util.logging.Slf4j
import pl.poznan.put.security.EncryptionSuite

@Slf4j
class EncryptionSuiteExample {
    static void main(String[] args) {
        // Represents server
        EncryptionSuite suite1 = new EncryptionSuite()

        // Represents client
        EncryptionSuite suite2 = new EncryptionSuite()

        suite1.generateKeys()
        suite2.generateKeys()

        // This should be sent to client
        String publicKey1 = suite1.serializePublicKey()
        // This should be sent to server
        String publicKey2 = suite2.serializePublicKey()

        // This should be used after receiving public key
        suite1.generateCommonSecretKey(publicKey2)
        suite2.generateCommonSecretKey(publicKey1)

        String message = "Yo man, how r ya"
        log.info("original message: " + message)
        message = suite1.encrypt(message)
        log.info("encrypted message: " + message)
        message = suite2.decrypt(message)
        log.info("decrypted message: " + message)
    }
}
