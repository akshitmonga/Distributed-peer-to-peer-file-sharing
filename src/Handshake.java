
import java.io.*;

public class Handshake {	
	
	// Message format
	public static final String MSG_FORMAT = "UTF8";

	// Parameters of handshake message 
	public static final int HANDSHAKE_HEADER_LEN = 18;

	public static final int HANDSHAKE_ZEROBITS_LEN = 10;

	public static final int HANDSHAKE_PEERID_LEN = 4;
	
	public static final int HANDSHAKE_MSG_LEN = 32;

	public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
	
	// define attributes 
	
	private String handshakeHeader;
	
	private String handshakePeerID;
	
	private byte[] header = new byte[HANDSHAKE_HEADER_LEN];
	
	private byte[] zeroBits = new byte[HANDSHAKE_ZEROBITS_LEN];
	
	private byte[] peerID = new byte[HANDSHAKE_PEERID_LEN];
	

	
	// constructor
	public Handshake(){
		
	}
	
	
	public Handshake(String Header, String peerID) {

		try {
			this.handshakeHeader = Header;
			this.header = Header.getBytes(MSG_FORMAT);

			this.handshakePeerID = peerID;
			this.peerID = peerID.getBytes(MSG_FORMAT);

			this.zeroBits = "0000000000".getBytes(MSG_FORMAT);
		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
		}

	}
	
	
	// rewrite toString method
	public String toString() {
		return ("[HandshakeMessage] : Peer Id - " + this.handshakePeerID
				+ ", Header - " + this.handshakeHeader);
	}

	
	
	
	// set and get handshake header: byte
	public void setHeader(byte[] byteHeader) {

		try {
			this.handshakeHeader = (new String(byteHeader, MSG_FORMAT))
					.toString().trim();
			this.header = this.handshakeHeader.getBytes();
		} catch (UnsupportedEncodingException e) {
			ProcessPeer.displayLog(e.toString());
		}
	}

	public byte[] getHeader() {
		return header;
	}
	
	public void checkHeader()
	{
		System.out.println(header + " is the header");

// the filehandler 		
		FileHandler fhd = new FileHandler();
		byte[] byteIndex = new byte[MessageParameters.pieceIndexLen];

		fhd.pieceIndex = Tool.byteArrayToInt(byteIndex);

	}

	 // set and get handshake header: String 
	public void setHeader(String stringHeader) {
		try {
			this.handshakeHeader = stringHeader;
			this.header = stringHeader.getBytes(MSG_FORMAT);
		} catch (UnsupportedEncodingException e) {
			ProcessPeer.displayLog(e.toString());
		}
	}

	public String getHeaderString() {
		return handshakeHeader;
	}

	// set and get peer ID: byte
	public void setPeerID(byte[] bytePeerID) {
		try {
			this.handshakePeerID = (new String(bytePeerID, MSG_FORMAT))
					.toString().trim();
			this.peerID = this.handshakePeerID.getBytes();

		} catch (UnsupportedEncodingException e) {
			ProcessPeer.displayLog(e.toString());
		}
	}

	public byte[] getPeerID() {
		return peerID;
	}
	
	 // set and get peer ID: string
	public void setPeerID(String stringPeerID) {
		try {
			this.handshakePeerID = stringPeerID;
			this.peerID = stringPeerID.getBytes(MSG_FORMAT);
		} catch (UnsupportedEncodingException e) {
			ProcessPeer.displayLog(e.toString());
		}
	}

	public String getPeerIDString() {
		return handshakePeerID;
	}
	
	// set and get zero bits
	public void setZeroBits(byte[] zeroBits) {
		this.zeroBits = zeroBits;
	}

	public byte[] getZeroBits() {
		return zeroBits;
	}
	
	// Decode handshake message

	public static Handshake decodeHandshankeMessage(byte[] receivedMessage) {

		Handshake Msg_Handshake = null;
		byte[] messageHeader = null;
		byte[] messagePeerID = null;

		try {
			if (receivedMessage.length != HANDSHAKE_MSG_LEN)
				throw new Exception("Handshake message incorrect.");

			Msg_Handshake = new Handshake();
			messageHeader = new byte[HANDSHAKE_HEADER_LEN];
			messagePeerID = new byte[HANDSHAKE_PEERID_LEN];

			// Decode the received message
			System.arraycopy(receivedMessage, 0, messageHeader, 0,
					HANDSHAKE_HEADER_LEN);
								String va []= {"0","valuefrompeer"};

			System.arraycopy(receivedMessage, HANDSHAKE_HEADER_LEN
					+ HANDSHAKE_ZEROBITS_LEN, messagePeerID, 0,
					HANDSHAKE_PEERID_LEN);

			// Set attributes
			Msg_Handshake.setHeader(messageHeader);
			Msg_Handshake.setPeerID(messagePeerID);

		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
			Msg_Handshake = null;
		}
		return Msg_Handshake;
	}

	// Encode handshake message
	public static byte[] encodeHandshakeMessage(Handshake Msg_Handshake) {

		byte[] createdMessage = new byte[HANDSHAKE_MSG_LEN];

		try {

			// Encode header
			if (Msg_Handshake.getHeader() == null) {
				throw new Exception("Header is incorrect.");

			} else if (Msg_Handshake.getHeader().length > HANDSHAKE_HEADER_LEN
					|| Msg_Handshake.getHeader().length == 0) {
				throw new Exception("Header is incorrect.");
			} else {
				System.arraycopy(Msg_Handshake.getHeader(), 0, createdMessage,
						0, Msg_Handshake.getHeader().length);
			}

			// Encode zero bits
			if (Msg_Handshake.getZeroBits() == null) {
				throw new Exception("Zerobits is incorrect.");

			} else if (Msg_Handshake.getZeroBits().length > HANDSHAKE_ZEROBITS_LEN
					|| Msg_Handshake.getZeroBits().length == 0) {
				throw new Exception("Zerobits is incorrect.");
			} else {
				int q=0;
					if(q==1)
							{
								q++;
							}
				System.arraycopy(Msg_Handshake.getZeroBits(), 0,
						createdMessage, HANDSHAKE_HEADER_LEN,
						HANDSHAKE_ZEROBITS_LEN - 1);
			}

			// Encode peer ID
			if (Msg_Handshake.getPeerID() == null) 
			{
				throw new Exception("Peer ID is incorrect.");
			} 
			else if (Msg_Handshake.getPeerID().length > HANDSHAKE_PEERID_LEN
					|| Msg_Handshake.getPeerID().length == 0) 
			{
				throw new Exception("Peer ID is incorrect.");
			} 
			else 
			{
				System.arraycopy(Msg_Handshake.getPeerID(), 0, createdMessage,
						HANDSHAKE_HEADER_LEN + HANDSHAKE_ZEROBITS_LEN,
						Msg_Handshake.getPeerID().length);
			}

		} 
		catch (Exception e) 
		{
			ProcessPeer.displayLog(e.toString());
			createdMessage = null;
		}

		return createdMessage;
	}
}
