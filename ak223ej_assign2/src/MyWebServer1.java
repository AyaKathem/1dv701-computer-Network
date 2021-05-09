import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class MyWebServer1 extends Thread {
    protected Socket socket;
    private String getRequst;
    private String getPath;
    private  String ResponseStatus ;
    private  String sourcePath=webMain.path+"web_source/" ;
	private String RequestType;
	private boolean error500 = false;//set to true to check 500 Internal server error 
    // websites that i get some help from 
    // http://bethecoder.com/applications/tutorials/java/socket-programming/simple-web-server.html
    
    
    public MyWebServer1(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
   
    	
        while (true) {
            try {
            	if ( error500 == true) {
            		throw new IllegalArgumentException();
            		
            	}
             	readClientRequest(socket);
            	handelPath();         
            } catch (IOException e) {
               System.out.println("Socket is closed");
                return;
            }catch (IllegalArgumentException  | SecurityException e) {
    			
            	StatusCode(500);
                handleRespone();
                System.out.println(e);
                break; ///////
                
            
    		}
           
         }
     
    }
  
 protected void readClientRequest(Socket client) throws IOException {   
	 
	    BufferedReader in = new BufferedReader( new InputStreamReader(client.getInputStream()));
	    String readrequst = in.readLine();
	    getRequst = readrequst.split(" ")[0];
	    getPath = readrequst.split(" ")[1]; 
	    
	    for (int i = 0; i< readrequst.split(" ").length ; i++ ) {
	    	
	    	System.out.println(readrequst.split(" ")[i]);
	    
	    }
	    
	  }
 
 
 
   
	  protected void writeClientResponse(Socket client, File file) throws IOException  {
		  
		  
	try { 
			
		DataOutputStream outToClient =  new DataOutputStream(client.getOutputStream());
		 
            int numOfBytes =  (int) file.length();
           
            FileInputStream inFile  = new FileInputStream (file.getAbsoluteFile());

            byte[] fileInBytes = new byte[numOfBytes];
            inFile.read(fileInBytes);
            outToClient.writeBytes( Status()+"\n" );
            outToClient.writeBytes("Date: " + new Date()+" \n");
            outToClient.writeBytes("Content-Type: " +RequestType(file)+" \n" );
            outToClient.writeBytes("Content-Length: " + fileInBytes.length +" \n"  );       
            outToClient.writeBytes("\r\n");
            outToClient.write(fileInBytes, 0, numOfBytes);
            outToClient.flush();
            client.close();

		} catch (IOException e) {	
		
			client.close();
		}
   
	  }
	 
	  private void handelPath() throws IOException {
		
		  
		  File file ;
		 if (getPath.endsWith("/")) {
				 getPath.substring(0, getPath.length() - 1);	 
		 } 
		 
		 if (getPath.endsWith("demo.html")) {
			 StatusCode(302);
			 handleRespone();
	       }
		 if (getPath.endsWith("admin.html")) {
				 StatusCode(403);
				 handleRespone();
		     }
		 if (getPath.endsWith("png") || getPath.endsWith("jpg")) {
			  file = new File (sourcePath+getPath);
			  
			  
			  if (file.getParentFile().exists()) {
				  file = new File (sourcePath+"img/"+ file.getName());
			  }
		  }else {
			  file = new File (sourcePath+getPath); 
		 
		  }
		 
		  Boolean fileExist=false;
		   
		   if (file.isDirectory() ) {
			  String [] list =  file.list();
			  for(String list2: list ) {
				 
				 if (list2.equals("index.htm") ||list2.equals("index.html")) {
					
					 getPath += "/"+list2;
					 file = new File (sourcePath+getPath); 
					 fileExist=true;	 
				 } 
			 }
			  
			     if(fileExist.equals(false)){
				   System.out.println(fileExist);
				   StatusCode(404);
				   handleRespone();
			    }
			  
		   }else if (!file.exists()) {
			   
			   StatusCode(404);
			   handleRespone();
		   }
		 
	         System.out.println(file.getAbsolutePath());
	         StatusCode(200);
	         writeClientResponse(socket, file);   
		   
	  } 
	 
	  
	 
	  private String RequestType(File fileType) {
		    String type =fileType.getName();
		    type.toLowerCase();
			if (type.endsWith(".html") || type.endsWith(".htm"))
				RequestType = "text/html";
			else if (type.endsWith(".png"))
				RequestType = "image/png";
			else if (type.endsWith(".css"))
				RequestType = "text/css";
			else if ( type.endsWith(".jpg"))
				RequestType = "image/jpeg";
			return RequestType;
			
		}
	  private void StatusCode(int num ) {
		 
		  switch (num){
			  case 200:
				  ResponseStatus = "HTTP/1.0 200  OK";
				  break;	  
			  case 404:
				  ResponseStatus= "HTTP/1.0 404 Not Found"; 
				  break;	  
			  case 403:
				  ResponseStatus= "HTTP/1.0 403 Forbidden";
				  break;	  
			  case 302:
				  ResponseStatus = "HTTP/1.0 302 Found";
				  break;
			  case 500:
				  ResponseStatus = "HTTP/1.0 500 Internal Server Error";
				  break;
		  }  	
		
	  }
	  
	  private void handleRespone() {
		  
		    PrintWriter out;
			try {
				
						out = new PrintWriter(socket.getOutputStream());
						out.println(Status() );
						out.println("Date: " + new Date() );
						out.println("Server: SimpleWebServer ");
						out.println("Content-Type: text/html ");
						 if(Status().split(" ")[1].equals("302")) {
							    out.println("Location: "+ "/NEW/n.html"); 
					           }  					    
					 // out.println("Connection: close");
					   	out.println("\r\n");
					  	out.println(Status() );
					    out.flush();
					    socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }


	
	  
	  private String Status() {
		  return ResponseStatus;
	  }
	  
 }
