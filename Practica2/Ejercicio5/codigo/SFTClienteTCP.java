package clienteservidor;
/*
    Guillermo Gomez Trenado
    Adrian Pelaez Vegas
    Jose Antonio Ruiz Millan
    SFT
*/
import clienteservidor.securestream.SecureStreamCliente;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Scanner;

public class SFTClienteTCP {

	public static void main(String[] args) throws Exception {
		
                //vector de bytes donde almacenamos el fichero
		byte []bufferEnvio;
                
		// Parametros por defecto:
		String host="localhost";
                String user = "comun" ,pass = "comun",option = "listar";
                boolean hayFichero = false;
		// Puerto en el que espera el servidor:
		int port=8989;
                int i;
                
		// Socket para la conexión TCP
		Socket socketServicio;
                
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                
                String filename = ".";
                
                int j = 0;
                String arg;
                while(j < args.length){
                    arg = args[j];
                    switch (arg){
                        case "-t":
                            host = args[++j];
                        break;
                        case "-u":
                            user = args[++j];
                        break;
                        case "-p":
                            pass = args[++j];
                        break;
                        case "-f":
                            filename = args[++j];
                            hayFichero = true;
                        break;
                        case "-up":
                            option = "subir";
                        break;
                        case "-d":
                            option = "bajar";
                        break;
                        case "-l":
                            option = "listar";
                        break;
                    }
                    j++;
                }
                
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(pass.getBytes());
                BigInteger number = new BigInteger(1, messageDigest);
                String hashtext = number.toString(16);
 
                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }
                if(!hayFichero && !(option.equals("listar")))System.err.println("Fichero no indicado.");
                else{
                    try {
                        
                    File localFile = null;
                    if(!option.equals("listar"))localFile = new File( filename );//Aquí debe estar el fichero final, con la ruta de descarga indicada
                    if(option.equals("subir") && (!localFile.exists() || localFile.isDirectory())){
                        System.err.println("El fichero a subir no existe o es un directorio.");
                        System.exit(-1);
                    }

                    // Creamos un socket que se conecte a "host" y "port":
                    //////////////////////////////////////////////////////
                    socketServicio=new Socket(host,port);
                    SecureStreamCliente secureStreamCliente = new SecureStreamCliente(socketServicio);
                    //////////////////////////////////////////////////////	

                    //////////////////////////////////////////////////////
                    // Enviamos el nombre del fichero
                    //////////////////////////////////////////////////////
                    secureStreamCliente.writeUTF(option);
                    secureStreamCliente.writeUTF(user);
                    secureStreamCliente.writeUTF(hashtext);
                    if(!option.equals("listar"))secureStreamCliente.writeUTF(localFile.getName());
                    //////////////////////////////////////////////////////
                    String respuesta = null; 
                    double uploaded = 0;
                    double downloaded = 0;
                    int iter = 0;
                    if(option.equals("subir")){
                        bis = new BufferedInputStream(new FileInputStream(localFile));
                        bufferEnvio = new byte[2048];
                        long current = System.currentTimeMillis(), last, elapsed;
                        double mb_s;
                        double perc;
                        while ((i = bis.read(bufferEnvio)) != -1){
                            secureStreamCliente.write(bufferEnvio,0,i);
                            iter++;
                            if((iter%1000) == 0){
                                last = current;
                                current = System.currentTimeMillis();
                                elapsed = current - last;
                                uploaded += ((i*1000)/(1024.0*1024));
                                perc = uploaded / (localFile.length() / (1024.0*1024))*100;
                                mb_s = ((i*1000)/(1024.0*1024))/(1.0*elapsed/1000);
                                System.out.print("Uploaded: " + String.format("%.2f", uploaded) + "MB, " + String.format("%.2f", mb_s) + "MB/s ( "+String.format("%.2f",perc) +"% )                        \r");
                            }
                            
                        }
                        secureStreamCliente.writeInt(-1);
                        respuesta = secureStreamCliente.readUTF();
                        bis.close();
                    }
                    else if(option.equals("bajar")){  
                        bufferEnvio = new byte[2048];
                        iter = 0;
                        long current = System.currentTimeMillis(), last, elapsed;
                        double mb_s;
                        double perc;
                        respuesta = secureStreamCliente.readUTF();
                        if(!respuesta.contains("no existe")){
                            int tamanio = secureStreamCliente.readInt();
                            bos = new BufferedOutputStream(new FileOutputStream(localFile));
                            while((i = secureStreamCliente.read(bufferEnvio,0,bufferEnvio.length)) != -1){
                                bos.write(bufferEnvio,0,i);
                                iter++;
                                if((iter%1000) == 0){
                                    last = current;
                                    current = System.currentTimeMillis();
                                    elapsed = current - last;
                                    downloaded += ((i*1000)/(1024.0*1024));
                                    perc = downloaded / (tamanio / (1024.0*1024))*100;
                                    mb_s = ((i*1000)/(1024.0*1024))/(1.0*elapsed/1000);
                                    System.out.print("Downloaded: " + String.format("%.2f", downloaded) + "MB, " + String.format("%.2f", mb_s) + "MB/s ( "+String.format("%.2f",perc) +"% )                        \r");
                                }
                            }
                            bos.close();
                        }
                    }
                    else if(option.equals("listar"))respuesta = secureStreamCliente.readUTF();
                    
                    
                    System.out.println(respuesta+"                                   ");

                    //////////////////////////////////////////////////////
                    // Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
                    // el inpuStream  y el outputStream)
                    //////////////////////////////////////////////////////
                    secureStreamCliente.close();
                    socketServicio.close(); 
                    //////////////////////////////////////////////////////

                    // Excepciones:
                    } catch (UnknownHostException e) {
                            System.err.println("Error: Nombre de host no encontrado.");
                    } catch (IOException e) {
                            System.err.println("Error de entrada/salida al abrir el socket o leer el fichero.");
                            System.err.println(e.toString());
                            e.printStackTrace(System.err);
                    }
                }
	}
}
