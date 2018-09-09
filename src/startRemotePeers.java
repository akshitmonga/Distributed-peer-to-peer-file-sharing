import java.io.*;
import java.util.*;

public class startRemotePeers 
{
	public Vector<Process> peerProcesses = new Vector<Process>();
	public Vector<Peer_Remote> peerVector = new Vector<Peer_Remote>();
		
	
	public static synchronized boolean isFinished() {

		String line;
		int checkFileCount = 1;
		
		try {
			BufferedReader input = new BufferedReader(new FileReader("PeerInfo.cfg"));

			while ((line = input.readLine()) != null) {
				checkFileCount = checkFileCount
						* Integer.parseInt(line.trim().split("\\s+")[3]);
			}
			if (checkFileCount == 0) {
				input.close();
				return false;
			} else {
				input.close();
				return true;
			}

		} catch (Exception e) {
			
			return false;
		}

	}
	
	public void returnConf()
	{
		String st;
		try 
		{
			BufferedReader input = new BufferedReader(new FileReader("PeerInfo.cfg"));
			int i =0;
			while((st = input.readLine()) != null) 
			{	
				 String[] tokens = st.split("\\s+");
		         peerVector.addElement(new Peer_Remote(tokens[0], tokens[1], tokens[2], i));
		         i++;
			}
			
			input.close();
		}
		catch (Exception ex) 
		{
			System.out.println("EXception:" +ex.toString());
		}
	}

	public static void main(String[] args) 
	{
		try 
		{
			startRemotePeers startRemotePeersObject = new startRemotePeers();
			startRemotePeersObject.returnConf();
					

			String filepath = System.getProperty("user.dir");
						
			for (int i = 0; i < startRemotePeersObject.peerVector.size(); i++) 
			{
				Peer_Remote pInfo = (Peer_Remote) startRemotePeersObject.peerVector.elementAt(i);
				
				System.out.println("Start remote peer " + pInfo.peerID +  " at " + pInfo.Addingpeer );
				String command = "ssh " + pInfo.Addingpeer + " cd " + filepath + "; java ProcessPeer " + pInfo.peerID; 
				startRemotePeersObject.peerProcesses.add(Runtime.getRuntime().exec(command));
			}		
				
			System.out.println("Waiting for remote peers to terminate.." );
			
			boolean isFinished = false;
			while(true)
			{
				// checks for termination
				isFinished = isFinished();
				if (isFinished) 
				{
					System.out.println("All peers are terminated!");
					break;
				}
				else
				{
					try {
						Thread.currentThread();
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
					}
				}
			}
			
		}
		catch (Exception ex) 
		{
			System.out.println("Exception: "+ex.toString());
		}
	}
	

	
}