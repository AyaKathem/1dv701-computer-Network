

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;


public class webMain {
	 protected static String path;

	   public static void main(String args[]) {
	        ServerSocket serverSocket = null;
	        Socket socket = null;
	        
	        
	        int port =Integer.parseInt(args[0]);
	            path =args[1];
	        
	        try {
	            serverSocket = new ServerSocket(port);
	        } catch (IOException e) {
	            e.printStackTrace();

	        }
	        while (true) {
	            try {
	                socket = serverSocket.accept();
	                new MyWebServer1(socket).start();
	            } catch (IOException e) {
	                System.out.println("I/O error: " + e);
	            }
	            // new thread for a client
	           
	        }
	    }
	  
	}