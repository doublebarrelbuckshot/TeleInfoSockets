package ServerPkg;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import ClientPkg.TIClient.MessageType;


public class TIServer {
    public static void main(String args[]) 
	{
	
		int port=1500;
		ServerSocket ss;
		BufferedReader input;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		boolean successPort = false;
		String strPort = "";
		
		/*
		 * Set up port number
		 */
		while(!successPort)
		{
			try
			{
				System.out.println("Please enter a port number, or press 'Enter' to use the default port (1500)");
				strPort = br.readLine();
				System.out.println(strPort);
				if(strPort.equals(""))
				{
					port = 1500;
					successPort = true;
				}
				else
				{
					port = Integer.parseInt(strPort);
					successPort = true;
				}
			}
			catch (Exception e)
			{
				System.out.println("Error, please enter a port number or press 'Enter' to use default port (1500)");
			}
		}
		
		
		System.out.println("\n\n*********************************");
		System.out.println("***********Serveur***************");
		System.out.println("*********************************\n\n");
		
		//Ouverture du socket en attente de connexions
		try 
		{
			ss = new ServerSocket(port);
			System.out.println("Socket Server is waiting for clients to connect on port " + port);
			
			// boucle infinie: traitement d'une connexion client
			while(true) 
			{
				int trameNumber = 0;
				String sMsgReceived = "";
				
				Socket socket = ss.accept();
				String sIPandPort = socket.getInetAddress() + ":" + socket.getPort();
				sIPandPort = sIPandPort.substring(1, sIPandPort.length()-1);
				
				String sConsoleHeader =  "\t" + sIPandPort + " : ";
				
				System.out.println("Accepting new connection from " + sIPandPort);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
				PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

				sendMessage(socket, sConsoleHeader, input, output, MessageType.eMsgBienvenue); //send msg de bienvenue				
				receiveMessage(socket, sConsoleHeader, input, output); //receive all msgs
				sendMessage(socket, sConsoleHeader, input, output, MessageType.eMsgAuRevoir );
			}
			
			
		}
		catch (IOException e) 
		{
			System.out.println(e);
		}
    }
    
	private static void sendMessage(Socket socket, String sConsoleHeader, BufferedReader input, PrintWriter output, MessageType eMT) {
		int trameNumber = 0;

		try
		{
			System.out.println(determineHeaderBanner(eMT));
			String msgSendMsg = trameNumber + "*" + determineMsgToSend(eMT);
			output.println(msgSendMsg);

			System.out.println(sConsoleHeader + "Transmission de la trame " + trameNumber + "(" + msgSendMsg.getBytes().length + " bytes" + ")" );

			System.out.println(sConsoleHeader + "Activation du timeout 1000ms");
			boolean bMsgReceived = false;
			long endTimeMillis = System.currentTimeMillis() + 1000;
			while (System.currentTimeMillis() < endTimeMillis) 
			{
				String sMsgReceived = input.readLine();

				if(sMsgReceived != null)// && sMsgReceived.equals(trameNumber + "*ClientMsgReceived"));
				{
					int iReceivedTrameNumber = getTrameNumber(sMsgReceived);
					sMsgReceived = isolateMessage(sMsgReceived);
					if(determineIncomingMsgType(sMsgReceived) == MessageType.eMsgClientConfirmation){
						bMsgReceived = true;
						System.out.println(sConsoleHeader + "Recu acquittement de la transmission de trame " + trameNumber );
						waitInMS(1000);
						
						if(eMT == MessageType.eMsgAuRevoir)
						{
							try 
							{
								socket.close();
								System.out.println(determineFooterBanner(eMT));
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
				System.out.println(sConsoleHeader + "Timeout a la reception de l'acquittement trame " + trameNumber);
			}
		}
		catch (IOException e) 
		{
			System.out.println(e);
		}

	}

	private static void receiveMessage(Socket socket, String sConsoleHeader, BufferedReader input, PrintWriter output) 
	{
		try 
		{
			while(true) 
			{
				String sMsgReceived = input.readLine();
				if(sMsgReceived != null)
				{
					int iTrameNumber = getTrameNumber(sMsgReceived);
					sMsgReceived = isolateMessage(sMsgReceived);
					MessageType eMT = determineIncomingMsgType(sMsgReceived);
					System.out.println(determineHeaderBanner(eMT)); //("Recevoir bienvenue");
					System.out.println(sConsoleHeader + "Recu la transmission de la trame " + iTrameNumber + "(" + sMsgReceived.getBytes().length + " octets)");

					output.println(iTrameNumber + "*ClientMsgReceived");
					System.out.println(sConsoleHeader + "Acquittement de la transmission de la trame " + iTrameNumber);

					/*
					 * Verify, what conditions need to be met for this msg to appear?
					 */
					System.out.println(sConsoleHeader + "Accept la transmission de la trame " + iTrameNumber + "(" + sMsgReceived.getBytes().length + " octets)");

					System.out.println(determineFooterBanner(eMT));
					//break;
					if(eMT == MessageType.eMsgFin)
					{
						break;
					}
				}
			}
		}
		catch (IOException e) 
		{
			System.out.println(e);
		}
	}

	private static String determineFooterBanner(MessageType eMT) {
		if(eMT == MessageType.eMsgTest)
			return "Recu la commande: test";
		else if(eMT == MessageType.eMsgFin)
			return "Recu message de fin";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Deconnecte, le socket est ferme";
		return "";
	}

	public enum MessageType
	{
		eMsgBienvenue,
		eMsgTest,
		eMsgFin,
		eMsgAuRevoir,
		eMsgClientConfirmation,
		eUnknown
	}

	private static MessageType determineIncomingMsgType(String strMsgReceived)
	{
		if(strMsgReceived.equals("Connexion réussie. Bienvenue !"))
			return MessageType.eMsgBienvenue;
		if(strMsgReceived.equals("ClientMsgReceived"))
			return MessageType.eMsgClientConfirmation;
		if(strMsgReceived.equals("test"))
			return MessageType.eMsgTest;
		if(strMsgReceived.equals("fin"))
			return MessageType.eMsgFin;
		return MessageType.eUnknown;	
	}

	private static String determineMsgToSend(MessageType eMT)
	{
		if(eMT == MessageType.eMsgBienvenue)
			return "Connexion réussie. Bienvenue !";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Au revoir client!";
		return "";
	}

	private static String determineHeaderBanner(MessageType eMT)
	{
		if(eMT == MessageType.eMsgBienvenue)
			return "Envoi du message de bienvenue";
		else if(eMT == MessageType.eMsgTest)
			return "Recevoir la commande: test";
		else if(eMT == MessageType.eMsgFin)
			return "Recevoir message de fin";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Envoi du message au revoir";
		return "";
	}
	
	
    private static void waitInMS(int ms)
    {
		long endTimeMillis = System.currentTimeMillis() + ms;
		while (System.currentTimeMillis() < endTimeMillis) {}
    	return;
    }
    private static String isolateMessage(String sMsgReceived)
    {
    	int iTrameEndIndex = sMsgReceived.indexOf("*");
    	return sMsgReceived.substring(iTrameEndIndex + 1, sMsgReceived.length());
    }
    
	private static int getTrameNumber(String sMsgReceived) {
		int iTrameEndIndex = sMsgReceived.indexOf("*");
		if(iTrameEndIndex == -1) 
			return -1;
		String sTemp = sMsgReceived.substring(0, iTrameEndIndex);
		return Integer.parseInt(sTemp);
	}
}
