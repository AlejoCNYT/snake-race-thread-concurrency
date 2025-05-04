package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Control {

    public static final int NTHREADS = 3;
    public static final int MAXVALUE = 30000000;
    public static final int TIME = 5000; // t milisegundos

    private final PrimeFinderThread[] threads = new PrimeFinderThread[NTHREADS];
    private final Object lock = new Object();
    private volatile boolean paused = false;

    public static void main(String[] args) throws InterruptedException {
        new Control().start();
    }

    public void start() throws InterruptedException {
        int range = MAXVALUE / NTHREADS;
        for (int i = 0; i < NTHREADS; i++) {
            int start = i * range;
            int end = (i == NTHREADS - 1) ? MAXVALUE : (i + 1) * range;
            threads[i] = new PrimeFinderThread(start, end, lock, this);
            threads[i].start();
        }

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            Thread.sleep(TIME);

            paused = true;

            Thread.sleep(200); // Pequeño retardo para asegurar que los threads hagan wait

            int total = 0;
            for (PrimeFinderThread t : threads) {
                total += t.getPrimes().size();
            }

            System.out.println("\n>>> Primos encontrados hasta ahora: " + total);
            System.out.println(">>> Presione ENTER para continuar...");
            scanner.nextLine();

            synchronized (lock) {
                paused = false;
                lock.notifyAll();
            }

            boolean finished = true;
            for (Thread t : threads) {
                if (t.isAlive()) {
                    finished = false;
                    break;
                }
            }

            if (finished) {
                System.out.println(">>> Cálculo terminado.");
                break;
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }
}
