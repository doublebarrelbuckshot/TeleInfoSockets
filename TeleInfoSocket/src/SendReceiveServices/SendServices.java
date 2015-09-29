package SendReceiveServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;


public class SendServices {

	public static int sendCounter = 0;
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
				boolean bMsgReceived = false;
				boolean bIsRetransmission = false;
				while(!bMsgReceived)
				{
					if(!bIsRetransmission)
						System.out.println(Tools.determineHeaderBannerSend(eMT, "", mf));

					outputStream.writeObject(mf);

					if(!bIsRetransmission)
						System.out.println(sConsoleHeader + "Transmission de la trame " + mf.getTrameNumber() + "(" + mf.getMessageBytes().length + " bytes" + ")" );
					else
						System.out.println(sConsoleHeader + "Retransmission de la trame " + mf.getTrameNumber() + "(" + mf.getMessageBytes().length + " bytes" + ")" );

					
					System.out.println(sConsoleHeader + "Activation du timeout 1000ms");
					
					long endTimeMillis = System.currentTimeMillis() + 1000;
					while (System.currentTimeMillis() < endTimeMillis) 
					{
						String sMsgReceived = "";
						MessageFragment mfReceived = null;

						if(eMT == MessageType.eMsgFileFragment)
							socket.setSoTimeout(1000);

						try
						{
							mfReceived = (MessageFragment) inputStream.readObject();
							if(sendCounter == 4)
							{
								System.out.println(sConsoleHeader + "Simulation de la perte d'acquittement de la cinquieme trame de fichier, l'acquittement recu est ignore et on attend un autre acquittement qui ne viendra pas.");
								mfReceived = null;
								sendCounter++;
							}
						}
						catch(Exception ste)
						{
						}
				 
						if(mfReceived != null)
						{
							int iReceivedTrameNumber = mfReceived.getTrameNumber(); 

							sMsgReceived = new String(Arrays.copyOfRange(mfReceived.getMessageBytes(), 0, mfReceived.getBytesUsed())); //Tools.isolateMessage(sMsgReceived);


							if(Tools.determineIncomingMsgType(sMsgReceived) == MessageType.eMsgServerConfirmation)
							{
								bMsgReceived = true;
								if(eMT == MessageType.eMsgFileFragment) sendCounter++;
								System.out.println(sConsoleHeader + "Recu acquittement de la transmission de trame " +  mf.getTrameNumber() );
								Tools.waitInMS(1000);
							}
							if(Tools.determineIncomingMsgType(sMsgReceived) == MessageType.eMsgClientConfirmation){
								bMsgReceived = true;
								if(eMT == MessageType.eMsgFileFragment) sendCounter++;
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
						bIsRetransmission = true;
					}
				}
			}
		}
		catch (IOException e) 
		{
			System.out.println(e);
			System.out.println("asdfasdF");
		}
		//		 catch(SocketTimeoutException ste)
		//		 {
		//			 System.out.println("STE");
		//		 }
		//		 
		//catch (ClassNotFoundException e1) {
		//	e1.printStackTrace();
		//}
	}
}
