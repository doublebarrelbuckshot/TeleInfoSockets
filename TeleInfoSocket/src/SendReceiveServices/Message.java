package SendReceiveServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Message{

	ArrayList<MessageFragment> listMsgFragments;
	public static int byteArraySize = 32;

	public Message()
	{		
		listMsgFragments = new ArrayList<MessageFragment>();
	}
	
	public Message(String strMsg, MessageType msgType)
	{
		byte[] byteMsg = strMsg.getBytes();
		
		convertByteArrayToMessageFragments(byteMsg, msgType);
		
	}
	
	public Message(byte[] fileInBytes, MessageType msgType)
	{
		convertByteArrayToMessageFragments(fileInBytes, msgType);

	}
	private void convertByteArrayToMessageFragments( byte[] byteMsg, MessageType msgType) {
		listMsgFragments = new ArrayList<MessageFragment>();
		int size = byteMsg.length;
		int iByteArrayPosition = 0;
		int trameCounter = 0;
		int endPosition = byteArraySize;
		while (iByteArrayPosition<size)
		{
			byte[] byteMsgFrag = new byte[byteArraySize];
			
			byteMsgFrag = Arrays.copyOfRange(byteMsg, iByteArrayPosition, endPosition);
			int bytesUsed = byteArraySize;
			if(byteMsg.length < (trameCounter + 1) * byteArraySize)
			{
				bytesUsed = byteMsg.length - (trameCounter * byteArraySize);
			}
			MessageFragment mf = new MessageFragment(trameCounter, byteMsgFrag, bytesUsed,msgType);
			
			this.listMsgFragments.add(mf);
			trameCounter++;
			iByteArrayPosition += byteArraySize;		

			 //= Math.min(byteMsg.length, size - trameCounter * byteArraySize);
			endPosition = Math.min(size, endPosition + byteArraySize);
		}
		
	}

	
	public ArrayList<MessageFragment> getListMsgFragments() {
		return listMsgFragments;
	}

	public void setListMsgFragments(ArrayList<MessageFragment> listMsgFragments) {
		this.listMsgFragments = listMsgFragments;
	}

	public static int getByteArraySize() {
		return byteArraySize;
	}

	public static void setByteArraySize(int byteArraySize) {
		Message.byteArraySize = byteArraySize;
	}

	public String toString()
	{
		String result = "";
		for(MessageFragment mf: listMsgFragments)
		{
			result += "Trame: " + mf.getTrameNumber() + "     " + "ArraySize: " + mf.getMessageBytes().length + "     "  + "PartialString: " + new String(mf.getMessageBytes());
		}
		
		return result;
	}
}
