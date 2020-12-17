package com.syd;

import java.io.PrintStream;

public class Looper implements Runnable {

    public static void main(String[] args) {
        PrintStream out = System.out;

        new Looper(out).run();
        out.println("Terminated.");
    }

    /////////

    public static final int[] CARD_VALUE = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};
    public static final int[] VALUE_COUNT = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 0, 0};
    public static final long CHECKSUM = factorial(13);

    private PrintStream out;

    public Looper(PrintStream out) {
        this.out = out;
    }

    @Override
    public void run() {

        for (int k = 1; k < 7; k++) {
            int target = 13 * k;
            for (int len = 2; len < 13; len++) {
                int[] loop = new int[len];//holds card values.
                loop[0] = 10;
                //TODO
            }
        }
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
        public long count;

        public Element(int[] loop) {
            this.loop = loop;
        }

    }
}
