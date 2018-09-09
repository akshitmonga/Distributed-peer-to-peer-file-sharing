import java.io.*;

public class Field_Bit implements MessageParameters 
{
	public FileHandler[] Filehandler_Pieces;
	public int Piece_Number;

	public FileHandler[] Get_Filehandler_Pieces() {
		return Filehandler_Pieces;
	}

	public void Set_Filehandler_Pieces(FileHandler[] Filehandler_Pieces) {
		this.Filehandler_Pieces = Filehandler_Pieces;
	}
	
	
	
	
	public int pc_no;
	// 	pc_no= Piece_Number;
	
	public void Set_Filehandler_Pieces_Number(int num) {
		this.Piece_Number = num;
	}

	int k;
	
	public Field_Bit() 
	{
		FileHandler[] Fp;
		
		Piece_Number = (int) Math.ceil(((double) CommonConfig.fileSize / (double) CommonConfig.pieceSize));
		
		pc_no = Piece_Number;
		
		this.Filehandler_Pieces = new FileHandler[Piece_Number];
		
		Fp = Filehandler_Pieces;

		
		for (int i = 0; i < this.Piece_Number; i++)
			this.Filehandler_Pieces[i] = new FileHandler();

	}
	
	public int get_Filehandler_Pieces_Number() {
		return Piece_Number;
	}
	
	public static Field_Bit decode(byte[] b) {
		
		Field_Bit reset_Field_Bit = new Field_Bit();
		int i=0;
		while(i < b.length){
			
			int count = 7;
			while(count >=0) {
				int tmp = 1 << count;
				if(i * 8 + (8-count-1) < reset_Field_Bit.Piece_Number) {
					if((b[i] & (tmp)) != 0)
						reset_Field_Bit.Filehandler_Pieces[i * 8 + (8-count-1)].currentPiece = 1;
					else
						reset_Field_Bit.Filehandler_Pieces[i * 8 + (8-count-1)].currentPiece = 0;
				}
				count--;
			}
			i++;
		}
		
		return reset_Field_Bit;
	}
	
	
	 static String Arraybyte_Converting_to_HexString(byte new_byt[]) {

		    if (new_byt == null || new_byt.length <= 0)
		        return null;
		    
			String digits;
			
			String hexDigits[] = {"0", "1", "2","3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E","F"};

		    StringBuffer osb = new StringBuffer(new_byt.length * 2);
			digits = hexDigits[4];
			
		    byte hex_byt = 0x00;
			int nb=0;
		    int i = 0; 
		    while (i < new_byt.length) 
		    {
		        hex_byt = (byte) (new_byt[i] & 0xF0); 
				nb++;
		        hex_byt = (byte) (hex_byt >>> 4);

		        hex_byt = (byte) (hex_byt & 0x0F);    

		        osb.append(hexDigits[ (int) hex_byt]);

		        hex_byt = (byte) (new_byt[i] & 0x0F); 
				nb--;

		        osb.append(hexDigits[ (int) hex_byt]); 
		        i++;
		    }

		    String result_String = new String(osb);

		    return result_String;
		}
	
	 
		public void bitfieldInit(String This_Peer_ID, int CompleteFile_checker) {

			if (CompleteFile_checker != 1) {

				for (int i = 0; i < this.Piece_Number; i++) {
					this.Filehandler_Pieces[i].setIsPresent(0);
					this.Filehandler_Pieces[i].setFromPeerID(This_Peer_ID);
				}

			} else {
				for (int i = 0; i < this.Piece_Number; i++) {
					this.Filehandler_Pieces[i].setIsPresent(1);
					this.Filehandler_Pieces[i].setFromPeerID(This_Peer_ID);
				}

			}

		}

	public synchronized boolean Comparing_Pieces(Field_Bit peerBitField) {

		int i=0;
		while ( i < peerBitField.get_Filehandler_Pieces_Number())
			{
						i++;

			if (peerBitField.Get_Filehandler_Pieces()[i].getIsPresent() == 1 && this.Get_Filehandler_Pieces()[i].getIsPresent() == 0) {
				return true;
			} else
				continue;
		}

		return false;
	}

	// return first different bit
	public synchronized int Bit_Finding(Field_Bit peerBitField) {
		
		int Peer_Number_Piece = peerBitField.get_Filehandler_Pieces_Number();
		int myPieceNum = this.get_Filehandler_Pieces_Number();
		
		if (myPieceNum >= Peer_Number_Piece) {
			int i=0;
			while (i < Peer_Number_Piece) {
				if (peerBitField.Get_Filehandler_Pieces()[i].getIsPresent() == 1
						&& this.Get_Filehandler_Pieces()[i].getIsPresent() == 0) {
					return i;
				}
				i++;
			}
		} else {
			int i=0;
			while(i < myPieceNum) {
				if (peerBitField.Get_Filehandler_Pieces()[i].getIsPresent() == 1
						&& this.Get_Filehandler_Pieces()[i].getIsPresent() == 0) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}
	
	// encoding_Functions and decode
	public byte[] encoding_Functions() {
		
		return this.getBytes();
	}
	
	public void Update_Peer_Info(String clientID, int hasFile)
	{
		BufferedWriter bfWriter = null;
		BufferedReader bfReader = null;
		try 
		{
			BufferedReader bf = null;
			bfReader= new BufferedReader(new FileReader("PeerInfo.cfg"));
			bf = bfReader;
			String curLine;
			int ctr=0;
			StringBuffer sbf = new StringBuffer();
		
			while((curLine = bfReader.readLine()) != null) 
			{
				if(curLine.trim().split("\\s+")[0].equals(clientID))
				{
					ctr++;
					sbf.append(curLine.trim().split("\\s+")[0] + " " + curLine.trim().split("\\s+")[1] + " " + curLine.trim().split("\\s+")[2] + " " + hasFile);
				}
				else
				{	
					ctr++;
					sbf.append(curLine);
				}
				sbf.append("\n");
			}
			
			bfReader.close();
		
			bfWriter= new BufferedWriter(new FileWriter("PeerInfo.cfg"));
			bfWriter.write(sbf.toString());	
			
			bfWriter.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() 
	{
		int byte_Number = this.Piece_Number / 8;
		if (Piece_Number % 8 != 0)
			byte_Number = byte_Number + 1;
		byte[] rstByteInd = new byte[byte_Number];
		int Test = 0;
		int count = 0;
		int ind=1;
		while( ind <= this.Piece_Number)
		{
			int Tempvar = this.Filehandler_Pieces[ind-1].currentPiece;
			Test = Test << 1;
			if (Tempvar == 1) 
			{
				Test = Test + 1;
			} else
				Test = Test + 0;

			if (ind % 8 == 0 && ind!=0) {
				rstByteInd[count] = (byte) Test;
				count++;
				Test = 0;
			}
			ind++;
			
		}
		if ((ind-1) % 8 != 0) {
			int tempShift = ((Piece_Number) - (Piece_Number / 8) * 8);
			Test = Test << (8 - tempShift);
			rstByteInd[count] = (byte) Test;
		}
		return rstByteInd;
	}

	
	public boolean isCompleted() {
		int i=0;
		while (i < this.Piece_Number) {
			if (this.Filehandler_Pieces[i].currentPiece == 0) {
				return false;
			}
			i++;
		}
		return true;
	}
	
	public int gotPiecesNum()
	{
		int Piece_Number = 0,i=0;
		while (i < this.Piece_Number)
		{
			if (this.Filehandler_Pieces[i].currentPiece == 1) 
				Piece_Number++;
			i++;
		}
		return Piece_Number; 
	}


	public synchronized void Bit_Field_Update(String peerID, FileHandler piece) {
		String fl;
		byte[] bt;
		int pt=0;
		int ins=0,com=0,ch=0;
		try 
		{
			if (ProcessPeer.Bit_Checking_Field.Filehandler_Pieces[piece.pieceIndex].currentPiece == 1) { } 
			else {
				String fileName = CommonConfig.fileName;
				fl = fileName;	
				File f = new File(ProcessPeer.peerID, fileName);
				int pos = piece.pieceIndex * CommonConfig.pieceSize;
				pt=pos;
				RandomAccessFile raf = new RandomAccessFile(f, "rw");

				byte[] new_byt = piece.filePiece;
				bt = new_byt;
				
				raf.seek(pos);
				ch=1;
				raf.write(new_byt);
				pt++;
				this.Filehandler_Pieces[piece.pieceIndex].setIsPresent(1);
				this.Filehandler_Pieces[piece.pieceIndex].setFromPeerID(peerID);
				raf.close();
				
				// write log
				ProcessPeer.displayLog(ProcessPeer.peerID + " has downloaded the PIECE " + piece.pieceIndex + " from Peer " + peerID
						+ ". Now the number of Filehandler_Pieces it has is " + ProcessPeer.Bit_Checking_Field.gotPiecesNum());
				
				if (ProcessPeer.Bit_Checking_Field.isCompleted()) {
					ProcessPeer.R_Peer_Info.get(ProcessPeer.peerID).isInterested = 0;
					ins++;;
					ProcessPeer.R_Peer_Info.get(ProcessPeer.peerID).isCompleted = 1;
					com=1;
					ProcessPeer.R_Peer_Info.get(ProcessPeer.peerID).isChoked = 0;
					ch++;
					Update_Peer_Info(ProcessPeer.peerID, 1);
					pt--;
					ProcessPeer.displayLog(ProcessPeer.peerID + " has DOWNLOADED the complete file.");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	
}
