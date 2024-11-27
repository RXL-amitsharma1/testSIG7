package com.rxlogix.util

import com.google.common.io.BaseEncoding
import com.google.gson.Gson

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.text.SimpleDateFormat

class SecurityUtil {

    static String encrypt(String pass, String message) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5")
        final byte[] digestOfPassword = md.digest(pass.getBytes("utf-8"))
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24)
        for (int j = 0; j < 8; j++) {
            keyBytes[16 + j] = keyBytes[j]
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede")
        final IvParameterSpec iv = new IvParameterSpec(new byte[8])
        final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        final byte[] plainTextBytes = message.getBytes("utf-8")
        final byte[] cipherText = cipher.doFinal(plainTextBytes)

        BaseEncoding.base64().encode(cipherText)
    }

    static String decrypt(String pass, String message) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(pass.getBytes("utf-8"))
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24)
        for (int j = 0; j < 8; j++) {
            keyBytes[16 + j] = keyBytes[j];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede")
        final IvParameterSpec iv = new IvParameterSpec(new byte[8])
        final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        decipher.init(Cipher.DECRYPT_MODE, key, iv)

        final byte[] encData = BaseEncoding.base64().decode(message)
        final byte[] plainText = decipher.doFinal(encData)

        return new String(plainText, "utf-8")
    }

    static String generateAPIToken(String pass, String input, String uuid, Date timestamp) {
        def msg = [content: input]

        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getLongDateFormatForLocale(Locale.ENGLISH, true))
        msg['timestamp'] = sdf.format(timestamp)
        msg['id'] = uuid

        Gson gson = new Gson()
        encrypt(pass, gson.toJson(msg))
    }

    static String decodeAPIToken(String pass, String tokenStr) {
        decrypt(pass, tokenStr)
    }
}
