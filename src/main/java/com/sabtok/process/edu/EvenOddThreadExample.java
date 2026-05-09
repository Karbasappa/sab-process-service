package com.sabtok.process.edu;

public class EvenOddThreadExample {
    int counter = 1;
    static int N;

    // Method to print odd numbers
    public void printOdd() {
        synchronized (this) {
            while (counter < N) {
                // If counter is even, wait for even thread to print
                while (counter % 2 == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Odd Thread: " + counter);
                counter++;
                notify(); // Notify even thread
            }
        }
    }

    // Method to print even numbers
    public void printEven() {
        synchronized (this) {
            while (counter < N) {
                // If counter is odd, wait for odd thread to print
                while (counter % 2 != 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Even Thread: " + counter);
                counter++;
                notify(); // Notify odd thread
            }
        }
    }

    public static void main(String[] args) {
        N = 10; // Print up to 10
        EvenOddThreadExample printer = new EvenOddThreadExample();

        Thread t1 = new Thread(printer::printOdd);
        Thread t2 = new Thread(printer::printEven);

        t1.start();
        t2.start();
    }
}
