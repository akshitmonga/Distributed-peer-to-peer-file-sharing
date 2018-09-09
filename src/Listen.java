import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class Listen implements Runnable {

	private Thread myThread;
	private ServerSocket ss;
	private String peerID;
	private Socket mySocket;
	
	public Listen(ServerSocket socket, String peerID) 
	{
		ss = socket;
		this.peerID = peerID;
	}
	
	public void run() {
		while(true)
		{
			try
			{
				mySocket = ss.accept();
				myThread = new Thread(new RemotePeerHandler(mySocket,0,peerID));
				ProcessPeer.displayLog(peerID + " Connection is established");
				ProcessPeer.Send_Thread.add(myThread);
				myThread.start(); 
			}
			catch(IOException e1)
			{
				ProcessPeer.displayLog(this.peerID + " Exception in connection: " + e1.toString());
			}
		}
	}
	
	public void closeConnection() {		
		try 
		{
			if(!mySocket.isClosed())
			mySocket.close();
		} 
		catch (IOException e2) 
		{
			e2.printStackTrace();
		}
	}
}


