package SendReceiveServices;

public class MessageFragment implements java.io.Serializable, Comparable<MessageFragment> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7707000080255313616L;
	private int trameNumber;
	private byte[] messageBytes;
	private int bytesUsed;
	private MessageType messageType;
	

	public MessageFragment(int trameNumber, byte[] messageBytes, int bytesUsed, MessageType messageType)
	{
		this.trameNumber = trameNumber;
		this.messageBytes = messageBytes;
		this.bytesUsed = bytesUsed;
		this.messageType = messageType;
	}
	
	public String toString()
	{
		String result = "";
		result += "Trame: " + trameNumber + "  bytesUsed: " + bytesUsed;
		return result;
	}
	public int getTrameNumber() {
		return trameNumber;
	}

	public void setTrameNumber(int trameNumber) {
		this.trameNumber = trameNumber;
	}

	public byte[] getMessageBytes() {
		return messageBytes;
	}

	public void setMessageBytes(byte[] messageBytes) {
		this.messageBytes = messageBytes;
	}

	public int getBytesUsed() {
		return bytesUsed;
	}

	public void setBytesUsed(int bytesUsed) {
		this.bytesUsed = bytesUsed;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	@Override
	public int compareTo(MessageFragment mf) {
		if(this.getTrameNumber()< mf.getTrameNumber())
			return -1;
		
		return 1;
	}
	
	
}

