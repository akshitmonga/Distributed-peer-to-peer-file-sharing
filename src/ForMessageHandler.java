
public class ForMessageHandler
{
	ActualMessage actualMsg;
	String senderPeerID;
	
	public ForMessageHandler() 
	{
		actualMsg = new ActualMessage();
		senderPeerID = null;
	}
	
	// set and return sender peerID
	public String getSenderPeerID() {
		return senderPeerID;
	}

	public void setSenderPeerID(String senderPeerID) {
		this.senderPeerID = senderPeerID;
	}
	
	
	
	// set and return data message
	public ActualMessage getActualMsg() {
		return actualMsg;
	}

	public void setActualMsg(ActualMessage actualMessage) {
		this.actualMsg = actualMessage;
	}
	
	
	
}
