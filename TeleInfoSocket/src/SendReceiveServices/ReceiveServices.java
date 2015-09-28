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

	public static void receiveMessage(Socket socket, String sConsoleHeader, BufferedReader input, PrintWriter output, ObjectOutputStream outputStream, ObjectInputStream inputStream) 
	{
		try 
		{
			boolean isReceivingFile = false;
			Message fileMsgReceived = null;
			String sMsgFileName = "";
			while(true) 
			{
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
						if(isReceivingFile)
						{
							System.out.println("File Received");
							isReceivingFile = false;
						}
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

					System.out.println(Tools.determineHeaderBannerReceive(eMT, mfReceived, sMsgFileName)); //("Recevoir bienvenue");
					System.out.println(sConsoleHeader + "Recu la transmission de la trame " + iTrameNumber + "(" + sMsgReceived.getBytes().length + " octets)");

					Message confirmationMessage = new Message("ClientMsgReceived", MessageType.eMsgClientConfirmation); 

					outputStream.writeObject(confirmationMessage.getListMsgFragments().get(0));

					System.out.println(sConsoleHeader + "Acquittement de la transmission de la trame " + iTrameNumber);

					/*
					 * Verify, what conditions need to be met for this msg to appear?
					 */
					System.out.println(sConsoleHeader + "Accept la transmission de la trame " + iTrameNumber + "(" + sMsgReceived.getBytes().length + " octets)");
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
						reconstructFileAndSave(fileMsgReceived, sMsgFileName);
						break;
					}
			
					if(eMT == MessageType.eMsgUpload)
					{
						isReceivingFile = true;
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
