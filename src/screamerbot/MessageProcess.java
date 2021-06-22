/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.requests.Requester;

///IMPORTANT: I should specify, a "Guild" is a discord server. They are actually called guilds because a server by definition is an application designed to
///host a function and connect throught he internet. This bot is technically a server. Discord "servers" are not servers, they are guilds.
///from this point on, I will be referencing discord "servers" as guilds, to avoid confusion.


/**
 *
 * @author SkibbleBip
 * the default way to process messages in the JDA REST API. Simply put, it extends the REST Listener and expands to 
 *  processing inputted events. An event in the API is an object that contains the message activity. an event can be a text message,
 *  an uploaded image or file, or basically any change that happens on discord. Specific events such as message posts from users can be filtered
 *  for processing. to save the information whether or not a server is blocking or allowing loud videos, the bot uses a FileWriter object to open 
 * and write to the guild configuration file. the guild configuration file is located in the guild folder(which is named after the ID number of 
 * the guild ex: 123456789012345678), which is located in the "repo" folder. an example of a config file: "repo/123456789012345678/status". if the file contains the text "on", then it considers it blocking
 * videos, if it contains the text "off", the it considers the video blocking disabled.
 */  
public class MessageProcess extends ListenerAdapter{
    
    final private char fs = File.separatorChar;///windows uses '\', Unix uses '/'
    final private JDA discord;
    final private Consts consts;
    //private FileWriter fw = null;///the file writer object, this is used for writing the server files
    final static private MediaConverter converter = new MediaConverter();
    //initialize the MediaConverter Object. this is the class that handles all media file conversion for checking the volume of the downloaded media file

    
    private final PrintWriter errorLogger; 
    

    public MessageProcess(Consts c) throws IOException{
        consts = c;
        discord = c.getJDA();
        //threads = new ArrayList();
        errorLogger = new PrintWriter(new FileWriter(Consts.getLogger(), true));
    }
    
    
    
    @Override
     public void onMessageReceived(MessageReceivedEvent event){
         //this is the method that processes the event object. all front-end is done here
        boolean botFlag = event.getAuthor().isBot();
        //simple flag to check if the event is written by another bot. we do not want to react to bots.
        
        
        
        if(event.getChannelType() == event.getChannelType().PRIVATE)
        {
            System.out.println("Just finished a DM with user: "+event.getAuthor());
            
            return;
            //dont need to do anything if it's a private message
        }
        
         
         if(starts(event, "<help>") && !botFlag){
             //if the message contains the help command, simply send a message containing the infomration on how the bot works
             event.getChannel().sendMessage("How to use: \n\n\t<help>: Prints this message.\n\t<enable>: enable loud/screamer videos detection."
                     + "\n\t<disable>: disable loud/screamer videos detection\n\t<invite>: Direct Messages user the invite for the bot."
                     + "\n\t<bang> \"name\": renames users with '!' in their name to \"name\"."
                     + "\n\t<bang> \"name\" [role1, role2, role3...]: renames users with '!' in their name to \"name\", gives them the pinged roles."
                     + "\n\t<bang> [role1, role2, role3...]: changes the ! to a z, gives them the pinged roles."
                     + "\n\t<bang>: changes the ! to a z."
                     + "\n\t<bangtoggle>: toggles this function on or off."
                     + "\n\t<malware> [role] [ChannelID] Removes all roles from a user and gives them the timeout role, pings them in the timeout chat."
                     + "\n\t<malwaretoggle> Enables or disables the act of automatically jailing users who upload malware.").queue();
         }
         //end help
         else if(starts(event, "<invite>") && !botFlag)
             //if the message contains the invite command, send a private message to the user that contains the bot invite url
         {
             PrivateChannel channel = event.getAuthor().openPrivateChannel().complete();
             //get the private message channel of the user who asked for the invite
             
            channel.sendMessage(/*"You've requested to add this bot to your server!\n\nhttps://discordapp.com/oauth2/authorize?client_id=735981272224235661&scope=bot&permissions=76864"
                    */
            "This feature is currently disabled. Check back later.").submit().whenComplete((message, error) -> {
                //send the actual invite into DMs
                if(error!=null)
                    event.getChannel().sendMessage("Error, failed to send DM. Check if your DMs are open. ").queue();
                //if for whatever reason the bot could not send the invite(closed DMs, etc) inform the user
                else
                    event.getChannel().sendMessage("Message sent. Check your DMs.").queue();
                //otherwise let the user know the invite was sent
            });
            channel.close();
            ///close the private channel
            System.out.println("User "+event.getAuthor()+" has requested a bot invite in "+event.getGuild());
            //inform the log terminal that a user requested an invite for the bot
             
         }//end invite
         else if(starts(event, "<enable>") && !botFlag && userAllowed(event))
             //if the message contains the enable command, the user is not a bot, and the user is an admin
         {
             if(isBlocking(event))
                 //if the guild is already blocking loud videos, display an error
                 event.getChannel().sendMessage("Error, Server already enabled").queue();
             
             else{
                 
                 consts.toggleScreamerStatus(event.getGuild().getId());
                 
                 event.getChannel().sendMessage("Server now blocking loud/screamer videos.").queue();
                 //post to the chat that blocking loud videos in now enabled
                 System.out.println("User " + event.getAuthor()+ " has enabled video checking in "+event.getGuild() );
             //inform the logging terminal that a user enabled blocking
                 
             }
             
                         
         }//end enable
         else if(starts(event, "<disable>") && !botFlag && userAllowed(event))
             //if the message contains the disable command, the user is not a bot, and the user is an admin
         {
             if(!isBlocking(event))
                 //if bot was already disabled and not blocking loud videos, send a message that its already blocking
                 event.getChannel().sendMessage("Error, Server already disabled").queue();
             else{
                
                 consts.toggleScreamerStatus(event.getGuild().getId());
                 
                 event.getChannel().sendMessage("Server no longer blocking loud/screamer videos.").queue();
                 //inform the user the bot is now blocking loud videos
                 System.out.println("User " + event.getAuthor()+ " has disabled video checking in "+event.getGuild() );
                 //print to logging terminal that the guild is now blocking loud videos
                 
             }
               
         }
         else if(starts(event,"<bang>") && !botFlag && userAllowed(event)){
             
             
             //if(!event.getMessage().getMentionedRoles().isEmpty()){
                 List<Role> dunceTemp = event.getMessage().getMentionedRoles();
                 //try {
                     
                     String msg = event.getMessage().getContentRaw();
                     
                     for(int i = 0; i< dunceTemp.size(); i++)
                         msg = msg.replace(dunceTemp.get(i).getAsMention(), "");
                     msg = msg.substring("<bang>".length());
                     msg = msg.trim();
                     //System.out.println(msg);
                     //remove = null;
                     
                     if(msg.length()>32 || msg.equals("")){
                         
                         if(msg.length()>32)
                            event.getChannel().sendMessage("Name provided is too long, defaulting to add a z to the front of the name").queue();
                         else
                            event.getChannel().sendMessage("Defaulting to add a z to the front of the name").queue();
                         
                         consts.setDunceName(event.getGuild().getId(), null);
                         consts.setDunceNameInThread(event.getGuild().getId(), null);
                         
                     }
                     else{
                         
                         event.getChannel().sendMessage("Renaming bangs with \""+msg+"\"").queue();
                         //dunceNames.add(temp);
                         consts.setDunceName(event.getGuild().getId(), msg);
                         consts.setDunceNameInThread(event.getGuild().getId(), msg);
                         
                     }
                     consts.setDunceRoles(event.getGuild().getId(), dunceTemp);
                     consts.replaceRolesInThread(event.getGuild().getId(), dunceTemp);
                     
                
         
         }//end if starts with <bang>
         else if(event.getMessage().getContentDisplay().trim().equals("<bangtoggle>") && userAllowed(event) && !botFlag){
            //toggle the bang
        
            Consts.GuildInfo gi = consts.getGuildInfo(event.getGuild().getId());

            if(gi.getDunceStatus()){
                event.getChannel().sendMessage("Switching off bang renaming").queue();
                consts.toggleDunceActive(event.getGuild().getId());
                consts.removeThread(event.getGuild().getId());

            }else{
                event.getChannel().sendMessage("Switching on bang renaming").queue();
                consts.toggleDunceActive(event.getGuild().getId());
                consts.addThread(event.getGuild().getId());
                
            }
             
         
         }
         else if(starts(event,"<malware>") && !botFlag && userAllowed(event)){
             //<malware> [role] [ChannelID] 
             String msg = event.getMessage().getContentRaw();
             try{
                msg = msg.substring("<malware>".length()).trim();
                String id = msg.substring("<@&".length(), "739587409196351578".length()+3);
                String channel = msg.substring(22).trim();
                GuildChannel channelObj = discord.getGuildChannelById(channel);
                Role role = discord.getRoleById(id);
                
                
                if(channelObj!= null && role != null){
                    
                    consts.setTimeOutRole(event.getGuild().getId(), role.getId());
                    consts.setTimeOutChannel(event.getGuild().getId(), channelObj);
                    
                    event.getChannel().sendMessage("Settings have been applied. To enable, use the command <malwaretoggle>.").queue();
                   
                    
                }
                else{
                    event.getChannel().sendMessage("Error, command format incorrect. Usage: ```<malware> [role] [ChannelID]``` ").queue();
                }
             
             }catch(Exception ex) {
                event.getChannel().sendMessage("Error, command format incorrect. Usage: ```<malware> [role] [ChannelID]``` ").queue();
             }
         
         }
         else if(starts(event,"<malwaretoggle>") && !botFlag && userAllowed(event)){
             
             Consts.GuildInfo gi = consts.getGuildInfo(event.getGuild().getId());
             if(gi.getGuildChannel() != null){
                 boolean response = gi.getMalwareStatus();
                 
                 
                 
                 if(response != true){
                     event.getChannel().sendMessage("Bot is now jailing users who post malicious files.").queue();
                 }
                 else{
                     event.getChannel().sendMessage("Bot is no longer jailing users who post malicious files.").queue();
                 }
                 
                 consts.toggleMalwareActive(event.getGuild().getId());
             
             }
             else{
                 event.getChannel().sendMessage("Error, you have not enforced jailing yet. To enable jailing of members "
                         + "who post malicious files, type `<malware> [role] [ChannelID]`").queue();
             }
         
         }
         
         /*Temporary*/
         else if(starts(event, "<nametoggle>") && !botFlag && userAllowed(event)){
             boolean current = consts.getGuildInfo(event.getGuild().getId()).getRenamingStatus();
             
             if(current){
                 System.out.println("Disabling naming based on text in "+event.getGuild());
                 event.getChannel().sendMessage("Disabling naming based on text").queue();
             }
             else{
                 System.out.println("Enabling naming based on text in "+event.getGuild());
                 event.getChannel().sendMessage("Enabling naming based on text").queue();
             }
             
             consts.toggleRenamingStatus(event.getGuild().getId());
             
             //event.getMessage().delete().queue();
         
         }
         
         if(!botFlag && isRenaming(event) && !isDunce(event)){
            String nameMsg = event.getMessage().getContentRaw();
            String msgUpper = nameMsg.toUpperCase();
            
            boolean triggered = false;
             
            String toName = "";
            int endingPoint;
            int start = 0;
            
            if(msgUpper.startsWith("IM ")){
                toName = "IM ";
                start = toName.length();
                triggered = true;
            }
            else if(msgUpper.startsWith("I'M ")){
                toName = "I'M ";
                start = toName.length();
                triggered = true;
            }
            else if(msgUpper.startsWith("I AM ")){
                toName = "I AM ";
                start = toName.length();
                triggered = true;
            }
            else if(msgUpper.contains(" IM ")){
                toName = " IM ";
                start = msgUpper.indexOf(toName)+ toName.length();
                triggered = true;
            }
            else if(msgUpper.startsWith(" I'M ")){
                toName = " I'M ";
                start = msgUpper.indexOf(toName)+ toName.length();
                triggered = true;
            }
            else if(msgUpper.startsWith(" I AM ")){
                toName = " I AM ";
                start = msgUpper.indexOf(toName)+ toName.length();
                triggered = true;
            }
            
            if(triggered == true){
                int len = nameMsg.length() - start;
                if(len > 32)
                    len = 32;
                
                endingPoint = start + len;
                
                String name = nameMsg.substring(start, endingPoint);
                name = name.trim();
                
                try{
                    event.getGuild().modifyNickname(event.getMember(), name).queue();
                    System.out.println("Renamed "+ event.getMember() + " to "+name);
                }catch(net.dv8tion.jda.api.exceptions.HierarchyException 
                               | net.dv8tion.jda.api.exceptions.InsufficientPermissionException ex)
                           //if it's unable to rename the user due to permissions or heirarchy problems,
                           //dont worry about it
                       {/*Dont do anything, this is a soft error*/}
                
            }
                
         
         }
         
         
         
         /*End temporary*/
         
         //end of commands
         String text = event.getMessage().getContentRaw().toUpperCase();
         //gets the text of the message so it can be processed, converted to uppercase so i dont have to worry about capital letters
         //System.out.print(text);
         if(!event.getMessage().getAttachments().isEmpty() && isBlocking(event)
                 && (!text.contains("LOUD") && !text.contains("EAR RAPE") || text.contains("||LOUD||"))){
             //hacky way to see if contains attachments. also checks if the bot is blocking loud videos and if the offender DID NOT
             //say the video was loud. this is doen by converting the message text to uppercase and checking if it mentions being loud 
             //or having "ear rape". I added checking for ||LOUD|| because some *BITCHES* like to hide the loud text using spoiler tags

             int status = 0;
             //status flag, defaults to 0. 0 is no problem, 1 is too loud, 2 is a crasher. 
             
             
             
             ArrayList<Attachment>list = new ArrayList<>();
             //arraylist to hold all the attachments. sometimes messages can have more than one.
             ArrayList<Attachment>allAttachments = new ArrayList<>();
             
             for(int i = 0; i< event.getMessage().getAttachments().size(); i++)
                 list.add(event.getMessage().getAttachments().get(i));
             //cycle through the event's attachments and add them to the list
             for(int i = 0; i< list.size(); i++){
                 //remove any non-videos
                 if(!list.get(i).isVideo())
                     list.remove(i);
             }
             
             for(int i = 0; i< event.getMessage().getAttachments().size(); i++)
                 allAttachments.add(event.getMessage().getAttachments().get(i));
             
             
             for(int i = 0; i< allAttachments.size(); i++){
                 //remove any non-image
                 if(!allAttachments.get(i).isImage() && !allAttachments.get(i).isVideo())
                     allAttachments.remove(i);
             }
             
             
             
             for(int i = 0; i<list.size(); i++){
                 //for each video, download it and process it. this is done by downloaing the media to a temp file,
                 //passing it to the MediaConverter object to test, and getting the boolean value of whether it 
                 //detected an offense.
                 //
                 
                 String out = "tmp"+fs+"temp."+list.get(i).getFileExtension();
                 //get the name of the temp media fie
                 //File temp = new File(out);
                 //initialize that file object
                 File temp = list.get(i).downloadToFile(out).join();
                 //download the media file to that initialized temp file
                 
                 //URL url = null;
                 try {

                      status = converter.test(temp);
 
                 }
                 catch (IOException | InterruptedException ex) {
                     Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
                 
             }
             
             for(int i=0; i<allAttachments.size(); i++){
                 URL url = null;
                 
                 
                 try {
                      url = new URL(allAttachments.get(i).getUrl());
                      URLConnection connection = url.openConnection();
                      connection.addRequestProperty("User-Agent", Requester.USER_AGENT);
                      InputStream is = connection.getInputStream();
                      //open up the websocket connection and grab the input stream
                      
                      
                      AntiMalware am = new AntiMalware(is);
                      //create an AntiMalware object from the input stream to 
                      //analyze the file the stream represents
                      
                      if(am.payloadDetected){
                          //if there was a malicious file uploaded,
                          //then delete the file and process the reason
                          //why it was removed
                          try{
                            event.getMessage().delete().queue();///delete the offending message
                            }catch(InsufficientPermissionException ex){
                                System.out.println("Error, bot attempted to delete an offending message in "
                                        +event.getGuild()+", but deleting messages is not enabled.");
                                //if the bot was unable to delete the message, simply log it and continue on
                            }
                          String reason = "Invalid Reason";
                          if(am.possibleMalwareFound == 1)
                              reason = "Non-Malicious Test File detected. No reason to be worried.";
                          if(am.possibleMalwareFound == 2)
                              reason = "Detected possble shell script embedded in file.";
                          //generate the message reason as to why the file was removed
                          
                          
                          event.getChannel().sendMessage(event.getAuthor().getAsMention()+ " POTENTIAL MALICIOUS PAYLOAD DETECTED! " + reason
                                  + " ```This feature is in beta, please notify the developer Spazmaster#8989 if a mistake was made.```").queue();
                          break;
                          //post the reason why it was deleted, then break; we dont need to process the rest of the attachments
                      
                      }
                      
                      
                 } catch (MalformedURLException ex) {Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex); }
                 catch (IOException ex) {
                     Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 //detect any malformedURL or IO exceptions and print out any errors.
             
             }
             
             
             //download all the files in the one message
             //event.getMessage().
             
             if(status != 0){
                try{
                event.getMessage().delete().queue();///delete the offending message
                }catch(InsufficientPermissionException ex){
                    System.out.println("Error, bot attempted to delete an offending message in "+event.getGuild()+", but deleting messages is not enabled.");
                }
                
                System.out.println("Video removed in "+ event.getGuild().toString());///inform the logging terminal that a essage was removed
                
                if(status == 1)
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Please do not post loud videos without first stating that the video is "
                        + "loud in the message. If you are going to post a loud video, describe in the same message that it is loud."/*\n```Note: this bot is still "
                        + "in development, mistakes can happen, please let the developer Spazmaster#8989 know of a bug or mistake```"*/).queue();
                else if(status == 2)
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + "DETECTED CRASHER FILE!!! Uploading files designed to crash discord will"
                        +" get you banned or timed out. Do not upload them.\n```Note: this feature is still in development, mistakes can happen, please let "
                            +"the developer Spazmaster#8989 know of a bug or mistake```").queue();
                //deleted the offender's message, post a warning
             }
             
         }//end get attachments
         
         String[] removeUrl = new String[1];
                 removeUrl[0] = event.getMessage().getContentRaw();
                 //hack to create a reference to pass to functions, acts like 
                 //func(&ref) passing by reference in C
                
         
        //getUrls
         try {
            int status = extractUrls(removeUrl, event);
            //remove the URLS by reference as a hack while also testing the URLS for bad files
            if(status != 0 && isBlocking(event))
                // && !text.contains("LOUD") && !text.contains("EAR RAPE"){
            {
                if(!removeUrl[0].toUpperCase().contains("LOUD") && !removeUrl[0].toUpperCase().contains("EAR RAPE")|| text.contains("||LOUD||")){
                    //if the file is loud and the message does not describe it as so, 
                try{
                event.getMessage().delete().queue();///delete the offending message
                }catch(InsufficientPermissionException ex){
                    System.out.println("Error, bot attempted to delete an offending message in "
                            +event.getGuild()+", but deleting messages is not enabled.");
                    //if it failed to delete the message, just print it out and continue on
                }
                
                System.out.println("Video removed in "+ event.getGuild().toString());///inform the logging terminal that a essage was removed
                        
                if(status == 1) //for the response returned by the url getter, respond as to what triggered.
                    ///TODO: make this optimized and cleaner
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Please do not post loud videos without first stating that the video is "
                        + "loud in the message. If you are going to post a loud video, describe in the same message that it is loud."/*\n```Note: this bot is still "
                        + "in development, mistakes can happen, please let the developer Spazmaster#8989 know of a bug or mistake```"*/).queue();
                else if(status == 2)
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + "DETECTED CRASHER FILE!!! Uploading files designed to crash discord will"
                        +" get you banned or timed out. Do not upload them.\n```Note: this feature is still in development, mistakes can happen, please let "
                            +"the developer Spazmaster#8989 know of a bug or mistake```").queue();
                //deleted the offender's message, post a warning
                System.out.println("Deleted an offending Attachment in "+event.getGuild());
                }
             }
        } catch (IOException | InterruptedException ex) {
            //process any interruptions or exceptions
            ex.printStackTrace(errorLogger);
            Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         if(checkForUrls(event.getMessage().getContentStripped()) && !event.getAuthor().isBot()){
             //anyone who posts a url or embed and doesnt have the permissions, they get bullied for it lol
             
             if(!event.getMember().getPermissions(event.getTextChannel()).contains(Permission.MESSAGE_EMBED_LINKS))
             
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + " https://tenor.com/view/bobux-roblox-embed-no-embed-perms-0bobux-gif-18287307").queue();
         
         }
         
         
     }
  
     

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){
        //detects when a new user has joined a guild
        System.out.println(event.getMember().toString() + " just joined.");
        
        String send = "SELECT Roles FROM RoleList WHERE GuildID=? AND Member=?";
        try {
            PreparedStatement ps = consts.getSQLConnection().prepareStatement(send);
            ps.setString(1, event.getGuild().getId());
            ps.setString(2, event.getMember().getUser().getId());
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                System.out.println("Member has been in the server before");
                String rolesRaw = rs.getString("Roles");
                
                if(rolesRaw == null){
                    return;
                    //if the roles string is null, then the roles do not exist
                    //(possible if user had no roles to begin with) if so, don't
                    //even bother adding roles
                }
                
                String roles[ ] = rolesRaw.split("-");
                
                for(int i = 0; i<roles.length; i++){
                    Role role = discord.getRoleById(roles[i]);
                    
                    if(role == null){
                        //if the role was invalid, just skip it
                        //System.out.println("invalid role: "+role);
                        continue;
                    }
                    try{
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                    
                    }catch(net.dv8tion.jda.api.exceptions.HierarchyException 
                            | net.dv8tion.jda.api.exceptions.InsufficientPermissionException ex)
                        //if it's unable to rename the user due to permissions or heirarchy problems,
                        //dont worry about it
                    {
                        System.out.println("Failed to give role to "+event.getMember());
                    }
                    
                }
            
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    
    }
    
    
    @Override
    public void onGuildJoin(GuildJoinEvent event){
        try {
            //detects when the bot has just joined a new guild
            
            String toSend = "INSERT OR IGNORE INTO Guild (GuildID, DunceActive, MalwareStat, ScreamerStat) VALUES (?,0,0,0)";
            PreparedStatement ps = consts.getSQLConnection().prepareStatement(toSend);
            
            ps.setString(1, event.getGuild().getId());
            ps.executeUpdate();
            //add the new Guild to the database
            
            
        } catch (SQLException ex) {
            Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
    }
     
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event){
        //when a member has roles given to it
        
        System.out.println(event.getMember() + " has just had roles added.");
        
        List<Role> totalRoles = event.getMember().getRoles();
        //obtain ALL the roles of the user
        
        String roleString = "";
        String toSend = "UPDATE RoleList SET Roles = ? WHERE GuildID = ? AND Member = ?";
        
        if(totalRoles.isEmpty())
        //if there are no new roles then set the roles list as null (unlikely but id rather have this
        //prepared than to fail)
        {
            roleString = null;
        }
        else{
            roleString += totalRoles.get(0).getId();
            
            for(int i = 1; i < totalRoles.size(); i++)
                roleString += "-" + totalRoles.get(i).getId();
        }
        
        try {
            PreparedStatement ps = consts.getSQLConnection().prepareStatement(toSend);
            ps.setString(1, roleString);
            ps.setString(2, event.getGuild().getId());
            ps.setString(3, event.getUser().getId());
            
            ps.executeUpdate();
            
            
        } catch (SQLException ex) {
            Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    
    }
    
    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event){
        //when a member has roles taken away
        
        System.out.println(event.getMember() + " has just had roles removed.");
        
        List<Role> totalRoles = event.getMember().getRoles();
        //obtain ALL the roles of the user
        
        String roleString = "";
        String toSend = "UPDATE RoleList SET Roles = ? WHERE GuildID = ? AND Member = ?";
        
        if(totalRoles.isEmpty())
        //if there are no new roles then set the roles list as null (unlikely but id rather have this
        //prepared than to fail)
        {
            roleString = null;
        }
        else{
            roleString += totalRoles.get(0).getId();
            
            for(int i = 1; i < totalRoles.size(); i++)
                roleString += "-" + totalRoles.get(i).getId();
        }
        
        try {
            PreparedStatement ps = consts.getSQLConnection().prepareStatement(toSend);
            ps.setString(1, roleString);
            ps.setString(2, event.getGuild().getId());
            ps.setString(3, event.getUser().getId());
            
            ps.executeUpdate();
            
            
        } catch (SQLException ex) {
            Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    
    } 
    
    private boolean isDunce(MessageReceivedEvent event){
        Role[] dunceRoles = consts.getGuildInfo(event.getGuild().getId()).getDunceRoles();
        List<Role> roles = event.getMember().getRoles();
        
        for(int i = 0; i < roles.size(); i++)
            for(int j = 0; j < dunceRoles.length; j++)
                if(roles.get(i).getId().equals(dunceRoles[j].getId()))
                    return true;
        return false;
    }
    
    
    private boolean starts(MessageReceivedEvent event, String in)
            //simple comparison method. this is so I dont need to copy and paste the same thing over and over.
            //this checks if the message text begins with the inputted strings. this is used for checking if the message is a command
    {
        if(event.getMessage().getContentStripped().startsWith(in))
            return true;
        
        return false;
    }
    
    private boolean userAllowed(MessageReceivedEvent event){
        //this is another comparison method. I use this to check if the message creator has permission to ban members. this is a simple way to check 
        //if the user is a moderator, since only moderators should have control over this bot
        if(event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.BAN_MEMBERS))
            return true;
        
        return false;
    }
    
    private boolean isBlocking(MessageReceivedEvent event){
        /*check if the guild is currently blocking videos or not*/
        String gid = event.getGuild().getId();
        if(consts.getGuildInfo(gid).getScreamerStatus())
            return true;

        return false;
        //otherwise return false
        
    }
    
    private boolean isRenaming(MessageReceivedEvent event){
        //check if the guild is renaming users with "i'm" in their message
        String gid = event.getGuild().getId();
        if(consts.getGuildInfo(gid).getRenamingStatus())
            return true;
        return false;
    
    }
     
    
    private  int extractUrls(String[] text, MessageReceivedEvent event) throws MalformedURLException, IOException, InterruptedException
            //function that takes in a hack reference string to remove the urls from and return it out as a reference 
            //(arrays in java are passed by what could be considered a reference like in C), and the event object that the 
            //message occured in. It then calculates whether or not a file had a problem with it, whether its a crasher file or file with embedded 
            //malicious code in it, etc
{
    int triggered = 0;
    List<String> containedUrls = new ArrayList<String>();
    String urlRegex = "\\b((https|http|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";//"((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
    Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
    Matcher urlMatcher = pattern.matcher(text[0]);
    //regex for detecting urls

    while (urlMatcher.find())
    {
        String holder = text[0].substring(urlMatcher.start(0),
                urlMatcher.end(0));
        if(holder.contains("https://cdn.discordapp.com/attachments/") || holder.contains("http://cdn.discordapp.com/attachments/"))
            containedUrls.add(holder);
        //if the url is an embedable discord link, then add it to a list
        
        if(holder.contains("://tornadus.net/")){
            //hack for detecting an embed crasher
            //System.out.println(holder);
                try{///TODO: optimize this
                            event.getMessage().delete().queue();///delete the offending message
                            }catch(InsufficientPermissionException ex){
                                System.out.println("Error, bot attempted to delete an offending message in "+event.getGuild()+", but deleting messages is not enabled.");
                            }
                          
                          
                          
                          event.getChannel().sendMessage(event.getAuthor().getAsMention()+ " POTENTIAL MALICIOUS PAYLOAD DETECTED! " + "Discord Embed link crasher."
                                  + " ```This feature is in beta, please notify the developer Spazmaster#8989 if a mistake was made.```").queue();
                          break;
            
        }
        
        
    }
    
    
    
    //remove the urls from the inputted reference string
    for(int i = 0; i<containedUrls.size(); i++)
        text[0]= text[0].replace(containedUrls.get(i), "");
    

    
    for(int i = 0; i<containedUrls.size() && triggered == 0; i++){
        //for each of the urls in the list, download the file and analyze it
            
            String ext = containedUrls.get(i).substring(containedUrls.get(i).lastIndexOf('.'));
            //get the extension of the file
            
            
            //checks if the file is a video, then download it and scan it
            if(ext.toLowerCase().contains("mp4")||ext.toLowerCase().contains("webm") ||ext.toLowerCase().contains("mov")){
                URL url = new URL(containedUrls.get(i));
                //apply the url string to the url objet
                URLConnection openConnection = url.openConnection();
                //open the url connection
                openConnection.addRequestProperty("User-Agent", Requester.USER_AGENT);//"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                //declare the http user agent as the discord protcol
                openConnection.connect();
                
                try{   
                if(openConnection.getContentLength() < 100000000){
                    //if the file download is not an abysmally large file
                    InputStream in = new BufferedInputStream(openConnection.getInputStream());
                    File temp = new File("tmp"+ fs+"temp"+ext);
                    //create a temporary file to download to
                    OutputStream os = new FileOutputStream(temp);
                    byte[] b = new byte[8192];
                    int read = 1;
                    //begin reading in the file in 8 kb chunksand storing it in a file output stream
                    while((read = in.read(b, 0, 8192)) != -1){
                        os.write(b, 0, read);
                        
                    }
                    
                    os.flush();
                    os.close();
                    //fflush and close the stream
                    
                    
                    triggered =  converter.test(temp);
                    //check whether the media converter detected a bad file
                    
                    }
                else
                    System.out.println("Attempted to download a dangerously large file");
                //let the logger be aware that the application almost downloaded a dangerously large file
                
                //triggered =  converter.test(outFile);
                }catch(IOException ex){System.out.println("Error, received an error 403 from discord while "
                        + "trying to download: \n\t"+containedUrls.get(i));}
                //let logger know that for unknown reasons discord sent a 403 error
               
              
            }//end if is a video
            if(ext.toLowerCase().contains("png")||ext.toLowerCase().contains("jpg") ||ext.toLowerCase().contains("jpeg")
                    || ext.toLowerCase().contains("mp4")){///TODO: remove the mp4 part? why am I checking twice for mp4s?
                URL url = new URL(containedUrls.get(i));
                //apply the url string to the url objet
                URLConnection openConnection = url.openConnection();
                //open the url connection
                openConnection.addRequestProperty("User-Agent", Requester.USER_AGENT);//"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                //declare the http user agent as the discord protcol
                openConnection.connect();
                
                try{   
                if(openConnection.getContentLength() < 100000000){
                    //if the file download is not an abysmally large file
                    InputStream in = new BufferedInputStream(openConnection.getInputStream());
                    //get the input stream of the url
                    
                    AntiMalware am = new AntiMalware(in);
                    //create a new malware scanner object
                    
                    if(am.payloadDetected){
                        //if it detected a bad file,
                        try{
                            event.getMessage().delete().queue();///delete the offending message
                            }catch(InsufficientPermissionException ex){
                                System.out.println("Error, bot attempted to delete an offending message in "+event.getGuild()+", but deleting messages is not enabled.");
                            }
                          String reason = "Invalid Reason";
                          if(am.possibleMalwareFound == 1)
                              reason = "Non-Malicious Test File detected. No reason to be worried.";
                          if(am.possibleMalwareFound == 2)
                              reason = "Detected possble shell script embedded in file.";
                          //calculate the reason why it it was removed
                          
                          event.getChannel().sendMessage(event.getAuthor().getAsMention()+ " POTENTIAL MALICIOUS PAYLOAD DETECTED! " + reason
                                  + " ```This feature is in beta, please notify the developer Spazmaster#8989 if a mistake was made.```").queue();
                          break;
                          //inform the reason it was removed, and break; we dont need to scan the rest of the files
                    }
                    
                    
                    }
                else
                    System.out.println("Attempted to download a dangerously large file");
                //let the logger be aware that the application almost downloaded a dangerously large file
                
                //triggered =  converter.test(outFile);
                }catch(IOException ex){System.out.println("Error, received an error 403 from discord while "
                        + "trying to download: \n\t"+containedUrls.get(i));}
                //let logger know that for unknown reasons discord sent a 403 error
                
                
            
            }
            
            
        }//for

    //return the status of whether or not a problem was calculated to be bad
    return triggered;
}
    
    
    private boolean checkForUrls(String in){
        //simple function that calculates whether or not a string contains a url in it, using regexes
        boolean triggered = false;
        String urlRegex = "\\b((https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";//"((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(in);
        //compile regex, the pattern recognizer, and the matcher
        if(urlMatcher.find()){
            triggered = true;
        }
        //if it detected it's a url, return true. if not, return false
        return triggered;
    }
    
}



