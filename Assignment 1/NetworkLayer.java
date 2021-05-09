import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class NetworkLayer implements Runnable {


	public int bufSize = 1024;
	public int transR;
	public String IP;
	public int port = 4950;
	public byte[] buffer;
	public StringBuilder sb=new StringBuilder("");  
	public SocketAddress socketremote;
	public final int MYPORT = 0;
	//Local point
	public InetSocketAddress localBindPoint = new InetSocketAddress(MYPORT);
	

	
	public abstract void Client() throws InterruptedException;


	protected void checkInput() {
		
			

			// Check port
			if (port > 65535 || port < 1) {
				System.err.println("Error: The port number have to be between (1-65535)");
				System.exit(1);
			}else if (bufSize < 1) { // if the buffer size less than one
				System.err.println("Error: The buffer size is not correct.");
				System.exit(1);
			}else if (transR < 0) { // transfer rate less than 0 
				System.err.println("Error: The transfer rate time is not correct.");
				System.exit(1);
			}

		
		try {
			buffer = new byte[bufSize];
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}


	
	public boolean checkIP(String ip) {
		String[] splitIP = ip.split("\\.");
		if (splitIP.length != 4) {
			System.err.println("Error:The IP Should look like ex, [192.168.56.101]");
			System.exit(1);
			
		}
		for (String str : splitIP) { // check if every part of IP is between 0 and 255s
			int num = Integer.parseInt(str);
			if ((num<0)||(num>255)) {
				System.err.println("Error:The IP Should not be greater than 255 , or less than 0");
				System.exit(1);
				
			}
		}
		return true;
	}
}
