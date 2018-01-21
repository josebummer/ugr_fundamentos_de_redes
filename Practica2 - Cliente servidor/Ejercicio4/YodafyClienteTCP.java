//
// YodafyServidorIterativo
// (CC) jjramos, 2012
//

import java.io.IOException;

public class YodafyClienteTCP {

	public static void main(String[] args) {
		
		byte []buferEnvio;
		byte []buferRecepcion=new byte[2048];
		int bytesLeidos=0;
		
		// Nombre del host donde se ejecuta el servidor:
		String host="localhost";
		// Puerto en el que espera el servidor:
		int port=8989;
		
		// Socket para la conexión TCP
		//Socket socketServicio=null;
		
		try {
			// Creamos un socket que se conecte a "hist" y "port":
			//////////////////////////////////////////////////////
			// socketServicio= ... (Completar)
			//socketServicio = new Socket(host,port);
			//////////////////////////////////////////////////////			
			
			//InputStream inputStream = socketServicio.getInputStream();
			//OutputStream outputStream = socketServicio.getOutputStream();

			//PrintWrite y BufferedReader
            //PrintWriter outPrinter = new PrintWriter(outputStream,true);
            //BufferedReader inReader = new BufferedReader(new InputStreamReader(inputStream));
            DatagramSocket socket = new DatagramSocket();
			
			// Si queremos enviar una cadena de caracteres por un OutputStream, hay que pasarla primero
			// a un array de bytes:
			buferEnvio="Al monte del volcán debes ir sin demora".getBytes();
            //String cadenaEnvio = "Al monte del volcán debes ir sin demora";
			// Enviamos el array por el outputStream;
			//////////////////////////////////////////////////////
			// ... .write ... (Completar)
			//////////////////////////////////////////////////////
			//outputStream.write(buferEnvio);
            //outPrinter.println(cadenaEnvio);
            DatagramPacket envio = new DatagramPacket(
                    buferEnvio,
                    buferEnvio.length,
                    InetAddress.getByName(host),
                    port
            );
            socket.send(envio);

			// Aunque le indiquemos a TCP que queremos enviar varios arrays de bytes, sólo
			// los enviará efectivamente cuando considere que tiene suficientes datos que enviar...
			// Podemos usar "flush()" para obligar a TCP a que no espere para hacer el envío:
			//////////////////////////////////////////////////////
			// ... .flush(); (Completar)
			//////////////////////////////////////////////////////
			//outputStream.flush();

			// Leemos la respuesta del servidor. Para ello le pasamos un array de bytes, que intentará
			// rellenar. El método "read(...)" devolverá el número de bytes leídos.
			//////////////////////////////////////////////////////
			// bytesLeidos ... .read... buferRecepcion ; (Completar)
			//////////////////////////////////////////////////////
			//bytesLeidos = inputStream.read(buferRecepcion,0,256);
            Thread.sleep(1000);

            //String cadenaRecibida = inReader.readLine();

            DatagramPacket paquete = new DatagramPacket(buferRecepcion,buferRecepcion.length);
            socket.receive(paquete);
            String cadenaRecibida = new String(paquete.getData(),0,paquete.getLength());
			// MOstremos la cadena de caracteres recibidos:
			System.out.println("Recibido: ");
			/*
			for(int i=0;i<bytesLeidos;i++){
				System.out.print((char)buferRecepcion[i]);
			}
			*/
			System.out.println(cadenaRecibida);
			
			// Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
			// el inpuStream  y el outputStream)
			//////////////////////////////////////////////////////
			// ... close(); (Completar)
			//////////////////////////////////////////////////////
			//socketServicio.close();
			socket.close();
			// Excepciones:
		} catch (UnknownHostException e) {
			System.err.println("Error: Nombre de host no encontrado.");
		} catch (IOException e) {
			System.err.println("Error de entrada/salida al abrir el socket.");
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
