/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clienteservidor.securestream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author guillermo
 */
public class SecureStreamUtils {
    public static PrivateKey getPrivate(String filename)
  throws Exception {

    byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

    PKCS8EncodedKeySpec spec =
      new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }
  
  public static PrivateKey getPrivate(byte[] encoded)
    throws Exception {
      PKCS8EncodedKeySpec spec =
        new PKCS8EncodedKeySpec(encoded);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePrivate(spec);
  }
  
  public static PublicKey getPublic(String filename)
    throws Exception {
        //System.out.println(Paths.get(filename));
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        X509EncodedKeySpec spec =
          new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
  }
  
  public static PublicKey getPublic(byte[] encoded)
    throws Exception {
        X509EncodedKeySpec spec =
          new X509EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
  }
  
  public static SecretKey genSecretKey(byte[] key){
        return new SecretKeySpec(key, "AES");
        //KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        //keyGen.init
    }
}
