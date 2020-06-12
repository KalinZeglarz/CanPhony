package pl.poznan.put.security


import javax.crypto.KeyAgreement
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Based on: https://github.com/firatkucuk/diffie-hellman-helloworld
 */

class EncryptionSuite {

    private PrivateKey privateKey
    private PublicKey publicKey
    private PublicKey receivedPublicKey
    private String secretKey
    private AES aes = new AES()
    private AES audioAes = new AES(true)

    void generateKeys() {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH")
        keyPairGenerator.initialize(1024)
        final KeyPair keyPair = keyPairGenerator.generateKeyPair()
        privateKey = keyPair.getPrivate()
        publicKey = keyPair.getPublic()
    }

    String serializePublicKey() {
        String result = null
        new ByteArrayOutputStream().withCloseable { byteOut ->
            new ObjectOutputStream(byteOut).withCloseable { objOut ->
                objOut.writeObject(publicKey)
                result = byteOut.toByteArray().encodeHex().toString()
            }
        }
        return result
    }

    void generateCommonSecretKey(String publicKey) {
        byte[] decodedPublicKey = publicKey.decodeHex()
        new ByteArrayInputStream(decodedPublicKey).withCloseable { byteIn ->
            new ObjectInputStream(byteIn).withCloseable { objIn ->
                receivedPublicKey = objIn.readObject() as PublicKey
            }
        }

        final KeyAgreement keyAgreement = KeyAgreement.getInstance(receivedPublicKey.algorithm)
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(receivedPublicKey, true)
        secretKey = keyAgreement.generateSecret()
        aes.setKey(secretKey)
        audioAes.setKey(secretKey)
    }

    String encrypt(final String message) {
        return aes.encrypt(message)
    }

    byte[] encryptAudio(final byte[] message) {
        return audioAes.encryptBytes(message)
    }

    String decrypt(final String message) {
        return aes.decrypt(message)
    }

    byte[] decryptAudio(final byte[] message) {
        return audioAes.decryptBytes(message)
    }

}