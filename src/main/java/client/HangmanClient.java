package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The client class for the hangman game.
 *
 * @author  Simon Schachenhofer
 * @version 2019-11-13
 */
public class HangmanClient {

    private String serverHostName;
    private int serverPort;

    /**
     * Creates a new instance of HangmanClient to play a game of hangman on the specified server.
     *
     * @param   args    A String array containing all command line parameters. The first parameter is the host name of
     *                  the hangman server to connect to. The second parameter is the port number of the hangman server.
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public static void main(String[] args) {
        HangmanClient client = new HangmanClient(args[0], Integer.parseInt(args[1]));
        client.startClient();
    }

    /**
     * Instantiates a new HangmanClient that is going to play a game of hangman on the specified server.
     *
     * @param   hostname    The host name (or IP address) of the hangman server
     * @param   port    The port number on which the server is waiting for incoming connections
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public HangmanClient(String hostname, int port) {
        this.serverHostName = hostname;
        this.serverPort = port;
    }

    /**
     * Starts the client. The client will then connect to the server (using hostname and port number as specified in
     * the constructor) and start the game of hangman, printing output to stdOut.
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public void startClient() {
        try (
            Socket socket = new Socket(this.serverHostName, this.serverPort);
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream());
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner consoleIn = new Scanner(System.in);
        ) {

            System.out.println("You are now playing a game of hangman on the server " + this.serverHostName);

            String msg;
            while ((msg = socketIn.readLine()) != null) {

                // Print out the message received from the server
                System.out.println(msg);

                // Read the user input from the console and send it to the server
                socketOut.println(consoleIn.nextLine());
                socketOut.flush();
            }

        } catch (IOException e) {
            //
        }
    }
}
