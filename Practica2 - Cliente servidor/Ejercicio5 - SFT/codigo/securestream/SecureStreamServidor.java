/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clienteservidor.securestream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 *
 * @author guillermo
 */
public class SecureStreamServidor extends SecureStream{
            
    //Mi clave privada
    private PrivateKey privateKey;
    
    //Clave para cifrado simétrico
    private byte[] cifrado;
    
    //Llave publica del cliente
    private PublicKey client_public_key;
    
    public SecureStreamServidor(Socket socketServicio) throws Exception{
        this.socketServicio = socketServicio; 
        stablishConnection();
    }
    
    private void stablishConnection() throws IOException, Exception{
        //Creo los streams de entrada y salida
        inputStream = new DataInputStream(socketServicio.getInputStream());
        outputStream = new DataOutputStream(socketServicio.getOutputStream());
        
        //Cargo mi llave privada y el cipher
        privateKey = SecureStreamUtils.getPrivate("src/private_key.der");
        Cipher cipher = Cipher.getInstance("RSA");
        
        //Genero base pass simetrica
        SecureRandom secureRandom = new SecureRandom();
        cifrado = new byte[16]; 
        secureRandom.nextBytes(cifrado);
        
        
        //Leo llave publica del cliente
        byte[] bufferEntrada = new byte[2048];
        int len;

        len = inputStream.read(bufferEntrada);
        //System.out.println("Read1: " + len);
        
        //Decodifico la llave publica del cliente
        cipher.init(Cipher.PRIVATE_KEY, privateKey);
        byte[] entrada = cipher.doFinal(bufferEntrada,0,len);
        
        //Cargo la llave publica del cliente
        client_public_key = SecureStreamUtils.getPublic(entrada);
        
        //Codifico la llave simetrica con su llave publica
        cipher.init(Cipher.PUBLIC_KEY, client_public_key);
        byte[] salida = cipher.doFinal(cifrado);
        outputStream.write(salida, 0, salida.length); 
        //System.out.println(cifrado.length);
        
        symDeCipher = Cipher.getInstance("AES");
        symEnCipher = Cipher.getInstance("AES");
        SecretKey key = SecureStreamUtils.genSecretKey(cifrado);
        symEnCipher.init(Cipher.ENCRYPT_MODE, key);
        symDeCipher.init(Cipher.DECRYPT_MODE, key);
        
        
        //cipherInputStream = new CipherInputStream(socketServicio.getInputStream(),symDeCipher);
        //cipherOutputStream = new CipherOutputStream(socketServicio.getOutputStream(), symEnCipher);
        //inputStream = new DataInputStream(cipherInputStream);
        //outputStream = new DataOutputStream(cipherOutputStream);
    }
}
