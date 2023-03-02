
package musicApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

//this class is for file manipulation methods, downloads, streams and also removes users that are already logged in and try to log in again

public class FileManipulation {
    
    
    ReentrantLock lock = new ReentrantLock(true);//used for mutex
        
    //this method is for manipulating the file that keeps track of logged in users
    public void removeLoggedInUser(String username)
    {
        FileReader fr = null;
        FileWriter fw = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        
        List<String> remainingUsers = new ArrayList<String>();
        
        if(!lock.isHeldByCurrentThread())
        {
            lock.lock();
            
            try
            {
                fr = new FileReader("loggedinusers.txt");
                fw = new FileWriter("loggedinusers.txt");
                br = new BufferedReader(fr);
                bw = new BufferedWriter(fw);
                
                String line = br.readLine();
                
                while(line != null)
                {
                    if(!username.equals(line))
                    {
                        remainingUsers.add(line+"/n");
                    }
                }
                
                if(username.equals("=empty="))
                {
                    remainingUsers.clear();
                }
                
                bw.write("");
                bw.flush();
                fw = new FileWriter("loggedinusers.txt",true);
                bw = new BufferedWriter(fw);
                
                for(String user : remainingUsers)
                {
                    bw.write(user);
                }
             
            }
            catch(Exception e)
            {}
            finally
            {
                try
                {
                    bw.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method :removeLoggedInUser, fail to close buffered writer \n"+e.toString());
                }

                try
                {
                    fw.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : removeLoggedInUser, fail to close File writer \n"+e.toString());
                }
                
                 try
                {
                    br.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : removeLoggedInUser, fail to close buffered reader \n"+e.toString());
                }
                try
                {
                    fr.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : removeLoggedInUser, fail to close File reader \n"+e.toString());
                }
                
                lock.unlock();
            
            }
           
        }
       
    }
    
    //this method checks if a user is already tracked as logged in and returns a boolean true or false
    public boolean isUserAlreadyLoggedIn(String username)
    {
        FileReader fr = null;
        FileWriter fw = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        
        boolean result = true;
        if(!lock.isHeldByCurrentThread())
        {
            lock.lock();
            
            try
            {
                fr = new FileReader("loggedinusers.txt");
                fw = new FileWriter("loggedinusers.txt",true);
                br = new BufferedReader(fr);
                bw = new BufferedWriter(fw);

                String line = br.readLine();
                
                int counter = 0;
                
                while(line != null)
                {
                    if(username.equals(line))
                    {
                        counter++;
                    }
                    line = br.readLine();
                }
                
                if(counter == 0)
                {
                    bw.write(username + "\n");
                    bw.flush();
                    result = false;
                }
                
            }
            catch(Exception e)
            {}
            finally
            {
                try
                {
                    bw.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method :isUserAlreadyLoggedIn, fail to close buffered writer \n"+e.toString());
                }

                try
                {
                    fw.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : isUserAlreadyLoggedIn, fail to close File writer \n"+e.toString());
                }
                
                 try
                {
                    br.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : isUserAlreadyLoggedIn, fail to close buffered reader \n"+e.toString());
                }
                try
                {
                    fr.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : isUserAlreadyLoggedIn, fail to close File reader \n"+e.toString());
                }
                
                lock.unlock();
             
           }
           
        }
        
        return result;
    }
    
    
    
    //this method is for writing in to a file and it keeps track of usernames and passwords of users
    public void Write2File(String filename, HashMap<String, String> hm)
    {
        
        if(!lock.isHeldByCurrentThread())
        {
            
            lock.lock();
            
            FileWriter fw = null;
            BufferedWriter bw = null;


            try
            {
                fw = new FileWriter(filename, true);
                bw = new BufferedWriter(fw);

                for(String username : hm.keySet())
                {
                    bw.write(username + "::" + hm.get(username));
                    System.out.println( "WROTE : " +username + "::" + hm.get(username));
                    bw.write("\n");
                }

            }
            catch(Exception e)
            {
                System.out.println("class FileIO, Method : Write2File \n"+e.toString());
                
            }
            finally
            {
                try
                {
                    bw.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : Write2File, fail to close buffered writer \n"+e.toString());
                }

                try
                {
                    fw.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : Write2File, fail to close File writer \n"+e.toString());
                }
                
                lock.unlock();
            }
        }
        
        
    }
    
    
    //this method reads from a file and it is for retrieving the usernames with the passwords of the users 
    public void ReadFromFile(String filename, HashMap<String, String> hm)
    {
        
        
        if(!lock.isHeldByCurrentThread())
        {
            
            
            
            lock.lock();
            FileReader fr = null;
            BufferedReader br = null;

            try
            {
                fr = new FileReader(filename);
                br = new BufferedReader(fr);

                String line = br.readLine();



                String[] brokenline = new String[2];

                while(line != null)
                {
                    System.out.println("READ LINE IS : " + line);
                    brokenline[0] = line.substring(0, line.indexOf("::"));
                    System.out.println("USERNAME IS : " + brokenline[0]);
                    brokenline[1] = line.substring(line.indexOf("::") + 2, line.length());
                    System.out.println("PASSWORD IS : " + brokenline[1]);

                    hm.put(brokenline[0], brokenline[1]);

                    line = br.readLine();
                }

            }
            catch(Exception e)
            {
                System.out.println("class FileIO, Method : ReadFromFile (hashmap) \n"+e.toString());
            }
            finally
            {
                try
                {
                    br.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : ReadFromFile(hashmap), fail to close buffered reader \n"+e.toString());
                }
                try
                {
                    fr.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : ReadFromFile(hashmap), fail to close File reader \n"+e.toString());
                }
                
                lock.unlock();



            }
        }
        
        
    }
    
    //this method reads the list of songs from a file
    public void ReadFromFile(String filename,List<String> songlist )
    {
        
         if(!lock.isHeldByCurrentThread())
         {
            lock.lock();
            FileReader fr = null;
            BufferedReader br = null;
            
            try
            {
                fr = new FileReader(filename);
                br = new BufferedReader(fr);

                String line = br.readLine();
                songlist.clear();
                while(line != null)
                {
                   

                    songlist.add(line);

                    line = br.readLine();
                }
               
            
            }
            catch(Exception e)
            {
                System.out.println("class FileIO, Method : ReadFromFile(List) \n"+e.toString());
            }
            finally
            {
                try
                {
                    br.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : ReadFromFile(List), fail to close buffered reader \n"+e.toString());
                }
                try
                {
                    fr.close();
                }
                catch(Exception e)
                {
                    System.out.println("class FileIO, Method : ReadFromFile(List), fail to close File reader \n"+e.toString());
                }
                
                lock.unlock();

            }
         }
    }
   
}
