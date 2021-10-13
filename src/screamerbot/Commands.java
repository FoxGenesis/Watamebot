/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screamerbot;

/**
 *
 * @author sean
 */
public class Commands {
    
    private final Node tree;
    //the mother node
    private int max_length;
    //maximum length to search a string for commands
    
    
    public Commands(String[] literals) throws RepeatedCommandException{
        
        tree = new Node(literals[0], 0);
        max_length = literals[0].length();
        
        
        for(int i = 1; i < literals.length; i++){
            tree.add(literals[i], i);
            
            if(max_length < literals[i].length())
                max_length = literals[i].length();
        }
    
    }
    
    
    private class Node{
        public Node left;
        //the left node of the tree
        public Node right;
        //the right node of the tree
        private final String command;
        //the actual command
        public int location;
        //the "ID" of the node, basically equal to it's location in the original 
        //command array
        
        
        public Node(String command, int location){
            this.command = command;
            this.location = location;
            left = null;
            right = null;
        
        }
        
        public void add(String command, int location) throws RepeatedCommandException{
            //left is lesser, right is greater
            int result = this.command.compareTo(command);
            if(result > 0){
                if(this.left == null){
                    this.left = new Node(command, location);
                }
                else{
                    this.left.add(command, location);
                }
                
            }
            else if(result < 0){
                if(this.right == null){
                    this.right = new Node(command, location);
                }
                else{
                    this.right.add(command, location);
                }
            }
            else if(result == 0){
                throw new RepeatedCommandException(command);
            }
            
        }
    
    }//class Node
    
    
    
    public int parse(String in){
            int len = in.length();
            
            if(len > max_length)
                len = max_length;
            
            //int q = in.indexOf(' ');
            int q = index(in, ' ', len);
            
            if(q < len && q != -1)
                len = q;
            
            String command = in.substring(0, len);
            
            if(command.length() > max_length)
                return -1;
            
            Node tmp = this.tree;
            
            while(tmp!=null){
            
                int result = command.compareTo(tmp.command);
                //traverse through the BST and return the location of the command
                if(result < 0)
                    tmp = tmp.left;
                else if(result > 0)
                    tmp = tmp.right;
                else
                    break;
            
            }
            
            if(tmp == null)
                //if command was not found, return negative
                return -1;
            
            return tmp.location;
        }
    
        int index(String str, char character, int limit){
            if(str.length() < limit)
                limit = str.length();
            
            for(int i = 0; i<limit; i++)
                if(str.charAt(i) == character)
                    return i;
            
            return -1;
        }
    
    
    
    
    public class RepeatedCommandException extends Exception{
        public RepeatedCommandException(String command){
            System.out.println("Found multiple copies of the same command: "
                + command
            );
        }
    
    }
    
}
