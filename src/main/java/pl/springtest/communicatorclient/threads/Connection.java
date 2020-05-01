package pl.springtest.communicatorclient.threads;

import pl.springtest.communicatorclient.messages.MessageHandler;
import pl.springtest.communicatorclient.sockets.ClientSocketHandler;
import pl.springtest.communicatorclient.statement.ClientStatement;

import java.util.List;
import java.util.Objects;

/**
 * Object connects to server, create threads to send and read messages
 */
public class Connection {
    ClientSocketHandler clientSocket = null;
    MessageHandler messageHandler;

    /**
     * Activate connection and threads to send and read messages
     * Have ShutdownHook - to close socket
     * @param addressIP - server address IP
     * @param port - servers port to connect
     */
    public Connection(String addressIP, int port) {
        activateConnection(addressIP, port);
        messageHandler = new MessageHandler();
        createReadTransmitConnectionThread();

        // closing socket when application is being shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ClientStatement.Info("CTRL+C");
                synchronized(clientSocket) {
                    clientSocket.closeSocket();
                    clientSocket.notifyAll();
                }
                messageHandler.resetConnectionToServerOK();
            }
        });
    }

    /**
     * Activate connection to server
     * @param addressIP - address IP of server
     * @param port - port to connect to server
     */
    private void activateConnection(String addressIP, int port) {
        clientSocket = new ClientSocketHandler(addressIP, port);
    }

    /**
     * Start reading and transmitting messages to and from server
     */
    private void createReadTransmitConnectionThread() {
        TransmitConnection tx = new TransmitConnection();
        ReadConnection rx = new ReadConnection();
        tx.start();
        rx.start();
        synchronized (clientSocket) {
            clientSocket.notifyAll(); // one of them have to start first (both are waiting to notify)
        }
    }

    /**
     * Thread to transmit messages from client to server
     */
    private class TransmitConnection extends Thread {
        @Override
        public void run() {
            this.setName("Thread - TransmitConnection");
            while (clientSocket.isConnected()) {
                ClientStatement.Info("TransmitConnection thread - new loop");
                synchronized (clientSocket) {
                    try {
                        clientSocket.wait(5000);
                    } catch (InterruptedException e) {
                        ; // do nothing
                    } finally {
                        if (!messageHandler.isMessagesToServerEmpty()) {
                            List<String> newMessagesToServer = messageHandler.GetMessagesToServerAndClear();

                            for (String message : newMessagesToServer)
                                clientSocket.sendMessageToServer(message);
                        }
                        clientSocket.notify();
                    }
                }
            }
            ClientStatement.Info("End of TransmitConnection thread");
        }
    }

    /**
     * Thread to read messages from server to client
     */
    private class ReadConnection extends Thread {
        @Override
        public void run() {
            this.setName("Thread - ReadConnection");
            while (clientSocket.isConnected()) {
                synchronized (clientSocket) {
                    ClientStatement.Info("ReadConnection thread - new loop");
                    try {
                        clientSocket.wait(5000);
                    } catch (InterruptedException e) {
                        ; // do nothing
                    } finally {
                        if (clientSocket.isConnected()) {
                            String message = clientSocket.readMessageFromServer();
                            if (message != null)
                                messageHandler.addMessageFromServer(message);
                        }
                        clientSocket.notify();
                    }
                }
            }
            ClientStatement.Info("End of ReadConnection thread");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(clientSocket, that.clientSocket) &&
                Objects.equals(messageHandler, that.messageHandler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientSocket, messageHandler);
    }
}
