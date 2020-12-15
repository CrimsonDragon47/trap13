//package com.syd;


import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.HashMap;

/** 
* e1: invalid value reference; check combination assignment.
* e2: as above for the stack in getSequence.
*/
public class Sequencer{

  private static final int[] value= {1,2,3,4,5,6,7,8,9,10,10,10,10};
  private static final long max = 6227020800l;
  private static final long incr = 62270208l;

  public static void main(String[] args) {
    new Sequencer().run();
    
  }
  
  public void run(){
  
   long prog=0;
  
   int[] indexKey = {0,0,0,0,0,0,0,0,0,0,0,0,0};
   
   HashMap<Element,Integer> sequences = new HashMap();
   
   
   do{
     
     if(prog++%incr==0) System.out.printf("=%.2f%% of sequences%n",100*prog/(float)max);
     
     
     //parse combination 
     int[] combination = {13,13,13,13,13,13,13,13,13,13,13,13,13};
     for(int i = 0;i<13;i++){
       int index = -1;//f(indexKey[i])//abs index of reffed indexKey 
        for(int y=0;y<=indexKey[i];y++){//check
          if(combination[++index]<13) y--;
          
        }
        
    
       combination[index]=i;
     } 
     
     
     //check combination 
     for(int i=0;i<13;i++){
       if(combination[i]==13){
         throw new RuntimeException("e1");
       }
     }
     
     
     
     
     Element e = new Element(getSequence(combination));
     
     if(sequences.containsKey(e)){
       sequences.put(e,sequences.get(e)+1);
     }else{
       sequences.put(e,0);
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
  
  private static byte[] getSequence(int[] stack){
    int[] count = {0,0,0,0,0,0,0,0,0,0,0,0,0};
    List<Integer> sequence = new LinkedList();
    int maxCount = 0;
    
    do{ 
      int card = stack[0];
      sequence.add(card); 
      //System.out.println(card);
      if(++count[card]>maxCount) maxCount=count[card];
      
      
      int[] nextStack = {13,13,13,13,13,13,13,13,13,13,13,13,13}; 
      
      int index = value[card];
      
      for(int y=0;y<13;y++){
        nextStack[y]=stack[index++]; 
        if(index==13) index=0; 
        
      }
      
      stack = nextStack;
      
      for(int i=0;i<13;i++){
       if(stack[i]==13){
         throw new RuntimeException("e2");
       }
     }
      
    }while(maxCount<2);
     
     //throw new RuntimeException("success");
     int size = sequence.size();
     int len = (int)Math.floor(0.5+size/2.0);
     
     byte[] b = new byte[len];
     for(int i=0;i<len;i++){
       b[i] = (byte)(sequence.get(i*2) + 16 * (i*2+1>size ? i*2+1 : 0));
     }
     
    return b;
    
  }
  
  private static boolean isFirstIndex(int[] indexKey){
    
      for(int i=0;i<13;i++){
        if(indexKey[i]!=0) return false;
      }
    
    return true;
  }
  
  private class Element{
    byte[] sequence;
    //int count=0;
    
    Element(byte[] sequence){
      this.sequence=sequence;
      //count=0;
    }
    
    /*
    Element setCount(int x){
      this.count=x;
      return this;
    }
    */ 
    
    @Override 
    public int hashCode(){
      int hash=0;
      int i=0;
      for(byte b:sequence){
        hash*=64;
        hash+=((int)b/4);
        if(++i>4) break;
      }
      return hash;
    }
    
    @Override 
    public boolean equals(Object o){
      if(Arrays.equals(((Element)o).sequence,this.sequence)) return true;
      return false;
    }
  }
}
