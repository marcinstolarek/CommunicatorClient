package pl.springtest.communicatorclient.statement;

import pl.springtest.communicatorclient.toServerConnection.ClientData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Print statements from server
 */
public abstract class ClientStatement {
    public static final boolean NO_EXIT = false;
    public static final boolean DO_EXIT = true;
    private static final String errorFileName = "log_errors_" + ClientData.name;
    private static final String infoFileName = "log_info_" + ClientData.name;

    /**
     * Error statement
     * @param message - message to print
     * @param exitApplication - true, if application should exit after print statement; false otherwise
     */
    public static void Error(String message, boolean exitApplication) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(errorFileName, true));
            writer.append(ClientStatement.getDateAndTime() + " - " + message + "\n");
            writer.close();
        }catch (IOException e) {
            ; // do nothing
        }
        System.out.println("Error: " + message);
        if (exitApplication)
            System.exit(-1);
    }

    /**
     * Info statement
     * @param message - message to print
     */
    public static void Info(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(infoFileName, true));
            writer.append(ClientStatement.getDateAndTime() + " - " + message + "\n");
            writer.close();
        }catch (IOException e) {
            ; // do nothing
        }
        //System.out.println("Info: " + message);
    }

    private static String getDateAndTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        LocalDateTime actualDT = LocalDateTime.now();
        return formatter.format(actualDT);
    }
}
