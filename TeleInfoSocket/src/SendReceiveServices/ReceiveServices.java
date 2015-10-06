package SendReceiveServices;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class ReceiveServices {
	public static int errorCount = 0;
	public static boolean receiveMessage(Socket socket, String sConsoleHeader, BufferedReader input, PrintWriter output, ObjectOutputStream outputStream, ObjectInputStream inputStream) 
	{
		try 
		{
			boolean isReceivingFile = false;
			boolean isReceivingDirectory = false;
			
			Message fileMsgReceived = null;
			boolean justRecievedFile = false;
			boolean justReceivedDir = false;
			boolean bExpectingRetransmission = false;
			String sMsgFileName = "";
			while(true) 
			{
				String strTransmission = "Transmission ";
				if(bExpectingRetransmission)
				{
					strTransmission = "Retransmission ";
				}
				String sMsgReceived = "";
				MessageFragment mfReceived;
				mfReceived = (MessageFragment) inputStream.readObject();
				if(sMsgReceived != null)
				{
					int iTrameNumber = mfReceived.getTrameNumber();
					sMsgReceived = new String(Arrays.copyOfRange(mfReceived.getMessageBytes(), 0, mfReceived.getBytesUsed())); 
					MessageType eMT = mfReceived.getMessageType(); 
					
					if(eMT == MessageType.eMsgFin)
					{
						if(isReceivingFile && !isReceivingDirectory)
						{
							System.out.println("File Received");
							isReceivingFile = false;
							justRecievedFile = true;
						}
						if(isReceivingDirectory)
						{
							isReceivingFile = false;
							justReceivedDir = true;
						}
					}
					if(eMT == MessageType.eMsgReceiveDir)
					{
						isReceivingDirectory = true;
						isReceivingFile = true;
					}

					if(isReceivingFile)
					{
						if(eMT == MessageType.eMsgFilename)
						{
							sMsgFileName = sMsgReceived;
						}
						else
						{
							if(fileMsgReceived == null)
							{
								fileMsgReceived = new Message();
							}
							ArrayList<MessageFragment> listMsgFragments = fileMsgReceived.getListMsgFragments();
							boolean trameExists = listMsgFragments.contains(mfReceived);

							if(!trameExists){
								listMsgFragments.add(mfReceived);
							}
						}
					}

					System.out.println(Tools.determineHeaderBannerReceive(eMT, mfReceived, sMsgFileName)); 
					System.out.println(sConsoleHeader + "Recu la " + strTransmission + "de la trame " + iTrameNumber + "(" + sMsgReceived.getBytes().length + " octets)");

					//Count only during reception of file trame
					if(eMT == MessageType.eMsgFileFragment && !isReceivingDirectory)
						errorCount++;

					//on 2nd file trame being received, don't send confirmation
					if(errorCount != 2 )
					{
						Message confirmationMessage = new Message("ClientMsgReceived", MessageType.eMsgClientConfirmation); 
						outputStream.writeObject(confirmationMessage.getListMsgFragments().get(0));			
						bExpectingRetransmission = false;
					}
					else
					{
						System.out.println(sConsoleHeader + "Simulation de la perte de la deuxieme trame de fichier, l'acquittement n'est pas envoye.");
						bExpectingRetransmission = true;
					}
					
					//If we simulate the loss of 2nd trame, then don't go in here
					if(!bExpectingRetransmission)
					{

						System.out.println(sConsoleHeader + "Acquittement de la " + strTransmission + "de la trame " + iTrameNumber);

						/*
						 * Verify, what conditions need to be met for this msg to appear?
						 */
						System.out.println(sConsoleHeader + "Accept la " + strTransmission + "de la trame " + iTrameNumber + "(" + sMsgReceived.getBytes().length + " octets)");
						System.out.println( Tools.determineFooterBanner(eMT));

						if(eMT == MessageType.eMsgBienvenue)
						{
							System.out.println("Bienvenue client " + socket.getInetAddress() + ":" + socket.getPort());
							break;
						}
						if(eMT == MessageType.eMsgAuRevoir)
						{
							try 
							{
								socket.close();
								System.out.println("Au revoir client " + socket.getInetAddress() + ":" + socket.getPort());
								break;
							}
							catch (IOException e) 
							{
								System.out.println(e);
							}
						}
						if(eMT == MessageType.eMsgFin)
						{
							if(justRecievedFile)
							{
								reconstructFileAndSave(fileMsgReceived, sMsgFileName);
								justRecievedFile = false;
							}
							if(justReceivedDir)
							{
								reconstructDirAndPrint(fileMsgReceived);
								justReceivedDir = false;
							}
							break;
						}

						if(eMT == MessageType.eMsgUpload)
						{
							isReceivingFile = true;
						}

						if(eMT == MessageType.eMsgSendDir)
						{
							return true; //signal that we need to send a message now.
						}
					}
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
		return false;
	}

	private static void reconstructDirAndPrint(Message fileMsgReceived) {
		ArrayList<MessageFragment> listFragments = fileMsgReceived.getListMsgFragments();
		Collections.sort(listFragments);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

		for(MessageFragment mf : listFragments)
		{
			try 
			{
				outputStream.write( mf.getMessageBytes() );
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] bResult =  outputStream.toByteArray( );
		String strDir = new String(bResult);
		
		System.out.println("Contenu du repertoire courant du serveur:\n" + strDir);
		
	}

	private static void reconstructFileAndSave(Message fileMsgReceived, String sMsgFileName) 
	{
		ArrayList<MessageFragment> listFragments = fileMsgReceived.getListMsgFragments();
		Collections.sort(listFragments);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

		for(MessageFragment mf : listFragments)
		{
			try 
			{
				outputStream.write( mf.getMessageBytes() );
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		byte[] bResult =  outputStream.toByteArray( );
		FileOutputStream fos;
		try 
		{
			fos = new FileOutputStream(sMsgFileName);
			fos.write(bResult);
			fos.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}


}
