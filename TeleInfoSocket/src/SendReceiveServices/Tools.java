package SendReceiveServices;


public class Tools {
	public static String isolateMessage(String sMsgReceived)
	{
		int iTrameEndIndex = sMsgReceived.indexOf("*");
		return sMsgReceived.substring(iTrameEndIndex + 1, sMsgReceived.length());
	}

	public static int getTrameNumber(String sMsgReceived) {
		int iTrameEndIndex = sMsgReceived.indexOf("*");
		if(iTrameEndIndex == -1) 
			return -1;
		String sTemp = sMsgReceived.substring(0, iTrameEndIndex);
		return Integer.parseInt(sTemp);
	}

	
	public static String determineHeaderBannerReceive(MessageType eMT, MessageFragment mfFragmentReceived, String sMsgFileName)
	{
		if(eMT == MessageType.eMsgBienvenue)
			return "Recevoir bienvenue";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Recevoir au revoir";
		else if(eMT == MessageType.eMsgTest)
			return "Recevoir la commande: test";
		else if(eMT == MessageType.eMsgFin)
			return "Recevoir message de fin";
		else if(eMT == MessageType.eMsgUpload)
			return "Recevoir la commande";
		else if(eMT == MessageType.eMsgFileFragment)
			return "Reception de la trame " + mfFragmentReceived.getTrameNumber() + " de fichier " + sMsgFileName;
		else if(eMT == MessageType.eMsgFilename)
			return "Recevoir le nom de fichier";
		return "MISSING BANNER";
	}
	
	

	public static String determineHeaderBannerSend(MessageType eMT, String sFileName, MessageFragment mf)
	{
		if(eMT == MessageType.eMsgBienvenue)
			return "Envoi du message de bienvenue";
		else if(eMT == MessageType.eMsgTest)
			return "Envoi de la commande: test";
		else if(eMT == MessageType.eMsgFin)
			return "Envoi du message de fin";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Envoi du message au revoir";
		else if(eMT == MessageType.eMsgUpload)
			return "Envoi de la commande: upload";
		else if(eMT == MessageType.eMsgFilename)
			return "Envoi du nom de fichier: " + sFileName;
		else if(eMT == MessageType.eMsgFileFragment)
			return "Envoi de la trame " + mf.getTrameNumber() + " de fichier" ;
		return "MISSING BANNER";
	}
	
	public static MessageType determineIncomingMsgType(String strMsgReceived)
	{
		if(strMsgReceived.equals("Connexion réussie. Bienvenue!"))
			return MessageType.eMsgBienvenue;
		if(strMsgReceived.equals("Au revoir client!"))
			return MessageType.eMsgAuRevoir;
		if(strMsgReceived.equals("ClientMsgReceived"))
			return MessageType.eMsgClientConfirmation;
		if(strMsgReceived.equals("ServerMsgReceived"))
			return MessageType.eMsgServerConfirmation;
		if(strMsgReceived.equals("test"))
			return MessageType.eMsgTest;
		if(strMsgReceived.equals("fin"))
			return MessageType.eMsgFin;
		if(strMsgReceived.equals("upload"))
			return MessageType.eMsgUpload;
		return MessageType.eUnknown;	
	}
	
	
	
	public static void waitInMS(int ms)
	{
		long endTimeMillis = System.currentTimeMillis() + ms;
		while (System.currentTimeMillis() < endTimeMillis) {}
		return;
	}
	
//	public static String determineMsgToSend(MessageType eMT)
//	{
//		if(eMT == MessageType.eMsgTest)
//			return "test";
//		else if(eMT == MessageType.eMsgFin)
//			return "fin";
//		else if(eMT == MessageType.eMsgBienvenue)
//			return "Connexion réussie. Bienvenue !";
//		else if(eMT == MessageType.eMsgAuRevoir)
//			return "Au revoir client!";
//		return "";
//	}

	public static Message determineMsgToSend(MessageType eMT)
	{
		if(eMT == MessageType.eMsgTest)
			return new Message("test", eMT);
		else if(eMT == MessageType.eMsgFin)
			return new Message("fin", eMT);
		else if(eMT == MessageType.eMsgBienvenue)
			return new Message("Connexion réussie. Bienvenue!", eMT);
		else if(eMT == MessageType.eMsgAuRevoir)
			return new Message("Au revoir client!", eMT);
		else if(eMT == MessageType.eMsgUpload)
			return new Message("upload", eMT);
		return new Message("", eMT);
	}

	
	public static String determineFooterBanner(MessageType eMT) {
		if(eMT == MessageType.eMsgTest)
			return "Recu la commande: test";
		else if(eMT == MessageType.eMsgUpload)
			return "Recu la commande upload";
		else if(eMT == MessageType.eMsgFin)
			return "Recu message de fin";
		else if(eMT == MessageType.eMsgAuRevoir)
			return "Deconnecte, le socket est ferme";

		return "";
	}

	




	

}
