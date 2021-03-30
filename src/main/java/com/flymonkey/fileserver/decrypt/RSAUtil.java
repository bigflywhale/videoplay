package com.flymonkey.fileserver.decrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;


public class RSAUtil {

    private static Map<Integer, String> keyMap = new HashMap<Integer, String>();  //用于封装随机产生的公钥与私钥

    public final static String publicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCfLy+T8cvFnn++dVsddeBHxaYGQiI6jty8RlrgCA1ZDx9a5h9T97e1Gshhf85E75v/TTv2ndUWdzbwV6/lSGKqyarfFREUlw21dzeCNBHH9+97frUCVZnV0cCKc/Az3xN8GtI6UZIsqzi9+D3rmF40BvAw8qkPOIeCAeUt54pWTQIDAQAB";
    public final static String privatKey ="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJ8vL5Pxy8Wef751Wx114EfFpgZCIjqO3LxGWuAIDVkPH1rmH1P3t7UayGF/zkTvm/9NO/ad1RZ3NvBXr+VIYqrJqt8VERSXDbV3N4I0Ecf373t+tQJVmdXRwIpz8DPfE3wa0jpRkiyrOL34PeuYXjQG8DDyqQ84h4IB5S3nilZNAgMBAAECgYEAhfedMAJXvyokUjLMCuAwb9bMYgn4apXe+WiwhEjiiugqOELhbLZW2kj/COcZvnR1MghbBbNRKUYNa3owo2Mm/UIeT99gQsnpvDnAxBkoeO3lidBS7sRq+ITjD69r3iz0eIY02S00DIz7k3f/7/o+7EJZ/fGwh8OxNl3/rRyngQECQQDwZdSLsnYwVLX1G0jbe6ggbauXM3Zt/GBYJS7Ky+R1RFrm6qwhU7GqoJkl5CEvr4+NsR1DLIHZSRUDCSzHEGpBAkEAqYQB31fEZm60xhR0c2gkF3k8bVNGexD8ONixPhC4b8LXbZwwr5dEqO9dONsJeL38HMvpV8d6MBYwSfG1Fr6xDQJAJSpZFc/dXdN63g390ZDoiTrrUFDwekiOTJJTDZ9ADrEmrJSTUAO0cACsLKN8TfcCZpF/SKdU29bknLeKaKwtAQJBAIYmSbUOKAdIJRd8qIuMJsPM1d1OwcAyuOfj1KBhaxRHBWVg7caH13Y5t3R/IwfCkfnYBVnKKxBS/vQFrGHLlyECQQCJRQST1d86nK6DVLjqK59steeiqCaTrLNP1JbcO4nUtXts3J9PBNvcXikcmeEeGNMXdmuGfP8y1eMsOaCN18ui";


    public static void main(String[] args) throws Exception {
        //生成公钥和私钥
//        genKeyPair();
        //加密字符串
        String message = "VbKXvzLWOJMTq4H4SNiI07eIfoPzwBcejOVXHuJEZZbi8gMrNHlLZNm12u6sa+m8OfjteIts0KJJVpmPVbGK5cDyToqbr5Tx6NJudYMEW3N4MNXcPM978zZ2YLIGxoubybGMz1KdcvIJIJelGTYgnlg+Qwrkbzr1Wz6X1xNhzvY=";

        String messageDe = decrypt(message, privatKey);
        System.out.println("还原后的字符串为:" + messageDe);
    }

    /**
     * 随机生成密钥对
     *
     * @throws NoSuchAlgorithmException
     */
    public static void genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        // 得到私钥字符串
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
        // 将公钥和私钥保存到Map
        keyMap.put(0, publicKeyString);  //0表示公钥
        keyMap.put(1, privateKeyString);  //1表示私钥
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }


}
