package com.caihongcity.com.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * 
 * @author hkrt
 * 加密类 
 */
public class EncryptUtils {
	
	  public static final String KEY_SHA="SHA";
	  public static final String KEY_MD5="MD5";
	  public static final String KEY_MAC="HmacMD5";
	    
	public static byte[] encryptMD5(byte[] data) throws Exception
	{
		MessageDigest md5=MessageDigest.getInstance(KEY_MD5);
		md5.update(data);
		return md5.digest();
	}
	public static byte[] encryptSHA(byte[] data) throws Exception
	{
		MessageDigest sha=MessageDigest.getInstance(KEY_SHA);
		sha.update(data);
		return sha.digest();
	}
	public static byte[] encryptHMAC(byte[] data,String key) throws Exception
	{
		SecretKey sk=new SecretKeySpec(key.getBytes(),KEY_MAC);
		Mac mac=Mac.getInstance(sk.getAlgorithm());
		mac.init(sk);
		return mac.doFinal(data);
	}

	
  /**
       * md5加密产生，产生128位（bit）的mac
       * 将128bit Mac转换成16进制代码
       * @param strSrc
       * @param key
       * @return
  */
  
      public static String MD5Encode(String strSrc, String key) {
  
          try {
              MessageDigest md5 = MessageDigest.getInstance("MD5");
              md5.update(strSrc.getBytes("UTF8"));
              String result = "";
              byte[] temp;
              temp = md5.digest(key.getBytes("UTF8"));
              for (int i = 0; i < temp.length; i++) {
                  result += Integer.toHexString(
                   (0x000000ff & temp[i]) | 0xffffff00).substring(6);
              }
              return result;
          } catch (NoSuchAlgorithmException e) {
              e.printStackTrace();
          } catch (Exception e) {
              e.printStackTrace();
          }
          return null;
  
      }
      
      /**
       * Bitwise XOR between corresponding bytes
       * @param op1 byteArray1
       * @param op2 byteArray2
       * @return an array of length = the smallest between op1 and op2
       */
      public static byte[] xor (byte[] op1, byte[] op2) {
          byte[] result;
          // Use the smallest array
          if (op2.length > op1.length) {
              result = new byte[op1.length];
          }
          else {
              result = new byte[op2.length];
          }
          for (int i = 0; i < result.length; i++) {
              result[i] = (byte)(op1[i] ^ op2[i]);
          }
          return  result;
      }

}
