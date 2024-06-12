package com.api.freemarket.domain.account.service;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;


public class PropEncryptTest {

    @Test
    public void jasypt_test() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        encryptor.setPassword("jasyptPropEncodeKey");          //암호화, 복호화할 때 사용할 패스워드
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setStringOutputType("base64");

        // jwt
        String jwtSecret = encryptor.encrypt("vmfhaltmskdlstkfkdgodyroqkfwkdbalroqkfwkdbalaaaaaaaaaaaaaaaabbbbb");

        // redis
        String redisHost = encryptor.encrypt("125.177.131.169");
        String redisUsername = encryptor.encrypt("free");
        String redisPassword = encryptor.encrypt("free");

        // oAuth2
        String kakaoId = encryptor.encrypt("5397e5b5bfb8d92a4bdc0636634328ba");
        String kakaoSecret = encryptor.encrypt("FviuK7zk0KQz9lsEUv54kcb3tSbbfUsM");

        String naverId = encryptor.encrypt("YGr2FqhHn4_wQ1_8wvs7");
        String naverSecret = encryptor.encrypt("TzMBhQfSkJ");

        String googleId = encryptor.encrypt("82131316117-dginrgt07m74sns9nv5errh3v3bd886q.apps.googleusercontent.com");
        String googleSecret = encryptor.encrypt("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");

        // h2
        String h2Url = encryptor.encrypt("jdbc:h2:mem:freemarket");
        String h2UserName = encryptor.encrypt("free");
        String h2UserPassword = encryptor.encrypt("free");

        // mail
        String mailPassword = encryptor.encrypt("dlgx xwwt mdrr nhic");

        // print
        System.out.println("Encrtpy jwtSecret: " + jwtSecret);

        System.out.println("Encrtpy redisHost: " + redisHost);
        System.out.println("Encrtpy redisUsername: " + redisUsername);
        System.out.println("Encrtpy redisPassword: " + redisPassword);

        System.out.println("Encrtpy kakaoId: " + kakaoId);
        System.out.println("Encrtpy kakaoSecret: " + kakaoSecret);
        System.out.println("Encrtpy naverId: " + naverId);
        System.out.println("Encrtpy naverSecret: " + naverSecret);
        System.out.println("Encrtpy googleId: " + googleId);
        System.out.println("Encrtpy googleSecret: " + googleSecret);

        System.out.println("Encrypt h2Url: " + h2Url);
        System.out.println("Encrypt h2UserName: " + h2UserName);
        System.out.println("Encrypt h2UserPassword: " + h2UserPassword);

        System.out.println("Encrypt mailPassword: " + mailPassword);
    }
}
