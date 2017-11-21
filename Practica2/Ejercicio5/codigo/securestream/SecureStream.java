/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clienteservidor.securestream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author guillermo
 */
public abstract class SecureStream {
    //Socket del servicio
    Socket socketServicio;
    
    //DataStreams
    DataInputStream inputStream;
    DataOutputStream outputStream;
    
    //Ciphers
    Cipher symDeCipher;
    Cipher symEnCipher;
    
    byte[] buf = new byte[1];
    
    /*
    public void testSize() throws IllegalBlockSizeException, IllegalBlockSizeException, BadPaddingException{
        for(int i=1; i<100; i++){
            byte[] b = new byte[i];
            byte[] b_enc = symEnCipher.doFinal(b,0,b.length);
            //System.out.println(b.length + " " + b_enc.length);
        }
    }
    
    private int readUnsafe(byte[] b, int off, int len) throws IOException, IllegalBlockSizeException, BadPaddingException{
        
        double block_size = Math.ceil((len+1)/16.0)*16;
        int b_size = (int)block_size;
        byte[] enc = new byte[b_size];
        //System.out.println("ReadSize0: " + b_size);
        int read = inputStream.read(enc,0,b_size);
        if(read == -1)
            return -1;
        //System.out.println("ReadSize1: " + b_size + " " + read);
        byte[] dec = symDeCipher.doFinal(enc,0,read);
        for(int i=off, j=0; i<b.length && j<dec.length; i++, j++)
            b[i] = dec[i-off];
        return dec.length;
    }
    */
    private void writeUnsafe(byte[] b, int off, int len) throws IOException, IllegalBlockSizeException, BadPaddingException{
        //cipherOutputStream.write(b,off,len);
        //cipherOutputStream.flush();
        byte[] enc = symEnCipher.doFinal(b,off,len);
        outputStream.write(enc,0,enc.length);
    }
    
    
    public int read(byte[] b, int off, int len) throws IOException, IllegalBlockSizeException, BadPaddingException, Exception{
        //System.out.println("BEGIN Readint");
        int siz = readInt();
        //System.out.println("END Readint");
        if(siz == -1)
            return -1;
        int n = -1, total_n = 0;
        if(buf.length < siz)
            buf = new byte[siz];
        //System.out.println("Siz: " + siz);
        while(siz > 0){
            n = inputStream.read(buf, total_n, siz);
            //System.out.println("siz > 0: " + siz + " " + total_n + " " + n);
            if(n == -1)
                break;
            siz -= n;
            total_n += n;
        }
        
        if((total_n == 0 && n == -1)||siz == -1)
            return -1;
        
        byte[] res = symDeCipher.doFinal(buf,0,total_n);
        
        if (len < res.length)
            throw new Exception("Not enough space to write in byte[]");
        
        //System.out.println("Read: " + off + " " + len + " " + b.length + " " + siz + " "  + res.length);
        for(int i=off, j=0; j < res.length; i++, j++){
            //System.out.println("#");
            b[i] = res[j];
        }
        //System.out.println("DONE");
        
        return res.length;
    }
    
    public void write(byte[] b, int off, int len) throws IOException, IllegalBlockSizeException, BadPaddingException{
        byte[] enc = symEnCipher.doFinal(b,off,len);
        int siz = enc.length;
        writeInt(siz);
        outputStream.write(enc,0,siz);
    }
    
    public void writeInt(int n) throws IOException, IllegalBlockSizeException, BadPaddingException{
        byte[] b_size = ByteBuffer.allocate(4).putInt(n).array();
        //System.out.println("Len : " + len + " " + b_cad.length);
        //Send size
        writeUnsafe(b_size,0,4);
    }
    
    public int readByte(byte[] b, int off, int len, int total) throws IOException, IllegalBlockSizeException, BadPaddingException{
        int siz = total;
        int n = -1, total_n = 0;
        if(buf.length < siz)
            buf = new byte[siz];
        //System.out.println("Siz: " + siz);
        while(siz > 0){
            //System.out.println("readByte about to read");
            n = inputStream.read(buf, total_n, siz);
            //System.out.println("readByte " + n);
            if(n == -1) break;
            
            siz -= n;
            total_n += n;
           // System.out.println(n);
            
        }
        if(total_n != total && total_n == 0)
            return -1;
        for(int i=off, j=0; j<total_n; i++, j++)
            b[i] = buf[j];
        return total_n;
    }
    
    public int readInt() throws IOException, IllegalBlockSizeException, BadPaddingException{
        if(buf.length < 16)
            buf = new byte[16];
        
        if(readByte(buf, 0, 16, 16)==-1)
            return -1;
        byte[] b_size = symDeCipher.doFinal(buf,0,16);
        ByteBuffer bb_size = ByteBuffer.wrap(b_size);
        int num = bb_size.getInt();
        //System.out.println(num);
        return num;
    }
    
    public void writeUTF(String cad) throws UnsupportedEncodingException, IOException, IllegalBlockSizeException, BadPaddingException{
        byte[] b_cad = cad.getBytes("UTF-16");
        
        byte[] enc = symEnCipher.doFinal(b_cad,0,b_cad.length);
        int len = enc.length;
        
        //System.out.println("Print number");
        writeInt(len);
        //Send string
        //System.out.println("Print string");
        outputStream.write(enc,0,len);
    }
    
    public String readUTF() throws IOException, IllegalBlockSizeException, BadPaddingException{
        //Get size
        int n;
        int siz = readInt();
        /*
        byte[] b_size = new byte[4];
        read(b_size,0,4);
        ByteBuffer bb_size = ByteBuffer.wrap(b_size);
        int c_size = bb_size.getInt();
        */
        //System.out.println("Len : " + c_size);
        //Get string
        //byte[] enc_cad = new byte[c_size];
        if(buf.length < siz)
            buf = new byte[siz];
        int total_n = 0;
        n=0;
        while(siz > 0){
            //System.out.println("#1: " + total_n + " " + c_size + " " + n);
            n = inputStream.read(buf, total_n, siz);
            total_n += n;
            siz -= n;
            //System.out.println("#2: " + total_n + " " + c_size + " " + n);
        }        
        byte[] b_cad = symDeCipher.doFinal(buf,0,total_n);
        String cad = new String(b_cad, "UTF-16");
        //System.out.println("ReadUTF: " + b_cad.length + " " + cad);
        return cad;
    }

    
    public void close() throws IOException{
        socketServicio.close();
    }    
}
