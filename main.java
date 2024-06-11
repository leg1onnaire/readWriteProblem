import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ReadWriteLock RW = new ReadWriteLock();

        for (int i = 0; i < 4; i++) {
            executorService.execute(new Reader(RW));
        }


        for (int i = 0; i < 4; i++) {
            executorService.execute(new Writer(RW));
        }


        executorService.shutdown();
    }
}

class ReadWriteLock {
    private Semaphore wrt = new Semaphore(1);
    private Semaphore mutex = new Semaphore(1);
    private Semaphore turnstile = new Semaphore(1);
    private Semaphore roomEmpty = new Semaphore(1);
    private int readers = 0;

    public void readLock() {
        try {
            turnstile.acquire();
            roomEmpty.acquire();
            mutex.acquire();
            readers++;
            if (readers == 1) {
                wrt.acquire(); // Acquire the resource if this is the first reader
            }
            mutex.release();
            roomEmpty.release();
            turnstile.release();

            // Reading section
            System.out.println("Thread " + Thread.currentThread().getName() + " is READING");
            Thread.sleep(1000);
            System.out.println("Thread " + Thread.currentThread().getName() + " has FINISHED READING");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void readUnlock() {
        try {
            mutex.acquire();
            readers--;
            if (readers == 0) {
                wrt.release(); // Release the resource if no readers are left
            }
            mutex.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void writeLock() {
        try {
            turnstile.acquire();
            wrt.acquire(); // Acquire the resource exclusively for writing
            turnstile.release();

            System.out.println("Thread " + Thread.currentThread().getName() + " is WRITING");
            Thread.sleep(1000);
            System.out.println("Thread " + Thread.currentThread().getName() + " has finished WRITING");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void writeUnlock() {
        wrt.release(); // Release the resource after writing
    }

    public void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Reader implements Runnable {
    private ReadWriteLock RW_lock;

    public Reader(ReadWriteLock rw) {
        RW_lock = rw;
    }

    public void run() {
        while (true) {
            RW_lock.readLock();
            // System.out.println("Thread " + Thread.currentThread().getName() + " is READING");
             RW_lock.sleep(100);
            // Perform reading operations here
            RW_lock.readUnlock();
        }
        
    }
}

class Writer implements Runnable {
    private ReadWriteLock RW_lock;

    public Writer(ReadWriteLock rw) {
        RW_lock = rw;
    }

    public void run() {
        while (true) {
            RW_lock.writeLock();
            // Perform writing operations here
            // System.out.println("Thread " + Thread.currentThread().getName() + " is WRITING");
             RW_lock.sleep(200);
            RW_lock.writeUnlock();
        }
    }
}
