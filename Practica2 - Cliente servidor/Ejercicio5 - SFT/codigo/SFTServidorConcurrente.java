package clienteservidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
    Guillermo Gomez Trenado
    Adrian Pelaez Vegas
    Jose Antonio Ruiz Millan
    SFT
*/
public class SFTServidorConcurrente {

	public static void main(String[] args) {
	
		// Puerto de escucha
		int port=8989;
               
		//Socket servidor para escuchar las peticiones
                ServerSocket serverSocket;
                
                
		try {
                        //////////////////////////////////////////////////
			// Abrimos el socket en modo pasivo, escuchando el en puerto indicado por "port"
			//////////////////////////////////////////////////
			serverSocket= new ServerSocket(port);
			
                        Socket socketServicio;
                        
			do {
				/////////////////////////////////////////////////
				// Aceptamos una nueva conexión con accept()
				/////////////////////////////////////////////////
				socketServicio= serverSocket.accept();
				
                                //////////////////////////////////////////////////
				// Creamos un objeto de la clase ProcesadorSFT, pasándole como 
				// argumento el nuevo socket, para que realice el procesamiento
				// Este esquema permite que se puedan usar hebras más fácilmente.
                                //////////////////////////////////////////////////
				ProcesadorSFT procesador=new ProcesadorSFT(socketServicio);
				procesador.start();
				
			} while (true);
			
		} catch (IOException e) {
			System.err.println("Error al escuchar en el puerto "+port);
		}

	}

}
