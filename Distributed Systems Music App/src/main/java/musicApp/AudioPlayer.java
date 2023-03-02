/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicApp;

/**
 *
 * @author Lalagkos Lysandros Konstantinos
 */

import java.io.ByteArrayInputStream;
import java.io.File; 
import java.io.IOException; 
import java.io.InputStream;
import java.util.Scanner; 
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException; 
import javax.sound.sampled.UnsupportedAudioFileException; 

//class for playing audio for the client, this class belong to the client and works for the client
public class AudioPlayer 
{ 

	// to store current position 
	Long currentFrame; 
	Clip clip; 
	
	// current status of clip 
	String status; 
	
	AudioInputStream audioInputStream; 
	static String filePath; 
            
        byte[] audio;
        InputStream inputStream;
        
        boolean downloadedSong=false;
        boolean streaming = false;
                
        
	// constructor 
	public AudioPlayer() throws UnsupportedAudioFileException, IOException, LineUnavailableException 
	{ 
               
	} 
        //constractor that takes path for file 
        public AudioPlayer(String pathname) throws UnsupportedAudioFileException, IOException, LineUnavailableException
        {
            filePath = pathname;
            downloadedSong=true;
            audioInputStream = AudioSystem.getAudioInputStream(new File(pathname).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        //constractor that takes audio input stream for playing streamed audio and not downloaded
        //method audio player, used to play downloaded and streamed audios from server or from client
        public AudioPlayer(byte[] fullAudio)throws UnsupportedAudioFileException,IOException, LineUnavailableException 
        {
            streaming = true;
            audio = fullAudio;
            inputStream = new ByteArrayInputStream(audio);
            
            audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            //AudioFormat format = audioInputStream.getFormat();
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        
        //menu that the client sees in order to play, pause, stop or go to a specific time in a STREAMED audio
	public void musicMenu(byte[] fullAudio,Scanner sc) 
	{ 
                AudioPlayer audioPlayer;
            
		try
		{ 
                        
                        
                        audioPlayer = new AudioPlayer(fullAudio);
                        
			
			audioPlayer.play(); 
			 
			boolean isPlaying =true;
                        
			while (isPlaying) 
			{ 
				System.out.println("1. pause"); 
				System.out.println("2. resume"); 
				System.out.println("3. restart"); 
				System.out.println("4. stop"); 
				System.out.println("5. Jump to specific time");
                                
                                String c;
                                       
				c = sc.nextLine();


                                audioPlayer.gotoChoice(c,sc); 
				if (c.equals("4")) 
                                {
                                    isPlaying = false;
                                    break;
                                }
                              
			} 
			
		} 
		
		catch (Exception ex) 
		{ 
                        
                        //sc.nextLine();
			System.out.println("Error with playing sound."); 
			ex.printStackTrace(); 
                        
		
		} 
	} 
        //menu that the client sees in order to play, pause, stop or go to a specific time in a DOWNLOADED audio
        public void musicMenu(String filepath,Scanner sc) 
	{ 
                AudioPlayer audioPlayer;
            
		try
		{ 
                     
                    audioPlayer = new AudioPlayer("ReceivedSongs/"+filepath);


                    audioPlayer.play(); 

                    boolean isPlaying =true;

                    while (isPlaying) 
                    { 
                        System.out.println("1. pause"); 
                        System.out.println("2. resume"); 
                        System.out.println("3. restart"); 
                        System.out.println("4. stop"); 
                        System.out.println("5. Jump to specific time");

                        String c;


                        c = sc.nextLine();

                        audioPlayer.gotoChoice(c,sc); 
                        if (c.equals("4")) 
                        {
                            isPlaying = false;
                            break;
                        }

                    } 
                    
		} 
		
		catch (Exception ex) 
		{ 
                        
                   
                    System.out.println("Error with playing sound."); 
                    ex.printStackTrace(); 

		
		} 
	} 
        

	// Work as the user enters his choice 
	
	private void gotoChoice(String c,Scanner sc) throws IOException, LineUnavailableException, UnsupportedAudioFileException 
	{ 
		switch (c) 
		{ 
			case "1": 
                            pause(); 
                            break; 
			case "2": 
                            resumeAudio(); 
                            break; 
			case "3": 
                            restart(); 
                            break; 
			case "4": 
                            stop(); 
                            break; 
			case "5": 
                            System.out.println("Enter time (" + 0 + 
                            ", " + clip.getMicrosecondLength() + ")"); 
                            
                            long c1 = sc.nextLong(); 
                            sc.nextLine();
                            jump(c1); 
                            break; 
	
		} 
	
	} 
	
	// Method to play the audio 
	public void play() 
	{ 
            
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            gainControl.setValue(6.0f);   
            
            //start the clip 
            clip.start(); 

            System.out.println("started clip");

            status = "play"; 
	} 
	
	// Method to pause the audio 
	public void pause() 
	{ 
		if (status.equals("paused")) 
		{ 
			System.out.println("audio is already paused"); 
			return; 
		} 
		this.currentFrame = 
		this.clip.getMicrosecondPosition(); 
                System.out.println("set mseconds" + currentFrame);
                
                
		clip.stop(); 
                System.out.println("stopped clip");
                
		status = "paused"; 
	} 
	
	// Method to resume the audio 
	public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException 
	{ 
		if (status.equals("play")) 
		{ 
			System.out.println("Audio is already "+ 
			"being played"); 
			return; 
		} 
                
                
		clip.close(); 
		
                System.out.println("closed clip");
                
                resetAudioStream(); 
                
                System.out.println("reset stream");
               
                
                System.out.println("mseconds are " + currentFrame);
		clip.setMicrosecondPosition(currentFrame); 
                
                System.out.println("got mseconds");
                
		this.play(); 
	} 
	
	// Method to restart the audio 
	public void restart() throws IOException, LineUnavailableException, UnsupportedAudioFileException 
	{ 
		clip.stop(); 
		clip.close(); 
		resetAudioStream(); 
		currentFrame = 0L; 
		clip.setMicrosecondPosition(0); 
		this.play(); 
	} 
	
	// Method to stop the audio 
	public void stop() throws UnsupportedAudioFileException, 
	IOException, LineUnavailableException 
	{ 
		currentFrame = 0L; 
		clip.stop(); 
		clip.close(); 
	} 
	
	// Method to jump over a specific part 
	public void jump(long c) throws UnsupportedAudioFileException, IOException, LineUnavailableException 
	{ 
		if (c > 0 && c < clip.getMicrosecondLength()) 
		{ 
			clip.stop(); 
			clip.close(); 
			resetAudioStream(); 
			currentFrame = c; 
			clip.setMicrosecondPosition(c); 
			this.play(); 
		} 
	} 
	
	// Method to reset audio stream 
	public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException 
	{ 
          
            
            if(downloadedSong)
            {
                audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
                
                System.out.println("reset audioInputStream");
               
		clip.open(audioInputStream); 
                
                System.out.println("open clip");
                
		clip.loop(Clip.LOOP_CONTINUOUSLY); 
                
                System.out.println("set loop true");
                
                try
                {
                   audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile()); 
                }
                catch(Exception e)
                    {}
            }
            else if(streaming)
            {
                inputStream = new ByteArrayInputStream(audio);
                audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                
                System.out.println("reset audioInputStream");
               
		clip.open(audioInputStream); 
                
                System.out.println("open clip");
                
		clip.loop(Clip.LOOP_CONTINUOUSLY); 
                
                System.out.println("set loop true");
                try
                {
                   audioInputStream = AudioSystem.getAudioInputStream(inputStream); 
                }
                catch(Exception e)
                    {}
            }
          
	} 

} 

