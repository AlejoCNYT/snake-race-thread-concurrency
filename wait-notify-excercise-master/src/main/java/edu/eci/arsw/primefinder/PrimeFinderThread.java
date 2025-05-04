package edu.eci.arsw.primefinder;

import java.util.*;

public class PrimeFinderThread extends Thread {

    int a, b;
    private List<Integer> primes = new ArrayList<>();
    private final Object pauseLock;
    private final PausableControl control;

    public PrimeFinderThread(int a, int b, Object pauseLock, PausableControl control) {
        this.a = a;
        this.b = b;
        this.pauseLock = pauseLock;
        this.control = control;
    }

    public List<Integer> getPrimes() {
        return primes;
    }

    public void run() {
        for (int i = a; i < b; i++) {
            synchronized (pauseLock) {
                while (control.isPaused()) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            if (isPrime(i)) {
                primes.add(i);
            }
        }
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
