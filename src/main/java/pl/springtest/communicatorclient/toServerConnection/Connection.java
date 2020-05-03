package pl.springtest.communicatorclient.toServerConnection;

import pl.springtest.communicatorclient.messages.MessageHandler;
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
     * @param clientName - client name which is added to messages data
     * @param groupId - communicator group ID which is added to messages data
     */
    public Connection(String addressIP, int port, String clientName, String groupId) {
        ClientData.name = clientName;
        ClientData.groupID = groupId;
        activateConnection(addressIP, port);
        messageHandler = new MessageHandler();
        createReadTransmitConnectionThread();

        // closing socket when application is being shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ClientStatement.Info("CTRL+C");
                synchronized(clientSocket) {
                    clientSocket.sendMessageToServer("LOGOUT", true);
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
    }

    /**
     * Thread to transmit messages from client to server
     */
    private class TransmitConnection extends Thread {
        @Override
        public void run() {
            this.setName("Thread - TransmitConnection");
            while (clientSocket.isConnected()) {
                synchronized (clientSocket) {
                    try {
                        clientSocket.notify(); // wake up ReadConnection thread
                        clientSocket.wait();
                    } catch (InterruptedException e) {
                        ClientStatement.Error("InterruptedException - TransmitConnection occurred.", ClientStatement.NO_EXIT);
                    } finally {
                        if (!messageHandler.isMessagesToServerEmpty()) {
                            List<String> newMessagesToServer = messageHandler.GetMessagesToServerAndClear();

                            for (String message : newMessagesToServer)
                                clientSocket.sendMessageToServer(message, false);
                        }
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
                    try {
                        clientSocket.notify(); // wake up TransmitConnection thread
                        clientSocket.wait();
                    } catch (InterruptedException e) {
                        ClientStatement.Error("InterruptedException - ReadConnection occurred.", ClientStatement.NO_EXIT);
                    } finally {
                        if (clientSocket.isConnected()) {
                            String message = clientSocket.readMessageFromServer();
                            if (message != null)
                                messageHandler.addMessageFromServer(message);
                        }
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
