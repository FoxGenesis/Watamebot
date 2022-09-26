/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.foxgenesis.watame.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Channel;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;


/**
 *
 * @author Spaz-Master
 */

public class GuildProperties {

    /**
     * The JDA object to obtain information of the servers from
     */
    private final JDA jda;

    private final Connection CONNECTION_HANDLER;
    /**
     * Prepared Statement for obtaining data
     */
    private final PreparedStatement GET_STATEMENT;
    
    /**
     * Prepared Statement for setting data
     */
    private final PreparedStatement SET_STATEMENT;

    

    public GuildProperties(JDA _jda) throws SQLException {
        this.jda = _jda;
        CONNECTION_HANDLER = DriverManager.getConnection("jdbc:sqlite:repo" + File.separatorChar + "database.db");

        GET_STATEMENT = CONNECTION_HANDLER.prepareCall("SELECT json_extract(GuildProperties, '$.?') from Guild WHERE GuildID = ?");
        
        //DR_SET_STATEMENT = CONNECTION_HANDLER.prepareCall("UPDATE Guild SET GuildProperties = json_set(Guild.GuildProperties, '$.DunceRoles', ?) WHERE GuildID = ?");
        SET_STATEMENT = CONNECTION_HANDLER.prepareCall("""
                                                       INSERT OR IGNORE INTO Guild(GuildID, GuildProperties) VALUES(?, '{ "?":? }');
                                                       UPDATE Guild SET GuildProperties = json_set(Guild.GuildProperties, "$.?", ?) WHERE GuildID = ?;"""
                                                       );
        
        
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                CONNECTION_HANDLER.close();
            } catch (SQLException ex) {
                Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, "SQLite Prepared Statement thread"));

    }

    /**
     * Attempts to obtain an arraylist of guild roles for the dunce users
     * 
     * @param guildID ID of the guild in question
     * @return Nullable array of roles, returns null if either an error occurred or no roles were found
     */
    @Nullable
    Role[] getDunceRoles(long guildID) {
        
        ArrayList<Role> roles = new ArrayList<>();
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "DunceRoles");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            String raw = rs.getString(1);
            if(raw == null)
                return null;
            if(raw.isBlank())
                return null;
            String[] rawArray = raw.substring(1, raw.length() - 1).split(",");
            
            for (String rawArray1 : rawArray) {
                Role r = this.jda.getRoleById(rawArray1);
                if(r != null)
                    roles.add(r);
            }

        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        return roles.toArray(Role[]::new);
    }
    
    /**
     * returns boolean status of whether the guild has enabled giving dunce roles to users
     * 
     * @param guildID ID of the guild in question
     * @return status of dunce role assignment
     */
    boolean getDunceActive(long guildID){
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "DunceActive");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            boolean res = rs.getBoolean(1);
            return res;
            
        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return false;
    }
    
    
    /**
     * Attempts to obtain the name for the dunce users to be renamed to
     * 
     * @param guildID ID of the guild in question
     * @return Nullable String to rename, or null if error or not found
     */
    @Nullable
    String getDunceName(long guildID){
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "DunceName");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            String raw = rs.getString(1);
            return raw;

        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
            
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return null;
    }
    
    /**
     * Attempts to obtain the jail role
     * 
     * @param guildID ID of the guild in question
     * @return Nullable Role of the jail role to assign, or null if error or not found
     */
    @Nullable
    Role getJailRole(long guildID){
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "JailRole");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            long raw = rs.getLong(1);
            Role r = this.jda.getRoleById(raw);
            return r;

        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
            
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return null;
    }
    
    /**
     * Attempts to obtain jail channel
     * 
     * @param guildID ID of the guild in question
     * @return Nullable Guild Channel, or null if error or not found
     */
    @Nullable
    Channel getJailChannel(long guildID){
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "JailChannel");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            long raw = rs.getLong(1);
            Channel r = this.jda.getGuildChannelById(raw);
            return r;

        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
            
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return null;
    }
    
        
    /**
     * returns boolean status of whether the guild has enabled detecting malware
     * 
     * @param guildID ID of the guild in question
     * @return status of malware detection
     */
    boolean getMalwareActive(long guildID){
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "MalwareActive");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            boolean res = rs.getBoolean(1);
            return res;
            
        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return false;
    }
        
    /**
     * returns boolean status of whether the guild has enabled detecting loud videos
     * 
     * @param guildID ID of the guild in question
     * @return status of dunce role assignment
     */
    boolean getLoudActive(long guildID){
        ResultSet rs = null;
        try {
            GET_STATEMENT.setString(1, "LoudActive");
            GET_STATEMENT.setLong(2, guildID);
            rs = GET_STATEMENT.executeQuery();
            boolean res = rs.getBoolean(1);
            return res;
            
        } catch (SQLException ex) {
            Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GuildProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return false;
    }
    
    /**
     * sets the dunce roles for the specified guild
     * 
     * @param guildID id of guild in question
     * @param roles array of the Roles to add
     * @return boolean of whether it failed or not
     */
    boolean setDunceRoles(long guildID, Role[] roles){
        String toOut = "[";
        for(int i = 0; i < roles.length -1; i++){
            Role r = roles[i];
            toOut = toOut + r.getId() + ", ";
        }
        toOut += roles[roles.length -1].getId() + "]";
        
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "DunceRoles");
            SET_STATEMENT.setString(3, toOut);
            SET_STATEMENT.setString(4, "DunceRoles");
            SET_STATEMENT.setString(5, toOut);
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    /**
     * sets the active status of manipulating the properties of dunce users
     * 
     * @param guildID id of guild in question
     * @param active flag of enabled or disabled
     * @return boolean of whether it failed or not
     */
    boolean setDunceActive(long guildID, boolean active){
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "DunceActive");
            SET_STATEMENT.setBoolean(3, active);
            SET_STATEMENT.setString(4, "DunceActive");
            SET_STATEMENT.setBoolean(5, active);
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * sets the name the dunce users will be renamed
     * 
     * @param guildID id of guild in question
     * @param name String to set the dunce user's name to
     * @return boolean of whether it failed or not
     */
    boolean setDunceName(long guildID, String name){
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "DunceName");
            SET_STATEMENT.setString(3, name);
            SET_STATEMENT.setString(4, "DunceName");
            SET_STATEMENT.setString(5, name);
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * sets the channel for jailed userd
     * 
     * @param guildID id of guild in question
     * @param role the jail role
     * @return boolean of whether it failed or not
     */
    boolean setJailRole(long guildID, Role role){
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "JailRole");
            SET_STATEMENT.setLong(3, role.getIdLong());
            SET_STATEMENT.setString(4, "JailRole");
            SET_STATEMENT.setLong(5, role.getIdLong());
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * sets the jail channel of the guild
     * 
     * @param guildID id of guild in question
     * @param channel channel to set
     * @return boolean of whether it failed or not
     */
    boolean setJailChannel(long guildID, Channel channel){
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "JailChannel");
            SET_STATEMENT.setLong(3, channel.getIdLong());
            SET_STATEMENT.setString(4, "JailChannel");
            SET_STATEMENT.setLong(5, channel.getIdLong());
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * sets the active status of detecting malware
     * 
     * @param guildID id of guild in question
     * @param active flag of enabled or disabled
     * @return boolean of whether it failed or not
     */
    boolean setMalwareActive(long guildID, boolean active){
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "MalwareActive");
            SET_STATEMENT.setBoolean(3, active);
            SET_STATEMENT.setString(4, "MalwareActive");
            SET_STATEMENT.setBoolean(5, active);
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * sets the active status of detecting loud videos
     * 
     * @param guildID id of guild in question
     * @param active flag of enabled or disabled
     * @return boolean of whether it failed or not
     */
    boolean setLoudActive(long guildID, boolean active){
        try {
            SET_STATEMENT.setLong(1, guildID);
            SET_STATEMENT.setString(2, "LoudActive");
            SET_STATEMENT.setBoolean(3, active);
            SET_STATEMENT.setString(4, "LoudActive");
            SET_STATEMENT.setBoolean(5, active);
            SET_STATEMENT.setLong(6, guildID);
            SET_STATEMENT.executeQuery();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }
    
    
}
