package ClientPkg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TIClient {

	public static void main(String[] args) 
	{
		int iMenuSelection = showMenuReturnChoice();
		if(iMenuSelection == 0)
		{
			System.out.println("Error, somehow menu selection is 0");
		}
		establishConnection(iMenuSelection);
	}

	public static int showMenuReturnChoice()
	{
		boolean validInput = false;
		while(!validInput)
		{
			System.out.println("******Bienvenu******");
			System.out.println("Menu :");
			System.out.println("1 : Tester une connexion au serveur");
			System.out.println("2 : Transferer un ficher vers le serveur");
			System.out.println("3 : Lister le contenu du repetoire courant du serveur");
			System.out.println("4 : Quitter l'application");
			System.out.println("Faites votre choix : ");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String sUserInput = "";
			try {
				sUserInput = br.readLine();
				int iUserSelection = Integer.parseInt(sUserInput);
				if(iUserSelection > 0 && iUserSelection < 5)
				{
					return iUserSelection;
				}
				else
				{
					System.out.println("****** Please choose a menu item from 1 to 4 ******");
				}
			} catch (Exception e) {
				System.out.println("****** Your selection: " + sUserInput + " is not valid, try again please ******" );
			}
			
		}
		return 0;
	}
	public static void establishConnection(int option)
	{
		//port et adresse
		int port=1500;
		InetAddress adresse=null;

		//socket
		Socket socket = null;

		//input-output
		BufferedReader input;
		PrintWriter output;

		String lineToBeSent;

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/*
		 * INPUT IP ADDRESS
		 */
		boolean successIP = false;
		String strIP = "";

		System.out.println("Please enter an IP address, or press 'Enter' to use the default IP address (127.0.0.1)");
		while(!successIP)
		{
			try
			{
				strIP = br.readLine();
				System.out.println(strIP);
				if(strIP.equals(""))
				{
					adresse = InetAddress.getByName("127.0.0.1");
					successIP = true;
				}
				else
				{
					adresse = InetAddress.getByName(strIP);
					successIP = true;
				}
			}
			catch (Exception e)
			{
				System.out.println("Error, please enter an IP address or press 'Enter' to use default IP address (127.0.0.1)");
			}
		}

		/*
		 * INPUT PORT NUMBER
		 */
		boolean successPort = false;
		String strPort = "";

		System.out.println("Please enter a port number, or press 'Enter' to use the default port (1500)");
		while(!successPort)
		{
			try
			{
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

		//connexion au serveur
		try 
		{
			System.out.println("Etablissement de connexion avec le serveur " +
					adresse.getHostAddress()+
					":" + port);
			socket = new Socket(adresse, port);

			System.out.println("Serveur " +  socket.getInetAddress() + ":" + socket.getPort()+ " est maintenant connecte");

		} catch (UnknownHostException e) {
			System.out.println("\nServeur " + adresse +":"+ port + " inconnu.");
			return; // Si serveur inconnu, la fonction arrete ici.
		}
		catch (IOException e) 
		{
			System.out.println("connexion échouée, adresse/port incorrect");
			//erreur, on quitte
			System.exit(1);
		}

		String sIPandPort = socket.getInetAddress() + ":" + socket.getPort();
		sIPandPort = sIPandPort.substring(1, sIPandPort.length()-1);
		String sConsoleHeader =  "\t" + sIPandPort + " : ";

		try {
			output = new PrintWriter(socket.getOutputStream(),true);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

			receiveMessage(socket, sConsoleHeader, input, output); //receive bienvenue messager
			if(option == 1)
			{
				sendMessage(socket, sConsoleHeader, input, output, MessageType.eMsgTest);
				sendMessage(socket, sConsoleHeader, input, output, MessageType.eMsgFin);
				receiveMessage(socket, sConsoleHeader, input, output); //receive au revoir
			}
		} catch (IOException e1) {
			e1.printStackTrace();
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
				if(sMsgReceived != null && sMsgReceived.equals(trameNumber + "*ServerMsgReceived"));
				{
					bMsgReceived = true;
					System.out.println(sConsoleHeader + "Recu acquittement de la transmission de trame " + trameNumber );
					waitInMS(1000);
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

					if(eMT == MessageType.eMsgBienvenue)
					{
						System.out.println("Bienvenue client " + socket.getInetAddress() + ":" + socket.getPort());
					}
					if(eMT == MessageType.eMsgAuRevoir)
					{
						try 
						{
							socket.close();
							System.out.println("Au revoir client " + socket.getInetAddress() + ":" + socket.getPort());
						}
						catch (IOException e) 
						{
							System.out.println(e);
						}
					}
					break;
				}
			}
		}
		catch (IOException e) 
		{
			System.out.println(e);
		}
	}

	public enum MessageType
	{
		eMsgBienvenue,
		eMsgTest,
		eMsgFin,
		eMsgAuRevoir,
		eUnknown
	}

	private static MessageType determineIncomingMsgType(String strMsgReceived)
	{
		if(strMsgReceived.equals("Connexion réussie. Bienvenue !"))
			return MessageType.eMsgBienvenue;
		if(strMsgReceived.equals("Au revoir client!"))
			return MessageType.eMsgAuRevoir;
		return MessageType.eUnknown;	
	}

	private static String determineMsgToSend(MessageType eMT)
	{
		if(eMT == MessageType.eMsgTest)
			return "test";
		else if(eMT == MessageType.eMsgFin)
			return "fin";
		return "";
	}

	private static String determineHeaderBanner(MessageType eMT)
	{
		if(eMT == MessageType.eMsgBienvenue)
			return "Recevoir bienvenue";
		else if(eMT == MessageType.eMsgTest)
			return "Envoi de la commande: test";
		else if(eMT == MessageType.eMsgFin)
			return "Envoi du message de fin";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Recevoir au revoir";
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
