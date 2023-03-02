/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicApp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */
//this is the access server class where is the first point of contact with the clients
//here the client request connection and if any server is available it connects the client there
//otherwise the load balancer is called and if necessery it creates a new server instance to service new clients
public class AccessServer {

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        // TODO code application logic here
        //instance of load balancer class
        LoadBalancer LB = new LoadBalancer();
        
        int port = 42069;//port number of access server
        //new server socket is created at specified port
        ServerSocket servSoc = new ServerSocket(port);
        //server prints the ip address so it is known in case we run the files of the client in different machine than the server
        System.out.println(servSoc.getInetAddress().getLocalHost().getHostAddress());
        //a new empty socket to connect the client to        
        Socket soc2serv = null;
        // message in server console in order to know if it is runnung or not
        System.out.println("Server up and running");
        //this lists are for the "what are other users listen to, they store the last songs downloaded or streamed
        List<String> lastDownloaded = new ArrayList<String>();
        List<String> lastStreamed = new ArrayList<String>();
        //duplicate checker is a class where it if last song list have duplicates in order to remove them 
        Check4DuplicateSongs dc = new Check4DuplicateSongs();
        
        while(true)//this while is for constant listening for client requests
        {
            
            //the server accepts the connection and pass it to a socket in the server class
            soc2serv = servSoc.accept();
            System.out.println("accepting client");//message to know that a new client has been accepted
            LB.passSoc(soc2serv);//load balancer class calls its pass socket method in order to pass it to the appropriate server
            System.out.println("sending client to server");//message to let us know that the client has been passed in to a server
            //the load balancer class calls its balancer servers method in order to check if the servers are full or not and if needed to create a new server
            LB.balanceServers();
            System.out.println("client sent to appropriate server");
            
            LB.LBsetLastSongs(lastDownloaded, lastStreamed);//the load balancer tracks and stores the last songs that where streamed or downloaded
            lastDownloaded = dc.checkListForDuplicates(lastDownloaded);//list checked for duplicates
            lastStreamed = dc.checkListForDuplicates(lastStreamed);//list checked for duplicates
            LB.LBgetLastSongs(lastDownloaded, lastStreamed);//list updated
            
            
        }
        
        
    }
    
    
    
    
}
