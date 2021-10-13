/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;


import QtFastStart_Pipes.QtFastStart;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



@interface Fallback{};


/**
 *
 * @author SkibbleBip
 */

/********************************
 * This is the object that converts the downloaded video into a list of split up 1 second long sample wav files using the FFMPEG wrapper.
 * It will then call the FFMPEG wrapper to analyze each individual wav file, in order, for the Mean Average volume in Decibels.
 * It feeds this information into a double-type array to plot the sample values.
 * Volume data ranges from -100 (the Quietest) to 0 (the peak loudest). in Decibels.
 * Typically greater than -10 dB is uncomfortable, greater than -5 is painful. 
 * Decibels are Logarithmic.
 *********************************/

public class MediaConverter {

    private char fs = File.separatorChar; ///windows uses '\', Unix uses '/'
    private Ffmpeg ff;  ///the FFMPEG wrapper object
    
    
    
     public MediaConverter(){
         //default contructor
        try {
            ff = new Ffmpeg();
            //create a new ffmpeg wrapper object
            
            
        } catch (IOException ex) {Logger.getLogger(MediaConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

     public int test(byte[] inputBytes, boolean webm) throws IOException, InterruptedException, Ffmpeg.BadVideoFile{
        
        byte[] original = inputBytes;
        //copy the original bytes in case we need to fall back on them
        
        if (webm == false) {
            inputBytes = QtFastStart.fastStart(original);
        }
        
        if(inputBytes == null)
            //if failed to obtain a mutated quickstart version (ie an error occured,
            //or the original file is allready quickstart), fall back to the original bytes
            inputBytes = original;

        //attempt to check if the video is a crasher 
        if(ff.checkCrasher(inputBytes)){
            return 2;
        }

        ArrayList<byte[]> segments = ff.grabSegments(inputBytes);
        
        if(segments == null){
            System.out.println("Failed to process media file");
            return 0;
        }
        
        
        double norm = 0;
        /*norm is the estimated "room-tempature" volume
        this is used to predict what the average video volume is to produce a rough average estimate throughout the video.
        this is used to check if the next audio sample is suddently substantially louder than the current "room tempature",
        typically used by screamer videos.
        */
        int triggered = 0;
        ///flag to check whether or not its detected a potential offender
        
        int strikes = 0;
        /*sometimes a video could have a loud peak for less than a second, possibly due to random noise or encoding error.
        This acts as a sort of "forgiveness meter" so that it takes more than a one-time detection of loud audio
        */    
        
        
        
        for(int i = 0; i < segments.size() && triggered != 1; i++){
            
            if(segments.get(i) == null){
                System.out.println("Segment was not processed correctly, skipping...");
                continue;
            }
            
            double value = ff.getVolumeMean(segments.get(i));
            
            
            /*
            due to the nature of the decibel's logarithmic behavior, dividing the current volume by half roughly equals a volume double the loudness.
            therefor, to check if there has been a sudden leap in volume, we divide norm volume by 2 and check if the next sample volume is even 
            louder than that. This is for Screamer detection. we also make sure this calculation is greater than -7, which is an unconfortable volume.
            this is because a video could possibly silent for a while, then someone talks at a reasonable volume, this would technically match 
            the norm/2<value rule, however this isn't a screamer volume. This is to prevent false positives, as most screamers are louder than -8 dB.
            */
            if(i>3 && i<segments.size()-1){
                if(norm/2 < value && value>-5)
                    triggered = 1;
            }
            
            
            /****************
             * In general, if a volume louder than -5.5 is detected, it could possibly be an annoyingly loud video, but it could also possibly be a one time
             * loudness for less than a second from noise, or encoding errors. to prevent false positives, this next part creates a sort of 
             * "3 strikes and you are out" rule
             */
            if(value > -5.5)
                strikes++;
            //*
            if(strikes>=5)
                triggered = 1;
            //*
            //********************************//
            
            norm = norm + value / 2.0;
            //recalculate the normal volume average
            
            if(triggered == 1)
                break;
            
        }//end for loop
        

         //System.out.println("Status: "+triggered);
         return triggered;
         //return whether or not it detected an offender
         
         
     }
    
     @Fallback
     public int test(File in) throws InterruptedException, IOException{
         
        
        /*Attempt to scan for a crashing vid*/
         if(ff.checkCrasher(in)){
             in.delete();
             return 2;
         
         }
         
         
         //if the video file isnt a crasher, then check it for being too loud
         
        File[] target = ff.split(in);
        //list of sampled audio files, in numerical order
        
        double[] values = new double[target.length];
        //create a buffer that contains the db data of each audio file
        
        for(int i = 0; i< target.length; i++){
            ///loop through all the files, and grab the mean volume data for each audio sample and store it in the plotted buffer
            values[i] = ff.getVolumeMean(target[i]);
            target[i].delete();
            //deleted the processed audio file
        }
        double norm = 0;
        /*norm is the estimated "room-tempature" volume
        this is used to predict what the average video volume is to produce a rough average estimate throughout the video.
        this is used to check if the next audio sample is suddently substantially louder than the current "room tempature",
        typically used by screamer videos.
        */
        int triggered = 0;
        ///flag to check whether or not its detected a potential offender
        
        int strikes = 0;
        /*sometimes a video could have a loud peak for less than a second, possibly due to random noise or encoding error.
        This acts as a sort of "forgiveness meter" so that it takes more than a one-time detection of loud audio
        */        

        for(int i = 0; i<values.length && triggered == 0; i++){
            //norm = norm + values[i] / 2.0;
            
            
            
            /*
            due to the nature of the decibel's logarithmic behavior, dividing the current volume by half roughly equals a volume double the loudness.
            therefor, to check if there has been a sudden leap in volume, we divide norm volume by 2 and check if the next sample volume is even 
            louder than that. This is for Screamer detection. we also make sure this calculation is greater than -7, which is an unconfortable volume.
            this is because a video could possibly silent for a while, then someone talks at a reasonable volume, this would technically match 
            the norm/2<value rule, however this isn't a screamer volume. This is to prevent false positives, as most screamers are louder than -8 dB.
            */
            if(i>3 && i<values.length-1){
                if(norm/2 < values[i] && values[i]>-5)
                    triggered = 1;
            }
            
            /****************
             * In general, if a volume louder than -8 is detected, it could possibly be an annoyingly loud video, but it could also possibly be a one time
             * loudness for less than a second from noise, or encoding errors. to prevent false positives, this next part creates a sort of 
             * "3 strikes and you are out" rule
             */
            if(values[i] > -5.5)
                strikes++;
            //*
            if(strikes>=5)
                triggered = 1;
            //*
            //********************************//
            
            norm = norm + values[i] / 2.0;
            //recalculate the normal volume average
        }
        
       
         in.delete();
         return triggered;
         //return whether or not it detected an offender
     }
     
     
    
    
}
