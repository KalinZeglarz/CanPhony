package pl.poznan.put.security

import org.apache.commons.lang3.RandomStringUtils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

//Based on: https://howtodoinjava.com/security/java-aes-encryption-example/

class AES {

    private static SecretKeySpec secretKey
    private static byte[] key

    private static void setKey(String myKey) {
        MessageDigest sha
        try {
            key = myKey.getBytes("UTF-8")
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = Arrays.copyOf(key, 16)
            secretKey = new SecretKeySpec(key, "AES")
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace()
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace()
        }
    }

    static String encrypt(String toEncrypt, String key) {
        try {
            setKey(key)
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return Base64.getEncoder().encodeToString(cipher.doFinal(toEncrypt.getBytes("UTF-8")))
        }
        catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString())
        }
        return null
    }

    static String decrypt(String toDecrypt, String key) {
        try {
            setKey(key)
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return new String(cipher.doFinal(Base64.getDecoder().decode(toDecrypt)))
        }
        catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString())
        }
        return null
    }

    static void main(String[] args)
    {
        final String secretKey = "123456789012345678"

        String originalString = "howtodoinjava.com"
        String encryptedString = encrypt(originalString, secretKey)
        String decryptedString = decrypt(encryptedString, secretKey)

        System.out.println(originalString)
        System.out.println(encryptedString)
        System.out.println(decryptedString)
    }
}

