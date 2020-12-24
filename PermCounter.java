package com.syd;

import java.io.PrintStream;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;


public class PermCounter implements Runnable{

  public static void main(String[] args) {
     int nThreads = 4;
     PermCounter.start(System.out, nThreads);
     System.out.println("Terminated");
     
  }
  
  public static void start(PrintStream out, int nThreads){
    if(nThreads<1) throw new IllegalArgumentException("nThreads must be a natural number.");
    PermCounter[] pc = new PermCounter[nThreads];
    for(int i=0;i<nThreads;i++) pc[i]= new PermCounter(out,nThreads,i);
    boolean complete=false;
    do{
      try{
        for(int i=0;i<nThreads;i++) pc[i].getThread().join();
        complete=true;
      }catch(InterruptedException e){
        out.print('I');
      }
    }while(!complete);
    
    out.println("Combining.....");
    
    for(int i=1;i<nThreads;i++) pc[0].combine(pc[i].getLoopList());
    
    long checksum = factorial(13)/24;
    
    long totalCount = 0l;
    
    List<Loop> loops = pc[0].getLoopList();
    
    for(Loop l: loops) totalCount += l.count;
    
    out.printf("CHECKSUM=%.3f%n>>>",totalCount/(float)checksum);
    
    if(totalCount==checksum){
      out.println("PASS");
      
      //iterate over all combs of 4*[pre+loop]
      
      LoopIterator[] li = pc[0].getLoopIterators();
      
      complete = false;
      do{
        try{
          for(int i=0;i<nThreads;i++) li[i].t.join();
          complete = true;
        } catch (InterruptedException e){
          out.print("I");
        }
      }while(!complete);
      
      long numL = 0l;
      long numP = 0l;
      long den = 0l;
      for(int i=0;i<nThreads;i++){
        numL+=li[i].numL;
        numP+=li[i].numP;
        den+=li[i].den;
      }
      
      out.printf("---LOWER---%nNum: %d%nDen: %d%n=%.9f%n",numL,den,numL/(double)den);
      out.printf("---UPPER---%nNum: %d%nDen: %d%n=%.9f%n",numP,den,numP/(double)den);
      
      //TODO checksum2
      checksum*=24*24*24*24;
      out.printf("------->%.3f<-------%n",den/(double)checksum);
      
      
      
    }else{
      out.println("FAIL");
    }
    
    
    
    
  }
  
  
  
  
  //////// 
  
  
  
  private PrintStream out;
  private int nThreads;
  private int threadId;
  
  private Thread t;
  private LoopList loops = new LoopList();
  
  private PermCounter(PrintStream out, int nThreads, int threadId){
    this.out=out;
    this.nThreads=nThreads;
    this.threadId=threadId;
    t = new Thread(this, String.format("[PC-%d]", threadId));
    t.start();
  }
  
  public Thread getThread(){
    return t;
  }
  
  public void run(){
    out.printf("%s started.%n", t.getName());
    
    for(int first=threadId; first<13; first+=nThreads){
      out.printf("----->%s: %d%n", t.getName(), first);
      StackIterator si = new StackIterator(first);
      do{
        //int[] stack = si.next();
        
        //addLoop(si.next);
        loops.add(getLoop(si.next()));
        
       
        
      }while(si.hasNext());
      
      
    }
    
  }
  
  public List<Loop> getLoopList(){
    return loops.getList();
  }
  
  public void combine(List<Loop> x){
    for(Loop l:x) loops.add(l);
  }
  
  
  private Loop getLoop(int[] stack){
    int head =0;
    int[] hit = {0,0,0,0,0,0,0,0,0,0,0,0,0};
    List<Integer> seq = new ArrayList();
    //seq holds indexs
    do{
      seq.add(head);
      head = (head+stack[head])%13;
    }while(++hit[head] <2);
    
    int loopIndex = seq.indexOf(head);

    List<Integer> preSeq = seq.subList(0,loopIndex);
    List<Integer> loopSeq = seq.subList(loopIndex,seq.size());
    int preSize = preSeq.size();
    int loopSize = loopSeq.size();
    int[] preSeqArr = new int[preSeq.size()];
    int[] loopSeqArr = new int[loopSeq.size()];
    // the seqArr vars will contain card values.
    for(int i=0;i<preSize;i++){
      preSeqArr[i]=stack[preSeq.get(i)];
    }
    for(int i=0;i<loopSize;i++){
      loopSeqArr[i]=stack[loopSeq.get(i)];
    }
    
   
    
    Loop loop = new Loop(loopSeqArr);
    loop.pre.add(new Element(preSeqArr));
    
    return loop;
  }
  
  public LoopIterator[] getLoopIterators(){
    LoopIterator[] li = new LoopIterator[nThreads];
    for(int i=0;i<nThreads;i++) li[i] = new LoopIterator(loops.getList(), i);
    return li;
  }
  
  private class LoopIterator implements Runnable{
    List<Loop> loops;
    int size;
    int step;
    
    Thread t;
    int threadId;
    
    long numL= 0l;
    long numP=0l;
    long den = 0l;
    
    public LoopIterator(List<Loop> loops, int threadId){
      this.loops=loops;
      size=loops.size();
      step=(size/50/nThreads)*nThreads;
      this.threadId=threadId;
      t = new Thread(this, String.format("[LI-%d]",threadId));
      t.start();
    }
    
    @Override 
    public void run(){
      out.printf("%s started.%n", t.getName());
      
      for(int i0=threadId;i0<size;i0+=nThreads){
        if((i0-threadId)%step==0) out.printf("----->%s %d%%%n",t.getName(),(100*(i0-threadId))/size);
        
        for(int i1=0;i1<size;i1++){
          for(int i2=0;i2<size;i2++){
            for(int i3=0;i3<size;i3++){
              Loop[] l = new Loop[4];
              l[0] = loops.get(i0);
              l[1] = loops.get(i1);
              l[2] = loops.get(i2);
              l[3] = loops.get(i3);
              
              //den++;
              long loopCount =l[0].count*l[1].count*l[2].count*l[3].count;
              den+=loopCount*24*24*24*24;
              long count=0l;
              
              
              if((count=isPossible(l[0].seq, l[1].seq, l[2].seq, l[3].seq))>0){
                //numL++;
                //numP++;
                numL+=count;
                //numP+=count;
                
              }
              int[] pmax = new int[4];
              for(int i=0;i<4;i++) pmax[i] = l[i].pre.size();
                
              int[] p = {0,0,0,0};
              for(p[0]=0;p[0]<pmax[0];p[0]++){
                for(p[1]=0;p[1]<pmax[1];p[1]++){
                  for(p[2]=0;p[2]<pmax[2];p[2]++){
                    for(p[3]=0;p[3]<pmax[3];p[3]++){
                        //for(int i=0;i<4;i++) hand[i] = add(l[i].seq,l[i].pre.get(p[i]).seq);
                      if((count = isPossible(add(l[0].seq, l[0].pre.get(p[0]).seq), add(l[1].seq, l[1].pre.get(p[1]).seq), add(l[2].seq, l[2].pre.get(p[2]).seq), add(l[3].seq, l[3].pre.get(p[3]).seq)))>0l){
                      
                        int x=1;
                        for(int i=0;i<4;i++) x*=l[i].pre.get(p[i]).count;
                        numP+=x*count;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
      
          
      
      private long isPossible(int[] seq0, int[] seq1, int[] seq2, int[] seq3){
        int[] hit = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        for(int value:seq0) hit[value<10?value:10]++;
        for(int value:seq1) hit[value<10?value:11]++;
        for(int value:seq2) hit[value<10?value:12]++;
        for(int value:seq3) hit[value<10?value:13]++;
        for(int i=1;i<10;i++) if(hit[i]==4) return 24*24*24*24;
        long x = 1l;
        for(int i=10;i<14;i++) x*=hit[i]>0?24/factorial(4-hit[i]):0;
        return x;
      }
      
    
     
        
    private int[] add(int[] a, int[] b){
      int[] c = new int[a.length+b.length];
      int i=-1;
      for(int x:a) c[++i]=x;
      for(int x:b) c[++i]=x;
      return c;
    }
    
    
    
  }
  
  
  private class Loop extends Element{
    public List<Element> pre = new ArrayList();
    
    public Loop(int[] seq){
      super(seq);
    }
    
    public void merge(Loop l){
      count += l.count;
      for(Element e:l.pre){
        int i = pre.indexOf(e);
        if(i>=0){
          pre.get(i).count += e.count;
        }else{
          pre.add(e);
        }
      }
    }
    
  }
  
  private class Element{
    public int[] seq;//ascending order 
    public int hc=1;
    public long fp=1l;
    
    public long count=1l;
    
    public Element(int[] seq){
      Arrays.sort(seq);
      this.seq=seq;
      for(int x:seq){
        hc=(hc*8+x)%16777216;//mod2^24
        fp=fp*10+x;
      }
    }
    
    @Override 
    public boolean equals(Object o){
      return ((Element) o).fp==fp;
    }
    
    @Override 
    public int hashCode(){
      return hc;
    }
    
  }
  
  private class LoopList{
    private HashMap<Integer,Loop> hm = new HashMap(8388608);//2^23
    
    public LoopList(){
      
    }
    
    public synchronized void add(Loop l){
      if(hm.containsKey(l.hc)){
        hm.get(l.hc).merge(l);
      }else{
        hm.put(l.hc, l);
      }
    }
    
    public List<Loop> getList(){
      return new ArrayList<Loop>(hm.values());
    }
  }
  
  private class StackIterator{
    private int[] index = {0,0,0,0,0,0,0,0,0,0,0,0,0};//last 4 must always be 0
    
    public StackIterator(int x){
      index[0]=x;
    }
    
    /** 
    *this will return false if called between init and first call of next, but will not be false again until it loops around.
    */
    public boolean hasNext(){
      for(int i=1;i<13;i++) if(index[i]>0) return true;
      return false;
    }
    
    public int[] next(){
      int[] stack = {0,0,0,0,0,0,0,0,0,0,0,0,0};
      for(int i=0;i<13;i++){
        int a=-1;
        for(int k=0;k<=index[i];k++){
          if(stack[++a]>0) k--;
        }
        stack[a]=(i>9?9:i)+1;
      }
      increment();
      return stack;
    }
    
    private void increment(){
      for(int i=1;i<9;i++){//first defined by threadno, last four always zero since same value (multiply later to correct count to 13!)
        if(++index[i]<(13-i)){
          break;
        }else{
          index[i]=0;
        }
      }
      
    }
    
  }
  
  private static long factorial(int x){
    long f=1l;
    for(int i=1;i<=x;i++) f*=i;
    return f;
  }
}
