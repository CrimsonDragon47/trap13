package com.syd;


import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.HashMap;

/** 
* e1: invalid value reference; check combination assignment.
* e2: as above for the stack in getSequence.
*/
public class Main {

  private static final int[] value= {1,2,3,4,5,6,7,8,9,10,10,10,10};
  private static final long max = 6227020800l;

  public static void main(String[] args) {
  
   long prog=0;
  
   int[] indexKey = {0,0,0,0,0,0,0,0,0,0,0,0,0};
   
   HashMap<List<Integer>, Integer> sequences = new HashMap();
   
   do{
     
     if(prog++%1000000==0) System.out.printf("=%.2f%% of sequences%n",100*prog/(float)max);
     
     
     //parse combination 
     int[] combination = {13,13,13,13,13,13,13,13,13,13,13,13,13};
     for(int i = 0;i<13;i++){
       int index = -1;//f(indexKey[i])//abs index of reffed indexKey 
        for(int y=0;y<=indexKey[i];y++){//check
          if(combination[++index]<13) y--;
          
        }
        
    
       combination[index]=i;
     }
     
     //System.out.println("=");
     //for(int c:combination) System.out.printf("-%d",c);
     
     
     //check combination 
     for(int i=0;i<13;i++){
       if(combination[i]==13){
         throw new RuntimeException("e1");
       }
     }
     
     List<Integer> sequence = getSequence(combination);
     
     if(sequences.containsKey(sequence)){
       sequences.put(sequence, sequences.get(sequence)+1);
     }else{
       sequences.put(sequence,0);
       
     }
     
     
     //next combination 
     for(int i=0;i<13;i++){
      if(++indexKey[i] > 12-i){
       indexKey[i] = 0; 
      }else{
        break;
      }
      
     }
     
     
     
   }while(!isFirstIndex(indexKey));
   
   System.out.println("got all sequences.");
   try{
   ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("sequences.data"));


   output.writeObject(sequences);

   output.close();
   
   System.out.println("sequences.data saved successfully."); 
   
   }catch(Exception e){
     System.out.printf("save failed. %s: %s", e.toString(), e.getMessage());
   }
   
  }
  
  private static List<Integer> getSequence(int[] stack){
    int[] count = {0,0,0,0,0,0,0,0,0,0,0,0,0};
    List<Integer> sequence = new LinkedList();
    int maxCount = 0;
    
    do{ 
      int card = value[stack[0]];
      sequence.add(card); 
      if(++count[card]>maxCount) maxCount=count[card];
      
      
      int[] nextStack = {13,13,13,13,13,13,13,13,13,13,13,13,13}; 
      
      for(int y=0;y<13;y++){
        nextStack[y]=stack[card];
        card++;
        if(card==13) card=0; 
        
      }
      
      stack = nextStack;
      
      for(int i=0;i<13;i++){
       if(stack[i]==13){
         throw new RuntimeException("e2");
       }
     }
      
    }while(maxCount<3);
    
    return sequence;
    
  }
  
  private static boolean isFirstIndex(int[] indexKey){
    
      for(int i=0;i<13;i++){
        if(indexKey[i]!=0) return false;
      }
    
    return true;
  }
  
  
}
