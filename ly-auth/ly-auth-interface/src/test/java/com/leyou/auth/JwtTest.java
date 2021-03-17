package com.leyou.auth;

import com.leyou.auth.etities.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {
        private static final String publicKeyPath = "D:\\tmp\\rsa\\rsa.pub";
        private static final String privateKeyPath = "D:\\tmp\\rsa\\rsa.pri";

        private PrivateKey privateKey;
        private PublicKey publicKey;


        @Test
        public void testRsa() throws Exception {
                RsaUtils.generateKey(publicKeyPath, privateKeyPath, "234");
        }

        @Before
        public void testGetRsa() throws Exception {
                privateKey = RsaUtils.getPrivateKey(privateKeyPath);
                publicKey = RsaUtils.getPublicKey(publicKeyPath);
        }

        @org.junit.Test
        public void generateToken() {
                //生成Token
                String token = JwtUtils.generateToken(new UserInfo(20L, "Jack"), privateKey, 5);
                System.out.println("token = " + token);
        }


        @org.junit.Test
        public void parseToken() throws Exception{
                String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiSmFjayIsImV4cCI6MTYxNDI0NDcyMH0.LNhrxsBng7F8fI_A9rpFoQwogDvHxvcrxRjBMqVcu6fMtgyvmkm7zUgjhih6O0z8G2LaD3B-PWbb1Wwa2P58sRGslbsNX44cT8j7lqqwCjd_m70b5yto6-7egClOqNDjyYgjIejbTb4v5TphUpAq0e92MpZiLVpGMDUpWKmeUM0";
                UserInfo userInfo = JwtUtils.getUserInfo(publicKey, token);
                System.out.println("id:" + userInfo.getId());
                System.out.println("name:" + userInfo.getName());
        }

}
