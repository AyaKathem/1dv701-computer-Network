
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
public class TFTPServer {
	public static final int TFTPPORT = 4970;
	public static final int BUFSIZE = 516;
	public static final String READDIR = "C:/Users/AYA/eclipse-workspace/eclipse-workspace2/TFTPserver/read/"; //custom address at your PC
	public static final String WRITEDIR ="C:/Users/AYA/eclipse-workspace/eclipse-workspace2/TFTPserver/write/"; //custom address at your PC
	// OP codes
		public static final int OP_RRQ = 1;
		public static final int OP_WRQ = 2;
		public static final int OP_DAT = 3;
		public static final int OP_ACK = 4;
		public static final int OP_ERR = 5; 
	    private DatagramPacket getP;
	    private int triesCount2 =0;
	    private int triesCount1=0;
	    
	    //some website i get some help from 
	    //https://stackoverflow.com/questions/35940279/how-to-get-the-file-name-from-an-rrq-datagrampacket
	    //https://www.javatpoint.com/DatagramSocket-and-DatagramPacket
	    //https://www.youtube.com/watch?v=A5fFxs_DUsQ
	    //https://stackoverflow.com/questions/62214516/in-java-how-do-you-read-n-bits-from-a-byte-array
		public static void main(String[] args) {
			if (args.length > 0) {
				System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
				System.exit(1);
			}
			try {
				TFTPServer server= new TFTPServer();
				server.start();
			} catch (SocketException e) {
	            System.out.println("Error when starting up the server.");
	            e.printStackTrace();
			}
		}
		
		
		
		private void start() throws SocketException 
		{
			byte[] buf= new byte[BUFSIZE];
			
			// Create socket
			DatagramSocket socket= new DatagramSocket(null);
			
			// Create local bind point 
			SocketAddress localBindPoint= new InetSocketAddress(TFTPPORT);
			socket.bind(localBindPoint);

			System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

			// Loop to handle client requests 
			while (true) 
			{        
				
				final InetSocketAddress clientAddress = receiveFrom(socket, buf);
				
				// If clientAddress is null, an error occurred in receiveFrom()
				if (clientAddress == null) 
					continue;

				final StringBuffer requestedFile= new StringBuffer();
				final int reqtype = ParseRQ(buf, requestedFile);

				new Thread() 
				{
					public void run() 
					{
						try 
						{
							DatagramSocket sendSocket= new DatagramSocket(0);

							// Connect to client
							sendSocket.connect(clientAddress);						
							
							System.out.printf("%s request for %s from %s using port %d\n",
									(reqtype == OP_RRQ)?"Read":"Write",requestedFile.toString(),
									clientAddress.getHostName(), clientAddress.getPort());  
									
							// Read request
							if (reqtype == OP_RRQ) 
							{      
								requestedFile.insert(0, READDIR);
								HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
							}
							// Write request
							else 
							{                       
								requestedFile.insert(0, WRITEDIR);
								HandleRQ(sendSocket,requestedFile.toString(),OP_WRQ);  
							}
							sendSocket.close();
						} 
						catch (SocketException e) 
							{e.printStackTrace();}
					}
				}.start();
			}
		}
		
		/**
		 * Reads the first block of data, i.e., the request for an action (read or write).
		 * @param socket (socket to read from)
		 * @param buf (where to store the read data)
		 * @return socketAddress (the socket address of the client)
		 */
		private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {
			// Create datagram packet
			DatagramPacket data = new DatagramPacket(buf,buf.length);

			try{
				// Receive packet
				socket.receive(data);
			}catch (IOException e){
				e.printStackTrace();
			}
			// Get client address and port from the packet
			return  new InetSocketAddress(data.getAddress(),data.getPort());
		}

		
		
		
		
		/**
		 * Parses the request in buf to retrieve the type of request and requestedFile
		 * 
		 * @param buf (received request)
		 * @param requestedFile (name of file to read/write)
		 * @return opcode (request type: RRQ or WRQ)
		 */
		private int ParseRQ(byte[] buf, StringBuffer requestedFile) {
			
			 ByteBuffer wrap= ByteBuffer.wrap(buf);
			 byte[] index =Arrays.copyOfRange(buf, 2, buf.length);
			 InputStream in = new ByteArrayInputStream(index);
		     BufferedReader bf = new BufferedReader(new InputStreamReader(in));
		       
		     
		     try {
					String filename = bf.readLine();
				
			        bf.close();
			        in.close();
			        requestedFile.append(filename); 
			
		     } catch (IOException e) {
					System.err.println("ERROR ");
				}
		 
			return wrap.getShort();
		}

		
		/**
		 * Handles RRQ and WRQ requests 
		 * 
		 * @param sendSocket (socket used to send/receive packets)
		 * @param requestedFile (name of file to read/write)
		 * @param opcode (RRQ or WRQ)
		 */
		private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) {
			
			Path path = Paths.get(requestedFile.split("\0")[0]);
			byte[] buffer = new byte[BUFSIZE-4];
            boolean statuse= true ;

			if(opcode == OP_RRQ){ 			
					if (!path.toFile().exists()) {
						send_ERR( sendSocket, 1);
					}else if ( !path.toFile().canWrite() || !path.toFile().canRead())  {
						send_ERR( sendSocket, 2);			
					}
					
					
			try { 
					FileInputStream inStream = new FileInputStream(path.toFile());
					short blockCounter = 1;
					int count =0;
					
					// by using while loop we can handle multiple packet  
					while(true ) { 
					
						int StreamLength = inStream.read(buffer); //Store input stream bytes in a byte buffer
						count  = StreamLength%512;
						
						if (StreamLength == -1) {
							StreamLength=0;
						}
						 
							if ( triesCount1 <5 && triesCount2 <5  &&  statuse != false  ) {
									
							    ByteBuffer databuff = ByteBuffer.allocate(BUFSIZE+4 + OP_ERR);
			    	 			databuff.putShort( (short) OP_DAT );
			    	 			databuff.putShort((short) blockCounter);
							  
			    	 			databuff.put(buffer);  //Set the length of the data and send input stream data 
			                 
							    //add for 4 for the first 4 byte of the header, the rest is the payload
			                    statuse=send_DATA_receive_ACK(blockCounter++, sendSocket,new DatagramPacket(databuff.array(), 4+StreamLength));
			                     
			  
			                  //if an ACK not arrives after 6 tries, then give up or if it is the last packet. 
							}if ( triesCount1 >=5|| triesCount2 >= 5||statuse == false ||  count !=0) {
								   triesCount1=0;
								   triesCount2=0;
								   inStream.close();
								   sendSocket.close();
								   break;
							}
					}
					
					inStream.close();
					sendSocket.close();
				}catch (FileNotFoundException e) {
					send_ERR( sendSocket, 0);
				} catch (IOException e) {
					send_ERR( sendSocket, 0);
				}	
				}
			
			else if(opcode == OP_WRQ){
					
				boolean  WriteStatus=true;
					try {		
							short blockNum = 0;
						
						if (path.toFile().exists()) {
							send_ERR( sendSocket, 6);		
						}else {				
							FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());	
							int count =0;
							
						while (true) {	
							if (triesCount2 ==5|| triesCount1 ==5||WriteStatus == false ||  count != 0)  {
								   
								   fileOutputStream.close();      
								   triesCount1=0;
								   triesCount2=0;
				                   break;
								   
							   }
							if (triesCount2 <=5 && triesCount1 <=5 && WriteStatus != false) {
							  ByteBuffer bufPut = data( BUFSIZE,  blockNum++,  OP_ACK );
							  WriteStatus = receive_DATA_send_ACK(blockNum, sendSocket,bufPut );
								    	   
									   int packSize = this.getP.getLength() -4;
								       fileOutputStream.write(this.getP.getData(), OP_ACK, packSize);
								       fileOutputStream.flush();
								       count =packSize %512;              
								      
							 }
		              	
							} 
						fileOutputStream.close();
						sendSocket.close();
						}
					
					} catch(IOException e) {
						send_ERR( sendSocket, 0);
					
		            }
				}
			}
		/**
		To be implemented
		*/
	  
		   private boolean receive_DATA_send_ACK( short block,DatagramSocket data, ByteBuffer bufPut ){
		    	
		    	DatagramPacket Ack = new DatagramPacket(bufPut.array(), OP_ACK);
		        byte[] buffer = new byte[BUFSIZE];
		        DatagramPacket receivingPacket = new DatagramPacket(buffer, buffer.length);
		        int sum=0;
					
			try {
		             
						data.send(Ack);
		                //Thread.sleep(100);// it is easier to check errors, but it slow down the protocol
						data.setSoTimeout(1000);
						data.receive(receivingPacket);

						this.getP = receivingPacket;
						sum=  (buffer[2] << 8) | (buffer[3] & 0x00ff);
						if(block == sum){
							System.out.println("An Ack arrives successfully from packet" + sum);

						}else {// if a wrong ack arrives 
								if (triesCount1<6) {
									triesCount1++;
									receive_DATA_send_ACK(block, data,  bufPut);
								}else {
									send_ERR(data,0);
									return false;
								}
							    triesCount1=0;
							}				
		            }catch(IOException e) { // if the ack does not arrives 
		        	  
		        	   triesCount2++;
		        	   
						if (triesCount2<6) {
							
							System.out.println("ERROR, The ACK of: "+block+ " did not arrive, This is the try : "+ triesCount2 );
							receive_DATA_send_ACK( block, data,  bufPut );
					
						}else {		
						data.close(); 
						return false;
						}
		            }
		           
		              
		     
					return true;
		    }
		              
	    

	    
private boolean send_DATA_receive_ACK(short blockCounter, DatagramSocket socket, DatagramPacket packet) {
	
	byte[] buffer = new byte[BUFSIZE];
	DatagramPacket receivingPacket = new DatagramPacket(buffer, buffer.length);
	int sum=0;
		try {
           
            socket.send(packet);
           // Thread.sleep(100);       
           socket.setSoTimeout(1000);
            socket.receive(receivingPacket); 
            sum=  (buffer[2] << 8) | (buffer[3] & 0x00ff);
				if(blockCounter == sum){ 
					System.out.println("An Ack arrives successfully from packet" +sum);
					
				}else {
						if (triesCount1<6) {//if the received ack does not relate to the sent packet, try again
							triesCount1++;
							send_DATA_receive_ACK( blockCounter,socket, packet);
						}else {
							send_ERR(socket,0); //send an error to message to the client after 5 fail tries.    							
							return false;
						}
					    triesCount1=0;
					}
		
		} catch (IOException e) {
			triesCount2++;
			if (triesCount2<6) {
				
			System.out.println("Resend packet number "+blockCounter+ "  this the try number: "+ triesCount2);
			send_DATA_receive_ACK( blockCounter, socket, packet);
		
			}else {	
				socket.close();
			
				return false;
			}
			
		} 

	return true;
}
		   
		   private void send_ERR(DatagramSocket socket, int ErrorNum) { 
			   ArrayList<String> errorText = new ArrayList<String>( 
			            Arrays.asList("Not defined", 
			                          "File not found", 
			                          "Access violation",
			                          " Disk full or allocation exceeded",
			                          "Illegal TFTP operation",
			                          "Unknown transfer ID",
			                          "File already exists",
			                          "No such user")); 
			  
			   
			   
			   		String indexNum = errorText.get(ErrorNum);
			   		ByteBuffer setError = data( indexNum.length() , ErrorNum, OP_ERR );
			   		setError.put(errorText.get(ErrorNum).getBytes());				   
						   
			   	    DatagramPacket sendError = new DatagramPacket(setError.array(), setError.array().length);
				try {
					socket.send(sendError);
					
					socket.close();
				} catch (IOException e) {
					System.err.println("The client did not receive the error message! ");
				}
			
		   }
		    
		    
		    private ByteBuffer data(int datalength, int num, int openCode ) {
		    	
		    	 ByteBuffer databuff = ByteBuffer.allocate(datalength + OP_ERR);
		    	 			databuff.putShort((short) openCode);
		    	 			databuff.putShort((short) num);
		    	
		    	return databuff;
		    	
		    }
		    

	}
