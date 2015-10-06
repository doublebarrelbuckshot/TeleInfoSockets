package ClientPkg;

import java.io.BufferedReader;

import SendReceiveServices.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.print.DocFlavor.URL;

public class TIClient {

	public static void main(String[] args) 
	{
		int iMenuSelection = showMenuReturnChoice();
		if(iMenuSelection == 4)
		{
			System.out.println("Application Exited Normally");
			System.exit(0);
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
					System.out.println("****** Please choose a menu item from 1 to 5 ******");
				}
			} 
			catch (Exception e) 
			{
				System.out.println("****** Your selection: " + sUserInput + " is not valid, try again please ******" );
			}
		}
		return 0;
	}


	public static void establishConnection(int option)
	{
		int port=1500;
		InetAddress adresse=null;
		Socket socket = null;
		ObjectInputStream inputStream = null;
		ObjectOutputStream outputStream = null;
		BufferedReader input;
		PrintWriter output;
		String lineToBeSent;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		adresse  = getUserInputIpAddress(br);
		port = getUserInputPortNumber(br);

		//Connect to server
		try 
		{
			System.out.println("Etablissement de connexion avec le serveur " +
					adresse.getHostAddress()+
					":" + port);
			socket = new Socket(adresse, port);

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());


			System.out.println("Serveur " +  socket.getInetAddress() + ":" + socket.getPort()+ " est maintenant connecte");

		} 
		catch (UnknownHostException e) 
		{
			System.out.println("\nServeur " + adresse +":"+ port + " inconnu.");
			return; // Si serveur inconnu, la fonction arrete ici.
		}
		catch (IOException e) 
		{
			System.out.println("connexion échouée, adresse/port incorrect");
			System.exit(1);
		}

		String sIPandPort = socket.getInetAddress() + ":" + socket.getPort();
		sIPandPort = sIPandPort.substring(1, sIPandPort.length()-1);
		String sConsoleHeader =  "\t" + sIPandPort + " : ";

		try {
			output = new PrintWriter(socket.getOutputStream(),true);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

			ReceiveServices.receiveMessage(socket, sConsoleHeader, input, output, outputStream, inputStream); //receive bienvenue messager
			if(option == 1)
			{
				performTest(socket, sConsoleHeader, input, output, outputStream, inputStream);			
			}
			if(option == 2)
			{
				sendFile(socket, sConsoleHeader, input, output, outputStream, inputStream, br);
			}
			if(option == 3)
			{
				getDir(socket, sConsoleHeader, input, output, outputStream, inputStream, br);
			}

		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
	}

	private static void getDir(Socket socket, String sConsoleHeader, BufferedReader input, PrintWriter output,ObjectOutputStream outputStream, ObjectInputStream inputStream, BufferedReader br) {

			SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgSendDir, null);
			ReceiveServices.receiveMessage(socket, sConsoleHeader, input, output, outputStream, inputStream); //get dir msg
			SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgFin, null);
			ReceiveServices.receiveMessage(socket, sConsoleHeader, input, output, outputStream, inputStream); //receive au revoir
			
			System.out.println("\nLister les fichers termine");
	}

	private static void performTest(Socket socket, String sConsoleHeader,
			BufferedReader input, PrintWriter output, ObjectOutputStream outputStream, ObjectInputStream inputStream) 
	{
		//Send test msg
		SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgTest, null);
		//Send fin msg
		SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgFin, null);
		ReceiveServices.receiveMessage(socket, sConsoleHeader, input, output, outputStream, inputStream);

		System.out.println("\nTest de connexion termine");

	}

	private static void sendFile(Socket socket, String sConsoleHeader,
			BufferedReader input, PrintWriter output,
			ObjectOutputStream outputStream, ObjectInputStream inputStream, BufferedReader br) 
	{
			String sFileName = "";
			try 
			{
				sFileName = getUserInputFileName(br);
				
				//System.out.println("Enter file name");
				//String sFileName = br.readLine(); //"test.txt";
				int iIndexDot = sFileName.lastIndexOf('.');
				String sNewFileName = new StringBuilder(sFileName).insert(iIndexDot, "_cp").toString();
				//System.out.println("********STRING!!: " + str);
				File file = new File(sFileName);
				byte[] fileInBytes = new byte[(int) file.length()];
				
				FileInputStream fileInputStream = new FileInputStream(file);
				fileInputStream.read(fileInBytes);
				fileInputStream.close();
				Message msgFileToSend = new Message(fileInBytes, MessageType.eMsgFileFragment);	   

				//Send upload msg
				SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgUpload, null);

				//Create filename msg and send
				Message msgFileName = new Message(sNewFileName, MessageType.eMsgFilename);
				SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgFilename, msgFileName);

				//Send all file fragments
				SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgFileFragment, msgFileToSend);

				//Send fin msg
				SendServices.sendMessage(socket, sConsoleHeader, input, output, outputStream, inputStream, MessageType.eMsgFin, null);

				//Receive closing msg
				ReceiveServices.receiveMessage(socket, sConsoleHeader, input, output, outputStream, inputStream); //receive au revoir
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (StringIndexOutOfBoundsException e)
			{
				System.out.println("Error with file name, try again");
			}
			
			System.out.println("\nTransfert de fichier termine: " + sFileName);

		
	}

	private static String getUserInputFileName(BufferedReader br) {
		boolean bSuccessFileName = false;
		String strFileName = "";
		System.out.println("Please enter a file name with the extension:");
		while(!bSuccessFileName)
		{
			FileInputStream fileInputStream = null;
			try
			{
				strFileName = br.readLine();
				if(strFileName.equals(""))
				{
				
				}
				else
				{
					int iIndexDot = strFileName.lastIndexOf('.');
					File file = new File(strFileName);
					if(file.exists() && !file.isDirectory()) 
					{
						bSuccessFileName = true;
						break;
					}
				}
				System.out.println("Error, please enter a file name with the extension");

			}
			catch (Exception e)
			{
				System.out.println("Error, please enter a file name with the extension");
			}
			
		}
		return strFileName;
	}

	/*
	 * INPUT PORT NUMBER
	 */
	private static int getUserInputPortNumber(BufferedReader br) 
	{
		boolean successPort = false;
		String strPort = "";
		int port = 0;
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
		return port;
	}

	/*
	 * INPUT IP ADDRESS
	 */
	private static InetAddress getUserInputIpAddress(BufferedReader br) 
	{
		boolean successIP = false;
		String strIP = "";
		InetAddress adresse = null;
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
		return adresse;
	}
}
