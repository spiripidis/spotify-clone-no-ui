/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicApp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

//the entire class is made to check if in the last downloaded/streamed songs list are any duplicates and remove them in order to show to client 
public class Check4DuplicateSongs {
    public void checkForDuplicates(List<String> lastDLsongs,List<String> lastSTRsongs)
    {
        List<String> tempDL = new ArrayList<String>();
        List<String> tempSTR = new ArrayList<String>();
        
        for(String dl : lastDLsongs)
        {
            if(!tempDL.contains(dl))
            {
                tempDL.add(dl);
            }
        }
        
        for(String str : lastSTRsongs)
        {
            if(!tempSTR.contains(str))
            {
                tempSTR.add(str);
            }
        }
        
        lastDLsongs = tempDL;
        lastSTRsongs = tempSTR;
    }
    
    public List<String> checkListForDuplicates(List<String> stringList)
    {
        List<String> temp = new ArrayList<>();
        
        for(String st : stringList)
        {
            if(!temp.contains(st))
            {
                temp.add(st);
            }
        }
        
        return temp;
        
    }
    
    
}
