import java.util.*;

public class TransitRateCmp implements Comparator<Peer_Remote> {

	private boolean flag;

	// Constructor 
	public TransitRateCmp() {
		this.flag = true;
	}

	public TransitRateCmp(boolean flag) {
		this.flag = flag;
	}

	// define comparison method
	public int compare(Peer_Remote rm1, Peer_Remote rm2) {
		
		if (rm1 == null && rm2 == null) return 0;
		if (rm1 == null) return 1;
		if (rm2 == null) return -1;

		if (rm1 instanceof Comparable) {
			if (flag) {
				return rm1.compareTo(rm2);
			} else {
				return rm2.compareTo(rm1);
			}
		} 
		else {
			if (flag) {
				return rm1.toString().compareTo(rm2.toString());
			} else {
				return rm2.toString().compareTo(rm1.toString());
			}
		}
	}

}
