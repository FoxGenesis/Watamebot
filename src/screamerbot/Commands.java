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
    
    
    public Commands(String[] literals) throws RepeatedCommandException{
        
        tree = new Node(literals[0], 0);
        
        
        for(int i = 1; i < literals.length; i++){
            tree.add(literals[i], i);
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
    
    
    
    private class RepeatedCommandException extends Exception{
        public RepeatedCommandException(String command){
            System.out.println("Found multiple copies of the same command: "
                + command
            );
        }
    
    }
    
}
