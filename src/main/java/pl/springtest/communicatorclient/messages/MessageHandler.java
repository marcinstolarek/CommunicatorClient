package pl.springtest.communicatorclient.messages;

import pl.springtest.communicatorclient.statement.ClientStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Read System.in and write System.out - messages from client to server and from server to client
 */
public class MessageHandler {
    private List<String> messagesToServer;
    private List<String> messagesFromServer;
    private boolean connectionToServerOK;

    /**
     * Create clean list of messages to and from server
     * It has to be connection to server established
     */
    public MessageHandler() {
        messagesToServer = new ArrayList<String>();
        messagesFromServer = new ArrayList<String>();
        connectionToServerOK = true;

        // activate threads for writing and reading
        ReadInput readInput = new ReadInput();
        WriteOutput writeOutput = new WriteOutput();
        readInput.start();;
        writeOutput.start();
    }

    /**
     * Thread reading input messages from System.in
     * When read, it is added to messagesToServer list
     * Thread will close when there is no longer connection to server
     */
    private class ReadInput extends Thread {
        Scanner input = new Scanner(System.in);
        String newMessage = null;
        @Override
        public void run() {
            this.setName("Thread - ReadInput");
            while (connectionToServerOK) {
                System.out.print("> ");
                newMessage = input.nextLine();
                synchronized (messagesToServer) {
                    messagesToServer.add(newMessage);
                }
            }
            ClientStatement.Info("End of ReadInput thread");
        }
    }

    /**
     * Thread writing output to System.out
     */
    private class WriteOutput extends Thread {
        @Override
        public void run() {
            this.setName("Thread - WriteOutput");
            while (connectionToServerOK) {
                synchronized (messagesFromServer) {
                    try {
                        messagesFromServer.wait(); // wait for any change (new message added)
                    } catch (InterruptedException e) {
                        ClientStatement.Error("InterruptedException - WriteOutput occurred.", ClientStatement.NO_EXIT);
                    } finally {
                        // writing messages from list and then delete it from list - list is only for new unhandled messages
                        while (messagesFromServer.size() > 0) {
                            System.out.println(messagesFromServer.get(0));
                            messagesFromServer.remove(0);
                        }
                    }
                }
            }
            ClientStatement.Info("End of WriteOutput thread");
        }
    }

    public boolean isMessagesToServerEmpty() {
        return messagesToServer.isEmpty();
    }

    /**
     * Get copy of list and then clear original list
     * @return copy of messagesToServer
     */
    public List<String> GetMessagesToServerAndClear() {
        List<String> copyMessagesToServer = new ArrayList<String>();

        synchronized(messagesToServer) {
            // make a copy of list
            for (String mess : messagesToServer)
                copyMessagesToServer.add(mess);
            messagesToServer.clear();
        }
        return copyMessagesToServer;
    }

    public void addMessageFromServer(String newMessage) {
        synchronized (messagesFromServer) {
            messagesFromServer.add(newMessage);
            messagesFromServer.notify();
        }
    }

    public boolean isConnectionToServerOK() {
        return connectionToServerOK;
    }

    public void resetConnectionToServerOK() {
        connectionToServerOK = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageHandler that = (MessageHandler) o;
        return connectionToServerOK == that.connectionToServerOK &&
                Objects.equals(messagesToServer, that.messagesToServer) &&
                Objects.equals(messagesFromServer, that.messagesFromServer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messagesToServer, messagesFromServer, connectionToServerOK);
    }
}
