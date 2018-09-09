public interface MessageParameters {

	// Message format
	public static final String msgFormat = "UTF8";

	// Parameters of handshake message 
	public static final int handshakeHeaderLen = 18;

	public static final int handshakeZerobitsLen = 10;

	public static final int handshakePeerIDLen = 4;
	
	public static final int handshakeTotalLen = 32;

	public static final String handshakeHeaderString = "P2PFILESHARINGPROJ";

	// Parameters of actual message
	public static final int actualMsgLen = 4;

	public static final int actualMsgTypeLen = 1;

	public static final int pieceIndexLen = 4;
	
	public static final String msgChoke = "0";

	public static final String msgUnchoke = "1";

	public static final String msgInterested = "2";

	public static final String msgNotInterested = "3";

	public static final String msgHave = "4";

	public static final String msgBitField = "5";

	public static final String msgRequest = "6";

	public static final String msgPiece = "7";


}
