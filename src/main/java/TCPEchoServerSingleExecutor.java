import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TCPEchoServerSingleExecutor {
    private static final int BUFSIZE = 32;   // Size of receive buffer

    public static void main(String[] args) throws IOException {
        if (args.length != 1)  // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");

        int servPort = Integer.parseInt(args[0]);
        Logger logger = Logger.getLogger("single-executor");

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(servPort);

        // Create single thread executor
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Shutdown hook to properly close executor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down executor...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }));

        logger.info("Single Thread Executor Echo Server started on port " + servPort);

        while (true) { // Run forever, accepting connections
            try {
                Socket clntSock = servSock.accept();     // Get client connection

                // Submit task to single thread executor
                executor.submit(new EchoProtocol(clntSock, logger));

            } catch (IOException ex) {
                logger.warning("Client accept failed: " + ex.getMessage());
            }
        }
        /* NOT REACHED */
    }
}