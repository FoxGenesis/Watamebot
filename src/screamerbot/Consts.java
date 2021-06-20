/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
//import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
/**
 *
 * @author SkibbleBip
 * This is the Constants class. It holds variables that must be initialized once, and never changed again. It also provides 
 * a few initializer methods that create directories if they havent been already done. It creates the repo, exe, and tmp folder.
 * The repo folder contains the guild directories which contain the blocking status files of each guild. The exe folder contains
 * the extracted ffmpeg wrapper executable which is dependent on OS. the tmp folder is used to hold the splitted audio wav samples and the
 * downloaded attachment temp files. these are created the first time the application is ran.
 */
public class Consts {
    
    final private String token;
    //this is the private token.
    final private Connection connectionHandler;
    final private JDA discord;
    private static  PrintWriter errorLogger;
    private final ArrayList<MyThread> threads;
    
    //private final ArrayList<MalwareData> malwareDatabases = null;
    
    
    
    Consts() throws InterruptedException, LoginException, IOException, SQLException{
    
    errorLogger = new PrintWriter(new FileWriter(Consts.getLogger(), true));
    threads = new ArrayList();
    
    BufferedReader br = new BufferedReader(new FileReader("token.txt"));
    token = br.readLine();
    //obtain the token
    br.close();
    connectionHandler = DriverManager.getConnection("jdbc:sqlite:repo"+File.separatorChar+"database.db");
    //create connection to the database
    System.out.println("Successfully connected to the SQLite databbase");
    
    String newTable = "CREATE TABLE IF NOT EXISTS Guild (\n"
            + "GuildID TEXT,\n"
            + "Roles TEXT,\n"
            + "DunceActive INTEGER,\n"
            + "DunceName TEXT,\n"
            + "MalwareRole TEXT,\n"
            + "MalwareChannel TEXT,\n"
            + "MalwareStat INTEGER,\n"
            + "ScreamerStat INTEGER\n"
            +");";
    java.sql.Statement stmt = connectionHandler.createStatement();
    stmt.execute(newTable);
    stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS GuildString on Guild (GuildID);");
    /**Create a Guild table if it doesn't exist**/
    
    newTable = "CREATE TABLE IF NOT EXISTS RoleList (\n"
            + "GuildID TEXT,\n"
            + "Member TEXT,\n"
            + "Roles TEXT\n"
            +");";
    
    stmt.execute(newTable);
    stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS MemberString on RoleList (Member);");
    /**Create a Role table if it doesn't exist**/
    
        
        discord = JDABuilder.createDefault(getToken(), 
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES
                ).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        //connect the JDA to the bot account through the token passed from the Consts object
        discord.setAutoReconnect(true);
        //enable autoreconnecting so in case it gets disconnected it can reconnect to the internet socket
        discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Initializing..."));
        //make the game playing status say that it's starting up
        //discord.awaitReady();
        //set the JDA to waiting for action
        
        List<Guild> guilds = discord.getGuilds();
        for(int i = 0; i < guilds.size(); i++){
            //If the guilds arent already added to the database, then add them
            String toSend = "INSERT OR IGNORE INTO Guild (GuildID, DunceActive, MalwareStat, ScreamerStat) VALUES (?,0,0,0)";
            PreparedStatement ps = connectionHandler.prepareStatement(toSend);
            
            ps.setString(1, guilds.get(i).getId());
            ps.executeUpdate();
            
        }
        
        String getAll = "SELECT GuildID, DunceActive, Roles, DunceName FROM Guild";
        //Statement stmt = connectionHandler.createStatement();
        ResultSet rs = stmt.executeQuery(getAll);
        
        while(rs.next()){
            String guild = rs.getString("GuildID");
            String dRoles = rs.getString("Roles");
            int dActive = rs.getInt("DunceActive");
            String dName = rs.getString("DunceName");
            /*obtain all the dunce variables. The dunce variables are used to determine
            the users who have '!' at the front of their names, henceforth will be given
            a "bully" role and renamed to something specified by an admin*/
            
            if(dActive == 1){
                //if the renaming feature is active, then get the bully roles 
                //split them up by the '-' char, obtain the role object,
                //and put them in a list
                String[] lRoles = null;
                ArrayList<Role> roles = null;
                if(dRoles != null){
                    lRoles = dRoles.split("-");
                    roles = new ArrayList();
                    for(int i = 0; i < lRoles.length; i++){
                        Role r = discord.getRoleById(lRoles[i]);
                        if(r != null)
                            roles.add(r);      
                    }
                }
                
                Guild g = discord.getGuildById(guild);
                //obtain the guild object from the guild id
                
                MyThread thread = new MyThread(g, roles, dName);
                thread.start();
                threads.add(thread);
                //create a naming thread using the guild, roles, and name
            }//end if dActive = 1
        
        }//end while
        
        System.out.println("Adding member roles to database if not added already... This may take a bit of time");
        
        for(int i = 0; i < guilds.size(); i++){
            Guild g = guilds.get(i);
            List<Member> ms = g.loadMembers().get();
            for(int j = 0; j < ms.size(); j++){
                Member m = ms.get(j);
                
                String roles = "";
                String guildID = g.getId();
                String userID = m.getUser().getId();
                
                if(m.getRoles().isEmpty())
                    continue;
                
                roles = roles + m.getRoles().get(0).getId();
                
                for(int k = 1; k < m.getRoles().size(); k++){
                    roles = roles + "-" + m.getRoles().get(k).getId();
                    //add all the user's roles into a string separated by '-'
                }
                
                String toSend = "INSERT OR IGNORE INTO RoleList (GuildID, Member, Roles) VALUES(?,?,?)";
                
                PreparedStatement ps = connectionHandler.prepareStatement(toSend);
                ps.setString(1, guildID);
                ps.setString(2, userID);
                ps.setString(3, roles);
                
                ps.executeUpdate();
                
            }
        
        }
        
        System.out.println("Completed loading members");
        discord.awaitReady();
        //set the JDA to waiting for action
        
        discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Type <help>"));
        //set the game to "Type <help>"
    
    }
    
    
    private String getToken(){return token;}
    //get token method
    public JDA getJDA(){return discord;}
    //get the JDA object
    public Connection getSQLConnection(){return connectionHandler;}
    //get the connection handler
    
    static public File getLogger(){return new File("logger.log");}
    //get the logger
    
    public GuildInfo getGuildInfo(String in){
        //returns a GuildInfo onject that contains the entire guild information 
        //stored in the Guild table in the database, for the specified guild ID
        String query = "SELECT Roles, DunceActive, DunceName, MalwareRole, MalwareStat, MalwareChannel, ScreamerStat FROM Guild WHERE GuildID = ?";
        GuildInfo gInfo = null;
        try {
            PreparedStatement stmt = connectionHandler.prepareStatement(query);
            //ResultSet rs = stmt.executeQuery(query);
            stmt.setString(1, in);
            ResultSet rs = stmt.executeQuery();
            //prepare the statement, get the returned response

            while(rs.next()){
                
                String tmp = rs.getString("Roles");
                //obtain the Roles string
                
                
                List<String> roleIDs = null;
                if(tmp==null){
                    roleIDs = Collections.emptyList();
                    //if the roles string is null, then the roles list should be empty
                }
                else{
                    roleIDs = Arrays.asList(tmp.split("-"));
                    //if the roles's string is not empty, then split it
                    //up by the '-' char
                }
                
                int dActive = rs.getInt("DunceActive");
                String dName = rs.getString("DunceName");
                String mRole = rs.getString("MalwareRole");
                int mStat = rs.getInt("MalwareStat");
                String gChan = rs.getString("MalwareChannel");
                int sStat = rs.getInt("ScreamerStat");
                boolean dA = false, mS = false, sS = false;
                //obtain all the guild's variables in the Guild table
                if(dActive == 1)
                    dA = true;
                if(mStat ==1)
                    mS = true;
                if(sStat == 1)
                    sS = true;
                
                Guild g = discord.getGuildById(in);
                //obtain all data from the Guild table, for the inputted guild ID
                gInfo = new GuildInfo(in, roleIDs, dA, dName, mRole, mS, gChan, sS);
                //create new GuildInfo object
                
                
            
            }
        } catch (SQLException ex) {
            //if there is an error with reading the database, then throw error and return
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
        }
    return gInfo;
    //return the GuildInfo object
    }
    
    public class GuildInfo{
        //Sub-class that contains an encapsulation of all the properties of a guild
        //with respect to what was pulled from the Guild table.
        //Dunces are guild members who have '!' at the front of their name
        //so they stand out on the member list to be annoying, so the bot produces
        //daemon threads to rename them in the background
        private final String GuildID;                       //ID of the guild
        @Nullable private Role[] DunceRoles;                //roles to be given to the dunce
        private final boolean DunceActive;                  //flag for if renaming dunces is active
        @Nullable private final String DunceName;           //the name to rename the dunce
        @Nullable private final Role TimeOut;               //role of the timeout role
        private final boolean MalwareStatus;                //boolean flag if the guild is blocking malware uploads or not
        @Nullable private final GuildChannel guildChannel;  //guild channel of the timeout channel (to be used in the future to ping the offender)
        private final boolean ScreamerStatus;               //boolean flag if the guild is blocking screamer videos
        
        public String getGuildID(){return this.GuildID;}
        public Role[] getDunceRoles(){return this.DunceRoles;}
        public boolean getDunceStatus(){return this.DunceActive;}
        public String getDunceName(){return this.DunceName;}
        public Role getTimeOutRole(){return this.TimeOut;}
        public boolean getMalwareStatus(){return this.MalwareStatus;}
        public boolean getScreamerStatus(){return this.ScreamerStatus;}
        public GuildChannel getGuildChannel(){return this.guildChannel;}
        /*Getter functions*/
        
        
        
        
        public GuildInfo(String gid, List<String> dRoles, boolean dActive, String dName, String to, boolean mStat, String gChanID, boolean sStat){
            //this is a GuildInfo object constructor that contains all information stored in a single 
            //guild in the table, per inputted values. This is called and returned by getGuildInfo();
            this.GuildID = gid;
            this.DunceActive = dActive;
            this.DunceName = dName;
            this.MalwareStatus = mStat;
            this.ScreamerStatus = sStat;
            if(dRoles == null){
                //if there are no roles, then set DunceRoles to null
                this.DunceRoles = null;
                
            }
            else{
                //otherwise, take each inputted role ID and produce a 
                //array of role objects per each role.
                Role[] DunceRolesTmp = new Role[dRoles.size()];
                int c = 0;
                for(int i = 0; i<DunceRolesTmp.length; i++){
                    Role tmp = discord.getRoleById(dRoles.get(i));
                    if(tmp != null){
                        DunceRolesTmp[c] = tmp;
                        c++;
                    }

                }
                this.DunceRoles = new Role[c];
                System.arraycopy(DunceRolesTmp, 0, this.DunceRoles, 0, c);
                //copy the tmp roles into the final array (TODO: optimize this)
            }
            if(to == null)
                //if no timeout role, then set the timeout role to null
                this.TimeOut = null;
            else
                //otherwise get the role from the inputted id
                this.TimeOut = discord.getRoleById(to);
            
            if(gChanID == null)
                //if the timeout channel is null, set the guildchannel to null
                this.guildChannel = null;
            else
                this.guildChannel = discord.getGuildChannelById(gChanID);
            //otherwise set the timeout channel to the channel object from the guildchannel ID 
            
        }
        
    }
    
    public boolean setDunceName(String guild, String in){
        //function that updates the Dunce name for a guild, by it's ID and inputted string
        try {

            if(in != null){
                //if the string is null, then it's invalid
                if(in.length() > 32)
                    return false;
                
            }

            
            String update = "UPDATE Guild \n"
                    + "SET DunceName = ?\n"
                    +"WHERE \n GuildID = ?";
            PreparedStatement stmt = connectionHandler.prepareStatement(update);
            stmt.setString(1, in);
            stmt.setString(2, guild);
            stmt.executeUpdate();
            //prepare the statement, and set the duncename and guild ID for the database querry 
            
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean setDunceRoles(String guild, List<Role> roles){
        try {
            if(roles == null)
                return false;
            //if the roles are null, then it's invalid
            
            String toSend;
            if(!roles.isEmpty()){
                //if the list is not empty 
                if(roles.get(0) == null)
                    //if first index is null, it is invalid
                    return false;
                
                //***************************************************
                toSend = roles.get(0).getId();

                for(int i = 1; i< roles.size(); i++){
                    if(roles.get(i) == null)
                        return false;

                    toSend = toSend+ "-" + roles.get(i).getId();
                }
                //combine each role ID into one string separated by '-' chars
                //***************************************************
            }
            //if the list is empty, set the string to null
            else
                toSend = null;
            
            String update = "UPDATE Guild \n"
                    + "SET Roles = ?\n"
                    +"WHERE \n GuildID = ?";
            PreparedStatement stmt = connectionHandler.prepareStatement(update);
            stmt.setString(1, toSend);
            stmt.setString(2, guild);
            stmt.executeUpdate();
            //prepare the statement, send the role string and guild id
            
            
            return true;
        } catch (SQLException ex) {
            //should there be an error, throw a log and return false
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    public boolean toggleDunceActive(String guild){
        //function that toggles the dunce active bool flag of the inputted guild, by it's ID
        try {
            String select = "SELECT DunceActive FROM Guild WHERE GuildID = ? ";//'"+guild+"';";
            PreparedStatement stmt = connectionHandler.prepareStatement(select);
            stmt.setString(1, guild);
            //prepare the statement, and sed a request for the DunceActive variable,
            //by searching for the guild with the inputted ID
            
            ResultSet rs = stmt.executeQuery();
            //obtain response from the query
            if(rs.next()){
                //if there was a response, process the response
                int val = rs.getInt("DunceActive");
                int toOut = 0;
                if(val == 0){
                    toOut = 1;
                }
                //invert the response integer
                String update = "UPDATE Guild \n"
                        + "SET DunceActive = ? "//'"+toOut+"'\n"
                        +"WHERE \n GuildID = ?";//'"+guild+"'";
                //Statement stmt = connectionHandler.createStatement();
                stmt = connectionHandler.prepareStatement(update);
                //re-prepare the statement 
                stmt.setInt(1, toOut);
                stmt.setString(2, guild);
                stmt.executeUpdate();
                //submit the new DunceActive boolean flag and guild ID,
                //execute the query
                
                return true;
            }
            else
                return false;
            //if no response was captured, something went wrong, return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    public boolean setTimeOutRole(String guild, String roleID){
        //sets the timeout role of the offender, per the guid ID and the role ID
        try {
            Role r = discord.getRoleById(roleID);
            if(r == null)
                return false;
            //if the role object is null, then it is invalid
            
            String update = "UPDATE Guild \n"
                    + "SET MalwareRole = ?\n"
                    +"WHERE \n GuildID = ?";
            PreparedStatement stmt = connectionHandler.prepareStatement(update);
            stmt.setString(1, roleID);
            stmt.setString(2, guild);
            stmt.executeUpdate();
            //prepare the statement, apply the role ID and the Guild ID, and submit the statement
            return true;
            //return true on success
        } catch (SQLException ex) {
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            //if there was an error, log it and return false
        }
    }
    public boolean toggleMalwareActive(String guild){
        try {
            String select = "SELECT MalwareStat FROM Guild WHERE GuildID = ?";//'"+guild+"';";
            PreparedStatement stmt = connectionHandler.prepareStatement(select);
            //assuming that the guild ID returned from discord itself is legit,
            stmt.setString(1, guild);
            ResultSet rs = stmt.executeQuery();
            //prepare the statement, apply the guild ID, and rexecute it.
            if(rs.next()){
                int val = rs.getInt("MalwareStat");
                int toOut = 0;
                if(val == 0){
                    toOut = 1;
                }
                //invert the MalwareStat flag
                
                String update = "UPDATE Guild \n"
                        + "SET MalwareStat = ? "//+toOut+"\n"
                        +"WHERE \n GuildID = ? ";//'"+guild+"'";
                //Statement stmt = connectionHandler.createStatement();
                stmt = connectionHandler.prepareStatement(update);
                stmt.setInt(1, toOut);
                stmt.setString(2, guild);
                stmt.executeUpdate();
                //re-prepare the statement and set the MalwareStat flag per the guild
                //ID, and push the update.
                
                return true;
            }
            else
                //if there was no response, then there was an error
                return false;
            
        } catch (SQLException ex) {
            //should there be an exception, log it and return false;
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
       
    
    }
    public boolean toggleScreamerStatus(String guild){
        //function that toggles the screamer video blocking flag, per the inputted guild ID
        try {
            String select = "SELECT ScreamerStat FROM Guild WHERE GuildID = ?";//'"+guild+"';";
            PreparedStatement stmt = connectionHandler.prepareStatement(select);
            //assuming that the guild ID returned from discord itself is legit,
            stmt.setString(1, guild);
            ResultSet rs = stmt.executeQuery();
            //prepare the statement, pass the guild ID and obtain the results
            if(rs.next()){
                int val = rs.getInt("ScreamerStat");
                int toOut = 0;
                if(val == 0){
                    toOut = 1;
                }
                //obtain the ScreamerStat flag and invert it
                String update = "UPDATE Guild \n"
                        + "SET ScreamerStat = ? "//'"+toOut+"'\n"
                        +"WHERE \n GuildID = ?";//'"+guild+"'";
                //Statement stmt = connectionHandler.createStatement();
                stmt = connectionHandler.prepareStatement(update);
                stmt.setInt(1, toOut);
                stmt.setString(2, guild);
                //re-prepare the statement, give the guild the new status flag,
                //using it's guild ID
                
                stmt.executeUpdate();//(update);
                return true;
                //push the update and return true
            }
            else
                //if there is no response, then its invalid, return false.
                return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            //if an error were to happen, log it and return false.
        }
        
    
    }
    
    public void replaceRolesInThread(String guildID, List<Role> roles){
        //function that takes the inputted role aand replaces the current roles 
        //in a running htread by the parent guild's ID
        for(int i =0; i< threads.size(); i++){
            if(threads.get(i).getGuild().getId().equals(guildID)){
                threads.get(i).setRoles(roles);
                break;
                //cycle through linearly and find the thread that has the guild's
                //ID, replace the roles inside of it, then break out
            }
        }
    
    }
    
    public void setDunceNameInThread(String guildID, String name){
        //function that takes the inputted name string and sets it to the 
        //renaming thread's dunce name, per the guild ID
        for(int i = 0; i <  threads.size(); i++){
            if(threads.get(i).getGuild().getId().equals(guildID)){
                threads.get(i).setDunceName(name);
                break;
                //linear search through the threads, when it comes accross the
                //thread with the requested Guild ID, it replaces the dunce name and 
                //then breaks.
            }
        }
    }
    
    public boolean setTimeOutChannel(String guildID, GuildChannel gc){
        try {
            //Role r = discord.getRoleById(roleID);
            if(gc == null)
                return false;
            if(guildID == null)
                return false;
            //if either the guild ID or the Guild channel are null, the inputs are invalid
            
            String update = "UPDATE Guild \n"
                    + "SET MalwareChannel = ? "//'"+gc.getId()+"'\n"
                    +"WHERE \n GuildID = ?";//'"+guildID+"'";
            PreparedStatement stmt = connectionHandler.prepareStatement(update);
            //assuming that the guild ID returned from discord itself is legit,
            stmt.setString(1, gc.getId());
            stmt.setString(2, guildID);
            stmt.executeUpdate();
            //prepare the statement, append the MalwareChannel (which the bot will use
            //to ping the offender member) by getting it's ID, and get the guild ID of the
            //guild channel by it's string.
            //push the update
            return true;
        } catch (SQLException ex) {
            //should an error occure, log it and return false
            Logger.getLogger(Consts.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    
    }
    
    public void removeThread(String guildID){
        //When the running renaming thread is no longer needed, it can be removed.
        for(int i = 0; i <  threads.size(); i++){
            if(threads.get(i).getGuild().getId().equals(guildID)){
                //cycle through the naming threads linearly,
                //when the thread we are looking for is identified by it's 
                //guild ID, it is stopped and then removed from the thread list
                threads.get(i).stopRunning();
                threads.remove(i);
                break;
            }
        }
    }
    
    public void addThread(String guildID){
        //creates a new thread and adds it to the renaming thread heap
        GuildInfo gi = getGuildInfo(guildID);
        //obtain the information of the guild,
        List<Role> r = Arrays.asList( gi.DunceRoles);
        //convert the array of roles into a list
        String n = gi.DunceName;
        //obtain the dunce name fo that guild
        Guild g = discord.getGuildById(guildID);
        //get the guild object from it's ID
        MyThread mt = new MyThread(g, r, n);
        //create a new Naming thread object
        mt.start();
        //start the thread
        threads.add(mt);
        //add it to the heap of threads
    }
    
}
