package ch.ethz.globis.distindex.middleware.api;

/**
 * Represents a Middleware node.
 */
public interface Middleware {

    public void run();

    public void shutdown();

    public boolean isRunning();
}