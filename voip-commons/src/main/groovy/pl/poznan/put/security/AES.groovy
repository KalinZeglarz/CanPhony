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

    void setKey(String key) {
        key = key.digest("SHA-1").substring(0, 16)
        secretKey = new SecretKeySpec(key.bytes, "AES")
        encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey)
        decryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey)
    }

    String encrypt(String toEncrypt) {
        try {
            return Base64.getEncoder().encodeToString(encryptCipher.doFinal(toEncrypt.getBytes("UTF-8")))
        }
        catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString())
        }
        return null
    }

    String decrypt(String toDecrypt) {
        try {
            return new String(decryptCipher.doFinal(Base64.getDecoder().decode(toDecrypt)))
        }
        catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString())
        }
        return null
    }

}

