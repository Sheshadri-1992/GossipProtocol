
package com.POD.Server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import com.POD.Client.Client;
import com.POD.Interface.*;
import com.POD.Server.MessageUnit.ClockNData;
import com.google.protobuf.InvalidProtocolBufferException;

public class Server implements GossipInterface{

	
	public static String id=null;
	public static int no_of_processes =1;
	public static String filename = null;
	public static Client[] myClients;
	public static String new_filename = null;
	public static String output_file="Output";
	public static int[] vectorClock;
	//gossip interface methods
	//java -classpath  bin: com.POD.Server.Server 1 5 -i input.txt
	
	
	
	

	public void hearGossip(byte[] serializedMessage) throws RemoteException {
		// TODO Auto-generated method stub
		/**need to increment the clock of the current server because it just received an event **/
//		vectorClock[Integer.parseInt(id)]++;
		
		try {
			ClockNData obj = ClockNData.parseFrom(serializedMessage);
			
//			System.out.println("You are in process "+id);
			
			
//			System.out.print("Message originator is "+obj.getId()+" "+"The message is "+obj.getMsgLine()+ " ");
//			System.out.println("Originator's clock is "+obj.getClock()+ " "+"The local clock of originator "+obj.getId()+ " is "+vectorClock[obj.getId()]);
			
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(vectorClock[obj.getId()]==obj.getClock())
			{
				System.out.println("Reject / Ignore "+obj.getMsgLine());
			}
			else if(vectorClock[obj.getId()]>obj.getClock())//local clock > sent clock
			{
				System.out.println("Reject / Ignore"+obj.getMsgLine());
				processGossip("Reject "+obj.getMsgLine());
			}
			else
			{
				vectorClock[obj.getId()]=obj.getClock();
				
				
				
				System.out.println("Accept "+obj.getMsgLine());
				
				int[] random_id = new int[2];
				random_id[0]=Integer.parseInt(id);
				random_id[1]=Integer.parseInt(id);
				Random rn = new Random();
//				
				while(random_id[0]==Integer.parseInt(id) || random_id[0]==obj.getId())
				{
					random_id[0] =rn.nextInt(no_of_processes-1+1)+1;
				}
				
				while(random_id[1]==Integer.parseInt(id) || random_id[1]==random_id[0] || random_id[1]==obj.getId())
				{
					random_id[1] =rn.nextInt(no_of_processes-1+1)+1;
				}
//				System.out.println(random_id[0] + "<-- (in implementation of server) chosen random hosts--> "+random_id[1]);
				
				processGossip("Accept "+obj.getMsgLine());

//				vectorClock[Integer.parseInt(id)]++;

				for(int i=0;i<2;i++)
				{
					int val = random_id[i];
					String to_id = val+"";
					
//					System.out.println("Originator: " +obj.getId()+" Forwarding from "+id+" to "+ to_id);
					
					myClients[Integer.parseInt(id)-1].myMethod(id,to_id,obj.getMsgLine(),vectorClock[(obj.getId())],obj.getId()+"");
					try {

						Random rn1 = new Random();
						int sleep_random = rn1.nextInt(5)+1;
						
						sleep_random = sleep_random * 1000;
//						System.out.println("Forwader : "+ id + " Sleeping for "+sleep_random);
						try {
							Thread.sleep(sleep_random);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				
			}
			
			
		} catch (InvalidProtocolBufferException e) {
		
			e.printStackTrace();
		}
	}


	
	public void processGossip(String msg) throws RemoteException {
		// TODO Auto-generated method stub
//		System.out.println("processGossip: Got the message "+msg);
		//true = append file
		String file = output_file.concat(id);
		FileWriter fileWritter;
		try {
			fileWritter = new FileWriter((file),true);
		    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		    bufferWritter.write(msg);
		    bufferWritter.write("\n");
		    bufferWritter.close();
		    fileWritter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	public void sendToClient(int id_args,String args) {
		// TODO Auto-generated method stub
		System.out.println("Received this from server "+id_args + " message is "+args);
	}
	
	
	public static void main(String[] args)
	{
		
		if(args.length==2)
		{
			id=args[0];
			no_of_processes = Integer.parseInt(args[1]);
		}
		else if(args.length==4 || args.length==3)
		{
			id=args[0];
			no_of_processes = Integer.parseInt(args[1]);
			if(args.length==4)
				filename = args[3]; //because -i is argument
			else
				filename = args[2];
		}
		else
		{
			System.out.println("Invalid number of arguments!!");
			System.exit(0);
		}
		
		
		FileWriterClass outputFile = new FileWriterClass(output_file+id);
		outputFile.createFile();
		outputFile.closeFile();
	
		int base_port = 8999;
		base_port = base_port+Integer.parseInt(id);
		
		String baseString = base_port+"";
		
		Server obj = new Server();
		Registry register = null;
		try {
			
			GossipInterface stub = (GossipInterface) UnicastRemoteObject.exportObject(obj, Integer.parseInt(baseString));
			register = LocateRegistry.createRegistry(Integer.parseInt(baseString));
			register.rebind("Gossip"+id, stub);
			System.out.println("The port is "+baseString);
			System.out.println("Server is ready... Gossip"+id);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//each process waits for 5 mins, lets make it 2 mins
		
		
		try {
			
			/**Vector clock which holds each process**/
			vectorClock = new int[no_of_processes+1];
			
			new_filename = null;
			
			/**The following code prefixes the input file with its own process ID**/
			prefixFile();
			
			
			myClients = new Client[no_of_processes];
			for(int i=0;i<no_of_processes;i++)
			{
				myClients[i]=new Client();
			}

			
			int[] random_id = new int[2];
			random_id[0]=Integer.parseInt(id);
			random_id[1]=Integer.parseInt(id);
			
			Thread.sleep(60000);//20s
			System.out.println("This wont execute right away");
			
			if(filename!=null) //then it means the process is a generator
			{
				Random rn = new Random();
				while(random_id[0]==Integer.parseInt(id))
				{
					random_id[0] =rn.nextInt(no_of_processes-1+1)+1;
				}
				
				while(random_id[1]==Integer.parseInt(id) || random_id[1]==random_id[0])
				{
					random_id[1] =rn.nextInt(no_of_processes-1+1)+1;
				}
				
				
				FileReaderClass read_obj = new FileReaderClass(new_filename);
				read_obj.openFile();
				
				try {
					String line = read_obj.buff_reader.readLine();
					while(line!=null)
					{
						vectorClock[Integer.parseInt(id)]++;

						System.out.println(random_id[0] + "<--chosen hosts--> "+random_id[1]);
						for(int i=0;i<2;i++)
						{
							int val = random_id[i];
							String to_id = val+"";
							/**increment the clock before an event is sent **/
							myClients[Integer.parseInt(id)-1].myMethod(id,to_id,line,vectorClock[Integer.parseInt(id)],id);
							Thread.sleep(val*1000);
							
						}

						line = read_obj.buff_reader.readLine();
						
						random_id[0] =rn.nextInt(no_of_processes-1+1)+1;
						while(random_id[0]==Integer.parseInt(id))
						{
							random_id[0] =rn.nextInt(no_of_processes-1+1)+1;
						}
						
						random_id[1] =rn.nextInt(no_of_processes-1+1)+1;
						while(random_id[1]==Integer.parseInt(id) || random_id[1]==random_id[0])
						{
							random_id[1] =rn.nextInt(no_of_processes-1+1)+1;
						}
												
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				read_obj.closeFile();
				
//				register.unbind("Gossip"+id);
				
				/**Testing code **/
				/*Client myClient = new Client();
				myClient.myMethod("1");*/
			}
		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//if() the server has a input file it should send it to the 2 randomly chosen processes
		
	}
	
	/**method to prefix the input file **/
	public static void prefixFile()
	{
		if(filename!=null)
		{
			//create new file and fill records into it
			new_filename = filename.concat("."+id);
			FileWriterClass write_obj = new FileWriterClass(new_filename);
			write_obj.createFile();
			FileReaderClass read_obj = new FileReaderClass(filename);
			read_obj.openFile();
			
			try {
				String line = read_obj.buff_reader.readLine();
				while(line!=null)
				{
					String prefix = id+":";
					prefix = prefix.concat(line);
					write_obj.writeline(prefix);
					line = read_obj.buff_reader.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			read_obj.closeFile();
			write_obj.closeFile();
			
		}
	}

}
