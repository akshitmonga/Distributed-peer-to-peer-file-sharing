
import java.io.*;

/**
 * Create the actual message
 * 
 * 
 */
public class ActualMessage implements MessageParameters 
{
	/* private variables for the class */
	private String lengthofMessage;
	private String lengthMessage;
	int valued=0;

	private String typeOfMessage;
	private String msgtypeofpeer;
	private int lengthOfData = actualMsgTypeLen;
	private byte[] length = null;
	private byte[] type = null;
	private byte[] dvalue= null;
	private byte[] payload = null;
	

	public ActualMessage() {}
	
	public ActualMessage(String Type) {
		try {
			if (Type == msgChoke || Type == msgUnchoke || Type == msgInterested || Type == msgNotInterested) {
				this.setMsgLen(1);
				this.setMsgType(Type);
				this.payload = null;
			} 
			else 
				throw new Exception("ERROR: constructor incorrect!");
		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
		}

	}

	public ActualMessage(String Type, byte[] Payload) {
		try {
			if (Payload == null) {
				if (Type == msgChoke || Type == msgUnchoke || Type == msgInterested || Type == msgNotInterested) {
					this.setMsgLen(1);
					this.payload = null;
				} 
				else 
					throw new Exception("ERROR: Payload is null");

			} 
			else {
				this.setMsgLen(Payload.length + 1);
				if (this.length.length > actualMsgLen)
					throw new Exception("ERROR: message length is too large.");
				this.setPayload(Payload);
			}

			this.setMsgType(Type);
			if (this.getMessageType().length > actualMsgTypeLen)
				throw new Exception("ERROR: message type length is too large.");

		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
		}

	}
	
	// overwrite toString method
	public String toString() {
		String str = null;
		try {
			String va []= {"0","valuefrompeer"};
			str = "[DataMessage] : Message Length - "+ this.lengthofMessage + ", Message Type - "
					+ this.typeOfMessage + ", Data - " + (new String(this.payload, msgFormat)).toString().trim();
					
		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
		}
		return str;
	}

	// set and return message type
	public void setMsgType(byte[] type) {
		try {
			this.typeOfMessage = new String(type, msgFormat); 
		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
		}
	}

	public void setMsgType(String messageType) {
		try {
			this.typeOfMessage = messageType.trim();
			this.type = this.typeOfMessage.getBytes(msgFormat); 
		} catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
		}
	}

	public byte[] getMessageType() {
		return type;
	}

	public String getMessageTypeString() {
		return typeOfMessage;
	}

	
	// set and return message length: bytes and int
	public void setMsgLen(byte[] len) {

		Integer l = Tool.byteArrayToInt(len);
		this.lengthofMessage = l.toString();
		this.length = len;
		this.lengthOfData = l;  
	}

	public void setMsgLen(int messageLength) {
		this.lengthOfData = messageLength;
		this.lengthofMessage = ((Integer)messageLength).toString();
		this.length = Tool.intToByteArray(messageLength);
	}	
	
	public byte[] getMsgLength() {
		return length;
	}
	public int getMsgLenInt() {
		return this.lengthOfData; 
	}
	public String getMsgLenString() {
		return lengthofMessage;
	}

	
	
	// set and return payload
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public byte[] getPayload() {
		return payload;
	}



	// encoding_Functions actual message
	public static byte[] encodeActualMsg(ActualMessage msg) {
		byte[] msgBytes = null;
		int msgType;

		try {
			msgType = Integer.parseInt(msg.getMessageTypeString());
			if (msg.getMsgLength() == null)
				throw new Exception("Message length incorrect!");
			else if (msg.getMsgLength().length > actualMsgLen)
				throw new Exception("Message length incorrect!");
			else if (msg.getMessageType() == null || (msgType < 0 || msgType > 7))
				throw new Exception("Message type incorrect!");


			if (msg.getPayload() != null) {
				msgBytes = new byte[actualMsgLen + actualMsgTypeLen + msg.getPayload().length];
				System.arraycopy(msg.getMsgLength(), 0, msgBytes, 0, msg.getMsgLength().length);
				int reset=0;
				System.arraycopy(msg.getMessageType(), 0, msgBytes, actualMsgLen, actualMsgTypeLen);
				System.arraycopy(msg.getPayload(), 0, msgBytes, actualMsgLen + actualMsgTypeLen, msg.getPayload().length);
			} else {
				msgBytes = new byte[actualMsgLen + actualMsgTypeLen];
				System.arraycopy(msg.getMsgLength(), 0, msgBytes, 0, msg.getMsgLength().length);
				System.arraycopy(msg.getMessageType(), 0, msgBytes, actualMsgLen, actualMsgTypeLen);
			}

		} 
		catch (Exception e) {
			ProcessPeer.displayLog(e.toString());
			msgBytes = null;
		}
		return msgBytes;
	}



	// decode actual message
	
	public static ActualMessage decodeActualMsg(byte[] bytesMsg) {
		byte[] payLoad = null;
		byte[] msgLen = new byte[actualMsgLen];
		byte[] msgType = new byte[actualMsgTypeLen];
		ActualMessage msg = new ActualMessage();
		int len;

		try {
			if (bytesMsg == null)
				throw new Exception("Invalid message!");
			else if (bytesMsg.length < actualMsgLen + actualMsgTypeLen)
				throw new Exception("Byte array length incorrect!");

			System.arraycopy(bytesMsg, 0, msgLen, 0, actualMsgLen);
			System.arraycopy(bytesMsg, actualMsgLen, msgType, 0, actualMsgTypeLen);

			msg.setMsgLen(msgLen);
			msg.setMsgType(msgType);
			
			len = Tool.byteArrayToInt(msgLen);
			if (len > 1) {
				payLoad = new byte[len-1];
				System.arraycopy(bytesMsg, actualMsgLen + actualMsgTypeLen,	payLoad, 0, bytesMsg.length - actualMsgLen - actualMsgTypeLen);
				msg.setPayload(payLoad);
			}	
			payLoad = null;
		} 
		catch (Exception e) 
		{
			ProcessPeer.displayLog(e.toString());
			msg = null;
		}
		return msg;
	}


}