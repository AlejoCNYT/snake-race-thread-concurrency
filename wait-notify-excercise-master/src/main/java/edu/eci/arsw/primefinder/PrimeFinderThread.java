package edu.eci.arsw.primefinder;

import java.util.ArrayList;
import java.util.List;

public class PrimeFinderThread extends Thread {

    int a, b;
    List<Integer> primes = new ArrayList<>();
    private final Object lock;
    private final Control control;

    public PrimeFinderThread(int a, int b, Object lock, Control control) {
        this.a = a;
        this.b = b;
        this.lock = lock;
        this.control = control;
    }

    public List<Integer> getPrimes() {
        return primes;
    }

    @Override
    public void run() {
        for (int i = a; i < b; i++) {
            synchronized (lock) {
                while (control.isPaused()) {
                    try {
                        lock.wait();
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
