/*
 * 
 * 
 */
package musicApp;

import java.net.Socket;
import java.io.*;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

//this class is made to handle communication between client and server, it also has all the handshakes used for smooth communication
//every method is mutexed in order to achieve smooth communication
//files are send in the form of packets and buffers are used for this, the packet size is 1024
//also this class sends files to client
public class ServerCommunicator {

   
    String input = "";
    String output = "";
    DataInputStream dis ;
    DataOutputStream dos;
    BufferedInputStream bis;
    BufferedOutputStream bos;
    FileInputStream fis;
    FileOutputStream fos;
    Timestamp lastAction ;//= new Timestamp(System.currentTimeMillis());
    ReentrantLock lock = new ReentrantLock(true);//this is the way i chose to do mutex 
    String pathName;
    Integer endoffile = new Integer(129);
    byte[] EndOfFile = new byte[1024];
    boolean SocExep = false;
    
    
    //constractor
    public ServerCommunicator(Socket comSoc) throws IOException
    {
        this.dis = new DataInputStream(comSoc.getInputStream());
        this.dos = new DataOutputStream(comSoc.getOutputStream());
        comSoc.setSoTimeout(2*60*1000);
       
        
        for(int i = 0; i< EndOfFile.length;i++)
        {
            EndOfFile[i] = endoffile.byteValue();
        }
        
    }
    
    //method for closing data strems
    public void closeCommunicator() throws IOException
    {
        //flag for exception and close from handler
        if(lock.isHeldByCurrentThread())
        {
            lock.unlock();
        }
        dis.close();
        dos.close();
    }
    
    //method for handshake to establish communication with client, it returns true if handshake successfull
    public boolean initialHandshake() throws IOException
    {
        //System.out.println("i should HS now");
        input = recieveMessage();
        //System.out.println("i rcvd : "+input);
        if(input.equals("HELLO SERVER"))
        {
            output="HELLO CLIENT";
            sendMessage(output);
            //System.out.println("i sent : "+output);
            input = recieveMessage();
            //System.out.println("i rcvd : "+input);
            if(input.equals("OK SERVER"))
            {
                System.out.println("Handshake is done");
                return true;
                
            }
            else
            {
                return false;
            }
                          
        }
        else
        {
            return false;
        }
    }
    
    //this method sends a msg to the client
    public void sendMessage(String toSend) throws IOException
    {
        
        if(!lock.isHeldByCurrentThread())
        {
            lock.lock();
            
            try
            {
                
                dos.writeUTF(toSend);
                System.out.println("com wrote : " + toSend);
                dos.flush();
                System.out.println("flushed");
            }
            catch(SocketException se)
            {
                SocExep = true;
                dis.close();
                dos.close();
                System.out.println("Communicator-> connection terminated unexpectedly");
                //close stuff
            }
            catch(Exception e)
            {
                System.out.println("communicator -> sendMessage failed");
                System.out.println(e.toString());
            }
            finally
            {
                lock.unlock();
            }
        }
       
    }   
       
             
    //this method is responsible for recieving messages from the client
    public String recieveMessage() throws IOException
    {
        //System.out.println("check if lock is locked");
        if(!lock.isHeldByCurrentThread())
        {
            //System.out.println("lock is free ill take it");
            lock.lock();
            
            try
            {
                //System.out.println("im supposed to rcv");
                output = dis.readUTF();
                setTime();
                System.out.println("i got from client :"+output);
                
                return output;
            }
            catch(SocketException se)
            {
                SocExep = true;
                dis.close();
                dos.close();
                return "";
            }
            catch(Exception e)
            {
                System.out.println(e.toString());
                return "";
            }
            finally
            {
                lock.unlock();
            }
        }
        else
        {
            throw new IOException("problem with rcving messages");
        }
        
        
    }
    
    //method sets the time that the client last did something in order to keep track if active or not    
    public void setTime()
    {
        lastAction = new Timestamp(System.currentTimeMillis());
    }
    
    //handshake for sending file and getting client in a state to recieve the file, if client ready then returns true
    public boolean fileHandshake() throws IOException
    {
        
        output="SEND FILE";
        sendMessage(output);
        input=recieveMessage();
        
        if(input.equals("FILE OK"))
        {
            sendMessage("SENDING FILE");
            return true;
        }
        else 
        {
            return false;
        }
    }
    
    
    //method/handshake to terminate client after clients request, returns boolean in order to close resources gracefully
    public boolean terminationHandshake() throws IOException
    {
        input = recieveMessage();
        if(input.equals("EXIT"))
        {
            sendMessage("OK EXIT");
            input=recieveMessage();
            if(input.equals("OK BYE"))
            {
                return true;
            }
            else
            {
                return false;
            }
                 
        }
        else
        {
            return false;
        }
    }
    
    
    //this method is responsible for sending a file to the client, using bytes made it able to send any type of file
    public void SendFile(String pathName) throws FileNotFoundException, IOException
    {
        
        if(!lock.isHeldByCurrentThread())//mutex 
        {
            
            ObjectOutputStream oos;
            
            lock.lock();
            
            try
            {
                File file2send = new File(pathName);
                System.out.println(file2send.length());
                
               
               
                long fileSize = file2send.length();
                dos.writeLong(fileSize);
                dos.flush();
              
                byte[] bytearray = new byte[1024];
                fis = new FileInputStream(file2send);
               
                bos = new BufferedOutputStream(dos);
                int counter;
                
                while((counter=fis.read(bytearray))>0)
                {
                    
                    System.out.println(counter);
                                                            
                    bos.write(bytearray, 0, counter);
                }
               
                bos.flush();
                fis.close();
                
            }
            catch(SocketException se)
            {
                SocExep = true;
                dis.close();
                dos.close();
            }
            catch(Exception e)
            {
                System.out.println("communicator -> sendfile");
                System.out.println(e.toString());
            }
            finally
            {
                lock.unlock();
            }
      
        }
        else
        {
            throw new IOException("problem with sending file");
        }
      
    }
    
    //this method was created with an extra feature in mind that a client could upload to the server a song, but i did not find this feature logical to happen so i aborted
    public void rcvFile(String pathName) throws FileNotFoundException, IOException
    {
        
//        if(!lock.isHeldByCurrentThread())
//        {
//            lock.lock();
//            
//            try
//            {
//                bis = new BufferedInputStream(dis);
//                fos = new FileOutputStream(pathName);
//                byte[] rcvBuffer = new byte[1024];
//                int counter;
//                while((counter=bis.read(rcvBuffer))>0)
//                {
//                    fos.write(rcvBuffer, 0, counter);
//                }
//            }
//            catch(Exception e)
//            {
//                System.out.println(e.toString());
//            }
//            finally
//            {
//                lock.unlock();
//            }
//        }
//        else
//        {
//            throw new IOException("problem with receving file");
//        }
        
        
        
    }
    
    
}
