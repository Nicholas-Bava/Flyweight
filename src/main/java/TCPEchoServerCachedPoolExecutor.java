
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TCPEchoServerCachedPoolExecutor {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int echoServPort = Integer.parseInt(args[0]); // Server port
        Logger logger = Logger.getLogger("cached-executor");

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(echoServPort);

        // Create cached thread pool executor (similar to thread per connection)
        ExecutorService executor = Executors.newCachedThreadPool();

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

        logger.info("Cached Thread Pool Echo Server started on port " + echoServPort);

        // Run forever, accepting and submitting connections to thread pool
        while (true) {
            try {
                Socket clntSock = servSock.accept(); // Block waiting for connection

                // Submit connection to cached thread pool
                executor.submit(new EchoProtocol(clntSock, logger));

                logger.info("Submitted new connection to cached thread pool");
            } catch (IOException ex) {
                logger.warning("Client accept failed: " + ex.getMessage());
            }
        }
        /* NOT REACHED */
    }
}
