/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicApp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

//this class is handling the connection of the clients and is the server that when gets a client it will open a thread to service him
public class Server {

    //list of clienthandler class/threads meaning a list of clients
    List<ClientHandler> clHandList = new ArrayList<ClientHandler>();
            
    //this method creates a new thread client handler and adds a client there in order to serve them the app
    public void addClient2handler(Socket soc)
    {
        ClientHandler clHand = new ClientHandler();
        
        clHand.addclient(soc);
        clHand.start();
        clHandList.add(clHand);
        
    }
    //this method returns a boolean true if a server is full or false if it can accept clients
    public boolean isFull()
    {
        boolean full=false;
        int maxClients = 5;//this number represents the max number of clients a server can have, it can be changed, it is five for testing purposes, depending on the machine it can as high as we want
        if(clHandList.size()==maxClients)
        {
            full=true;
        }
        else
        {
            full=false;
        }
        return full;
       
    }
    //this method returns a boolean if a server is available to accept another client
    public boolean isAvailable()
    {
        if(!isFull())
        {
            return true;
        }
        else 
        {
            return false;

        }
       
    }
    //this method connects to client handler in order to get the last song streamed or downloaded and return to the loadbalance to achieve client server client communication
    public void getLastSongs(List<String> lastDLsongs,List<String> lastSTRsongs)
    {
        for(ClientHandler ch : clHandList)
        {
            for(String dl : lastDLsongs)
            {
                
                ch.otherUsersLastDownloaded.add(dl);
            }
            ch.otherUsersLastDownloaded.clear();
            
            for(String str : lastSTRsongs)
            {
                
                ch.otherUsersLastStreamed.add(str);
            }
            ch.otherUsersLastStreamed.clear();
        }
    }
    //like the above method but this time for adding songs to the list and not retrieving
    public void setLastSongs(List<String> lastDLsongs,List<String> lastSTRsongs)
    {
        for(ClientHandler ch : clHandList)
        {
            lastDLsongs.add(ch.lastDownloaded);
            lastSTRsongs.add(ch.lastStreamed);
        }
    }
    
   //this method is for removing clients that are still connected but have not done any action in a while so they are considered inactive and removed     
    public void RemoveInactiveClientHandlers() throws IOException
    {

        List<Integer> tempList = new ArrayList<Integer>();

        int tempint = clHandList.size();
        for(int i=0; i<tempint; i++)
        {

            if(!clHandList.get(i).isActive())
            {
                tempList.add(i);
            }
            
        }

        for(int j : tempList)
        {
            clHandList.get(j).clientSocket.close();
            clHandList.get(j).stopThread();
            clHandList.remove(j);

        }

    }
    //this method is for removing clients that chose to exit the app and close their resources gracefully
    public void RemoveExitedClients() throws IOException
    {
        List<Integer> tempList = new ArrayList<Integer>();
        int tempint = clHandList.size();
        
        for(int i=0; i<tempint; i++)
        {
            
            if(clHandList.get(i).terminatesenpai)
            {
                tempList.add(i);
            }
            
        }
        for(int j : tempList)
        {
            clHandList.get(j).clientSocket.close();
            clHandList.get(j).stopThread();
            clHandList.remove(j);
        }
            
        
    }
    
    
}
