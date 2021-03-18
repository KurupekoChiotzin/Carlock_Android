package com.mulin.larlock.larlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class EncryprtionAndStore {
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String KEYSTORE_ALIAS = "LarLock_KeyStore";
    private static SharedPreferences recoedData;
    private static KeyStore keyStore = null;

    public EncryprtionAndStore(Context context) {
        try {
            recoedData=context.getSharedPreferences("record",Context.MODE_PRIVATE);
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                recoedData.edit().putString("PREF_KEY_IV","").commit();
                genKeyStoreKey(context);
                genAESKey();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void genKeyStoreKey(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            generateRSAKey_AboveApi23();
        } else {
            generateRSAKey_BelowApi23(context);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void generateRSAKey_AboveApi23() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);

            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec
                    .Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build();
            keyPairGenerator.initialize(keyGenParameterSpec);
            keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateRSAKey_BelowApi23(Context context) {
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 100);

            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEYSTORE_ALIAS)
                    .setSubject(new X500Principal("CN=" + KEYSTORE_ALIAS))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);
            keyPairGenerator.initialize(spec);
            keyPairGenerator.generateKeyPair();

            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements())
            {
                System.out.println("內容:"+aliases.nextElement());
            }
        } catch (Exception e) {
            Log.d("Encryption:",e.getMessage());
            e.printStackTrace();
        }
    }

    public  String encryptRSA(byte[] message)
    {
        try {

            PublicKey publicKey = keyStore.getCertificate(KEYSTORE_ALIAS).getPublicKey();
            Cipher cipher=Cipher.getInstance(RSA_MODE);
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            byte[] encryptmessage=cipher.doFinal(message);
            System.out.println("加密資料"+encryptmessage.length);
            return android.util.Base64.encodeToString(encryptmessage, android.util.Base64.DEFAULT);
        }
        catch (Exception e) {
            Log.d("Encryption:",e.getMessage());
            e.printStackTrace();
        }
        return "";

    }

    public byte[] decryptionRSA(String encryptedMessage)
    {
        try {
            PrivateKey privateKey =(PrivateKey) keyStore.getKey(KEYSTORE_ALIAS,null);
            Cipher cipher=Cipher.getInstance(RSA_MODE);
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            byte[] encryptedBytes=android.util.Base64.decode(encryptedMessage, Base64.DEFAULT);
            byte[] decryptedBytes=cipher.doFinal(encryptedBytes);
            return decryptedBytes;
        }
        catch (Exception e)
        {
            Log.d("Encryption:",e.getMessage());
            e.printStackTrace();
        }
        return "".getBytes();
    }

    public final static String SHA1(String encryptionString)
    {
        try
        {
            MessageDigest digest=java.security.MessageDigest.getInstance("SHA-1");
            digest.update(encryptionString.getBytes());
            byte messageDigest[]=digest.digest();
            StringBuffer hexString=new StringBuffer();

            for(int i=0;i<messageDigest.length;i++)
            {
                String shaHex=Integer.toHexString(messageDigest[i]&0xFF);
                if(shaHex.length()<2)
                {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }
    private void genAESKey()
    {
        byte[] aesKey=new byte[16];
        SecureRandom secureRandom=new SecureRandom();
        secureRandom.nextBytes(aesKey);

        byte[] generated=secureRandom.generateSeed(12);
        String iv=encryptRSA(android.util.Base64.encodeToString(generated, Base64.DEFAULT).getBytes());
        recoedData.edit().putString("PREF_KEY_IV",iv).commit();

        String encryptAESKey=encryptRSA(aesKey);
        recoedData.edit().putString("PREF_AES_KEY",encryptAESKey);
    }

    private String encryptAES(String message)
    {
        try {
            Cipher cipher=Cipher.getInstance("AES_MODE");
            cipher.init(Cipher.ENCRYPT_MODE,getAESKey(),new IvParameterSpec(getIV()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private byte[] getIV()
    {
        String prefIV=recoedData.getString("PREF_KEY_IV","");
        return android.util.Base64.decode(prefIV, Base64.DEFAULT);
    }
    private SecretKeySpec getAESKey()
    {
        String encryptedKey=recoedData.getString("PREF_AES_KEY","");
        byte[] aesKey=decryptionRSA(encryptedKey);
        return new SecretKeySpec(aesKey,"AES_MODE");
    }
}
