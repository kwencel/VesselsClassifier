import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Krzysztof Wencel
 */
public class ProgressInfo {
    private final long totalWorkCount;
    private AtomicLong workCount;
    private Thread loggingThread;

    private void loggingFunction() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(10000);
                long localWorkCount = workCount.get();
                System.out.printf("Progress: [%.2f%c - %d %s %d %s\n", (float) 100 * localWorkCount / totalWorkCount,
                                  '%', localWorkCount, "out of", totalWorkCount, "processed]");
            }
        } catch (InterruptedException e) {
            // nop
        } finally {
            System.out.println("Work finished.");
        }
    }

    public ProgressInfo(long totalWorkCount) {
        this.totalWorkCount = totalWorkCount;
        this.workCount = new AtomicLong(0);
        this.loggingThread = new Thread(this::loggingFunction);
        loggingThread.start();
    }

    void incrementWorkCount() {
        long localWorkCount = workCount.incrementAndGet();
        if (localWorkCount == totalWorkCount) {
            loggingThread.interrupt();
        }
    }
}
