import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TCPEchoServerFixedPoolExecutor {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port> <Pool Size>");
        }

        int echoServPort = Integer.parseInt(args[0]); // Server port
        int poolSize = Integer.parseInt(args[1]);     // Thread pool size
        Logger logger = Logger.getLogger("fixed-executor");

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(echoServPort);

        // Create fixed size thread pool executor
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

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

        logger.info("Fixed Thread Pool Echo Server started on port " + echoServPort +
                " with pool size: " + poolSize);

        // Run forever, accepting and submitting connections to thread pool
        while (true) {
            try {
                Socket clntSock = servSock.accept(); // Block waiting for connection

                // Submit connection to fixed thread pool
                executor.submit(new EchoProtocol(clntSock, logger));

                logger.info("Submitted new connection to fixed thread pool");
            } catch (IOException ex) {
                logger.warning("Client accept failed: " + ex.getMessage());
            }
        }
    }
}
