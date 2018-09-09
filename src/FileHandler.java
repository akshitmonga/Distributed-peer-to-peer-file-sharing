
/* handle file Filehandler_Pieces
 */
public class FileHandler {
	
	public int pieceIndex;
	public String senderPeerID;
	public byte[] filePiece; 			
	public int currentPiece;
	
	// constructor
	public FileHandler() {
		senderPeerID = null;
		pieceIndex = -1;
		currentPiece = 0;
		filePiece = new byte[CommonConfig.pieceSize];
	}

	
	// set and return sender peerID
	public String getFromPeerID() {
		return senderPeerID;
	}

	public void setFromPeerID(String fromPeerID) {
		this.senderPeerID = fromPeerID;
	}
	
	// set and return current piece
	public int getIsPresent() {
		return currentPiece;
	}

	public void setIsPresent(int isPresent) {
		this.currentPiece = isPresent;
	}

	// extract playload
	public static FileHandler extractPiece(byte []payload)
	{
		FileHandler fhd = new FileHandler();
		byte[] byteIndex = new byte[MessageParameters.pieceIndexLen];
		System.arraycopy(payload, 0, byteIndex, 0, MessageParameters.pieceIndexLen);
		fhd.pieceIndex = Tool.byteArrayToInt(byteIndex);

		fhd.filePiece = new byte[payload.length-MessageParameters.pieceIndexLen];
		System.arraycopy(payload, MessageParameters.pieceIndexLen, fhd.filePiece, 0, payload.length - MessageParameters.pieceIndexLen);

		return fhd;
	}
}
