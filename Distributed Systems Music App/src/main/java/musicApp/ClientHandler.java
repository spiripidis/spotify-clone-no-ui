/*
 * 
 * 
 * encrypt/decrypt file (and how it will work)
 * 
 */
package musicApp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

//this class/ thread is used to serv the connected client, each client has his own handler in order to serve all clients in the same time
public class ClientHandler extends Thread {

    Socket clientSocket=null;
    ServerCommunicator com;
    boolean ClientHandlerRunCondition = true;
    public boolean terminatesenpai=false;
    
    String lastDownloaded = "";
    String lastStreamed = "";
    
    List<String> otherUsersLastDownloaded = new ArrayList<>();
    List<String> otherUsersLastStreamed = new ArrayList<>();
    
    //this method is to aquire the socket of the client
    public void addclient(Socket soc)
    {
        clientSocket=soc;
    }
    //this method is to connect the communicator class to the client streams 
    public void initcom() throws IOException
    {
        com = new ServerCommunicator(clientSocket);
    }
    
    //this method returns a boolean, it checks if a client is active or has not taken an action for more than 15 minutes
    public boolean isActive() throws IOException
    {
       boolean active = true;
       
       if((System.currentTimeMillis()-com.lastAction.getTime())>(15*60*1000)) //time of server not reading anything from client 15mins
       {
           System.out.println("client timed out "+clientSocket);
           com.closeCommunicator();
           active = false;
       }
              
       return active;
    }
    
    //in order to stop the client handler i change the condition of the loop so it will exit the thread when reaches the end according to the documents by oracle for java threads
    public void stopThread()
    {
        ClientHandlerRunCondition = false;
    }
    
    
    //the thread runs here
    @Override
    public void run()
    {
        
        boolean runningCondition;
        HashMap<String,String> userpass = new HashMap<String,String>();
        HashMap<String,String> newEntries = new HashMap<String,String>();
        FileManipulation fileManip =new FileManipulation();
        String w8="w8";
        boolean loggedin=false;
        List<String> songlist = new ArrayList<String>();
        String choice="";
        boolean filemanipulation = false;
        String pathname="";
        String username="";
        fileManip.removeLoggedInUser("=empty=");
        
        
        
        try
        {
           initcom();//initalize communication
            
           //System.out.println("i started trying");
           runningCondition = com.initialHandshake();//handshake is done here
           //System.out.println("finished HS");
           int counter=0;
           
           com.sendMessage("Welcome to THE MUSIC APP (tm Distributed Systems) \n "
                       + "Press the number of the choice you want and press enter to validate");//message for client 
           
           fileManip.ReadFromFile("userpass.txt", userpass);
           
           while(runningCondition&&ClientHandlerRunCondition)//this loop keeps the thread alive and runs for as long the conditions are met
           {

               if(!loggedin)//if client not logged in we prompt him to log in or register if he does not have an account
               {
                   com.sendMessage("1.Register\n"+"2.Log In\n"+"0.Exit"+w8);//menu for client
                   choice = com.recieveMessage();
                   switch(choice)
                   {
                       case "1":
                       {
                           com.sendMessage("please give a username longer than 4 characters"+w8);
                           username = com.recieveMessage();
                           if(username.length()<=4)
                           {
                               com.sendMessage("username too short, try another username");//check if username standards are met
                           }
                           else if(userpass.containsKey(username)||newEntries.containsKey(username))//check if username already exists
                           {
                               com.sendMessage("username already exists");
                           }
                           else
                           {
                               com.sendMessage("username accepted \n Please give a password, longer than 4 characters"+w8);//accepting username, waiting for password
                               String password = com.recieveMessage();
                               if(password.length()<=4)//check if password conditions are met
                               {
                                   com.sendMessage("invalid password, too short");
                               }
                               else 
                               {
                                   newEntries.put(username, password);//creating new user, using hashmap we keep track of usernames and passwords
                                   com.sendMessage("Account Created");
                               }
                           }

                           break;
                       }
                       case "2"://if account exists and client wants to log in
                       {
                           com.sendMessage("Write your Username :"+w8);
                           username = com.recieveMessage();
                           
                           if(fileManip.isUserAlreadyLoggedIn(username))//check if user is already logged in
                           {
                               com.sendMessage("User is already logged in");
                               break;
                           }
                           
                           
                           if(userpass.containsKey(username)||newEntries.containsKey(username))//check if username exists 
                           {
                               com.sendMessage("username correct\n insert password"+w8);
                               String password = com.recieveMessage();
                               if(password.equals(userpass.get(username))||password.equals(newEntries.get(username)))//check if password matches username
                               {
                                   com.sendMessage("correct password\n Welcome "+username);
                                   loggedin=true;
                                   //next menu switch case starts here or use booleans to if() and there the switch if true
                               }
                               else
                               {
                                   com.sendMessage("wrong password");
                               }
                           }
                           else
                           {
                               com.sendMessage("username does not exist");
                           }

                           break;
                       }
                       case "0"://if clients chose to exit 
                       {
                           
                           com.sendMessage("If you really want to exit type EXIT else type no"+w8);//check if client pressed exit by mistake
                           
                           if(com.terminationHandshake())
                           {
                              
                               terminatesenpai=true;//flag to let the server know that client wants to exit
                               runningCondition = false;//kill thread by stoping the loop
                               com.closeCommunicator();//close streams of communication
                           }
                           
                           break;
                       }
                       default :
                           if(com.SocExep)//if something wrong with the socket close communication
                            {
                                runningCondition = false;
                                
                            }
                           
                           
                           com.sendMessage("invalid choice, please try again");
                           break;
                   }
               }

               if(loggedin)//if the user is logged in
               {
                                                                            
                   //menu for user to chose from
                   com.sendMessage("1.List of Song \n 2.Search \n 3.other users listen to\n 4.Listen to Downloaded songs \n"+"0.EXIT"+w8);
                                      
                   
                   choice=com.recieveMessage();
                   switch(choice)
                   {
                       case "1"://show list of songs to client to chose action
                       {
                           //read txt list, show to client

                           fileManip.ReadFromFile("list of songs.txt", songlist);//read and show available songs to client

                           for(String i: songlist)
                           {
                               com.sendMessage(songlist.indexOf(i)+". "+i);
                           }
                           //at this point we wain for a response from the client
                           com.sendMessage("end of list"+w8);
                           choice = com.recieveMessage();
                           while(Integer.parseInt(choice)<0||Integer.parseInt(choice)>=songlist.size())//check client input
                           {
                               com.sendMessage("invalid choice, please select again"+w8);
                               choice = com.recieveMessage();
                               
                                if(com.SocExep)//if something wrong with socket close communication
                                {
                                    runningCondition = false;
                                    break;
                                }
                           }
                           pathname = songlist.get(Integer.parseInt(choice));
                           filemanipulation =true;//flag to prompt new menu for client
                           
                           
                          
                           songlist.clear();
                           break;
                       }
                       case "2"://custom search for term in order to show all entries that contain term
                       {
                           fileManip.ReadFromFile("list of songs.txt", songlist);
                           
                           List<String> searchResults = new ArrayList<String>();
                           com.sendMessage("enter a search term"+w8);//waiting for client response
                           choice=com.recieveMessage();
                           choice=choice.toLowerCase();
                           for(String i: songlist)
                           {

                               String comparator=i.toLowerCase();
                               if(comparator.contains(choice))
                               {
                                   searchResults.add(i);
                               }   
                           }
                           if(searchResults.isEmpty())//if no result tell client
                           {
                               com.sendMessage("no results found");
                               
                           }
                           else
                           {
                               for(String i: searchResults)//show list of results to client for the searched term
                               {
                                   com.sendMessage(searchResults.indexOf(i)+". "+i);
                               }
                               com.sendMessage("end of list"+w8);
                               choice = com.recieveMessage();
                               while(Integer.parseInt(choice)<0||Integer.parseInt(choice)>=searchResults.size())
                               {
                                    com.sendMessage("invalid choice, please select again"+w8);
                                    choice = com.recieveMessage();
                                    
                                    if(com.SocExep)//again handling socket exceptions
                                    {
                                        runningCondition = false;
                                        break;
                                    }
                                    
                               }
                               pathname = searchResults.get(Integer.parseInt(choice));
                               filemanipulation =true;
                           }
                           
                           songlist.clear();
                           break;
                       }
                       case "3"://what other users listen to
                       {
                           
                           com.sendMessage("Other users recently downloaded :");
                           for(String dl : otherUsersLastDownloaded)//list of the last song each connected user has downloaded
                           {
                               com.sendMessage(dl);
                           }
                           
                           com.sendMessage("Other users recently streamed :");//list of the last song each connected user has streamed
                           for(String str : otherUsersLastStreamed)
                           {
                               com.sendMessage(str);
                           }
                           
                           break;
                       }
                       case "0"://again exit confirmation
                       {
                           com.sendMessage("If you really want to exit type EXIT else type no"+w8);
                           
                           if(com.terminationHandshake())
                           {
                               terminatesenpai=true;
                               runningCondition = false;
                               com.closeCommunicator();
                           }
                           break;
                       }
                       case "4"://client chose to listen to already downloaded songs so server does nothing and resends the menu to client 
                           break;
                       default:
                           
                            if(com.SocExep)//again socket exception handling
                            {
                                runningCondition = false;
                            }
                           
                           System.out.println("CHOICE :  "  + choice);
                           com.sendMessage("invalid choice");
                           break;
                   }
               }
               else
               {
                   com.sendMessage("If you dont have an account please Register \n"+"otherwise log in correctly \n");
               }
               
               if(filemanipulation)//if client chose to recieve or stream a file
               {
                   
                   String fileReceived = "";
                   com.sendMessage("1. download file \n"+"2. Stream file \n"+w8);//menu of choices for client
                   choice = com.recieveMessage();
                   switch(choice)
                   {
                       case "1"://client chose to download a file
                       {
                           //download
                           com.sendMessage("sending file" + pathname+".wav");//a handshake in order to begin transmition
                           com.SendFile("songs/"+pathname+".wav");//may work may not, it is what it is
                           
                           lastDownloaded = pathname;
                           
                           
                           while(true)
                           {
                               fileReceived = com.recieveMessage();
                               
                               if(fileReceived.equals("OK"))//waiting for ok from client
                               {
                                   break;
                               }
                               else
                               {
                                   com.sendMessage("Operation will continue only if server receives OK"+w8);//tell client to send ok after transmition is finished
                                   //fileReceived = com.recieveMessage();
                               }
                               
                               if(com.SocExep)
                               {
                                   runningCondition = false;
                                   break;
                               }
                               
                            }
                           
                           break;
                       }
                       case "2"://client chose to stream a song
                       {
                           //stream
                           com.sendMessage("Starting Stream");
                           com.SendFile("songs/"+pathname+".wav");//sending the file
                           
                           lastStreamed = pathname;
                           
                           while(true)
                           {
                               
                               try
                               {
                                   fileReceived = com.recieveMessage();
                               }
                               catch(Exception e)
                               {
                                   break;
                               }
                                   
                               
                               if(fileReceived.equals("OK"))//again waiting for ok from client
                               {
                                   break;
                               }
                               else
                               {
                                   com.sendMessage("Operation will continue only if server receives OK");
                               }
                               
                               if(com.SocExep)
                               {
                                   runningCondition = false;
                                   break;
                               }
                               
                           }
                           
                           
                           break;
                       }
                       default :
                           com.sendMessage("invalid input");
                           break;
                               
                   }
                   choice = "";
                   filemanipulation = false;
               }
                   
                  
               
               counter++;
               
           }
            
            
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
        finally
        {
            if(!username.equals(""))
            {
                fileManip.removeLoggedInUser(username);
                System.out.println("User " + username + " logged out");
            }

            fileManip.Write2File("userpass.txt", newEntries);
        }
            
            
        
    }
    
}
