import java.net.*;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;


public class RemotePeerHandler implements Runnable, MessageParameters 
{
	
	/* variable declaration for the class*/
	private InputStream in;
	private int connectionType;
	private Socket Peer_Socket = null; // peer Socket
	private Handshake Msg_Handshake; //handshake message of type Handshake
	
	String ownPeerId, rPeerId;
	
	final int connectionActive = 1;
	
	private OutputStream out;
	final int connectionPassive = 0;
	
	public RemotePeerHandler(String add, int port, int connType, String ownPeerID) 
	{	
		try 
		{
			this.connectionType = connType;
			this.ownPeerId = ownPeerID;
			//ProcessPeer.showLog(ownPeerId + " Receiving Port = " + port + " Address = "+ add);
			this.Peer_Socket = new Socket(add, port);			
		} 
		catch (UnknownHostException e) 
		{
			ProcessPeer.displayLog(ownPeerID + " RemotePeerHandler : " + e.getMessage());
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(ownPeerID + " RemotePeerHandler : " + e.getMessage());
		}
		this.connectionType = connType;
		
		try 
		{
			in = Peer_Socket.getInputStream();
			out = Peer_Socket.getOutputStream();
		} 
		catch (Exception ex) 
		{
			ProcessPeer.displayLog(ownPeerID + " RemotePeerHandler : " + ex.getMessage());
		}
	}
	
	public boolean handshakeSender() 
	{
		try 
		{
			out.write(Handshake.encodeHandshakeMessage(new Handshake(MessageParameters.handshakeHeaderString, this.ownPeerId)));
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " SendHandshake : " + e.getMessage());
			return false;
		}
		return true;
	}

	 public int recEditDistance(char[]  str1, char str2[], int len1,int len2){
        
        if(len1 == str1.length){
            return str2.length - len2;
        }
        if(len2 == str2.length){
            return str1.length - len1;
        }
		return 1;
    }
	
	public boolean handshakeReceiver() 
	{
		byte[] handshakeByteReceiver = new byte[32];
		try 
		{
			in.read(handshakeByteReceiver);
			Msg_Handshake = Handshake.decodeHandshankeMessage(handshakeByteReceiver);
			rPeerId = Msg_Handshake.getPeerIDString();
			ProcessPeer.Map_Pid.put(rPeerId, this.Peer_Socket);
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " ReceiveHandshake : " + e.getMessage());
			return false;
		}
		return true;
	}		
	
	public boolean SendRequest(int index)
	{
		try 
		{
			out.write(ActualMessage.encodeActualMsg(new ActualMessage( msgRequest, Tool.intToByteArray(index))));
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " SendRequest : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public void openClose(InputStream i, Socket socket)
	{
		try {
			i.close();
			i = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean ReceiveUnchoke()
	{
		byte [] receiveUnchokeByte = null;
		
		try 
		{
			in.read(receiveUnchokeByte);
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " ReceiveUnchoke : " + e.getMessage());
			return false;
		}
				
		ActualMessage m = ActualMessage.decodeActualMsg(receiveUnchokeByte);
		if(m.getMessageTypeString().equals(msgUnchoke))
		{
			ProcessPeer.displayLog(ownPeerId + "is unchoked by " + rPeerId);
			return true;
		}
		else 
			return false;
	}
	public void run() 
	{	
		byte []bufferForHandshake = new byte[32];
		byte []bufferForDataWithoutPayload = new byte[actualMsgLen + actualMsgTypeLen];
		String str;
		int ch=0;
		int unchoke=0;
		byte[] lengthOfMessage;
		int flag=1;
		byte[] typeOfMessage;
		ForMessageHandler wrapperForDataMessage = new ForMessageHandler();

		byte [] b1;
		try
		{
			if(this.connectionType == connectionActive)
			{
				if(!handshakeSender())
				{
					ProcessPeer.displayLog(ownPeerId + " HANDSHAKE sending failed.");
					System.exit(0);
				}
				else
				{
					ch++;
					ProcessPeer.displayLog(ownPeerId + " HANDSHAKE has been sent...");
				}
				while(true)
				{
					in.read(bufferForHandshake);
					str = "";
					Msg_Handshake = Handshake.decodeHandshankeMessage(bufferForHandshake);
					if(Msg_Handshake.getHeaderString().equals(MessageParameters.handshakeHeaderString))
					{					
						rPeerId = Msg_Handshake.getPeerIDString();
	
						ProcessPeer.displayLog(ownPeerId + " makes a connection to Peer " + rPeerId);
						unchoke++;
						ProcessPeer.displayLog(ownPeerId + " Received a HANDSHAKE message from Peer " + rPeerId);

						ProcessPeer.Map_Pid.put(rPeerId, this.Peer_Socket);
						break;
					}
					else
					{
						continue;
					}		
				}
				
				// send the bitfield
				ActualMessage dummyMessage = new ActualMessage(msgBitField, ProcessPeer.Bit_Checking_Field.encoding_Functions());
				byte  []b = ActualMessage.encodeActualMsg(dummyMessage);  
				b1=b;
				flag=0;
				out.write(b);
				
				ProcessPeer.R_Peer_Info.get(rPeerId).state = 8;
				
			}

			else
			{
				while(true)
				{
					in.read(bufferForHandshake);
					int PeervaluefromInfo=0;
					Msg_Handshake = Handshake.decodeHandshankeMessage(bufferForHandshake);
					if(Msg_Handshake.getHeaderString().equals(MessageParameters.handshakeHeaderString))
					{
						rPeerId = Msg_Handshake.getPeerIDString();
						String rttr=" ";
						ProcessPeer.displayLog(ownPeerId + " makes a connection to Peer " + rPeerId);
						PeervaluefromInfo=1;
						
						ProcessPeer.displayLog(ownPeerId + " Received a HANDSHAKE message from Peer " + rPeerId);
	
						ProcessPeer.Map_Pid.put(rPeerId, this.Peer_Socket);
						break;
					}
					else
					{
						continue;
					}		
				}
				if(!handshakeSender())
				{
					ProcessPeer.displayLog(ownPeerId + " HANDSHAKE message sending failed.");
					System.exit(0);
				}
				else
				{
					ProcessPeer.displayLog(ownPeerId + " HANDSHAKE message has been sent successfully.");
				}
				
				ProcessPeer.R_Peer_Info.get(rPeerId).state = 2;
			}

			while(true)
			{
				
				int headerBytes = in.read(bufferForDataWithoutPayload);
				
				if(headerBytes == -1)
					break;

				lengthOfMessage = new byte[actualMsgLen];
				typeOfMessage = new byte[actualMsgTypeLen];
	
				System.arraycopy(bufferForDataWithoutPayload, 0, lengthOfMessage, 0, actualMsgLen);
	
				System.arraycopy(bufferForDataWithoutPayload, actualMsgLen, typeOfMessage, 0, actualMsgTypeLen);
				
				ActualMessage dataMessage = new ActualMessage();
				dataMessage.setMsgLen(lengthOfMessage);
				dataMessage.setMsgType(typeOfMessage);
				

				if(dataMessage.getMessageTypeString().equals(MessageParameters.msgChoke)
						||dataMessage.getMessageTypeString().equals(MessageParameters.msgUnchoke)
						||dataMessage.getMessageTypeString().equals(MessageParameters.msgInterested)
						||dataMessage.getMessageTypeString().equals(MessageParameters.msgNotInterested))
				{
					if(dataMessage.getMessageTypeString().equals(MessageParameters.msgChoke))
							{
								//do nothing.
							}
					wrapperForDataMessage.actualMsg = dataMessage;
					wrapperForDataMessage.senderPeerID = this.rPeerId;
	
					ProcessPeer.insertToMessageQueue(wrapperForDataMessage);
				}
				else 
				{
					int alreadyReadBytes = 0;
					int bytesRead;
					byte []bufferDataPayload = new byte[dataMessage.getMsgLenInt()-1];
					while(alreadyReadBytes < dataMessage.getMsgLenInt()-1)
					{
						bytesRead = in.read(bufferDataPayload, alreadyReadBytes, dataMessage.getMsgLenInt()-1-alreadyReadBytes);
						if(bytesRead == -1)
							return;
						alreadyReadBytes += bytesRead;
					}

					byte []dataBuffWithPayload = new byte [dataMessage.getMsgLenInt()+actualMsgLen];
					System.arraycopy(bufferForDataWithoutPayload, 0, dataBuffWithPayload, 0, actualMsgLen + actualMsgTypeLen);
					System.arraycopy(bufferDataPayload, 0, dataBuffWithPayload, actualMsgLen + actualMsgTypeLen, bufferDataPayload.length);

					ActualMessage dataMsgWithPayload = ActualMessage.decodeActualMsg(dataBuffWithPayload);
					wrapperForDataMessage.actualMsg = dataMsgWithPayload;
					wrapperForDataMessage.senderPeerID = rPeerId;
					ProcessPeer.insertToMessageQueue(wrapperForDataMessage);

					dataBuffWithPayload = null;
					alreadyReadBytes = 0;
					bytesRead = 0;
					
					bufferDataPayload = null;
				}
			}
		}
		catch(IOException e)
		{
			//display exception
			ProcessPeer.displayLog(ownPeerId + " run exception: " + e);
		}	
		
	}
	
	public boolean SendNotInterested()
	{
		try 
		{
			out.write(ActualMessage.encodeActualMsg(new ActualMessage( msgNotInterested)));
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " SendNotInterested : " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean ReceiveChoke()
	{
		ActualMessage m2;
		byte [] receiveChokeByte = null;
		try 
		{
			if(in.available() == 0) return false;
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " ReceiveChoke : " + e.getMessage());
			return false;
		}
		
		try 
		{
			in.read(receiveChokeByte);
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " ReceiveChoke : " + e.getMessage());
			return false;
		}
		
		ActualMessage m = ActualMessage.decodeActualMsg(receiveChokeByte);
		m2=m;
		if(m.getMessageTypeString().equals(msgChoke))
		{
			// LOG 6:
			ProcessPeer.displayLog(ownPeerId + " is CHOKED by " + rPeerId);
			return true;
		}
		else 
			return false;
	}
	
	public void writeLog()
	{
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
		
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
	}
	
	public boolean receivePeice()
	{
		byte [] receivePeice = null;
		
		try 
		{
			in.read(receivePeice);
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " receivePeice : " + e.getMessage());
			return false;
		}
				
		ActualMessage m = ActualMessage.decodeActualMsg(receivePeice);
		if(m.getMessageTypeString().equals(msgUnchoke))
		{	
			// LOG 5:
			ProcessPeer.displayLog(ownPeerId + " is UNCHOKED by " + rPeerId);
			return true;
		}
		else 
			return false;

	}
	
	public RemotePeerHandler(Socket peerSocket, int connType, String ownPeerID) {
		
		this.ownPeerId = ownPeerID;
		int con;
		con = connType;
		String stp;
		stp = ownPeerID;
		this.connectionType = con;
		this.Peer_Socket = peerSocket;
		
		try 
		{
			in = peerSocket.getInputStream();
			out = peerSocket.getOutputStream();
		} 
		catch (Exception ex) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " Error : " + ex.getMessage());
		}
	}
	
	public void logWriter()
	{
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
		System.out.println(ownPeerId+ " starts listening to port " + rPeerId);
		
		FileHandler fhd = new FileHandler();
		byte[] byteIndex = new byte[MessageParameters.pieceIndexLen];

		fhd.pieceIndex = Tool.byteArrayToInt(byteIndex);

	}
	
	public void releaseSocket() {
		try {
			if (this.connectionType == connectionPassive && this.Peer_Socket != null) {
				this.Peer_Socket.close();
			}
			if (in != null) {
				in.close();
			}
			if (out != null)
				out.close();
		} catch (IOException e) {
			ProcessPeer.displayLog(ownPeerId + " Release socket IO exception: " + e);
		}
	}
	
	public boolean SendInterested()
	{
		try 
		{
			out.write(ActualMessage.encodeActualMsg(new ActualMessage(msgInterested)));
		} 
		catch (IOException e) 
		{
			ProcessPeer.displayLog(this.ownPeerId + " SendInterested : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	

}