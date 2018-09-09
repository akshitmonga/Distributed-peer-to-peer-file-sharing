import java.net.*;
import java.io.*;
import java.util.*;


public class ProcessPeer implements MessageParameters
{	
	public int Listening_Port;  
	public static volatile Hashtable<String, Peer_Remote> R_Peer_Info = new Hashtable<String, Peer_Remote>();
	public int Pindex;	
	public static volatile Queue<ForMessageHandler> MessageHandlermsg = new LinkedList<ForMessageHandler>();
	public static Field_Bit Bit_Checking_Field = null;
	public static volatile Timer Pref_Time;  
	public static volatile Timer TimerUnchok;	
	public static volatile Hashtable<String, Peer_Remote> Unchoked_Neighb = new Hashtable<String, Peer_Remote>();

	public String PeerIp = null;
	public static String peerID; 
	public static Vector<Thread> Recv_Thread = new Vector<Thread>();
	public Thread Listening_Thread; 

	
	public static void Start_Pref_Neighbors() {
		Pref_Time = new Timer();
		Pref_Time.schedule(new pref_neighbour(),
				CommonConfig.unchokingInterval * 1000 * 0,
				CommonConfig.unchokingInterval * 1000);
	}
	
	public static Hashtable<String, Socket> Map_Pid = new Hashtable<String, Socket>();
	
	public ServerSocket Server_Socket = null;
	public static volatile Hashtable<String, Peer_Remote> preferedNeighbors = new Hashtable<String, Peer_Remote>();	
	
	public static Thread Msg_Processor;
	
	private static void send_unchoke_msg(Socket socket, String remotePeerID) {

		ActualMessage d = new ActualMessage(msgUnchoke);
		
		byte[] msgByte = ActualMessage.encodeActualMsg(d);
		displayLog(peerID + " is sending UNCHOKE message to remote Peer " + remotePeerID);
		data_transfer(socket, msgByte);

	}

	public static void displayLog(String message)
	{
		CreateLogFile.writeLog(Tool.getDateTime() + ": Peer " + message);
		System.out.println(Tool.getDateTime() + ": Peer " + message);
	}

	
	public static boolean checker = false;
	public static Vector<Thread> Send_Thread = new Vector<Thread>();
	
	
	public static synchronized void insertToMessageQueue(ForMessageHandler msg)
	{
		MessageHandlermsg.add(msg);
	}
	
		public static void read_peer_info()
	{
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			String st;
			int ck,it,comp;
			while ((st = in.readLine()) != null)
			{
				String[]args = st.trim().split("\\s+");
				
				String temp;
				int checkIfCompleted = Integer.parseInt(args[3]);
				temp = args[0];
				String peerID = temp;
				
				
				if(checkIfCompleted == 1)
				{	
					R_Peer_Info.get(peerID).isChoked = 0;
					ck = 0;
					R_Peer_Info.get(peerID).isInterested = 0;
					it=0;
					R_Peer_Info.get(peerID).isCompleted = 1;
					comp = 0;
				}
			}
			in.close();
		}
		catch (Exception e) {
			displayLog(peerID + e.toString());
		}
	}
	
	public static void Stoping_Pref_Neighbors() {
		Pref_Time.cancel();
	}
	
	public static void Info_Peer() {

		String st;

		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			int i = 0;
			while ((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				
				String tokens2;

				R_Peer_Info.put(tokens[0], new Peer_Remote(tokens[0],
						tokens[1], tokens[2], Integer.parseInt(tokens[3]), i));

				i++;
			}

			in.close();
		} catch (Exception ex) {
			displayLog(peerID + ex.toString());
		}
	}
	
	public static synchronized ForMessageHandler removeFromMsgQueue()
	{
		ForMessageHandler msg = null;
		
		if(!MessageHandlermsg.isEmpty())
		{
			msg = MessageHandlermsg.remove();
		}
		
		return msg;
	}


	public static class UnChokedNeighbors extends TimerTask {

		public void run() 
		{
			//updates remotePeerInfoHash
			read_peer_info();
			int ch,un,hv;
			if(!Unchoked_Neighb.isEmpty())
				Unchoked_Neighb.clear();
			
			Enumeration<String> keys = R_Peer_Info.keys();
			un=0;
			hv=0;
			Enumeration<String> key1 = keys;
			Vector<Peer_Remote> peers = new Vector<Peer_Remote>();
			ch =0;
			
			while(keys.hasMoreElements())
			{
				String key = (String)keys.nextElement();
				Peer_Remote pref = R_Peer_Info.get(key);

				Vector<Peer_Remote> peer2 = peers;
				
				if (pref.isChoked == 1 
						&& !key.equals(peerID) 
						&& pref.isCompleted == 0 
						&& pref.isHandShaked == 1)
					peers.add(pref);
					
			}
			
			// Randomize the vector elements 	
			if (peers.size() > 0)
			{
				Collections.shuffle(peers);
				Peer_Remote p = peers.firstElement();
				
				R_Peer_Info.get(p.peerId).isOptUnchokedNeighbor = 1;
				Unchoked_Neighb.put(p.peerId, R_Peer_Info.get(p.peerId));
				// LOG 4:
				ProcessPeer.displayLog(ProcessPeer.peerID + " has the optimistically unchoked neighbor " + p.peerId);
				
				if (R_Peer_Info.get(p.peerId).isChoked == 1)
				{
					ProcessPeer.R_Peer_Info.get(p.peerId).isChoked = 0;
					ch = 0;
					send_unchoke_msg(ProcessPeer.Map_Pid.get(p.peerId), p.peerId);
					un=1;
					sending_have_msg(ProcessPeer.Map_Pid.get(p.peerId), p.peerId);
					hv=1;
					ProcessPeer.R_Peer_Info.get(p.peerId).state = 3;
				}
			}
			
		}

	}


	public static void Stoping_Choked_Neigh() {
		Pref_Time.cancel();
	}
	
	private static void sending_have_msg(Socket socket, String peerIDofRemotePeer) {
		
		byte[] bitFieldEncoded = ProcessPeer.Bit_Checking_Field.encoding_Functions();
		ActualMessage d = new ActualMessage(msgHave, bitFieldEncoded);
		bitFieldEncoded = null;
		data_transfer(socket,ActualMessage.encodeActualMsg(d));
		displayLog(peerID + " sending HAVE message to Peer " + peerIDofRemotePeer);
	}

	public static class pref_neighbour extends TimerTask {

		public void run() 
		{						
			int interestedCount = 0;
			read_peer_info();
			int inter =0;
			int ch=0;
			
			Enumeration<String> keys = R_Peer_Info.keys();
			String key_1;
			
			while(keys.hasMoreElements())
			{
				String key = (String)keys.nextElement();
				key_1 = key;
				Peer_Remote pref = R_Peer_Info.get(key_1);
				//showLog("peer" + key + " isCompleted =" + pref.isCompleted + " isInterested =" + pref.isInterested + " isChoked =" + pref.isChoked);
				if(key_1.equals(peerID))continue;
				
				if (pref.isCompleted == 0 && pref.isHandShaked == 1)
				{
					inter++;
					interestedCount++;
				} 
				else if(pref.isCompleted == 1)
				{
					try
					{
						ch++;
						preferedNeighbors.remove(key);
					}
					catch (Exception e) {
					}
				}
			}
			String strPref = "";
			if(interestedCount > CommonConfig.numOfPreferredNeighbr)
			{
				if(!preferedNeighbors.isEmpty())
					preferedNeighbors.clear();
									
				int count = 0;
				List <Peer_Remote> pv = new ArrayList <Peer_Remote>(R_Peer_Info.values());
				Collections.sort(pv, new TransitRateCmp(false));
				
				for (int i = 0; i < pv.size(); i++) 
				{
					if (count > CommonConfig.numOfPreferredNeighbr - 1)
						break;
					if(pv.get(i).isHandShaked == 1 && !pv.get(i).peerId.equals(peerID) 
							&& R_Peer_Info.get(pv.get(i).peerId).isCompleted == 0)
					{
						R_Peer_Info.get(pv.get(i).peerId).isPreferredNeighbor = 1;
						preferedNeighbors.put(pv.get(i).peerId, R_Peer_Info.get(pv.get(i).peerId));
						
						count++;
						
						strPref = strPref + pv.get(i).peerId + ", ";
						//ProcessPeer.showLog(ProcessPeer.peerID + " Selected preferred neighbor is " + pv.get(i).peerId + " data rate - " + pv.get(i).RateOfData);
						
						if (R_Peer_Info.get(pv.get(i).peerId).isChoked == 1)
						{
							send_unchoke_msg(ProcessPeer.Map_Pid.get(pv.get(i).peerId), pv.get(i).peerId);
							ProcessPeer.R_Peer_Info.get(pv.get(i).peerId).isChoked = 0;
							sending_have_msg(ProcessPeer.Map_Pid.get(pv.get(i).peerId), pv.get(i).peerId);
							ProcessPeer.R_Peer_Info.get(pv.get(i).peerId).state = 3;
						}
						
						
					}
				}
			}
			else
			{
				keys = R_Peer_Info.keys();
				while(keys.hasMoreElements())
				{
					String key = (String)keys.nextElement();
					Peer_Remote pref = R_Peer_Info.get(key);
					if(key.equals(peerID)) continue;
					
					if (pref.isCompleted == 0 && pref.isHandShaked == 1)
					{
						if(!preferedNeighbors.containsKey(key))
						{
							strPref = strPref + key + ", ";
							preferedNeighbors.put(key, R_Peer_Info.get(key));
							R_Peer_Info.get(key).isPreferredNeighbor = 1;
						}
						if (pref.isChoked == 1)
						{
							send_unchoke_msg(ProcessPeer.Map_Pid.get(key), key);
							ProcessPeer.R_Peer_Info.get(key).state = 3;
							sending_have_msg(ProcessPeer.Map_Pid.get(key), key);
							ProcessPeer.R_Peer_Info.get(key).isChoked = 0;
						}
						
					} 
					
				}
			}
			if (strPref != "")
				ProcessPeer.displayLog(ProcessPeer.peerID + " has selected the preferred neighbors - " + strPref);
		}
	}
	
	private static int data_transfer(Socket socket, byte[] encodedBit) {
		try {
		OutputStream out = socket.getOutputStream();
		out.write(encodedBit);
		} catch (IOException e) {
			
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

		public static void Begin_Unchoked_Neighbors() 
	{
		Pref_Time = new Timer();
		Pref_Time.schedule(new UnChokedNeighbors(),
				CommonConfig.optUnchokingInterval * 1000 * 0,
				CommonConfig.optUnchokingInterval * 1000);
	}


	
	public static void Config_FileReader() {
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\\s+");
				if (tokens[0].equalsIgnoreCase("NumberOfPreferredNeighbors")) {
					CommonConfig.numOfPreferredNeighbr = Integer
							.parseInt(tokens[1]);
				} else if (tokens[0].equalsIgnoreCase("UnchokingInterval")) {
					CommonConfig.unchokingInterval = Integer
							.parseInt(tokens[1]);
				} else if (tokens[0]
						.equalsIgnoreCase("OptimisticUnchokingInterval")) {
					CommonConfig.optUnchokingInterval = Integer
							.parseInt(tokens[1]);
				} else if (tokens[0].equalsIgnoreCase("FileName")) {
					CommonConfig.fileName = tokens[1];
				} else if (tokens[0].equalsIgnoreCase("FileSize")) {
					CommonConfig.fileSize = Integer.parseInt(tokens[1]);
				} else if (tokens[0].equalsIgnoreCase("PieceSize")) {
					CommonConfig.pieceSize = Integer.parseInt(tokens[1]);
				}
			}

			in.close();
		} catch (Exception ex) {
			displayLog(peerID + ex.toString());
		}
	}

	@SuppressWarnings("deprecation") // to avoid the Xlint deprecation 
	
	
	public static void main(String[] args) 
	{
	ProcessPeer pProcess = new ProcessPeer();
	peerID = args[0];

		try
		{
			CreateLogFile.create("log_peer_" + peerID +".log");
			displayLog(peerID + " is started");
			Config_FileReader();
			Info_Peer();					
			init_prefNeighbours();
			boolean Firstpeer_Checker = false;
			Enumeration<String> Peer_enum = R_Peer_Info.keys();
			
			while(Peer_enum.hasMoreElements())
			{
				Peer_Remote pinfo = R_Peer_Info.get(Peer_enum.nextElement());
				if(pinfo.peerId.equals(peerID))
				{
					// checks if the peer is the first peer or not
					pProcess.Listening_Port = Integer.parseInt(pinfo.Port_Pnumber);
					pProcess.Pindex = pinfo.peerIndex;
					if(pinfo.Getting_Peer_First() == 1)
					{
						Firstpeer_Checker = true; //sets true if it's the first peer
						break;
					}
				}
			}
			
			// Initialize the Bit field class 
			Bit_Checking_Field = new Field_Bit();
			Bit_Checking_Field.bitfieldInit(peerID, Firstpeer_Checker?1:0);
			
			Msg_Processor = new Thread(new Logger(peerID));
			Msg_Processor.start();
			
			if(Firstpeer_Checker)
			{
				try
				{
					pProcess.Server_Socket = new ServerSocket(pProcess.Listening_Port);
					pProcess.Listening_Thread = new Thread(new Listen(pProcess.Server_Socket, peerID));
					pProcess.Listening_Thread.start();
				}
				catch(SocketTimeoutException tox)
				{
					displayLog(peerID + " gets time out expetion: " + tox.toString());
					CreateLogFile.end();
					System.exit(0);
				}
				catch(IOException ex)
				{
					displayLog(peerID + " gets exception in Starting Listening thread: " + pProcess.Listening_Port + ex.toString());
					CreateLogFile.end();
					System.exit(0);
				}
			}
			else
			{	
				Creating_Blank_File();
				
				Peer_enum = R_Peer_Info.keys();
				while(Peer_enum.hasMoreElements())
				{
					Peer_Remote pinfo = R_Peer_Info.get(Peer_enum.nextElement());
					if(pProcess.Pindex > pinfo.peerIndex)
					{
						Thread tempThread = new Thread(new RemotePeerHandler(
								pinfo.getPeerAddress(), Integer
										.parseInt(pinfo.Peer_Port_Getter()), 1,
								peerID));
						Recv_Thread.add(tempThread);
						tempThread.start();
					}
				}

				try
				{
					pProcess.Server_Socket = new ServerSocket(pProcess.Listening_Port);
					pProcess.Listening_Thread = new Thread(new Listen(pProcess.Server_Socket, peerID));
					pProcess.Listening_Thread.start();
				}
				catch(SocketTimeoutException tox)
				{
					displayLog(peerID + " gets time out exception in Starting the listening thread: " + tox.toString());
					CreateLogFile.end();
					System.exit(0);
				}
				catch(IOException ex)
				{
					displayLog(peerID + " gets exception in Starting the listening thread: " + pProcess.Listening_Port + " "+ ex.toString());
					CreateLogFile.end();
					System.exit(0);
				}
			}
			
			Start_Pref_Neighbors();
			Begin_Unchoked_Neighbors();
			
			while(true)
			{
				// checks for termination
				checker = isFinished();
				if (checker) {
					displayLog("All peers have completed downloading the file.");

					Stoping_Pref_Neighbors();
					Stoping_Choked_Neigh();

					try {
						Thread.currentThread();
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
					}

					if (pProcess.Listening_Thread.isAlive())
						pProcess.Listening_Thread.stop();

					if (Msg_Processor.isAlive())
						Msg_Processor.stop();

					for (int i = 0; i < Recv_Thread.size(); i++)
						if (Recv_Thread.get(i).isAlive())
							Recv_Thread.get(i).stop();

					for (int i = 0; i < Send_Thread.size(); i++)
						if (Send_Thread.get(i).isAlive())
							Send_Thread.get(i).stop();

					break;
				} else {
					try {
						Thread.currentThread();
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
					}
				}
			}
		}
		catch(Exception ex)
		{
			displayLog(peerID + " Exception in ending : " + ex.getMessage() );
		}
		finally
		{
			displayLog(peerID + " Peer process is exiting..");
			CreateLogFile.end();
			System.exit(0);
		}
	}

	public static void Creating_Blank_File() {
		
		try {
			File dir = new File(peerID);
			dir.mkdir();

			File filenew = new File(peerID, CommonConfig.fileName);
			OutputStream os = new FileOutputStream(filenew, true);
			byte b = 0;
			
			for (int i = 0; i < CommonConfig.fileSize; i++)
				os.write(b);
			os.close();
		} 
		catch (Exception e) {
			displayLog(peerID + " ERROR in creating the file : " + e.getMessage());
		}

	}
	
	private static void init_prefNeighbours() 
	{
		Enumeration<String> keys = R_Peer_Info.keys();
		while(keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			if(!key.equals(peerID))
			{
				preferedNeighbors.put(key, R_Peer_Info.get(key));		
			}
		}
	}

	/**
	 * Checks if all peer have indeed downloaded the file
	 */
	public static synchronized boolean isFinished() {

		String line;
		int hasFileCount = 1;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));

			while ((line = in.readLine()) != null) {
				hasFileCount = hasFileCount
						* Integer.parseInt(line.trim().split("\\s+")[3]);
			}
			if (hasFileCount == 0) {
				in.close();
				return false;
			} else {
				in.close();
				return true;
			}

		} catch (Exception e) {
			displayLog(e.toString());
			return false;
		}

	}
	
}
