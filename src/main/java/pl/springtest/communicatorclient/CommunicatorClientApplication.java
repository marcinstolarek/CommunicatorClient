package pl.springtest.communicatorclient;

import pl.springtest.communicatorclient.statement.ClientStatement;
import pl.springtest.communicatorclient.toServerConnection.Connection;

import java.io.IOException;

/**
 * Version: 1.0.0
 * Author: Marcin Stolarek
 * Communicator application
 * args[0] - user name
 * args[1] - group ID
 */
public class CommunicatorClientApplication {
    public static void main(String[] args) {
        if (args.length < 2)
            ClientStatement.Error("Application needs to 2 arguments: User name and Group ID: eg. CommunicatorClient MyName MyGroupID", ClientStatement.DO_EXIT);
        Connection serverConnection = new Connection("localhost", 1234, args[0], args[1]);
    }
}