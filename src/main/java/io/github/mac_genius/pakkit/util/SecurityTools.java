package io.github.mac_genius.pakkit.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecurityTools {
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // for example
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static String encryptString(String toEncrypt, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] requesterBytes = toEncrypt.getBytes();
            return Base64.getEncoder().encodeToString(cipher.doFinal(requesterBytes));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptString(String toDecrypt, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] keyBytes = Base64.getDecoder().decode(key);

            SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(toDecrypt);
            return new String(cipher.doFinal(decodedBytes));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }
}
