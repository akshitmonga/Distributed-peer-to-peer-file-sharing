import java.io.*;
import java.net.Socket;
import java.util.*;

public class Logger implements Runnable, MessageParameters 
{
	public static int peerState = -1;
	RandomAccessFile f;
	
	int handshaketrue=0;
	private static String senderPeerID = null;
	
	// constructors
	public Logger() {}
	
	public Logger(String inPeerID) {
		senderPeerID = inPeerID;
	}

	private void msgRequest(Socket socket, int peiceInd, String remotePeerID) {

		byte[] pieceIndexByte = new byte[MessageParameters.pieceIndexLen];
		for (int i = 0; i < MessageParameters.pieceIndexLen; i++) {
			pieceIndexByte[i] = 0;
		}
		
		byte[] pieceIndexByteArray = Tool.intToByteArray(peiceInd);
		System.arraycopy(pieceIndexByteArray, 0, pieceIndexByte, 0, pieceIndexByteArray.length);
		ActualMessage attr = new ActualMessage(msgRequest, pieceIndexByte);
		byte[] new_byt = ActualMessage.encodeActualMsg(attr);
		sendData(socket, new_byt);

		pieceIndexByte = null;
		pieceIndexByteArray = null;
		new_byt = null;
		attr = null;
	}
	
	private void msgBitField(Socket socket, String remotePeerID) {
		
		ProcessPeer.displayLog(ProcessPeer.peerID + " sending BITFIELD message to Peer " + remotePeerID);
		byte[] encodedBitField = ProcessPeer.Bit_Checking_Field.encoding_Functions();

		ActualMessage attr = new ActualMessage(msgBitField, encodedBitField);
		sendData(socket,ActualMessage.encodeActualMsg(attr));
		
		encodedBitField = null;
	}
	
	private void msgNotInterested(Socket socket, String remotePeerID) 
	{
		ProcessPeer.displayLog(ProcessPeer.peerID + " sending a NOT INTERESTED message to Peer " + remotePeerID);
		ActualMessage attr =  new ActualMessage(msgNotInterested);
		byte[] msgByte = ActualMessage.encodeActualMsg(attr);
		sendData(socket,msgByte);
	}

	private void msgInterested(Socket socket, String remotePeerID) {
		ProcessPeer.displayLog(ProcessPeer.peerID + " sending an INTERESTED message to Peer " + remotePeerID);
		ActualMessage attr =  new ActualMessage(msgInterested);
		byte[] msgByte = ActualMessage.encodeActualMsg(attr);
		sendData(socket,msgByte);
		
	}
		
	private void msgRequestPeice(Socket socket, ActualMessage attr, String remotePeerID)  
	{
		byte[] bytePieceIndex = attr.getPayload();
		
		int pieceIndex = Tool.byteArrayToInt(bytePieceIndex);
		
		ProcessPeer.displayLog(ProcessPeer.peerID + " sending a PIECE message for piece " + pieceIndex + " to Peer " + remotePeerID);
		
		byte[] bytesSize = new byte[CommonConfig.pieceSize];
		int byteFlag = 0;
		
		File file = new File(ProcessPeer.peerID,CommonConfig.fileName);
		try {
			f = new RandomAccessFile(file,"r");
			f.seek(pieceIndex*CommonConfig.pieceSize);
			byteFlag = f.read(bytesSize, 0, CommonConfig.pieceSize);
		} 
		catch (IOException e) {
			ProcessPeer.displayLog(ProcessPeer.peerID + " ERROR: File can not be read properly!" +  e.toString());
		}
		if( byteFlag == 0) {
			ProcessPeer.displayLog(ProcessPeer.peerID + " ERROR :  Zero bytes read from the file!");
		}
		else if (byteFlag < 0) {
			ProcessPeer.displayLog(ProcessPeer.peerID + " ERROR : File could not be read properly!");
		}
		
		byte[] buffer = new byte[byteFlag + MessageParameters.pieceIndexLen];
		System.arraycopy(bytePieceIndex, 0, buffer, 0, MessageParameters.pieceIndexLen);
		System.arraycopy(bytesSize, 0, buffer, MessageParameters.pieceIndexLen, byteFlag);

		ActualMessage sendMessage = new ActualMessage(msgPiece, buffer);
		byte[] b =  ActualMessage.encodeActualMsg(sendMessage);
		sendData(socket, b);
		
		b = null;
		bytePieceIndex = null;
		sendMessage = null;
	
		buffer = null;
		bytesSize = null;

		
		try {
			f.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
		
	private boolean msgIsInterested(ActualMessage attr, String receiverPeerId) {

		Field_Bit bf = Field_Bit.decode(attr.getPayload());
		ProcessPeer.R_Peer_Info.get(receiverPeerId).bitField = bf;
		
		if(ProcessPeer.Bit_Checking_Field.Comparing_Pieces(bf))
			return true;
		return false;
	}

	private void msgUnChoke(Socket socket, String remotePeerID) {

		ProcessPeer.displayLog(ProcessPeer.peerID + " sending UNCHOKE message to Peer " + remotePeerID);
		ActualMessage d = new ActualMessage(msgUnchoke);
		byte[] msgByte = ActualMessage.encodeActualMsg(d);
		sendData(socket,msgByte);
	}

	private void msgChoke(Socket socket, String remotePeerID) {
		ProcessPeer.displayLog(ProcessPeer.peerID + " sending CHOKE message to Peer " + remotePeerID);
		ActualMessage d = new ActualMessage(msgChoke);
		byte[] msgByte = ActualMessage.encodeActualMsg(d);
		sendData(socket,msgByte);
	}

	private void msgHave(Socket socket, String remotePeerID) {
		
		ProcessPeer.displayLog(ProcessPeer.peerID + " sending HAVE message to Peer " + remotePeerID);
		byte[] encodedBitField = ProcessPeer.Bit_Checking_Field.encoding_Functions();
		ActualMessage attr = new ActualMessage(msgHave, encodedBitField);
		sendData(socket,ActualMessage.encodeActualMsg(attr));
		
		encodedBitField = null;
	}
	
	private int sendData(Socket socket, byte[] encodedBitField) {
		try {
		OutputStream ops = socket.getOutputStream();
		ops.write(encodedBitField);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
		
	public void run()
	{
		ActualMessage attr;
		ForMessageHandler messageWrapper;
		String messageType;
		String receiverPeerId;
				
		while(true)
		{
			messageWrapper  = ProcessPeer.removeFromMsgQueue();
			while(messageWrapper == null)
			{
				Thread.currentThread();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				messageWrapper  = ProcessPeer.removeFromMsgQueue();
			}
			
			attr = messageWrapper.getActualMsg();
			
			messageType = attr.getMessageTypeString();
			receiverPeerId = messageWrapper.getSenderPeerID();
			int state = ProcessPeer.R_Peer_Info.get(receiverPeerId).state;
			
			// writing log
			if(messageType.equals(msgHave) && state != 14) {
				
				// have piece
				ProcessPeer.displayLog(ProcessPeer.peerID + " receieved HAVE message from Peer " + receiverPeerId); 
				if(msgIsInterested(attr, receiverPeerId)) {
					msgInterested(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 9;
				}	
				else {
					msgNotInterested(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 13;
				}
			}
			else if(messageType.equals(msgBitField) && state == 2) {
				ProcessPeer.displayLog(ProcessPeer.peerID + " receieved a BITFIELD message from Peer " + receiverPeerId);
				msgBitField(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
				ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 3;
			}
			else if(messageType.equals(msgNotInterested) && state == 3) {
				
				// not interested
				ProcessPeer.displayLog(ProcessPeer.peerID + " receieved a NOT INTERESTED message from Peer " + receiverPeerId);
				ProcessPeer.R_Peer_Info.get(receiverPeerId).isInterested = 0;
				ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 5;
				ProcessPeer.R_Peer_Info.get(receiverPeerId).isHandShaked = 1;
			}
			else if(messageType.equals(msgInterested) && state == 3) {	
				
				// interested
				ProcessPeer.displayLog(ProcessPeer.peerID + " receieved an INTERESTED message from Peer " + receiverPeerId);
				ProcessPeer.R_Peer_Info.get(receiverPeerId).isInterested = 1;
				ProcessPeer.R_Peer_Info.get(receiverPeerId).isHandShaked = 1;
				
				if(!ProcessPeer.preferedNeighbors.containsKey(receiverPeerId) && !ProcessPeer.Unchoked_Neighb.containsKey(receiverPeerId))
				{
					msgChoke(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).isChoked = 1;
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state  = 6;
				}
				else
				{
					ProcessPeer.R_Peer_Info.get(receiverPeerId).isChoked = 0;
					msgUnChoke(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 4 ;
				}
			}
			else if(messageType.equals(msgRequest) && state == 4) {
				msgRequestPeice(ProcessPeer.Map_Pid.get(receiverPeerId), attr, receiverPeerId);

				// Choke and unchoke
				if(!ProcessPeer.preferedNeighbors.containsKey(receiverPeerId) && !ProcessPeer.Unchoked_Neighb.containsKey(receiverPeerId)) {
					
					msgChoke(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).isChoked = 1;
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 6;
				} 
			}
			else if((messageType.equals(msgBitField) && state == 8) || (messageType.equals(msgHave) && state == 14)) {
	
				if(msgIsInterested(attr,receiverPeerId)) {	
					msgInterested(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 9;
				}	
				else {
					msgNotInterested(ProcessPeer.Map_Pid.get(receiverPeerId), receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 13;
				}
			}
			else if(messageType.equals(msgChoke) && state == 9) {
				// CHOKED
				ProcessPeer.displayLog(ProcessPeer.peerID + " is CHOKED by Peer " + receiverPeerId);
				ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 14;
			}
			else if(messageType.equals(msgUnchoke) && state == 9) {
				// UNCHOKED
				ProcessPeer.displayLog(ProcessPeer.peerID + " is UNCHOKED by Peer " + receiverPeerId);
				int selected = ProcessPeer.Bit_Checking_Field.Bit_Finding(ProcessPeer.R_Peer_Info.get(receiverPeerId).bitField);
				
				if(selected != -1) {
					msgRequest(ProcessPeer.Map_Pid.get(receiverPeerId), selected, receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 11;
					ProcessPeer.R_Peer_Info.get(receiverPeerId).TimingStart = new Date();
				}
				else
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 13;
	
			}
			else if(messageType.equals(msgPiece) && state == 11) {
				
				byte[] buffer = attr.getPayload();
				
				ProcessPeer.R_Peer_Info.get(receiverPeerId).TimingEnding = new Date();
				long timeGap = ProcessPeer.R_Peer_Info.get(receiverPeerId).TimingEnding.getTime() - 
						ProcessPeer.R_Peer_Info.get(receiverPeerId).TimingStart.getTime() ;
				
				ProcessPeer.R_Peer_Info.get(receiverPeerId).RateOfData = ((double)(buffer.length + actualMsgLen + actualMsgTypeLen)/(double)timeGap) * 200;
				
				FileHandler fb = FileHandler.extractPiece(buffer);
				ProcessPeer.Bit_Checking_Field.Bit_Field_Update(receiverPeerId, fb);			
				
				int currentPeiceIndex = ProcessPeer.Bit_Checking_Field.Bit_Finding(ProcessPeer.R_Peer_Info.get(receiverPeerId).bitField);
				if(currentPeiceIndex != -1) {
					msgRequest(ProcessPeer.Map_Pid.get(receiverPeerId),currentPeiceIndex, receiverPeerId);
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state  = 11;

					ProcessPeer.R_Peer_Info.get(receiverPeerId).TimingStart = new Date();
				}
				else
					ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 13;
				
				//updates peer info 
				ProcessPeer.read_peer_info();
				
				Enumeration<String> keys = ProcessPeer.R_Peer_Info.keys();
				
				while(keys.hasMoreElements()) {
					
					String key = (String)keys.nextElement();
					Peer_Remote pref = ProcessPeer.R_Peer_Info.get(key);
					
					if(key.equals(ProcessPeer.peerID)) {
						continue; 
					}
					if (pref.isCompleted == 0 && pref.isChoked == 0 && pref.isHandShaked == 1) {
						
						msgHave(ProcessPeer.Map_Pid.get(key), key);
						ProcessPeer.R_Peer_Info.get(key).state = 3;
						
					} 	
				}
								
				buffer = null;
				attr = null;
	
			}
			else if(messageType.equals(msgChoke) && state == 11)
			{
				// choked
				ProcessPeer.displayLog(ProcessPeer.peerID + " is CHOKED by Peer " + receiverPeerId);
				ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 14;
			}
			else if(messageType.equals(msgUnchoke) && state == 14)
			{
				// unchoked
				ProcessPeer.displayLog(ProcessPeer.peerID + " is UNCHOKED by Peer " + receiverPeerId);
				ProcessPeer.R_Peer_Info.get(receiverPeerId).state = 14;
			}	
		}
	}

}
