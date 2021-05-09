


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPEchoServer {

	final static int PORT = 4950;
	static int userId = 0;

	public static void main(String[] args) throws IOException {
		
		
		
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Server Running");

		
		do{
			//Listens for a connection.
			Socket socket = serverSocket.accept();

			//Running multiple client
			multiplClient multipleClient= new multiplClient(socket, ++userId);
			
			multipleClient.ThreadRun();
			
			multipleClient.start();
		}while (!false) ;
	}
}

class multiplClient  extends Thread {

	private final int BUFSIZE = 1024;
	private Socket socket;
	private InputStream inS;
	private OutputStream outS;
	private final int ClintId;
	private String rMessage = "";
	
	public multiplClient(Socket socket, int userId) {
		this.socket = socket;
		this.ClintId = userId;
	}
	
	public void ThreadRun() throws IOException {
			
		inS = socket.getInputStream();
		outS = socket.getOutputStream();
	}


	@Override
	public void run() {
	try {
			
		do {	//buffer
				byte[] buf = new byte[BUFSIZE];
				//save the message into buffer
				inS.read(buf);
				
				rMessage = new String(buf).trim();
				int rlength= rMessage.length();
			
				if (rlength != 0 ) {// check if the message is empty
					
						//set message
					outS.write(rMessage.getBytes());
					outS.flush();
					System.out.println(	toString(rlength));
				}
			} while (rMessage != null);

			//close connection
		
			socket.close();
			outS.close();
			inS.close();
			System.out.println(" Client (" + ClintId + ")  close the connection");
		} catch (Exception e) {
			System.exit(1);

			
		}
		
	}

public String toString (int length) {
		
		
   String output = "[User " + ClintId + "] Sent a Message ["+rMessage+"] from IP " +
                    socket.getInetAddress() + " though PORT "
				   + socket.getPort() + " Received " + length + " bytes and  Sent "
				   + length + " bytes, and  Buffer size = " + BUFSIZE ;
   					return output;
		
		
	}	
}
