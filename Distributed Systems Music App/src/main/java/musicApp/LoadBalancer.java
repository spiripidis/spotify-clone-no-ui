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
//the load balancer class is responsible for checking servers if are at capacity and if they are it creates new servers
//also it handles some client-server-client communication
public class LoadBalancer {

    //a list of servers
    List<Server> serverList = new ArrayList<Server>();
    Socket soc = null;//a socket used to pass clients to server
            
    //this methods updates the list of the last downloaded and las streamed songs
    public void LBsetLastSongs(List<String> lastDLsongs,List<String> lastSTRsongs)
    {
        for(Server server: serverList)
        {
            server.setLastSongs(lastDLsongs,lastSTRsongs);
        }
    }
    //this method is also used in getting the last songs we have already on the list
    public void LBgetLastSongs(List<String> lastDLsongs,List<String> lastSTRsongs)
    {
        for(Server server : serverList)
        {
            server.getLastSongs(lastDLsongs, lastSTRsongs);
        }
    }
    //this method is used to get the socket of the client in order to pass it to a server later on
    public void passSoc(Socket clsoc)
    {
        soc=clsoc;
    }
    //this method adds a client to a server 
    public void AddClient2Server(Server server)
    {
        
        server.addClient2handler(soc);
    
    }
    //this method is used to create new servers when called
    public Server CreateServer()
    {
        Server servingServer = new Server();
        return servingServer;
    }
    //this method contains the balancing logic for the servers
    public void balanceServers() throws IOException
    {
        if(serverList.isEmpty())//if there is no server i create a new one
        {
            
            System.out.println("first instance of serving Server is created");
            serverList.add(CreateServer());
        }
        int fullServers=0;
        for (Server i: serverList)//cannot modify while in foreach
        {
            
            i.RemoveInactiveClientHandlers();//this method is from the class server and it removes inactive clients
            i.RemoveExitedClients();//this method also from class server removes clients that have chose to exit the app
            
            if(i.isAvailable())//if server is available then add client to available server
            {
                AddClient2Server(i);
                System.out.println("adding client to serving server "+serverList.indexOf(i) +" " +soc);
            }
            else if (i.isFull())
            {
                fullServers++;
                
                
            }
            
        }
        if(fullServers==serverList.size())//if all servers existing are full then create a new server and add client there
        {
                    serverList.add(CreateServer());
                    AddClient2Server(serverList.get(serverList.size()-1));
                    System.out.println("servers are full, creating new server to add client");
                    System.out.println("adding client to serving server "+serverList.indexOf(serverList.get(serverList.size()-1)) +" " +soc);
        }
        
    }
}
