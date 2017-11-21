/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clienteservidor.securestream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 *
 * @author guillermo
 */
public class SecureStreamCliente extends SecureStream{
    
    private PublicKey server_pub_key;
    private PrivateKey priv_key;
    private PublicKey pub_key;
    private byte[] cifrado;
    
    public SecureStreamCliente(Socket socketServicio) throws Exception{
        this.socketServicio = socketServicio;
        stablishConection();
    }
    
    private void genKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyGen.initialize(512,random);
        KeyPair keyPair = keyGen.generateKeyPair();
        priv_key = keyPair.getPrivate();
        pub_key = keyPair.getPublic();
    }
    
    private void stablishConection() throws Exception{
        //Creo los streams de entrada y salida
        inputStream = new DataInputStream(socketServicio.getInputStream());
        outputStream = new DataOutputStream(socketServicio.getOutputStream());         
        
        //Obtengo llave publica servidor
        server_pub_key = SecureStreamUtils.getPublic("src/public_key.der");
        Cipher cipher = Cipher.getInstance("RSA");
        
        //Genero mi par de llaves
        genKeyPair();
        
        //Codifico mi llave publica
        byte[] pub_key_cifr;        
        cipher.init(Cipher.PUBLIC_KEY, server_pub_key);
        pub_key_cifr = cipher.doFinal(pub_key.getEncoded());
        
        //Mando al servidor mi llave publica
        outputStream.write(pub_key_cifr);

        //Leo llave simetrica
        byte[] bufferEntrada = new byte[2048];
        int len;
        len = inputStream.read(bufferEntrada);
        //System.out.println("Read1: " + len);
        
        //Decifro llave simetrica
        bufferEntrada = Arrays.copyOf(bufferEntrada, len);
        cipher.init(Cipher.PRIVATE_KEY, priv_key);
        cifrado = cipher.doFinal(bufferEntrada, 0, len); 
        //System.out.println(cifrado.length);
        
        symDeCipher = Cipher.getInstance("AES");
        symEnCipher = Cipher.getInstance("AES");
        //Cipher symEnCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKey key = SecureStreamUtils.genSecretKey(cifrado);
        symDeCipher.init(Cipher.DECRYPT_MODE, key);
        symEnCipher.init(Cipher.ENCRYPT_MODE, key);
        
        //cipherInputStream = new CipherInputStream(socketServicio.getInputStream(),symDeCipher);
        //cipherOutputStream = new CipherOutputStream(socketServicio.getOutputStream(), symEnCipher);
        //inputStream = new DataInputStream(cipherInputStream);
        //outputStream = new DataOutputStream(cipherOutputStream);
    }
    
}
