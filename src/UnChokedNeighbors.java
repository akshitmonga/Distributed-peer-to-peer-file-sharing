public static class UnChokedNeighbors extends TimerTask {

		public void run() 
		{
			//updates remotePeerInfoHash
			read_peer_info();
			
			int ch,un,hv;
			if(!Unchoked_Neighb.isEmpty())
				Unchoked_Neighb.clear();
			
			Enumeration<String> keys = R_Peer_Info.keys();
		
			Enumeration<String> key1 = keys;
			Vector<Peer_Remote> peers = new Vector<Peer_Remote>();
	
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
					send_unchoke_msg(ProcessPeer.Map_Pid.get(p.peerId), p.peerId);
					sending_have_msg(ProcessPeer.Map_Pid.get(p.peerId), p.peerId);
					ProcessPeer.R_Peer_Info.get(p.peerId).state = 3;
				}
			}
			
		}

	}