package com.de4bi.common.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtil {

    // CipherType
    private static final String TF_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    public static final int AES_CBC_PKCS5 = 0;

    // HashingType
    private static final String[] HASHING_ALGORITHMS = { "MD5", "SHA-256" };
    public static final int MD5 = 0;
    public static final int SHA256 = 1;
    
    // 내부 상수
    private static final byte[] IV_ARY = "ABC0abc1DEF2def3GHI4ghi5JKL6jkl7MNO8mno9PQR+pqr-STU_stu=VWX~vwx/YZ.yz".getBytes();
    private static final IvParameterSpec IV_AES_CBC_PKCS5 = new IvParameterSpec(Arrays.copyOfRange(IV_ARY, 0, 16));
    
    /**
     * <p>입력된 바이트 배열을 암호화하여 반환합니다.</p>
     * @param cipherType : 암호화 알고리즘, 모드 및 패딩.
     * <pre>
     * - AES_CBC_PKCS5 : 알고리즘:AES-128, 암호화 모드:CBC, 패딩:PKCS5</pre>
     * @param plainBytes : 평문 바이트 배열.
     * @param secretKeySpec : SecretKeySpec(byte[] key, String algorithm) 생성자로 생성된 암호화 키.
     * @return 암호화된 바이트 배열.
     */
    public static byte[] encrypt(int cipherType, byte[] plainBytes, SecretKeySpec secretKeySpec) {
        // 파라미터 검사
        if (plainBytes == null) {
            throw new NullPointerException("'plainBytes' is null!");
        }
        else if (plainBytes.length == 0) {
            throw new IllegalArgumentException("planBytes' length is zero!");
        }
        
        if (secretKeySpec == null) {
            throw new NullPointerException("'secretKeySpec' is null!");
        }
        
        // 암호화 수행
        byte[] cipherBytes = null;
        
        if (cipherType == AES_CBC_PKCS5) {
            cipherBytes = cipherAES(Cipher.ENCRYPT_MODE, plainBytes, secretKeySpec);
        }
        else if (true) {
            // Add new CipherType here...
        }
        
        // 암호화 결과 검사 및 반환
        if (cipherBytes == null) {
            throw new IllegalStateException("The encode reseult 'cipherBytes' is null. encoding failed!");
        }

        return cipherBytes;
    }
    
    /**
     * <p>입력된 바이트 배열을 복호화하여 반환합니다.</p>
     * @param cipherType : 복호화 알고리즘, 모드 및 패딩.
     * <pre>
     * - AES_CBC_PKCS5 : 알고리즘:AES-128, 복호화 모드:CBC, 패딩:PKCS5</pre>
     * @param cipherBytes : 암호문 바이트 배열.
     * @param secretKeySpec : SecretKeySpec(byte[] key, String algorithm) 생성자로 생성된 복호화 키.
     * @return 복호화된 바이트 배열.
     */
    public static byte[] decrypt(int cipherType, byte[] cipherBytes, SecretKeySpec secretKeySpec) {
        // 파라미터 검사
        if (cipherBytes == null) {
            throw new NullPointerException("'cipherBytes' is null!");
        }
        else if (cipherBytes.length == 0) {
            throw new IllegalArgumentException("cipherBytes' length is zero!");
        }
        
        if (secretKeySpec == null) {
            throw new NullPointerException("'secretKeySpec' is null!");
        }
        
        // 복호화 수행
        byte[] plainBytes = null;
        
        if (cipherType == AES_CBC_PKCS5) {
            plainBytes = cipherAES(Cipher.DECRYPT_MODE, cipherBytes, secretKeySpec);
        }
        else if (true) {
            // Add new CipherType here...
        }
        
        // 복호화 결과 검사 및 반환
        if (plainBytes == null) {
            throw new IllegalStateException("The decode reseult 'plainBytes' is null. decoding failed!");
        }
        
        return plainBytes;
    }

    /**
     * <p>입력된 바이트 배열을 해싱하여 반환합니다.</p>
     * @param hashingType : 해시 알고리즘
     * <ul><li>MD5 : MD5 알고리즘 (16byte output)</li>
     *     <li>SHA256 : SHA256 알고리즘 (32byte output)</li></ul>
     * @param originBytes : 원본 데이터 바이트 배열.
     * @param saltBytes : SALTING을 위한 바이트 배열. (null일 경우 Salting 생략)
     * @return 해싱된 바이트 배열.
     */
    public static byte[] hashing(int hashingType, byte[] originBytes, byte[] saltBytes) {
        // 파라미터 검사
        if (hashingType != MD5 && hashingType != SHA256) {
            throw new IllegalArgumentException("Undefined 'hashingType'! (hashingType:" + hashingType + ")");
        }

        if (originBytes == null) {
            throw new NullPointerException("'originBytes' is null!");
        }
        else if (originBytes.length == 0) {
            throw new IllegalArgumentException("'originBytes' length is zero!");
        }

        // SALTING
        byte[] hashingTargetBytes = originBytes;

        if (saltBytes != null && saltBytes.length > 0) {
            byte[] saltedOriginBytes = new byte[originBytes.length + saltBytes.length];
            System.arraycopy(originBytes, 0, saltedOriginBytes, 0, originBytes.length);
            System.arraycopy(saltBytes, 0, saltedOriginBytes, originBytes.length, saltBytes.length);
            hashingTargetBytes = saltedOriginBytes;
        }

        // 해싱 수행
        byte[] hashingResultBytes = null;

        try {
            MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHMS[hashingType]);
            md.update(hashingTargetBytes);
            hashingResultBytes = md.digest();
        }
        catch (NoSuchAlgorithmException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Hashing Fail!", e);
        }

        return hashingResultBytes;
    }

    /**
     * <p>입력된 바이트 배열을 해싱하여 반환합니다.</p>
     * @param hashingType : 해시 알고리즘
     * <ul><li>MD5 : MD5 알고리즘 (16byte output)</li>
     *     <li>SHA256 : SHA256 알고리즘 (32byte output)</li></ul>
     * @param originStr : 원본 문자열.
     * @return 해싱된 바이트 배열.
     */
    public static byte[] hashing(int hashingType, String originStr) {
        return hashing(hashingType, originStr.getBytes(), null);
    }
    
    /**
     * <p>AES 암복호화를 수행합니다.</p>
     * Algorithm    : AES-128 (16byte block) /
     * Mode         : CBC / 
     * Padding      : PKCS5
     * @param opMode - 암복호화 모드.
     * <ul><li>Cipher.ENCRYPT_MODE : 암호화 수행</li>
     * <li>Cipher.DECRYPT_MODE : 복호화 수행</li></ul>
     * @param inBytes - 암/복호화를 수행할 바이트 배열.
     * @param secretKeySpec - 암/복호화에 사용할 대칭키.
     * @return 암/복호화된 바이트 배열을 반환합니다.
     */
    private static byte[] cipherAES(int opMode, byte[] inBytes, SecretKeySpec secretKeySpec) {
        byte[] outBytes = null;
        
        try {
            Cipher aesCipher = Cipher.getInstance(TF_AES_CBC_PKCS5);
            aesCipher.init(opMode, secretKeySpec, IV_AES_CBC_PKCS5);
            outBytes = aesCipher.doFinal(inBytes);
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException | 
               IllegalBlockSizeException | BadPaddingException | 
               NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Fail to En/decrypt!" , e);
        }
        
        return outBytes;
    }
}
