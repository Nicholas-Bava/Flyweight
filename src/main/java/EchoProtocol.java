import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoProtocol implements Runnable {
    private static final int BUFSIZE = 1024;
    private Socket clntSock;
    private Logger logger;

    public EchoProtocol(Socket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
    }

    public static void handleEchoClient(Socket clntSock, Logger logger) {
        SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
        logger.info("Handling client at " + clientAddress);

        try {

            clntSock.setSoTimeout(2000);

            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();

            byte[] receiveBuf = new byte[BUFSIZE];
            ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();

            try {
                int recvMsgSize;
                while ((recvMsgSize = in.read(receiveBuf)) != -1) {
                    messageBuffer.write(receiveBuf, 0, recvMsgSize);


                    if (recvMsgSize < BUFSIZE) {
                        break;
                    }
                }
            } catch (SocketTimeoutException e) {

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

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (SocketException ex) {
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