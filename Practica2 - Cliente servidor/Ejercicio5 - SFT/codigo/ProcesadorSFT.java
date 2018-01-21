package clienteservidor;
/*
    Guillermo Gomez Trenado
    Adrian Pelaez Vegas
    Jose Antonio Ruiz Millan
    SFT
*/
import clienteservidor.securestream.SecureStreamServidor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

//
// Nota: si esta clase extendiera la clase Thread, y el procesamiento lo hiciera el método "run()",
// ¡Podríamos realizar un procesado concurrente! 
//
public class ProcesadorSFT extends Thread{
	// Referencia a un socket para enviar/recibir las peticiones/respuestas
	private final Socket socketServicio;
	
	// Constructor que tiene como parámetro una referencia al socket abierto en por otra clase
	public ProcesadorSFT(Socket socketServicio) {
		this.socketServicio=socketServicio;
	}
	
	
	// Aquí es donde se realiza el procesamiento realmente:
        @Override
	public void run(){
            
		// Como máximo leeremos un bloque de 1024 bytes. Esto se puede modificar.
		byte [] receivedData;
		int i,n;
                
                String file = null,user,pass,option;
                BufferedInputStream bis;
                BufferedOutputStream bos;
		
		
		try {
                    SecureStreamServidor secureStreamServidor = new SecureStreamServidor(socketServicio);
                    receivedData = new byte[2048];
              
                    //recibimos el nombre del fichero
                    option = secureStreamServidor.readUTF();
                    user = secureStreamServidor.readUTF();
                    pass = secureStreamServidor.readUTF();
                    if(!option.equals("listar"))file = secureStreamServidor.readUTF();
                    if(!option.equals("listar"))file = file.substring(file.indexOf('\\')+1,file.length());
                    n = validarUsuarios(user);
                    if(comprobarPass(n, pass) || user.equals("comun")){
                        File dir = new File("Ficheros/"+user);
                        dir.mkdirs();
                        file = "Ficheros/"+user+"/"+file;
                        File fich = new File(file);
                        switch (option){
                            case "listar":
                                String[] ficheros = dir.list();
                                if(ficheros.length != 0){
                                    secureStreamServidor.writeUTF(Arrays.toString(ficheros));
                                }
                                else{
                                    secureStreamServidor.writeUTF("No tienes archivos.");
                                }
                                System.out.println("Usuario "+user+" a listado sus archivos.");
                            break;
                            case "subir":
                                //para guardar el fichero
                                bos = new BufferedOutputStream(new FileOutputStream(file));
                                while ((i = secureStreamServidor.read(receivedData,0,receivedData.length)) != -1){
                                    bos.write(receivedData,0,i);
                                }
                                secureStreamServidor.writeUTF("Fichero subido correctamente.");
                                System.out.println(user+" ha subido: "+file);
                                bos.close();
                            break;
                            case "bajar":
                                if(!fich.exists()) secureStreamServidor.writeUTF("El fichero "+ file +" no existe.");
                                else{
                                    bis = new BufferedInputStream(new FileInputStream(file));
                                    secureStreamServidor.writeUTF("Fichero bajado correctamente.");
                                    secureStreamServidor.writeInt((int) fich.length());
                                    while ((i = bis.read(receivedData,0,receivedData.length)) != -1){
                                        secureStreamServidor.write(receivedData,0,i);
                                    }
                                    System.out.println(user+" ha bajado: "+file);
                                    bis.close();
                                }
                            break;
                        }
                    }
                    else{
                        secureStreamServidor.writeUTF("Error con el usuario/contraseña.");
                        System.err.println("El usuario/contraseña no son correctos -> "+user);
                    }
                    secureStreamServidor.close();
                    socketServicio.close();
		} catch (IOException e) {
			System.err.println(e);
		} catch (Exception ex) {
                Logger.getLogger(ProcesadorSFT.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
        int validarUsuarios(String user) throws FileNotFoundException, IOException{
            File fich = new File("users.txt");
            FileReader fr = new FileReader(fich);
            BufferedReader br = new BufferedReader(fr);
            int pos = -1;
            String l;
            boolean existe = false;
            
            while(((l=br.readLine())!=null) && !existe){
                existe = (user.equals(l));
                pos++;
            }   
            if(!existe) pos = -1;
            br.close();
            fr.close();
            
            return pos;
        }
        boolean comprobarPass(int pos, String pass) throws FileNotFoundException, IOException{
            File fich = new File("pass.txt");
            FileReader fr = new FileReader(fich);
            BufferedReader br = new BufferedReader(fr);
            int i = 0;
            String l = br.readLine();
            
            while (i < pos){
                i++;
                l = br.readLine();
            }
            return (l.equals(pass));
        }
}
