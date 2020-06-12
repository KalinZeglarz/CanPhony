package pl.poznan.put.security

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Based on: https://howtodoinjava.com/security/java-aes-encryption-example/
 */

class AES {

    private SecretKeySpec secretKey
    private Cipher encryptCipher
    private Cipher decryptCipher
    private String encryptionMode

    AES() {
        this(false)
    }

    AES(boolean audio) {
        if (audio) {
            encryptionMode = "AES/ECB/NoPadding"
        } else {
            encryptionMode = "AES/ECB/PKCS5Padding"
        }
    }

    void setKey(String key) {
        key = key.digest("SHA-1").substring(0, 16)
        secretKey = new SecretKeySpec(key.bytes, "AES")
        setupCipher()
    }

    private void setupCipher() {
        encryptCipher = Cipher.getInstance(encryptionMode)
        decryptCipher = Cipher.getInstance(encryptionMode)
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey)
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey)
    }

    String encrypt(String toEncrypt) {
        return Base64.getEncoder().encodeToString(encryptCipher.doFinal(toEncrypt.getBytes("UTF-8")))
    }

    byte[] encryptBytes(byte[] toEncrypt) {
        return Base64.getEncoder().encode(encryptCipher.doFinal(toEncrypt))
    }

    String decrypt(String toDecrypt) {
        return new String(decryptCipher.doFinal(Base64.getDecoder().decode(toDecrypt)))
    }

    byte[] decryptBytes(byte[] toDecrypt) {
        return decryptCipher.doFinal(Base64.getDecoder().decode(toDecrypt))
    }


}

