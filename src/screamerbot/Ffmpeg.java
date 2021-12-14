/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
//import com.google.common.jimfs.Jimfs;
//import com.google.common.jimfs.Configuration;
import javax.annotation.Nullable;


/**
 *
 * @author SkibbleBip
 * 
 * This class the the Actual FFMPEG wrapper that allows the JRE to communicated with the compiled FFMPEG executable.
 * This application comes with 2 different 32-bit versions of ffmpeg embedded in the source files, one for windows and one for linux.
 * The right version for the OS is installed into the "exe" directory when the constructor is called by checking the status of the file 
 * separator 'char fs'. This is so the user does not need to manually install a version on the machine, and makes this application more
 * portable and java friendly. FFMPEG is wrapped and used to convert the downloaded media files into sampled 1 second long mono wav files
 * and to check the Mean Average volume of each sample. This is done by running the executable through the ProcessBuilder class,
 * and taking the output from the executable process and checking the volume data provided by it.
 * 
 * 
 */
public class Ffmpeg {
    final private File instDir;///the directory ffmpeg will be installed
    final private File ffmpeg;///the actual ffmpeg executable
    final private File ffprobe;//actual ffprobe executable
    final private char fs = File.separatorChar;///windows uses '\', Unix uses '/'
    final private boolean os;///true for windows, false for linux
    final private int EBUR128;
    
    final int ErrorCode;
    
    final boolean DEBUG = false; 
    
    public Ffmpeg() throws IOException{
        ErrorCode = 0;
        EBUR128 = 35;
        
        InputStream is;
        //used to extract the ffmpeg unwrapped executable
        instDir = new File("exe");
        //make the new 'exe' folder
        instDir.mkdir();
        
        if(fs == '\\'){
            //if the file separate is '\', then its on a windows machine
            //therefor it sets the os flag to true and extracts the windows executable into the exe folder
            os = true;
            //copy ffmpeg over
            if(!new File("exe"+fs+"ffmpeg-x86.exe").exists()){
                is = Ffmpeg.class.getResourceAsStream("/screamerbot/resources/ffmpeg-x86.exe");
                //unwrap the executable
                Files.copy(is, new File("exe"+fs+"ffmpeg-x86.exe").toPath(), StandardCopyOption.REPLACE_EXISTING);
                //copy it into the directory
            }
            //copy ffprobe over
            if(!new File("exe"+fs+"ffprobe.exe").exists()){
                is = Ffmpeg.class.getResourceAsStream("/screamerbot/resources/ffprobe.exe");
                //unwrap the ffprobe
                Files.copy(is, new File("exe"+fs+"ffprobe.exe").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
        }
        else{
            //if the file separator is NOT '\', then its a linux machine. therefor, do the same for the linux executable
            os = false;
            if(!new File("exe"+fs+"ffmpeg-i386").exists()){
                is = Ffmpeg.class.getResourceAsStream("/screamerbot/resources/ffmpeg-i386");
                //unwrap the linux executable
                Files.copy(is, new File("exe"+fs+"ffmpeg-i386").toPath(), StandardCopyOption.REPLACE_EXISTING);
                //place it inside the exe directory
            }
            //copy ffprobe over
            if(!new File("exe"+fs+"ffprobe").exists()){
                is = Ffmpeg.class.getResourceAsStream("/screamerbot/resources/ffprobe");
                //unwrap the ffprobe
                Files.copy(is, new File("exe"+fs+"ffprobe").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
        }
        
        /*
        if the os is true(windows) declare the location of the ffmpeg executable as  "exe\ffmpeg-x86.exe"
        otherwise, set it to the linux location "exe/ffmpeg-i386"
        */
        if(os){
            ffmpeg = new File("exe"+fs+"ffmpeg-x86.exe");
            ffprobe = new File("exe"+fs+"ffprobe.exe");
        }else{
            ffmpeg = new File("exe"+fs+"ffmpeg-i386");
            ffprobe = new File("exe"+fs+"ffprobe");
        }
        
        
        
        
    }//end constructor

    
    
    public ArrayList<Double> grabSegments(byte[] buffer) throws IOException, InterruptedException{
        ArrayList<Double> output = new ArrayList<>();
        
        
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-hide_banner", "-nostats", "-i", "-", "-filter_complex", "ebur128", "-f", "null", "-");
        Process p = pb.start();
        
        
        FfmpegReaderThread ffrt = new FfmpegReaderThread(p.getErrorStream());
        
        ffrt.start();
        
        try{
            p.getOutputStream().write(buffer);
        
        }catch(IOException ex){
            if(!ex.getMessage().equals("Broken pipe"))
                throw ex;
        }
        
        p.getOutputStream().flush();
        p.getOutputStream().close();
        
        
        boolean spinning = true;
        
        do{
            try{
                ffrt.join();
                spinning = false;
            }catch(InterruptedException e){
                System.out.println("Reader was interrupted");
            
            }
            
        
        }while(spinning);
        
        String stdOut = new String(ffrt.getStdOut());
        p.getErrorStream().close();
        
        /*if(stdOut == null)
            return null;*/
        
        
        String[] results = stdOut.split("\n");
        
        if(results.length < 2)
            return null;
        
        for (String tmp : results) {
            if(tmp.startsWith("[Parsed_ebur128_0")){
                int start = tmp.indexOf("M:", EBUR128)+2;
                if(start < 2)
                    continue;
                int end = tmp.indexOf("S:", start);
                
                String loudStr = tmp.substring(start, end);
                
                try{
                    double val = Double.parseDouble(loudStr);
                    output.add(val);
                }catch(NumberFormatException ex){
                    System.out.println("Bad double value "+loudStr+ " skipping...");
                    break;
                }
                
            }
            
        }
        
        
        
        
        return output;
    
    }
    
    
    
    @Fallback
    public File[] split(File in) throws IOException, InterruptedException{
        
        /*
        This method returns an in-numerical order of the extracted 1 second audio samples. this is done by wrapping the ffmpeg executable and having it run
        a command to extract the audio as a 1 second long series of wav file samples. it will then arrange the files in numerical order where it will 
        pass them as return data.
        */
        
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-y", "-i", in.getPath(), "-ac", "1", "-af", "highpass=f=200", "-f", "segment", 
                "-segment_time", "1", "-vn", "tmp"+fs+"audio%d.wav");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        ///call the wrapped ffmpeg and have it run the commands yes to overwrite, input file, combine audio into mono, 1 second long segments,
        ///and to ignore video streams and only copy audio. Also adds a high pass filter to get rid of bass tones that may be loud but not
        ///harsh on the ears
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //set up the ffmpeg wrapper output parser
        
        String line = null;
        do{line = reader.readLine();}while(line != null);
        //cycle through the pipe output buffer and discard it, we do not need it
        
        int response = 0;
        boolean success = false;
        while(!success){
            //garuntees that it wont fail if interrupted
            try{
                response = p.waitFor();
                success = true;
            }catch(InterruptedException e){
                System.out.println("Temporarily interrupted...");
            }
        
        }

        in.delete();
        //delete the media file, we are done with it
        
        if(response!=0)
            //0 is a normal rturn value for ffmpeg to close on, this means everything worked out
            System.out.println("Error processing the media file");
        //this just lets me know if something went wrong 
        
        int length = new File("tmp").listFiles().length;
        //get number of audio files in the tmp folder
        File[] sorted = new File[length];
        //create an array of files the size of the number of audio sample files
        for(int i =0; i< length; i++){
            sorted[i] = new File("tmp"+fs+"audio"+i+".wav");
        }//sort each file in numeracle order into the array
        
        return sorted;
        //return that array
    }
    
    @Deprecated
    public File[] split(byte[] in) throws IOException, InterruptedException{
        
        /*
        This method returns an in-numerical order of the extracted 1 second audio samples. this is done by wrapping the ffmpeg executable and having it run
        a command to extract the audio as a 1 second long series of wav file samples. it will then arrange the files in numerical order where it will 
        pass them as return data.
        */
        
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-y", "-i", "-", "-ac", "1", "-af", "highpass=f=200", "-f", "segment", 
                "-segment_time", "1", "-vn", "tmp"+fs+"audio%d.wav");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        ///call the wrapped ffmpeg and have it run the commands yes to overwrite, input file, combine audio into mono, 1 second long segments,
        ///and to ignore video streams and only copy audio. Also adds a high pass filter to get rid of bass tones that may be loud but not
        ///harsh on the ears
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //set up the ffmpeg wrapper output parser
        
        String line = null;
        do{line = reader.readLine();}while(line != null);
        //cycle through the pipe output buffer and discard it, we do not need it
        
        int response = 0;
        boolean success = false;
        while(!success){
            //garuntees that it wont fail if interrupted
            try{
                response = p.waitFor();
                success = true;
            }catch(InterruptedException e){
                System.out.println("Temporarily interrupted...");
            }
        
        }
        
        if(response!=0)
            //0 is a normal rturn value for ffmpeg to close on, this means everything worked out
            System.out.println("Error processing the media file");
        //this just lets me know if something went wrong 
        
        int length = new File("tmp").listFiles().length;
        //get number of audio files in the tmp folder
        File[] sorted = new File[length];
        //create an array of files the size of the number of audio sample files
        for(int i =0; i< length; i++){
            sorted[i] = new File("tmp"+fs+"audio"+i+".wav");
        }//sort each file in numeracle order into the array
        
        return sorted;
        //return that array
    }
    
    
    @Fallback
    public double getVolumeMean(File in) throws IOException, InterruptedException{
        //ffmpeg -i input.mp4 -af "volumedetect" -f null NUL
        
        /*
        this method uses the wrapped ffmpeg to measure the Mean average volume in dB of the inputted argument wav file.
        FFMPEG requires an output file to save the processed file, but we dont need that output file so instead we send it to the OS specific null location.
        on windows this is simply NUL, and on Unix it is /dev/null. we detect this by using the os boolean flag, and it makes the application more universal. we once again use the ProcessBuilder to
        wrap the ffmpeg executable and run commands to analyze each file. after getting the double value of the volume of the sample, it returns it.
        */
        String nullLocation = null;
        if(os)
            nullLocation = "NUL";
        else
            nullLocation = "/dev/null";
        ///get the type of null location of the OS
        
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-hide_banner", "-i", in.getPath(), "-af", "volumedetect", "-f", "null", nullLocation);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        ///create the ffmpeg wrapper and run the commands for hiding the infobanner (this is to save processing time), the input file, the volumedetect filter,
        ///and the output file as the null location so it's discarded
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //create the output stream buffer of ffmpeg
        
        String line;
        String meanLine = null;
        while((line = reader.readLine())!=null){
            //System.out.println(line);
            if(line.contains("mean_volume: "))
                meanLine = line;
        
        }
        ///keep cycling through the output of the ffmpeg stream and check it until it states the Mean Volume. this is done by repeadiatly checking the buffer
        ///string until it find where it says "mean_volume: ", which is part of the output that tells the acctual value. it then saves this to its own variable for
        //later
        int response = 0;
        boolean success = false;
        while(!success){
            //garuntees that it wont fail if interrupted
            try{
                response = p.waitFor();
                success = true;
            }catch(InterruptedException e){
                System.out.println("Temporarily interrupted...");
            }
        
        }
        //wait until the process is done
        
        String temp = null;
        if(meanLine!=null)
            temp= meanLine.substring(meanLine.lastIndexOf("mean_volume: ")+"mean_volume: ".length(), meanLine.lastIndexOf("dB"));
        //extract the actual numerical value from that string that contained the mean value, by cutting out all the fat
        else{
            //System.out.println("Mean Parser failed to scan FFMPEG");
            temp = "-100";
            //if there was a failure parsing the mean/average values, default the response to -100, which is the quietest
        }
        return Double.valueOf(temp);
        //convert it into an actual double value from the string value
    }
    @Deprecated
    public double getVolumeMean(byte[] in) throws IOException, InterruptedException{
        String nullLocation;
        if(os)
            nullLocation = "NUL";
        else
            nullLocation = "/dev/null";
        ///get the type of null location of the OS
        
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-hide_banner", "-i", "-", "-af", "volumedetect", "-f", "null", nullLocation);
        pb.redirectErrorStream(true);
        
        
        Process p = pb.start();
        ///create the ffmpeg wrapper and run the commands for hiding the infobanner (this is to save processing time), the input file, the volumedetect filter,
        ///and the output file as the null location so it's discarded
        
        FfmpegReaderThread ffrt = new FfmpegReaderThread(p.getInputStream());
        
        ffrt.start();
        
        try{

            p.getOutputStream().write(in);


        }catch(IOException ex){
            if(!ex.getMessage().equals("Broken pipe"))
                throw ex;
        }
        
        p.getOutputStream().flush();
        p.getOutputStream().close();

        boolean spinning;
        
        do{
                try{  
                    spinning = false;
                    //ffrt.join();
                    p.waitFor();
                    ffrt.join();
                    //block until reader thread is finished
                }catch(InterruptedException ex){
                    System.out.println("Reader was temporarily interrupted");
                    spinning = true;
                }
            
        }while(spinning == true);
            //spin while interrupted
        
        String[] result = new String(ffrt.getStdOut()).split("\n");
        p.getInputStream().close();
        
        //String meanLine = null;
        String temp = "-100";
        
        for (String result1 : result) {
            if (result1.contains("mean_volume: ")) {
                temp = result1.substring(result1.lastIndexOf("mean_volume: ") + "mean_volume: ".length(), result1.lastIndexOf("dB"));
                break;
            }
        }
        
        return Double.valueOf(temp);
        //convert it into an actual double value from the string value
    }
    
    
    public boolean checkCrasher(byte[] in) throws IOException, BadVideoFile{
        
        
        byte[] mutated = in;
        
        ProcessBuilder pb = new ProcessBuilder(ffprobe.getPath(), "-v", "error", "-show_entries", "frame=width,height", "-select_streams", 
                                                "v", "-of", "csv=p=0", "-i", "-");
        pb.redirectErrorStream(true);
        
        
        Process p = pb.start();
        
        FfmpegReaderThread ffrt = new FfmpegReaderThread(p.getInputStream());
        
        ffrt.start();
        try{
                p.getOutputStream().write(mutated);
        }catch(IOException ex){
            if(!ex.getMessage().equals("Broken pipe"))
                throw ex;
        }
        
        p.getOutputStream().close();
        
        boolean spinning = false;
        while(!spinning){
                try{  
                    
                    ffrt.join();
                    spinning = true;
                    //block until reader thread is finished
                }catch(InterruptedException ex){
                    System.out.println("Reader was temporarily interrupted");
                    
                }
            
        }
            //spin while interrupted
        
        String[] numbers = new String(ffrt.getStdOut()).split("\n");
        p.getInputStream().close();
        
        String[] resString = numbers[0].split(",");
        
        if(resString.length != 2){
            System.out.println("Unable to get initial resolution from piped input");
            throw new BadVideoFile("Piped Resolution");
        }
        
        int h, w;
        
        try{
            w = Integer.parseInt(resString[0]);
            h = Integer.parseInt(resString[1]);
        }catch(NumberFormatException ex){
            System.out.println("Unable to get initial resolution from piped input");
            throw new BadVideoFile("Piped Resolution");
        }
        
        
        
        
        for(int i = 1; i < numbers.length; i++){
            
            if(numbers[i].equals(""))
                continue;
            
            String[] clump = numbers[i].split(",");
            
            if(clump.length != 2){
                System.out.println("Failed to obtain testing frame resolution");
                throw new BadVideoFile("Could not obtain resolution: " + numbers[i]);
            }
            
            int tmpH, tmpW;
            
            try{
                tmpW = Integer.parseInt(clump[0]);
                tmpH = Integer.parseInt(clump[1]);
            
            }catch(NumberFormatException ex){
                System.out.println("Failed to obtain testing frame resolution");
                throw new BadVideoFile("Resolution values: '"+clump[0]+"' by '"+clump[1]+"'");
            }
            
            if(h != tmpH || w != tmpW){
                System.out.println("Resolution went from "+w+","+h+" to "+tmpW+","+tmpH);
                p.destroy();
                return true;
            
            }
            

        }
        
        
        
        p.destroy();
        return false;
    
    
        
    }
    
    
    @Fallback
    public boolean checkCrasher(File in) throws IOException{
        /*
        This method returns true if it detected a suddent change in resolution midway in the video file, which will
        result in discord crashing. This is done by running a frame query throughout the video using ffprobe and 
        detecting if the resolution has changed
        */
        
        ProcessBuilder pb = new ProcessBuilder(ffprobe.getPath(), "-v", "error", "-show_entries", "frame=width,height", "-select_streams", 
                                                "v", "-of", "csv=p=0", "-i", in.getPath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        //create the ffprobe process
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //create the output stream buffer of ffmpeg
        
        String line = reader.readLine();
        
        if(line == null){
            //if it failed to read from stdout, inform the terminal
            //and return false
            System.out.println("Failed to read from ffprobe Process");
            p.destroy();
            //in.delete();
            return false;
        }
        
        //String res = line.substring(line.indexOf(",")+1);
        String[] resolution = line.split(",");
        
        if(resolution.length != 2){
            System.out.println("Failed to obtain the initial resolution");
            p.destroy();
            //in.delete();
            return false;
        }
        
        
        int w, h;
        
        try{
            w = Integer.parseInt(resolution[0]);
            h = Integer.parseInt(resolution[1]);
        
        }catch(NumberFormatException ex){
            System.out.println("Resolutions are not integers");
            p.destroy();
            //in.delete();
            return false;
        }

        
        while((line = reader.readLine())!=null){
            
            if(line.equals(""))
                continue;
            //dont actually know if I will need this this time, but who knows
            
            
            
            String[] clump = line.split(",");
            if(clump.length != 2){
                System.out.println("Failed to obtain testing resolution");
                p.destroy();
                //in.delete();
                return false;
            }
            
            int tmpH, tmpW;
            
            try{
                tmpW = Integer.parseInt(clump[0]);
                tmpH = Integer.parseInt(clump[1]);
            
            }catch(NumberFormatException ex){
                System.out.println("Tested resolutions are not integers");
                p.destroy();
                //in.delete();
                return false;
            }
            
            if(tmpH != h || tmpW != w){
                System.out.println("Resolution changed from "+w+","+h+" to "+tmpW+","+tmpH);
                p.destroy();
                //in.delete();
                return true;
            }
            
        
        }
        
        p.destroy();
        //in.delete();
        return false;
    
    }
    

    
    private class FfmpegReaderThread extends Thread{
        //public AtomicBoolean blocking;
        //spinlock
        private final InputStream inputStream;
        private @Nullable byte[] stdOutTotal;
        
        public byte[] getStdOut(){return this.stdOutTotal;}
        
        
        public FfmpegReaderThread(InputStream is){
            //this.blocking = new AtomicBoolean(true);
            this.inputStream = is;
            this.stdOutTotal = null;
        }
        
        
        
        @Override
        public void run(){
            
            try {
                this.stdOutTotal = this.inputStream.readAllBytes();
                
                if(this.stdOutTotal.length == 0){
                    this.stdOutTotal = null;
                }
                //why didnt I fucking think of this before
                
                
            } catch (IOException ex) {

                this.stdOutTotal = null;
                //if interrupted, unfortunately that data is lost
            }
        }
    
    
    }
    
    
    
    public static class BadVideoFile extends Exception{
    
        public BadVideoFile(String errorMessage) {
            super(errorMessage);
        }
    
    }
    
    
    
    
}
