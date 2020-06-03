package pl.poznan.put.security

import javax.crypto.KeyAgreement
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

//Based on: https://github.com/firatkucuk/diffie-hellman-helloworld

class DiffieHellman {

    private PrivateKey privateKey
    private PublicKey publicKey
    private PublicKey receivedPublicKey
    private byte[] secretKey
    private AES aes

    void generateCommonSecretKey() {
        final KeyAgreement keyAgreement = KeyAgreement.getInstance("DH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(receivedPublicKey, true)
        secretKey = keyAgreement.generateSecret()
    }

    void generateKeys() {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH")
        keyPairGenerator.initialize(1024)
        final KeyPair keyPair = keyPairGenerator.generateKeyPair()
        privateKey = keyPair.getPrivate()
        publicKey = keyPair.getPublic()
    }

    PublicKey getPublicKey() {
        return publicKey
    }

    String encrypt(final String message) {
        String key = new String(secretKey)
        return aes.encrypt(message, key)
    }

    String decrypt(final String message) {
        String key = new String(secretKey)
        return aes.decrypt(message, key)
    }
}