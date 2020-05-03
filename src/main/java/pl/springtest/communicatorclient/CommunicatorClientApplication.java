package pl.springtest.communicatorclient;

import pl.springtest.communicatorclient.toServerConnection.Connection;

/**
 * Version: 1.0.0
 * Author: Marcin Stolarek
 * Communicator application
 */
public class CommunicatorClientApplication {
    public static void main(String[] args) {
        Connection serverConnection = new Connection("localhost", 1234, args[0], "Group1");
    }
}

//TODO - initial messages to server