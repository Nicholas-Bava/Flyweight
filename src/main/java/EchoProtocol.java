import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoProtocol implements Runnable {
    private static final int BUFSIZE = 1024;   // Larger buffer
    private Socket clntSock;                   // Client socket
    private Logger logger;                     // Server logger

    public EchoProtocol(Socket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
    }

    public static void handleEchoClient(Socket clntSock, Logger logger) {
        SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
        logger.info("Handling client at " + clientAddress);

        try {
            // Set short socket timeout for JMeter compatibility
            clntSock.setSoTimeout(2000); // 2 second timeout

            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();

            byte[] receiveBuf = new byte[BUFSIZE];
            ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();

            // Read all available data
            try {
                int recvMsgSize;
                while ((recvMsgSize = in.read(receiveBuf)) != -1) {
                    messageBuffer.write(receiveBuf, 0, recvMsgSize);

                    // Check if we have a complete message (ends with newline or no more data coming)
                    if (recvMsgSize < BUFSIZE) {
                        // Likely end of message
                        break;
                    }
                }
            } catch (SocketTimeoutException e) {
                // Timeout means client is done sending - this is normal for JMeter
                logger.info("Client finished sending data (timeout) - processing message");
            }

            byte[] fullMessage = messageBuffer.toByteArray();
            if (fullMessage.length > 0) {
                logger.info("Received " + fullMessage.length + " bytes from " + clientAddress);

                // Add delay simulation for 4+ characters
                if (fullMessage.length >= 4) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Echo back the received data
                out.write(fullMessage);
                out.flush();

                logger.info("Echoed " + fullMessage.length + " bytes to " + clientAddress);

                // Give client time to read response
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (SocketException ex) {
            // Expected when client closes connection
            logger.info("Client " + clientAddress + " disconnected: " + ex.getMessage());
        } catch (IOException ex) {
            logger.log(Level.WARNING, "IOException with client " + clientAddress, ex);
        } finally {
            try {
                clntSock.close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Exception closing socket for " + clientAddress, ex);
            }
        }
    }

    @Override
    public void run() {
        handleEchoClient(clntSock, logger);
    }
}