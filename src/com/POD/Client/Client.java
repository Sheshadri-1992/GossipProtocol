package com.POD.Client;

import com.POD.Interface.*;
import com.POD.Server.MessageUnit.ClockNData;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


// java -classpath  bin: com.POD.Server.Server 4

public class Client {

	public static Registry register;
	public static GossipInterface stub ;
	public static ClockNData.Builder msgNClock;

	
	public static void prepareMessage(String originator_id,String line,int clock)
	{
		msgNClock = ClockNData.newBuilder();
		msgNClock.setId(Integer.parseInt(originator_id));
		msgNClock.setMsgLine(line);
		msgNClock.setClock(clock);
		
	}
	
	public void myMethod(String from_id,String to_id,String line_args,int clock,String originator_id)
	{
		
//		System.out.println("POD_Codemore welcomes you ");
		
		int base_port = 8999;
		base_port = base_port+Integer.parseInt(to_id);
		
		String baseString = base_port+"";
		
//		String baseString = "900"+Integer.parseInt(to_id);
		int port = Integer.parseInt(baseString);
		
		String lookupString = "Gossip"+Integer.parseInt(to_id);
		
//		System.out.println("Sending from "+sending_id_args+" Sending to "+id_args);
		
		try
		{
			Registry register = LocateRegistry.getRegistry("localhost",port);
			GossipInterface stub = (GossipInterface)register.lookup(lookupString);
			prepareMessage(originator_id, line_args, clock);
			stub.hearGossip(msgNClock.build().toByteArray());
//			register.unbind(lookupString);			
//			stub.sendToClient(Integer.parseInt(sending_id_args),line_args);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
