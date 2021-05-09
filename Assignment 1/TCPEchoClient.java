package labb2;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPEchoClient extends NetworkLayer {
	
	protected Socket socket = new Socket();
	protected DataInputStream inStream;
	protected DataOutputStream outStream;
	protected String Message = "aya kathem me!";
	protected int sentM = 0;
	protected long TotalTime;
	protected final int T = 995;
	protected String st;
		public static void main(String[] args) {
		// you can you use Command line to insert args or you can use the below String array
		/*
		String[] ar = new String[4];
		ar[0]= "192.168.56.101";
		ar[1]= "4950";
		ar[2]= "64";
		ar[3]= "4";*/
		TCPEchoClient layer = new TCPEchoClient(args);

	 try {
			layer.Client();
			
			
		} catch (InterruptedException e) {
		
			e.printStackTrace();
		}
}
	public TCPEchoClient(String[] args) {
		 if (!args[0].isEmpty()  && !args[1].isEmpty()  && !args[2].isEmpty()  && !args[3].isEmpty() ) {
				
				
				// get Port , Ip , buffersize  and  transfer rate
					super.IP = args[0];
					// convert string to Integer 
					super.port =Integer.parseInt(args[1]);
					super.bufSize =Integer.parseInt(args[2]);
					super.transR = Integer.parseInt(args[3]);
					super.socketremote = new InetSocketAddress(IP, port);
					super.checkIP(IP);
					super.checkInput();
					
			}	else { 
				
				System.out.println("Error:The info you provide is incomplete, Please fill in IP, PORT,transfer rate and BufferSize");
				System.exit(1);
			}
		
	}

	@Override
	public void run() {
		while ( sentM < super.transR ) {
			sentM++;
			Messages();
			int messagelength = Message.length() ;
			
			
			//Check if the received message is equal to the sent message
			if (st.length() == messagelength)
				
				System.out.println( messagelength +" bytes has been sent - The received "+ st.length()+" with Buffer Size = " + bufSize );
			else if (st.length() != messagelength)
					System.out.println( messagelength + " bytes has been sent -The received " + st.length()
						+ " which is not equal [Buffer Size = " + bufSize + "]");
			try {
				Thread.sleep(1000 / super.transR);
			} catch (InterruptedException e) {
				
				return ;
				}
		}
	}

	@Override
	public void Client() throws InterruptedException {
		
	

		try {
			 //check if the sent message is empty 
			if (Message.length()== 0) {
				System.err.println(" The message is Empty.");
				System.exit(1);
			}else {
			
			// connect the socket to a local address. 
			socket.bind(super.localBindPoint);
			//connect to the server
			socket.connect(super.socketremote);

			}
		
		} catch (Exception e) {
			// Catch if the server is not running and exit from the system
			System.err.println("Server is not running");
			System.exit(1);
		}

			// a thread pool
		ExecutorService executor = Executors.newFixedThreadPool(1);
		                executor.submit(new Thread(this));
		               	executor.shutdown();

		
	    	//termination after one second 
		      executor.awaitTermination(T, TimeUnit.MILLISECONDS);
		     
		

			executor.shutdownNow();

			int leftM = (super.transR - sentM);
		
			System.out.println("The rest of left messages after running for one second: " + leftM);

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public void Messages() {
		try {
			String rMessage = "";

			
				// reading message, input
			inStream = new DataInputStream(socket.getInputStream());
				//output
			outStream = new DataOutputStream(socket.getOutputStream());
			
			//send the message
			outStream.write(Message.getBytes());


		 while (rMessage.length() < Message.length() ){ // run until get complete of the message
				super.buffer = new byte[super.bufSize];
					//
				rMessage += new String(super.buffer, 0, inStream.read(super.buffer));

			}
		 	st = rMessage.trim();
		 	
			} catch (Exception e) {
			return;
			
		}
	}
	
} 