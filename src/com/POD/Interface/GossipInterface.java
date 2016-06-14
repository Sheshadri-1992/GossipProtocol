package com.POD.Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GossipInterface extends Remote
{
	
	public void sendToClient(int id,String args) throws RemoteException;
	public void hearGossip(byte[] serializedMessage) throws RemoteException;
	public void processGossip(String msg) throws RemoteException;
}
