package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class PausableControl extends Thread {

    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 5000;
    private final int NDATA = MAXVALUE / NTHREADS;

    private final PrimeFinderThread[] pft = new PrimeFinderThread[NTHREADS];
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public boolean isPaused() {
        return paused;
    }

    @Override
    public void run() {
        for (int i = 0; i < NTHREADS; i++) {
            int start = i * NDATA;
            int end = (i == NTHREADS - 1) ? MAXVALUE + 1 : (i + 1) * NDATA;
            pft[i] = new PrimeFinderThread(start, end, pauseLock, this);
            pft[i].start();
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                Thread.sleep(TMILISECONDS);

                // Pausar
                paused = true;

                Thread.sleep(200); // Dar tiempo a que se detengan

                // Mostrar resultados
                int totalPrimes = 0;
                for (PrimeFinderThread t : pft) {
                    totalPrimes += t.getPrimes().size();
                }
                System.out.println("Primos encontrados hasta ahora: " + totalPrimes);
                System.out.println("Presione ENTER para continuar...");
                scanner.nextLine();

                // Reanudar
                synchronized (pauseLock) {
                    paused = false;
                    pauseLock.notifyAll();
                }

                // Verificar si todos terminaron
                boolean allFinished = true;
                for (Thread t : pft) {
                    if (t.isAlive()) {
                        allFinished = false;
                        break;
                    }
                }

                if (allFinished) {
                    System.out.println("Todos los hilos han terminado.");
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
