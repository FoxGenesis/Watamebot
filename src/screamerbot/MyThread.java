/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;

//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

/**
 *
 * @author SkibbleBip
 * This class is the implementation of the daemon thread that procedurally goes through the entire guild member
 * list and checks if they have a '!' in the front of their name. If they do, it changes their name to the specified 
 * setting changed by a moderator.
 */
public class MyThread extends Thread{
    private Guild guild;
    private boolean isRunning = true;
    private List<Role> roles = null;
    @Nullable private String dunceName = null;
    private final int LENGTH = 32;
    
    public Guild getGuild(){return guild;}
    public void setRoles(List<Role> r){roles = r;}
    public void setDunceName(String in){dunceName = in;}
    //getters and setters
    
    public void stopRunning(){
        //function that sets the running flag to false, which ends the infinate loop
        //that the renaming feature loops in
        isRunning = false;
    }
    
    public MyThread(Guild in, List<Role> iroles, String name){
        //constructor
        guild = in;
        if(iroles != null)
            roles = iroles;
        else
            roles = new ArrayList();
        dunceName = name;
    }
    public MyThread(){
        //default constructor
        guild = null;
    }
    public void setGuild(Guild in){
        //set the guild of the renaming thread
        guild = in;
    }
    
    public void run(){
        
        String lastUser = null;
        
        System.out.println("Started thread "+this.getId());

            
            List<Member> members;
            ArrayList<Member> usable;
            
            do{
                members = guild.retrieveMembersByPrefix("!", 100).get();
                usable = new ArrayList();
                
                //System.out.println(members.size());
                
                for(int q = 0; q < members.size(); q++){
                    
                    
                    String n = members.get(q).getEffectiveName();
                    boolean inter = guild.getSelfMember().canInteract(members.get(q));
                    
                    if(inter && n.startsWith("!")){
                        //remove members who either already have their name changed, or members we dont have permission to change
                        usable.add(members.get(q));
                        
                    }
                    
                    
                    
                }
                
                for(int i = 0; i<usable.size(); i++){
                    //get the member list of the guild, and cycle through each memeber.

                   

                    if(usable.get(i).getEffectiveName().startsWith("!")){
                        //if the member has a name with '!' at the beginning
                        //try{

                            if(dunceName == null){
                                //if the dunce name is null, then append only a z
                                String name = usable.get(i).getEffectiveName();

                                name = name.substring(1, name.length());
                                name = "z"+name;
                                guild.modifyNickname(usable.get(i), name).queue();
                                System.out.println("Set the nickname of "+ usable.get(i).toString()+" to "+name);
                                //change the nickname to replace '!' to 'z'
                            }
                            else {   //otherwise set the name to admin-specified
                                guild.modifyNickname(usable.get(i), dunceName).queue();
                                System.out.println("Set the nickname of "+ usable.get(i).toString()+" to "+dunceName);
                            }

                            try {Thread.sleep(1000);} catch (InterruptedException ex) {Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);}
                            //sleep so I dont accedently trigger discord's anti-spam

                            /*****************DEBUGGING********************/
                            if(lastUser != null){
                                if(lastUser.equals(usable.get(i).getId())){
                                    System.out.println("DEBUGGING INFINATE LOOP RENAMING\n" +
                                            "Memberlist size: " + guild.getMembers().size() + "\n"
                                            + "Total member size: " + guild.getMemberCount() + "\n"
                                            + "Guild:" + guild + "\n"
                                            + "Member: " + usable.get(i)
                                            );
                                }
                                else
                                    lastUser = usable.get(i).getId();
                            }

                            /*********************************************/


                        for(int j = 0; j<roles.size(); j++)
                            //add each role to the dunce user
                            guild.addRoleToMember(usable.get(i), roles.get(j)).queue();


                    }//if(members.get(i).getEffectiveName().startsWith("!"))
                }//for
                
            }while(!usable.isEmpty());
            
        System.out.println("Stopping thread "+this.getId());
        //notify the terminal that the thread has been terminated
    
    }
    
}
