package ch.ethz.globis.distindex.util;

import ch.ethz.globis.distindex.middleware.api.Middleware;

import java.util.concurrent.ExecutorService;

public class TestUtil {

    public static void startMiddleware(ExecutorService threadPool, Middleware middleware) {
        threadPool.execute((Runnable) middleware);
        while (!middleware.isRunning()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.err.format("Failed to sleep while initializing middleware.");
                e.printStackTrace();
            }
        }
    }
}
