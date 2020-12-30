//package com.syd;

import java.io.PrintStream;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.math.*;


public class PermCounter{

    private static final int nThreads = 8;
    private static final boolean upper = false;//true;//

    public static void main(String[] args) {
        int nThreads = 8;
        new PermCounter(System.out).run();
        System.out.println("Terminated.");

    }

    
    private PrintStream out;
    private Counter<Loop> loopCounter;
    private Counter<Element> reachCounter;
    

    /*
     * move this to class Stacker and put static main method here
     */
    private PermCounter(PrintStream out){
        this.out = out;
    }
    
    

    public void run(){
        if(nThreads<1) throw new IllegalArgumentException("nThreads must be a natural number.");
        Stacker[] st = new Stacker[nThreads];
        for(int i=0;i<nThreads;i++) st[i]= new Stacker(i);
        boolean complete=false;
        do{
            try{
                for(int i=0;i<nThreads;i++) st[i].getThread().join();
                complete=true;
            }catch(InterruptedException e){
                out.print('I');
            }
        }while(!complete);

        out.println("Combining.....");

        for(int i=1;i<nThreads;i++) st[0].combine(st[i].getLoopList());
       

        long checksum = factorial(13)/24;

        long totalCount = 0l;

        List<Loop> perms = st[0].getLoopList();

        for(Loop l: perms) totalCount += l.count;

        out.printf("CHECKSUM=%.3f%n>>>",totalCount/(float)checksum);

        if(totalCount==checksum){
            out.println("PASS");

            
            loopCounter = new Counter(perms, "LC");
            
            loopCounter.waitFor();

            BigInteger numL = loopCounter.num;
            BigInteger numR = BigInteger.ZERO;
            BigInteger den = loopCounter.den;


            if(upper){

                //st[0].pre.run();//blocking
                //TODO make preList
                reachCounter = new Counter(perms, "RC");

                numR=reachCounter.num;
            }

            out.printf("%n---LOWER---%nNum: %s%nDen: %s%n=%.9f%n",numL,den,numL.doubleValue()/den.doubleValue());
            out.printf("%n%nPreDen:");
            if(upper&&reachCounter.den.equals(den)){
                out.println("PASS");
                out.printf("---UPPER---%nNum: %d%nDen: %d%n=%.9f%n",numR,den,numR.doubleValue()/den.doubleValue());
            }else if(!upper){
                out.println("N/A");
            }else{
                out.print("FAIL:");
                out.println(reachCounter.den);
            }



            //checksum2
            BigInteger a = new BigInteger("6227020800");
            for(int i=0;i<3;i++)den=den.divide(a);
            //den=den.subtract(new BigInteger(Long.toString(checksum)));
            double x = den.doubleValue()/6227020800.0;
            //checksum*=24*24*24*24;
            out.printf("------->%.9f<-------%n",x);
            out.print("Checksum:");
            if(x==1.0){
                out.println("PASS.");
            }else{
                out.println("FAIL.");
            }



        }else{
            out.println("FAIL");
        }




    }


    private class Stacker implements Runnable{
        
        private int threadId;

        private Thread t;
        private LoopList loops = new LoopList();

        

        public Stacker(int threadId){
            
            this.threadId=threadId;
            
            t = new Thread(this, String.format("[ST-%d]", threadId));
            t.start();
        }

        public Thread getThread(){
            return t;
        }

        @Override
        public void run(){
            out.printf("%s started.%n", t.getName());

            for(int first=threadId; first<13; first+=nThreads){
                out.printf("----->%s: %d%n", t.getName(), first);
                StackIterator si = new StackIterator(first);

                do{
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
            //seq holds stack indexs
            do{
                seq.add(head);
                head = (head+stack[head])%13;
            }while(++hit[head] <2);

            int loopIndex = seq.indexOf(head);

            List<Integer> preSeq = seq.subList(0,loopIndex);
            List<Integer> loopSeq = seq.subList(loopIndex,seq.size());
            int preSize = preSeq.size();
            int loopSize = loopSeq.size();
            int[] preSeqArr = new int[preSize];
            int[] loopSeqArr = new int[loopSize];
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

        public long fp=1l;

        public long count=1l;

        public Element(int[] seq){
            Arrays.sort(seq);
            this.seq=seq;
            for(int x:seq) fp=fp*10+x;
        }

        @Override
        public boolean equals(Object o){
            return ((Element) o).fp==fp;
        }

        @Override
        public int hashCode(){
            return (int)(fp%16777216);//mod2^24
        }

    }

    private class LoopList{
        private HashMap<Long,Loop> hm = new HashMap(8388608);//2^23

        public LoopList(){

        }

        public synchronized void add(Loop l){
            if(hm.containsKey(l.fp)){
                hm.get(l.fp).merge(l);
            }else{
                hm.put(l.fp, l);
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

    private class Counter<T extends Element>{
        List<T> perms;
        int size;
        String name;
        private Worker[] w;
        BigInteger num = BigInteger.ZERO;
        BigInteger den = BigInteger.ZERO;

        public Counter(List<T> perms, String name){
            this.perms=perms;
            size=perms.size();
            this.name=name;
            out.printf("[%s]: %d%n", name, size);
            w = new Worker[nThreads];
            for(int i=0;i<nThreads;i++) w[i] = new Worker<T>(perms, name, i);
        }

        public void waitFor(){
            boolean complete=false;
            do{
                try{
                    for(Worker x:w) x.t.join();
                    complete=true;
                }catch(InterruptedException e){
                    out.print('I');
                }

            }while(!complete);

            for(Worker x:w){
                num.add(x.num);
                den.add(x.den);
            }

            out.printf("[%s] complete%n", name);
        }

        private class Worker<T extends Element> implements Runnable{
            Thread t;
            int threadId;
            List<T> perms;
            BigInteger num = BigInteger.ZERO;
            BigInteger den = BigInteger.ZERO;

            public Worker(List<T> perms, String name, int threadId){
                this.threadId = threadId;
                this.perms = perms;
                t = new Thread(this, String.format("[%s-%d]", name, threadId));
                t.start();
            }

            @Override
            public void run(){
                out.printf("%s started%n",t.getName());
                for(int i0=threadId;i0<size;i0+=nThreads){
                    if((i0-threadId)/nThreads%10==1) out.printf("%s: %d%%%n",t.getName(),(100*(i0-threadId))/size);
                    for(int i1=0;i1<size;i1++){
                        for(int i2=0;i2<size;i2++){
                            for(int i3=0;i3<size;i3++){
                                T t0 = perms.get(i0);
                                T t1 = perms.get(i1);
                                T t2 = perms.get(i2);
                                T t3 = perms.get(i3);
                                BigInteger permCount = new BigInteger(Long.toString(t0.count+t1.count+t2.count+t3.count));
                                den = den.add(permCount.multiply(new BigInteger("331776")));
                                long count;
                                if((count = isPossible(t0.seq,t1.seq,t2.seq,t3.seq))>0){
                                    num=num.add(permCount.multiply(new BigInteger(Long.toString(count))));

                                }

                            }
                        }
                    }
                }
                out.printf("%s complete%n");
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
    }

    private static long factorial(int x){
        long f=1l;
        for(int i=2;i<=x;i++) f*=i;
        return f;
    }

    private static int[] arrayCombine(int[] a, int[] b){
        int[] c = new int[a.length+b.length];
        int i=-1;
        for(int x:a) c[++i]=x;
        for(int x:b) c[++i]=x;
        return c;
    }



    /*
    public LoopIterator[] getLoopIterators(){
        LoopIterator[] li = new LoopIterator[nThreads];
        for(int i=0;i<nThreads;i++) li[i] = new LoopIterator(loops.getList(), i, pre);
        return li;
    }

     */

    /*
    private class LoopIterator implements Runnable{
        List<Loop> loops;
        int size;

        Thread t;
        int threadId;

        private PreList pre;


        BigInteger numL = BigInteger.ZERO;
        //BigInteger numP = BigInteger.ZERO;
        BigInteger den = BigInteger.ZERO;

        public LoopIterator(List<Loop> loops, int threadId, PreList pre){
            this.loops=loops;
            size=loops.size();
            this.pre=pre;

            this.threadId=threadId;
            t = new Thread(this, String.format("[LI-%d]",threadId));
            t.start();
        }

        @Override
        public void run(){
            out.printf("%s started. %d%n", t.getName(),size);



            for(int i0=threadId;i0<size;i0+=nThreads){
                if((i0-threadId)/nThreads%10==1) out.printf("----->%s %d%%%n",t.getName(),(100*(i0-threadId))/size);
                pre.add(loops.get(i0));
                for(int i1=0;i1<size;i1++){
                    //out.print('.');
                    for(int i2=0;i2<size;i2++){
                        for(int i3=0;i3<size;i3++){

                            Loop[] l = new Loop[4];
                            l[0] = loops.get(i0);
                            l[1] = loops.get(i1);
                            l[2] = loops.get(i2);
                            l[3] = loops.get(i3);



                            long loostount = l[0].count;
                            for(int i=1;i<4;i++) loostount *= l[i].count;

                            den=den.add(new BigInteger(Long.toString(loostount)).multiply(new BigInteger("331776")));
                            long count=0l;


                            if((count=isPossible(l[0].seq, l[1].seq, l[2].seq, l[3].seq))>0){

                                numL=numL.add(new BigInteger(Long.toString(count)).multiply(new BigInteger(Long.toString(loostount))));


                            }


                        }
                    }
                }
            }
            out.printf("----->%s 100%%%n",t.getName());
        }



    }

     */

    /*
    private class PreList{
        private HashMap<Long,Element> hm = new HashMap();

        public BigInteger numP = BigInteger.ZERO;
        public BigInteger den = BigInteger.ZERO;

        private PreList(){

        }

        public void add(Loop l){
            for(Element e:l.pre){

                Element pre = new Element(arrayCombine(l.seq,e.seq));
                pre.count = e.count;

                synchronized(this){
                    if(hm.containsKey(pre.fp)){
                        hm.get(pre.fp).count+=pre.count;
                    }else{
                        hm.put(pre.fp,pre);
                    }
                }
            }
        }

        public void run(){
            List<Element> preList = new ArrayList(hm.values());
            Worker[] w = new Worker[nThreads];
            for(int i=0;i<nThreads;i++) w[i] = new Worker(preList, i);
            boolean complete = false;
            do{
                try{
                    for(int i=0;i<nThreads;i++) w[i].t.join();
                    complete=true;
                }catch(InterruptedException e){
                    out.print('I');
                }
            }while(!complete);

            for(Worker x:w){
                numP=numP.add(x.numP);
                den=den.add(x.den);
            }


        }

        private class Worker implements Runnable{
            private List<Element> preList;
            private int size;
            public Thread t;
            private int threadId;

            public BigInteger numP = BigInteger.ZERO;
            public BigInteger den = BigInteger.ZERO;


            public Worker(List<Element> preList, int threadId){
                this.preList=preList;
                this.size=preList.size();
                this.threadId=threadId;
                t = new Thread(this, String.format("[Pre-%d]",threadId));
                t.start();
            }

            @Override
            public void run(){
                out.printf("%s started. %d%n", t.getName(), size);

                for(int i0=threadId;i0<size;i0+=nThreads){
                    if((i0-threadId)/nThreads%20==1) out.printf("-----> %s: %d%%%n",t.getName(),(100*(i0-threadId))/size);
                    for(int i1=0;i1<size;i1++){
                        for(int i2=0;i2<size;i2++){
                            for(int i3=0;i3<size;i3++){
                                Element[] reach = new Element[4];
                                reach[0] = preList.get(i0);
                                reach[1] = preList.get(i1);
                                reach[2] = preList.get(i2);
                                reach[3] = preList.get(i3);
                                long reachCount = 1l;
                                for(Element e:reach) reachCount*=e.count;
                                //reachCount*=24*24*24*24;
                                den=den.add(new BigInteger(Long.toString(reachCount)).multiply(new BigInteger("331776")));
                                long count;
                                if((count=isPossible(reach[0].seq,reach[1].seq,reach[2].seq,reach[3].seq))>0){
                                    numP=numP.add(new BigInteger(Long.toString(count)).multiply(new BigInteger(Long.toString(reachCount))));

                                }
                            }
                        }
                    }
                }
                out.printf("-----> %s: 100%%%n", t.getName());
            }
        }
    }

     */



}
