import java.util.*;

public class Peer_Remote implements Comparable<Peer_Remote> {
	
	public int Peer_First_Flag;
	public int peerIndex;
	public Date TimingStart;
	public Date TimingEnding;
	
	public String peerId;
	public String Addingpeer;
	public String Port_Pnumber;
	
	public double RateOfData = 0;
	public int isInterested = 1;
	public int isPreferredNeighbor = 0;
	public int isOptUnchokedNeighbor = 0;
	public int isChoked = 1;
	public Field_Bit bitField;
	public int state = -1;

	public int isCompleted = 0;
	public int isHandShaked = 0;
	

	// constructors 
	public Peer_Remote(String peerID, String Addingpeer, String peerPort, int peerIndex)
	{
		this.peerId = peerID;
		this.Addingpeer = Addingpeer;
		this.Port_Pnumber = peerPort;
		this.peerIndex = peerIndex;
		bitField = new Field_Bit();
	}
	
	public Peer_Remote(String peerID, String Addingpeer, String peerPort, int IsFirstPeer, int peerIndex)
	{
		this.peerId = peerID;
		this.Addingpeer = Addingpeer;
		this.Port_Pnumber = peerPort;
		this.peerIndex = peerIndex;
		bitField = new Field_Bit();
		Peer_First_Flag = IsFirstPeer;
		
	}
	
	// set and return peer ID
	public void setPeerId(String peerID) {
		this.peerId = peerID;
	}

	public String getPeerId() {
		return peerId;
	}
	
	// set and return peer Address
	public void setPeerAddress(String peerAddress) {
		this.Addingpeer = peerAddress;
	}
	
	public String getPeerAddress() {
		return Addingpeer;
	}

	// set and return peer port number
	public void Peer_Port_Setter(String peerPort) {
		this.Port_Pnumber = peerPort;
	}
	public String Peer_Port_Getter() {
		return Port_Pnumber;
	}

	
	// set and get firstPeer flag
	public void Setting_First_Peer(int isFirstPeer) {
		this.Peer_First_Flag = isFirstPeer;
	}

	public int Getting_Peer_First() {
		return Peer_First_Flag;
	}

	public int compareTo(Peer_Remote o1) {
		
		if (this.RateOfData > o1.RateOfData) 
			return 1;
		else if (this.RateOfData == o1.RateOfData) 
			return 0;
		else 
			return -1;
	}

}
