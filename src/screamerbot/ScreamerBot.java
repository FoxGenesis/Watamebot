/*
This is a bot designed to detect loud videos and screamer videos. This is done by using an FFMPEG wrapper to convert and split
media files into mono channel wav files samples 1 second long, and uses the FFMPEG wrapper to analyze the Mean Average volume dB
of each sample and uses the logarithmic dB to detect if the volume is suddenly boosted in the video(in the case of a screamer video),
or if the video is just annoyingly loud. it then removes the video and warns the user. To use FFMPEG, a 32-bit executable of the OS 
type is extracted into the exe directory and wrapped and ran from there. 

This application is written in Java and uses the JDA API, which is a REST API

Contact SkibbleBip for any questions
 */
package screamerbot;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
//import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;


/**
 *
 * @author SkibbleBip
 * this is the main driver class. the application is started from here. the application takes no arguments.
 * there are 2 major final objects, the JDA object and the Consts object. The JDA contains the REST handles and contains all guild info, 
 * member info, and other information. the Consts object contains single-use information suh as the token and other methods thayt are initialized once 
 * 
 */
public class ScreamerBot {
    
    final private JDA discord;
    ///the JDA object
    static private Consts consts;
    //Contains all one-use items
    private static  PrintWriter errorLogger;
    //the error logger
    
    
    private ScreamerBot() throws LoginException, InterruptedException, IOException, SQLException{
        //default contructor
        consts = new Consts();
        errorLogger = new PrintWriter(new FileWriter(Consts.getLogger(), true));
        //create a new Consts
        
        discord = consts.getJDA();
        
        discord.addEventListener(new MessageProcess(consts));
        //start listening
    }
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ScreamerBot sb;
        try {
            Runtime.getRuntime().addShutdownHook(new Shutdown());
            sb = new ScreamerBot();
            //initialize the main bot object

            //now its able to read messages
        } catch (LoginException | InterruptedException | IOException | SQLException ex) {
            ex.printStackTrace(errorLogger);
            Logger.getLogger(ScreamerBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
    }
    /*
        This is the internal shutdown class that the bot uses to enforce it's shutdown sequence.
    when the bot shut downs, this daemon shutdown thread is run and procedes to finalize various functions
    and prepares itself for shutdown
    */
    private static class Shutdown extends Thread{
        public Shutdown(){}
        
        public void run(){
            System.out.println("Envoking shutdown sequence...");
            try {
                consts.getSQLConnection().close();
                System.out.println("Closed connection to SQL Server...");
                //close the SQL server
                consts.terminateAllThreads();
                System.out.println("Stopped all renaming threads");
                
                consts.getJDA().shutdownNow();
                System.out.println("Shut down bot instance...");
                //shut down the bot instance
                System.out.println("Shut down complete. Exiting.");
            } catch (SQLException ex) {
                //should an error occure, then log it.
                Logger.getLogger(ScreamerBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
    
    }
}
