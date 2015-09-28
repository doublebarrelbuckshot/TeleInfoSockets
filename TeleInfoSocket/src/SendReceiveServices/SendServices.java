package SendReceiveServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;


public class SendServices {

	public static void sendMessage(Socket socket, String sConsoleHeader, BufferedReader input, PrintWriter output, ObjectOutputStream outputStream, ObjectInputStream inputStream, MessageType eMT, Message msgFileToSend) {
		try
		{
			Message msgToSend = msgFileToSend;
			if(msgFileToSend == null)
			{
				msgToSend = Tools.determineMsgToSend(eMT);
			}

			for(MessageFragment mf : msgToSend.getListMsgFragments())
			{
				
				System.out.println(Tools.determineHeaderBannerSend(eMT, "", mf));
				
				outputStream.writeObject(mf);

				System.out.println(sConsoleHeader + "Transmission de la trame " + mf.getTrameNumber() + "(" + mf.getMessageBytes().length + " bytes" + ")" );

				System.out.println(sConsoleHeader + "Activation du timeout 1000ms");
				boolean bMsgReceived = false;
				long endTimeMillis = System.currentTimeMillis() + 1000;
				while (System.currentTimeMillis() < endTimeMillis) 
				{
					 String sMsgReceived = "";
					 MessageFragment mfReceived;
					 mfReceived = (MessageFragment) inputStream.readObject();
			
					if(mfReceived != null)
					{
						int iReceivedTrameNumber = mfReceived.getTrameNumber(); //Tools.getTrameNumber(sMsgReceived);
						
						sMsgReceived = new String(Arrays.copyOfRange(mfReceived.getMessageBytes(), 0, mfReceived.getBytesUsed())); //Tools.isolateMessage(sMsgReceived);

						if(Tools.determineIncomingMsgType(sMsgReceived) == MessageType.eMsgServerConfirmation)
						{
							bMsgReceived = true;
							System.out.println(sConsoleHeader + "Recu acquittement de la transmission de trame " +  mf.getTrameNumber() );
							Tools.waitInMS(1000);
						}
						if(Tools.determineIncomingMsgType(sMsgReceived) == MessageType.eMsgClientConfirmation){
							bMsgReceived = true;
							System.out.println(sConsoleHeader + "Recu acquittement de la transmission de trame " +  mf.getTrameNumber() );
							Tools.waitInMS(1000);

							if(eMT == MessageType.eMsgAuRevoir)
							{
								try 
								{
									socket.close();
									System.out.println(Tools.determineFooterBanner(eMT));
								}
								catch (IOException e) {
									System.out.println(e);
								}
							}
						}
					}
				}
				if(bMsgReceived == false)
				{
					System.out.println(sConsoleHeader + "Timeout a la reception de l'acquittement trame " +  mf.getTrameNumber());
				}
			}
		}
		catch (IOException e) 
		{
			System.out.println(e);
		}
		catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
