/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

package labb2;




import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;




public class UDPEchoClient extends NetworkLayer {
	

	protected DatagramSocket socket;
	protected DatagramPacket sPacket;
	protected DatagramPacket rPacket;
	protected String Messege = "aya kathem message ";
	protected int sMessages = 0;
	protected int T = 994;
	
	public static void main(String[] args) throws InterruptedException {
		
		// you can you use Command line to insert args or you can use the below String array
		
	/*	String[] args1 = new String[4];
		args1[0]= "192.168.56.101"; 
		args1[1]= "4950";
		args1[2]= "64";
		args1[3]= "5";*/
		UDPEchoClient runClient = new UDPEchoClient(args);

		runClient.Client();
}
	public UDPEchoClient(String[] args) {
       if (!args[0].isEmpty()  && !args[1].isEmpty()  && !args[2].isEmpty()  && !args[3].isEmpty() ) {
			
			
			// get Port , Ip , buffersizea and  transfer rate
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
		
	    sMessages = 0;
		while(sMessages < super.transR) {
					sMessages++;
			try {
				
				socket.send(sPacket);

				
				socket.receive(rPacket);

				String me = new String(rPacket.getData(), rPacket.getOffset(), rPacket.getLength());
				int receivedLength = Messege.length();
				//Check if the received message is equal to the sent message

				if (receivedLength ==  me.trim().length())
					System.out.printf(receivedLength+" bytes has been sent and received"+me.trim().length()+" with Buffer Size = " + bufSize + "\n" , me.trim().length());
				else if (receivedLength != me.trim().length())
					System.out.println( Messege.length() + " bytes has been sent -The received " + me.trim().length()
							+ " bytes which is not equal! [Buffer Size = " + bufSize + "]");
			} catch (IOException e) {
				return;
				
			}
			try {
				Thread.sleep(1000 / super.transR);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block

				return ;		
				}
		}
	}


	@Override
	public void Client() {
		

		try {
			
			 if (Messege.length() == 0) {
				System.err.println( "Empty message");
				System.exit(1);
			}else if (Messege.length() > 65507) {
				System.err.println("The message size greater then UDP packet size");
				System.exit(1);
				 //check if the sent message is empty 
			}else {
			socket = new DatagramSocket(null);
			// connect the socket to a local address. 
			socket.bind(super.localBindPoint);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		
		// UDP datagram to sent message
		sPacket = new DatagramPacket(Messege.toString().getBytes(), Messege.length(), super.socketremote);
			
		// UDP datagram to receiving message
		rPacket = new DatagramPacket(super.buffer, super.buffer.length);

		// a thread pool
		ExecutorService executor = Executors.newFixedThreadPool(1);

		executor.submit(new Thread(this));

		executor.shutdown();

		try {
			
			//termination after one second 
			executor.awaitTermination(T, TimeUnit.MILLISECONDS);
		
			executor.shutdownNow();
		    socket.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int  leftM= (transR - sMessages);
	
		System.out.println("The rest of left messages after running for one second: " + leftM );

	}

	

}