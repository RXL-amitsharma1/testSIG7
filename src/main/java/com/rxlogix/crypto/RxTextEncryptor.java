package com.rxlogix.crypto;

import com.rxlogix.RxCodec;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class RxTextEncryptor implements TextEncryptor {

    @Override
    public String encrypt(String text) {
        return RxCodec.encode(text);
    }

    @Override
    public String decrypt(String encryptedText) {
        return RxCodec.decode(encryptedText);
    }
}