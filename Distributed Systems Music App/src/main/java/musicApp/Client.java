
package musicApp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

//client class, contains all logic for client
public class Client {

    InetAddress ip ;//= InetAddress.getByName("localhost");
    int port; //= 42069;
    DataInputStream dis;
    DataOutputStream dos;
    Socket soc;
    ReentrantLock lock = new ReentrantLock(true);//used for mutual exclusion
    Scanner scn = new Scanner(System.in);
    String input;
    String output;
        
    BufferedInputStream bis;
    FileManipulation fileManip = new FileManipulation();
    Integer endoffile = new Integer(129);
    byte[] EndOfFile = new byte[1024];
    
    public void connect() throws UnknownHostException, IOException
    {
        System.out.println("Provide Server IP Address, if server is on same machine, give 127.0.0.1");//the client must provide the access server ip or if on same machine the local address
        ip = InetAddress.getByName(scn.nextLine());
        port = 42069;
        
        soc = new Socket(ip,port);//socket created
        
        dis = new DataInputStream(soc.getInputStream());//creating communication streams
        dos = new DataOutputStream(soc.getOutputStream());
        
        soc.setSoTimeout(2*60*1000);//internal time out of 2 minutes for the client
        
        bis = new BufferedInputStream(dis);
        
        for(int i = 0; i< EndOfFile.length;i++)
        {
            EndOfFile[i] = endoffile.byteValue();
        }
       
    }
    
    //handshake made to establish connection with server, server is waiting for something like this to establish connection
    public boolean clInitHS() throws IOException
    {
        output="HELLO SERVER";
        sendMessage(output);
        //System.out.println("i sent hello server");
        input=recieveMessage();
        //System.out.println("i rcvd :"+input);
        if(input.equals("HELLO CLIENT"))
        {
            output="OK SERVER";
            sendMessage(output);
            //System.out.println("i sent :"+output);
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    //method to send messages to server through the stream
    public void sendMessage(String toSend) 
    {
        if(!lock.isHeldByCurrentThread())//mutex
        {
            lock.lock();
            
            try
            {
                dos.writeUTF(toSend);
                dos.flush();
                
            }
            catch(Exception e)
            {
                System.out.println(e.toString());
            }
            finally
            {
                lock.unlock();
            }
        }
    }
    
    //method to recieve messages from server 
    public String recieveMessage() throws IOException 
    {
        
        String recieveMessage = "";
        
        if(!lock.isHeldByCurrentThread())//mutex
        {
            lock.lock();
            
            try
            {
                
                recieveMessage = dis.readUTF();
                
            }
            catch(Exception e)
            {
                System.out.println("client -> rcvmsg problem");
                System.out.println(e.toString());
            }
            finally
            {
                //System.out.println("unlock lock");
                lock.unlock();
            }
            
        }
        else
        {
            //System.out.println("Thread was occupied");
            throw new IOException("problem with sending messages");
        }
        return recieveMessage;
    }
    
    
    //method to recieve files from server, we recieve them in packets so we store them as such and reconstract the whole file into a byte array
    public void rcvFile(String pathName) throws FileNotFoundException, IOException
    {
        
        if(!lock.isHeldByCurrentThread())//mutex
        {
            
            FileOutputStream fos=null;
            lock.lock();
            
             try
              {
                
                
                File file = new File("ReceivedSongs/"+pathName);
                file.createNewFile();
                
                fos = new FileOutputStream(file);
                byte[] rcvBuffer = new byte[1024];
                int counter;
                
                boolean EndOfFileReached = false;
                
                long fileSize;
                
                fileSize = dis.readLong();
                
                System.out.println(fileSize);
                
                long tempFS = 0;
                
                while(EndOfFileReached == false)    
                {
                    while((counter=bis.read(rcvBuffer))>0)
                    {   
                        System.out.println(counter);
                        try
                        {
                                                        
                            tempFS+=counter;
                            
                            System.out.println(tempFS);
                            
                            if(tempFS==fileSize)
                            {
                                fos.write(rcvBuffer, 0, counter);
                                System.out.println("reached end of file");
                                EndOfFileReached = true;
                                break;   
                            }
                            else
                            {
                                fos.write(rcvBuffer, 0, counter);  
                            }
                        }   
                    
                    
                        catch(EOFException e)
                        {
                            System.out.println("problem");
                            break;
                        }
                        catch(Exception ex)
                        {
                            System.out.println("eofe was here");
                            break;
                        
                        }
                    }
                    
                    FileWriter myWriter = new FileWriter("ReceivedSongsList.txt",true);
                    
                    myWriter.write(pathName+"\n");
                    myWriter.close();
                    
                    System.out.println("Server stopped transmition,type OK to continue");
                    
                }
               
                fos.close();
                
            }
            catch(EOFException eofer)
            {
                System.out.println("eofe exception in rcvFile");
            }
            catch(IOException e)
            {
                System.out.println(e.toString());
                System.out.println("IO exception in rcvFile");
                
            }
            finally
            {
                              
                lock.unlock();
            }     

          }
        else
        {
            throw new IOException("problem with receving file");
        }
    }     
    
    
    //this method is for recieving a song stream from the server
    //as the stream comes from the server we recive each packet and relocate it to a temp memory in order to play it correctly, it is stored in byte array
    public void rcvStream()
    {
        if(!lock.isHeldByCurrentThread())//mutex
        {
            lock.lock();
            
            AudioPlayer audioplayer ;
            
            try
            {
                
                long fileSize;
                
                fileSize = dis.readLong();
                System.out.println("fileSize : " +fileSize);
                
                boolean endoffile = false;
                
                byte[] wholesong = new byte[(int)fileSize];
                
                byte[] rcvBuffer = new byte[1024];
                
                int counter;
                
                long tempcounter = 0;
                
                long tempsize = 0;
                
                while(!endoffile)
                {
                    
                    while((counter = bis.read(rcvBuffer))>0)
                    {
                        tempsize += counter;
                         
                       
                        if(tempsize!= fileSize)
                        {
                            for(byte b : rcvBuffer)
                            {
                                wholesong[(int)tempcounter] = b;
                                tempcounter++;
                            }
                        }
                        else
                        {
                            System.out.println("Last buffer operation");
                            System.out.println(counter + "#" + rcvBuffer.length);
                            for(byte b : rcvBuffer)
                            {
                                
                                if(tempcounter<fileSize)
                                {
                                    System.out.println(tempcounter);
                                    wholesong[(int)tempcounter] = b;
                                    System.out.println(tempcounter);
                                    tempcounter++;
                                }
                            }
                            endoffile = true;
                            break;
                        
                        }
                    
                    }
               
                }
                        
                System.out.println("Containing array is size : " + wholesong.length);
                
                audioplayer = new AudioPlayer();
                
                audioplayer.musicMenu(wholesong,scn);
            }
            catch(Exception e)
            {
                System.out.println(e.toString());
                
            }
            finally
            {
                lock.unlock();
            }
         
        }
        else
        {
            System.out.println("Thread was occupied");
        }
    }
    
    //this is the main method of the client 
    public static void main(String args[]) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        
       Client client = new Client();
       client.connect();//we connect to the server through socket
       boolean keepwhiling = client.clInitHS();//getting results of handshake
       List<String> songList = new ArrayList<>();
       
       
       if(keepwhiling)//checking results 
       {
           System.out.println("Handshake done successfully");
           client.fileManip.ReadFromFile("ReceivedSongsList.txt",songList);
       }
       else
       {
           System.out.println("Server not responding to handshake");
           client.soc.close();
       }
       
       String servermsg="";
        
       while(keepwhiling)//main while for keeping client open and responsive 
        {
       
            try
            {
                servermsg = client.recieveMessage();
            }
            catch(Exception e)
            {
                servermsg="";
                System.out.println("exception in client main after rcv msg");
            }
            
            if(servermsg.contains("w8"))//server said is waiting for response
            {
                //System.out.println("rcv had w8");
                servermsg=servermsg.replace("w8","");
                servermsg=servermsg.trim();
                System.out.println(servermsg);
                client.output=client.scn.nextLine();
                
                while(servermsg.contains("4.Listen to Downloaded songs") && client.output.equals("4"))//client chose to listen to already downloaded songs
                {
                    do
                    {
                        System.out.println("Choose a song");//song list of downloaded songs appear and client choose one
                        for(String song : songList)
                        {
                           System.out.println(songList.indexOf(song)+". "+song);

                        }

                        client.output = client.scn.nextLine();

                        String songPath = songList.get(Integer.parseInt(client.output));

                        AudioPlayer audioplayer = new AudioPlayer();

                        audioplayer.musicMenu(songPath, client.scn);

                        System.out.println("Want (1)another song, or (2)go to server?");//keep listening to songs or return to server

                        client.output = client.scn.nextLine();

                    }
                    while(client.output.equals("1"));
                    
                    System.out.println(servermsg);
                    client.output=client.scn.nextLine();
                    
                }
                
                
                
                System.out.println(client.output);
                client.sendMessage(client.output);
            }
            else if(servermsg.contains("sending file"))//server sais that he is sending a file so clients prepare for recieving a file
            {
                
                try
                {
                    servermsg=servermsg.replace("sending file","");
                    client.rcvFile(servermsg);
                    client.fileManip.ReadFromFile("ReceivedSongsList.txt", songList);
                    client.output = client.scn.nextLine();
                    client.sendMessage(client.output);
                }
                catch(Exception e)
                {
                    System.out.println("exception in client rcv file");
                    break;
                }
            }
            else if(servermsg.equals("OK EXIT"))//exit handshake and closing resources and socket
            {
                client.sendMessage("OK BYE");
                keepwhiling = false;
                client.dis.close();
                client.dos.close();
                client.soc.close();
                                
            }
            else if(servermsg.equals("Starting Stream"))//ready to recieve stream
            {
                
                client.rcvStream();
                
                System.out.println("intermediate");
                
                
                client.sendMessage("OK");
                
                
            }
            else
            {
                
                System.out.println(servermsg);
            }
            
            
        }
        
    }
}
