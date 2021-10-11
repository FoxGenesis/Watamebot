/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
//import com.google.common.jimfs.Jimfs;
//import com.google.common.jimfs.Configuration;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

@interface FuckedUp{}
@interface Unneeded{}

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
    //FileSystem virtualFileSystem = Jimfs.newFileSystem(Configuration.forCurrentPlatform());
    
    final int ErrorCode;
    
    final boolean DEBUG = false; 
    
    public Ffmpeg() throws IOException{
        ErrorCode = 0;
        
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
    @Deprecated
    public File encode(File is) throws IOException, InterruptedException{
        
        /*
        as of now, this method is never used. It may be used in a later update. all it does in rip the audio out of the downloaded media file
        and save it as a wav file.
        */
        
        
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-y", "-i", is.getPath(), "-ac", "1", "-vn", "tmp"+fs+"audio.wav");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        ///run ffmpeg with the commands for overwrite, input file, merging audio into mono, and ignoring any video streams. this saves it to "tmp/audio.wav"
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //get the input stream reader so we can grab the piped output from ffmpeg
        
        String line = null;
        do{line = reader.readLine();}while(line != null);
        //simply take ffmpeg's piped output and discard it, we dont need it
        
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
        //wait until its done encoding
        Thread.sleep(2000);
        //sleep for 2 seconds. this garuntees that the file wont still be open when its time to delete it, which will hang the program
        is.delete();
        //delete the media file
        
       return new File("tmp"+fs+"audio.wav");
       //return the newly extracted audio file
    }
    
    
    public ArrayList<byte[]> grabSegments(byte[] buffer) throws IOException, InterruptedException{
        ArrayList<byte[]> output = new ArrayList<>();
        
        
        String nullLocation;
        if(os)
            nullLocation = "NUL";
        else
            nullLocation = "/dev/null";
        ///get the type of null location of the OS
        
        
        //calculate actual duration of video
        ProcessBuilder pb = new ProcessBuilder(ffmpeg.getPath(), "-hide_banner", "-i", "-", "-f", "null", nullLocation);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try{
            p.getOutputStream().write(buffer);
        }catch(IOException ex){
            if(!ex.getMessage().equals("Broken pipe"))
                throw ex;
        }
        p.getOutputStream().flush();
        p.getOutputStream().close();
        //receive the piped data and detect it's format and duration


        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
        String line, time;
        
        double seconds = -1.0;
        
        while((line=reader.readLine()) != null){
            if(line.startsWith("frame=")){
                int start = line.indexOf("time=") + "time=".length();
                int end = line.indexOf(" ", start);
                
                time = line.substring(start, end);
                
                String[] tmp = time.split(":");
                
                seconds = 3600 * Integer.parseInt(tmp[0]) + 60 * Integer.parseInt(tmp[1]) + Double.parseDouble(tmp[2]);
                break;
            }
        
        }
        p.getInputStream().close();
        p.destroy();
        //p = null;
        
        if(seconds < 0)
            return null;
        
        int count = 0;
        
        while(count < seconds){
            

            
            if(seconds - count < 1.0)
                //depending on if there is data duration that can occupy a full second, or the remaining
                //bit of audio that is less than a second, we generate a new process for the duration
                pb = new ProcessBuilder(ffmpeg.getPath(), "-hide_banner", "-i", "-", "-codec", "copy","-f", "wav", "-ac", "1", /*"-af", 
                        "highpass=f=200",*/ "-ss", String.valueOf(count), "-"
                
                );
            else
                pb = new ProcessBuilder(ffmpeg.getPath(), "-hide_banner", "-i", "-", "-codec", "copy", "-f", "wav", "-ac", "1", /*"-af", 
                        "highpass=f=200",*/ "-ss", String.valueOf(count), "-t", "1", "-"
                
                );
            pb.redirectErrorStream(false);
            
            
            p = pb.start();
            
            @Nullable byte[] stdOut;
            
            FfmpegReaderThread ffrt = new FfmpegReaderThread(p.getInputStream());
            
            ffrt.start();
            //start the reader thread
            
            try{
                //messy hack to accept a closed stdin to the FFMPEG wrapper, as 
                //ffmpeg will sometimes close it's stdin early when it figures out it's 
                //done reading data in in order to finish the wav data (enough data is provided for wav,
                //we dont need anymore data)
            
                p.getOutputStream().write(buffer);
            }catch(IOException ex){
                if(!ex.getMessage().equals("Broken pipe")){
                    //in the event that the exception ISNT a broken pipe, then something fucky has 
                    //happened and we do in fact need this exception
                    throw ex;
                }
            }
            
            p.getOutputStream().flush();
            p.getOutputStream().close();
            //flush and close the STDIN stream
            
            //System.out.println("AAAAAAAAAAAAA" + new String (p.getErrorStream().readAllBytes()));
            
            //while(ffrt.blocking.get())
                //spinblock until thread reader is ready to pass the read data
                //;
                
            boolean success = false;
            
            while(!success){
                try{  
                    //System.out.println("joining");
                    ffrt.join();
                    success = true;
                    //block until reader thread is finished
                }catch(InterruptedException ex){
                    System.out.println("Reader was temporarily interrupted");
                }
            
            }
            //spin while interrupted
            //System.out.println("im out");
            
            
            
            stdOut = ffrt.getStdOut();
            //obtain the STDOUT piped data from the reader thread
            
            //if(stdOut == null)
            //    throw new NullPointerException();
            
            output.add(stdOut);
            //add the wav data to the collection of datas.
            p.destroy();
            
            count++;
        }
        
        
        return output;
    
    }
    
    
    
    @Deprecated
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

        //wait for the ffmpeg process to finish, get the return response of the wrapped ffmpeg
        //Thread.sleep(2000);
        //sleep for 2 seconds. this gives enough time to make sure the media file is done being processed by ffmpeg, so it has had time to close and allow it
        //to be deleted
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
    
    @Unneeded
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

        //wait for the ffmpeg process to finish, get the return response of the wrapped ffmpeg
        //Thread.sleep(2000);
        //sleep for 2 seconds. this gives enough time to make sure the media file is done being processed by ffmpeg, so it has had time to close and allow it
        //to be deleted
        //in.delete();
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
                    ffrt.join();
                    //block until reader thread is finished
                }catch(InterruptedException ex){
                    System.out.println("Reader was temporarily interrupted");
                    spinning = true;
                }
            
        }while(spinning == true);
            //spin while interrupted
        
        String[] result = new String(ffrt.getStdOut()).split("\n");
        
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
        
        
        //byte[] mutated = mutateBuffer(in);
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
            
            /*if(clump.contains("partial file")){
                ///TODO: fix the issue with partial files
                System.out.println("Partial file, bad encoding");
                
                java.util.Random r = new java.util.Random();
            
                String name = "Partial_" + String.valueOf(r.nextInt());
            
                try (FileOutputStream fos = new FileOutputStream("tmp/"+name)) {
                    fos.write(mutated);
                }
            }*/
            
            /*if(!res.equals(clump) && !clump.trim().equals("")){
                //if the resolution suddenly changes
                
                System.out.println("Video resolution changed from " + res + " to " + clump);
                p.destroy();
                return true;
                //destroy the process, delete the file, return true
            }*/
        }
        
        
        
        p.destroy();
        return false;
    
    
        
    }
    
    
    @Deprecated
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
        
        //initial resolution of file
        //reader.readLine();
        //burn a byte
        
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
    
    
    
    
    void create(Path path, String fileName) {
        //function to create a file by it's name and the path it is located in
        Path filePath = path.resolve(fileName);
        try {
            Files.createFile(filePath);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    void update(Path path, byte[] b) {
        //function to write bytes to a file 
        try {
            Files.write(path, b);
            //return newContent;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    
    
    public static byte[] mutateBuffer(byte[] bytes) throws IOException, BadVideoFile{
        //byte[] bytes = in.readAllBytes();
        byte[] mutated = new byte[bytes.length];
        //ByteBuffer byteBuff;
        byte[] tmpSize = new byte[4];
                     
        byte[] header = Arrays.copyOf(bytes, 4);
        
        //byteBuff = ByteBuffer.wrap(header).getInt();
        int startingPoint = ByteBuffer.wrap(header).getInt();
        //int startingPoint = byteBuff.getInt();
                     
        System.arraycopy(bytes, 0, mutated, 0, startingPoint);
        //copy the first bits of header we want into the mutated file

                    
        boolean foundMoov = false;
        boolean foundMdatFirst = false; 
        
                     
        //int v = 0;
        //int z = 0;
        byte[] moov = {'m', 'o', 'o', 'v'};
        byte[] mdat = {'m', 'd', 'a', 't'};
       // byte last = 0x0;
        
        int moovLocation;
        for(moovLocation = startingPoint; moovLocation <  bytes.length; moovLocation++){
            if(bytes[moovLocation] == moov[0]){
                foundMoov = true;
                for(int j = 1; j < moov.length; j++){
                    if(bytes[moovLocation+j] != moov[j]){
                        foundMoov = false;
                        break;
                    }
                }
                
                if(foundMoov == true)
                    break;

            }
            
            if(bytes[moovLocation] == mdat[0] && !foundMdatFirst){
                foundMdatFirst = true;
                
                for(int j = 1; j < mdat.length; j++){
                    
                    if(bytes[moovLocation+j] != mdat[j]){
                        foundMdatFirst = false;
                        break;
                    }
                }
                
                
            
            }
            
            
        }
        
        //moovLocation+=4;
        
        /*for(moovLocation = startingPoint; moovLocation <  bytes.length; moovLocation++){
            if(bytes[moovLocation] == moov[v]){
                v++;
                last = bytes[moovLocation];
            }
            else if(last != bytes[moovLocation])
                v = 0;
            
            if(bytes[moovLocation] == mdat[z] && !foundMdatFirst){
                z++;
                last = bytes[moovLocation];
            }
            else if(last != bytes[moovLocation])
                z = 0;
                        
            if(v == 4){
                foundMoov = true;
                break;
            }
            if(z == 4){
                
                
                foundMdatFirst = true;
                z = 0;
                //break;
            }
            
        }*/
        
        
        if(foundMdatFirst != true){
            //if mdat was found before the moov atom, then we know the moov atom is after it (a big no no),
            //in which we need to mutate the data. if moov was found BEFORE mdat, then ffmpeg is capable of 
            //reading the file unaltered
            ///DONT FUCKING TOUCH THIS
            
            return bytes;
            
        }
                     
        
        
        System.arraycopy(bytes, moovLocation-4, tmpSize, 0, 4);
        
        //byteBuff = ByteBuffer.wrap(tmpSize);

        //int moovLength = byteBuff.getInt();
        int moovLength = ByteBuffer.wrap(tmpSize).getInt();
        //TODO: garuntee ints fit in this             
        
        
        if(moovLength < 0 /*|| moovLength > bytes.length - moovLocation*/){
            //moovLength = bytes.length - moovLocation;
            throw new BadVideoFile("The video file has an moov atom with a bad size");
            ///TODO: Fix this, I need a way to detect multiple moov atoms
            
        }
        
                    
        try{           
        System.arraycopy(bytes, moovLocation-4, mutated, startingPoint, moovLength);
        //move the moov atom and container to the front
        ///TODO: somehwere here theres an issue with array out of bounds
        }catch(java.lang.ArrayIndexOutOfBoundsException ex){
            
            System.out.println("Bad Array!!!");
            java.util.Random r = new java.util.Random();
            
            String name = String.valueOf(r.nextInt());
            
            try (FileOutputStream fos = new FileOutputStream("tmp/"+name)) {
            fos.write(bytes);
            
        }
        
        }
        
        
        moovLocation-=4;
        
        if(moovLocation == startingPoint){
            //if the moov atom is located EXACTLY after the header, we dont need to perform
            //any changes
            return bytes;
        }

        byte[] before = new byte[moovLocation-startingPoint];     
        //                src       srcPos      dest   destPos      length
        System.arraycopy(bytes, startingPoint, before, 0, moovLocation-startingPoint);
        //put everything that was before the moov atom, after the moov atom
        
        
        int remaining = bytes.length - (moovLocation+moovLength);
        //int startOfRemaining = startingPoint+moovLength + (moovLocation-startingPoint);
        //int startOfRemaining = moovLocation+moovLength;
        
        byte[] after = new byte[remaining];
        
        System.arraycopy(bytes, (moovLocation+moovLength), after, 0, remaining);
        //copy remaining data to end of output stream
                    
        System.arraycopy(before, 0, mutated, startingPoint+moovLength, before.length);
        System.arraycopy(after, 0, mutated, startingPoint+moovLength+before.length, after.length);
                    
        boolean stcoFound = false;
        boolean co64Found = false;
        
        
        
        
        //v = 0;
        //int p = 0;
        
        byte[] stcoStr = {'s', 't', 'c', 'o'};
        byte[] co64Str = {'c', 'o', '6', '4'};
        
        int count;
        
        for(int q = startingPoint; q < moovLength+startingPoint; q++){
            if(mutated[q] == stcoStr[0]){
                stcoFound = true;
                
                for(int j = 1; j < stcoStr.length && stcoFound; j++){
                    if(mutated[q+j] != stcoStr[j])
                        stcoFound = false;
                }   
          
            
            }
            
            if(mutated[q] == co64Str[0]){
                co64Found = true;
                
                for(int j = 1; j < co64Str.length && co64Found; j++){
                    if(mutated[q+j] != co64Str[j])
                        co64Found = false;
                }
                
   
            }
            
            if(stcoFound || co64Found){
                q+=8;
                byte[] tmpClump = {mutated[q], mutated[q+1], mutated[q+2], mutated[q+3]};
                count = ByteBuffer.wrap(tmpClump).getInt();
                q+=4;
                
                if(stcoFound){
                    for(int qq = 0; qq < count; qq++){
                        int tmp;
                        byte[] tmpClump2 = {mutated[q], mutated[q+1], mutated[q+2], mutated[q+3]};
                        //byteBuff = ByteBuffer.wrap(tmpClump2);
                        //tmp = byteBuff.getInt();
                        tmp = ByteBuffer.wrap(tmpClump2).getInt();


                        tmp += moovLength;
                        //byteBuff = ByteBuffer.allocate(4);
                        //byteBuff.putInt(tmp);
                        byte[] result = ByteBuffer.allocate(4).putInt(tmp).array();//byteBuff.array();
                        mutated[q] = result[0];
                        mutated[q+1] = result[1];
                        mutated[q+2] = result[2];
                        mutated[q+3] = result[3];

                        q+=4;
                        
                        stcoFound = false;
                        co64Found = false;
                    }
                
                }else{
                    for(int qq = 0; qq < count; qq++){
                    long tmp;
                    byte[] tmpClump2 = {mutated[q], mutated[q+1], mutated[q+2], mutated[q+3], mutated[q+4], mutated[q+5], mutated[q+6], mutated[q+7]};
                    tmp = ByteBuffer.allocate(8).wrap(tmpClump2).getLong();//byteBuff.getLong();


                    tmp += moovLength;
                    byte[] result = ByteBuffer.allocate(8).putLong(tmp).array();
                    
                    mutated[q] = result[0];
                    mutated[q+1] = result[1];
                    mutated[q+2] = result[2];
                    mutated[q+3] = result[3];
                    mutated[q+4] = result[4];
                    mutated[q+5] = result[5];
                    mutated[q+6] = result[6];
                    mutated[q+7] = result[7];

                    q+=8;
                }
                
                }
                
                
                
                
            }
        
        }
        
        
        return mutated;
    
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
                
                /*int readBytes = 1;
                
                do{
                byte[] tmp = new byte[1024];
                //boolean wasInterrupted;
                //int c = 0;
                
                
                //do{
                try {
                //wasInterrupted = false;
                readBytes = inputStream.read(tmp);
                
                if(readBytes < 0)
                break;
                
                
                if(stdOutTotal == null){
                stdOutTotal = new byte[readBytes];
                System.arraycopy(tmp, 0, stdOutTotal, 0, readBytes);
                }
                else{
                byte[] stdOutTmp = new byte[readBytes + stdOutTotal.length];
                System.arraycopy(stdOutTotal, 0, stdOutTmp, 0, stdOutTotal.length);
                System.arraycopy(tmp, 0, stdOutTmp, stdOutTotal.length, readBytes);
                this.stdOutTotal = stdOutTmp;
                }
                
                
                //c+=readBytes;
                //System.out.println("im stuck");
                
                } catch (IOException ex) {
                //System.out.println("Reader was temporarily interrupted");
                Logger.getLogger(ScreamerBot.class.getName()).log(Level.SEVERE, null, ex);
                //wasInterrupted = true;
                }
                //}while(wasInterrupted == true);
                
                }while(readBytes >= 0 );
                */
                
                //this.blocking.set(false);
                //System.out.println("im outskis");
                
                //return;
            } catch (IOException ex) {
                //Logger.getLogger(Ffmpeg.class.getName()).log(Level.SEVERE, null, ex);
                this.stdOutTotal = null;
            }
        }
    
    
    }
    
    
    
    public static class BadVideoFile extends Exception{
    
        public BadVideoFile(String errorMessage) {
            super(errorMessage);
        }
    
    }
    
    
    
    
}
