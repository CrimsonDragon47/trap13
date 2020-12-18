package com.syd;

import java.io.PrintStream;
import java.util.*;

public class Looper implements Runnable {

    private static final int totalThreads = 4;

    private static final int[] CARD_VALUE = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};
    private static final int[] VALUE_COUNT = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 0, 0};
    private static final long CHECKSUM = factorial(13);

    public static void main(String[] args) {
        PrintStream out = System.out;
        Looper.start(out);
        out.println("Terminated.");
    }
    
    public static void start(PrintStream out){
      
      Looper[] looper = new Looper[totalThreads];
      for(int i=0;i<totalThreads;i++) looper[i] = new Looper(out,i);
      boolean complete = false;
      do{
        try{
          for(Looper l:looper) l.getThread().join();
          complete=true;
        }catch(InterruptedException e){
          out.print('.');
        }
      }while(!complete);
      
      List<Element> loops = new ArrayList();
      long totalCount = 0l;
      for(int i=0;i<totalThreads;i++){
        loops.addAll(looper[i].getLoops());
        totalCount += looper[i].getTotalCount();
        looper[i] = null;
      }
      out.printf("%nS:%d%nC:%d%n=%.2f%n%n", loops.size(), totalCount, totalCount/(float)CHECKSUM);
      
      
      //TODO
    }






    /////////
    
    

    

    private PrintStream out;
    private List<Element> loops = new ArrayList();
    private long totalCount = 0;
    
    private int threadId;
    private Thread t;

    private Looper(PrintStream out, int threadId) {
        this.out = out;
        this.threadId =threadId;
        this.t = new Thread(this, String.format("[Looper-%d]", threadId));
        t.start();
    }
    
    //todo LoopMaster to analyse data
    
    public Thread getThread(){
       return t;
    }
    
    public List<Element> getLoops(){
      return loops;
    }
    
    public long getTotalCount(){
      return totalCount;
    }

    @Override
    public void run() {
      out.printf("%s started.%n",t.getName());
      for(int len=2+threadId;len<13;len+=totalThreads){
        int[] loop = new int[len];
        for(int i=0;i<len;i++) loop[i]=0;
        
        boolean complete=false; 
        do{
          try{
            
            loop = nextLoop(loop,len,0);
            Element e = new Element(loop, len);
            loops.add(e);
            totalCount+=e.count;
          }catch(IllegalArgumentException e){
            complete=true;
          }
        }while(!complete);
          
        out.println(totalCount);
      }
      
      out.printf("%s done.%n", t.getName());
      
      
        
    }
    
    
    
    private int[] nextLoop(int[] loop, int len, int i){
    
      if(i==len-1){
        int[] loop2 = new int[len];
         
        if(loop[i]>0){
          loop2[i]=loop[i]-1;
        }else{
          loop2[i]=10;
        }
        
        return loop2;
        
      }else if(i>0){
        
          
          int[] loop2;
          if(loop[i]>0){//first 
            loop2 = nextLoop(loop,len,i+1);
            if(loop2[i+1]>0){
              loop2[i]=loop[i];
            }else{
              loop2 = nextLoop(loop2,len,i+1);
              loop2[i]=loop[i]-1;
            }
          }else{//second 
            loop2=loop;
            if(loop2[i+1]==0){
              loop2 = nextLoop(loop,len,i+1);
            } 
            loop2[i]=10;
          }
          return loop2;
        
      }else{//i==0
      
        //for(int value:loop) out.print(value);
        //out.println();
        
        boolean complete=false;
        int[] loop2 = loop;
        do{
          loop2 = nextLoop(loop2,len,i+1);
          loop2[0]=0;
          if(loop2[i+1]>0){
            int sum=0;
            for(int k=1;k<len;k++) sum+=loop2[k];
            loop2[0] = ((sum/13)+1)*13-sum;
            
          }else{
            throw new IllegalArgumentException("next len");
          }
          
          if(loop2[0]<11&&isPossible(loop2,len)){
            complete=true;
          }
        }while(!complete);
        return loop2;
      }
    }
    
    private boolean isPossible(int[] loop, int len){
      //out.print('.');
      boolean[] hit = new boolean[13];
      Arrays.fill(hit, false);
      int[] remaining = Arrays.copyOf(VALUE_COUNT, 13);
      int index=0;
      for(int value:loop){
        if(--remaining[value]<0) return false;
        index=(index+value)%13;
        if(hit[index]){
          return false;
        }else{
          hit[index]=true;
        }
      }
      if(index>0) return false;
      return true;
    }

    private static long factorial(int x) {
        long f = 1l;
        for (int i = 1; i <= x; i++) {
            f *= i;
        }
        return f;
    }

    private class Element {
        public int[] loop;
        public int len;
        public long count = 1l;

        public Element(int[] loop, int len) {
            this.loop = loop;
            this.len = len;
            this.count = getCount();
        }
        
        private long getCount(){
      long count = 1l;
      int[] remaining = Arrays.copyOf(VALUE_COUNT, 13);
      for(int i=0;i<len;i++) remaining[loop[i]]--;
      
      count *= factorial(4) / factorial(remaining[10]);
      
      
      int[] value = new int[13-len];
      int j=0;
      for(int k=1;k<14;k++){
        int v=k;
        if(v>10) v=10;
        if(--remaining[v]>=0){
          value[j] = v;
          j++;
        }
      }
      
      int x=1;
      
      //TODO
      
      count *= x;
      
      return count;
    }

    }
}
