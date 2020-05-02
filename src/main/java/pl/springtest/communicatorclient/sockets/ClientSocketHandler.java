package pl.springtest.communicatorclient.sockets;

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
     * Send message to server
     * @param message - data to send
     */
    public void sendMessageToServer(String message) {
        ClientStatement.Info("Message to server: " + message);
        outMessage.write(message + "\n");
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
        return message;
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
