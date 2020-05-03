package pl.springtest.communicatorclient.toServerConnection;

import pl.springtest.communicatorclient.info.AppInfo;
import pl.springtest.communicatorclient.statement.ClientStatement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Connect to server and then manage reading and transmitting message by socket
 */
public class ClientSocketHandler {
    private Socket clientSocket = null;
    private PrintWriter outMessage = null;
    private BufferedReader inMessage = null;

    /**
     * Create new socket connection client-server
     * @param addressIP - server address IP
     * @param port - servers port to connect
     */
    public ClientSocketHandler(String addressIP, int port) {
        try {
            clientSocket = new Socket(addressIP, port);
            outMessage = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            ClientStatement.Error("UnknownHostException occurred.", ClientStatement.DO_EXIT);
        } catch (IOException e) {
            ClientStatement.Error("IOException occurred.", ClientStatement.DO_EXIT);
        }
    }

    /**
     * Prepare text message to one line format (all expect MESSAGE ended by ";" - MESSAGE ends by "\n"):
     * "VERSION_INFO:" + version (eg. "2.1.3;")
     * "CLIENT_NAME:" + name (eq. "Adam;")
     * "GROUP_ID:" + groupId (eq. "Group123;"
     * "EXTRA:" + extra info to server (eg. "SHUTDOWN;" - client is shutting down)
     * "MESSAGE:" + message from parameter
     * EXTRA list:
     * - SHUTDOWN - client is shutting down (since ver. 1.0.0)
     * - NEW_CONNECTION - client send information about new connection to server (since ver. 1.0.0)
     * GROUP_ID can be "BROADCAST" - sending to everyone, but message should be from server, not client
     * @param message - text message to server
     * @param isShuttingDown - information about being shutting down
     * @return prepared string with data
     */
    private static String prepareMessage(String message, boolean isShuttingDown) {
        String preparedMessage = "VERSION_INFO:" + AppInfo.VERSION_INFO + ";";
        preparedMessage += "CLIENT_NAME:" + ClientData.name + ";";
        preparedMessage += "GROUP_ID:" + ClientData.groupID + ";";
        if (isShuttingDown)
            preparedMessage += "EXTRA:SHUTDOWN;";
        else
            preparedMessage += "EXTRA:;";
        preparedMessage += "MESSAGE:" + message + "\n";

        return preparedMessage;
    }

    /**
     * Decode message to String with userName and message
     * @param message - full message to decode
     * @return clientName: message
     */
    private static String decodeMessage(String message) {
        int clientNameStartIndex = message.indexOf("CLIENT_NAME:") + 13; // 13 is number of characters in "CLIENT_NAME:" + 1
        int clientNameEndIndex = message.indexOf(";", clientNameStartIndex) - 1;
        int messageStartIndex = message.indexOf("MESSAGE:") + 10; // 10 is number of characters in "MESSAGE:" + 1
        int messageEndIndex = message.length();
        return message.substring(clientNameStartIndex, clientNameEndIndex) + ": " + message.substring(messageStartIndex, messageEndIndex);
    }

    private static boolean isServerShuttingDown(String message) {
        int messageStartIndex = message.indexOf("MESSAGES:") + 10; // 10 is number of characters in "MESSAGE:" + 1
        int shutdownStartIndex = message.indexOf("EXTRA:SHUTDOWN;");
        if (shutdownStartIndex != -1 && shutdownStartIndex < messageStartIndex) // not in MESSAGE: body and EXTRA:SHUTDOWN; exist - server is shutting down
            return true;
        return false;
    }

    /**
     * Send message to server
     * @param message - data to send
     * @param isShuttingDown - information about client is shutting down
     */
    public void sendMessageToServer(String message, boolean isShuttingDown) {
        //ClientStatement.Info("Message to server: " + message);
        ClientStatement.Info(this.prepareMessage(message, isShuttingDown));
        outMessage.write(this.prepareMessage(message, isShuttingDown));
        outMessage.flush();
    }

    /**
     * Read message from server
     * @return - received data
     */
    public String readMessageFromServer() {
        String message = null;

        try {
            inMessage = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            ClientStatement.Error("IOException occurred when tried to get InputStream from socket.", ClientStatement.DO_EXIT);
        }

        try {
            if (!inMessage.ready())
                return null;
            message = inMessage.readLine();
        } catch (IOException e) {
            ClientStatement.Error("IOException occurred when reading message from server.", ClientStatement.DO_EXIT);
        }

        if (isServerShuttingDown(message)) {
            ClientStatement.Info("Server is shutting down.");
            this.closeSocket();
            return null;
        }

        return this.decodeMessage(message);
    }

    /**
     * Check connection to server
     * @return information about socket - closed or not
     */
    public boolean isConnected() {
        return !clientSocket.isClosed();
    }

    /**
     * Close socket to server
     */
    public void closeSocket() {
        boolean error = false;

        try {
            error = false;
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            ClientStatement.Error("Closing Socket: IOException occurred.", ClientStatement.NO_EXIT);
            error = true;
        } finally {
            if (!error)
                ClientStatement.Info("Closed socket");
        }
        if (outMessage != null) {
            outMessage.close();
            ClientStatement.Info("Closed OutputStream");
        }
        try {
            error = false;
            if (inMessage != null)
                inMessage.close();
        } catch (IOException e) {
            ClientStatement.Error("Closing BufferedReader: IOException occurred.", ClientStatement.NO_EXIT);
            error = true;
        } finally {
            if (!error)
                ClientStatement.Info("Closed InputStream");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientSocketHandler that = (ClientSocketHandler) o;
        return Objects.equals(clientSocket, that.clientSocket) &&
                Objects.equals(outMessage, that.outMessage) &&
                Objects.equals(inMessage, that.inMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientSocket, outMessage, inMessage);
    }
}
