package pl.springtest.communicatorclient.statement;

/**
 * Print statements at server
 */
public abstract class ClientStatement {
    public static final boolean NO_EXIT = false;
    public static final boolean DO_EXIT = true;

    /**
     * Error statement
     * @param message - message to print
     * @param exitApplication - true, if application should exit after print statement; false otherwise
     */
    public static void Error(String message, boolean exitApplication) {
        System.out.println("Error: " + message);
        if (exitApplication)
            System.exit(-1);
    }

    /**
     * Info statement
     * @param message - message to print
     */
    public static void Info(String message) {
        System.out.println("Info: " + message);
    }
}
