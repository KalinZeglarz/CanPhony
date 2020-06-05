package pl.poznan.put.examples

import groovy.util.logging.Slf4j
import pl.poznan.put.security.AES

@Slf4j
class AesEncryptionExample {
    static void main(String[] args) {
        final String secretKey = "123456789012345678"

        AES aes = new AES()
        aes.setKey(secretKey)

        String originalString = "howtodoinjava.com"
        String encryptedString = aes.encrypt(originalString)
        String decryptedString = aes.decrypt(encryptedString)

        log.info('original string: ' + originalString)
        log.info('encrypted string: ' + encryptedString)
        log.info('decrypted string: ' + decryptedString)
    }
}
