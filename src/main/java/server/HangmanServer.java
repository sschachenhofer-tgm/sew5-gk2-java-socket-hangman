package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class starts a Hangman server that accepts new connections from clients via TCP sockets.
 * For every incoming client connection, a new HangmanSession thread is started.
 *
 * @author  Simon Schachenhofer
 * @version 2019-11-13
 */
public class HangmanServer {

    private int port;

    private ExecutorService exec;
    private ServerSocket socket;
    private boolean listening;

    private HighscoreList highscores;

    /**
     * Instantiates a new HangmanServer to listen for incoming connections on the specified port.
     *
     * @param   args    A String array containing all command line parameters. The first parameter is the port number.
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public static void main(String[] args) {
        HangmanServer server = new HangmanServer(Integer.parseInt(args[0]));
        server.startServer();
    }

    /**
     * Creates a new instance of HangmanServer that is going to wait for incoming connections on the specified port. To
     * start the server and accept clients, call the method startServer().
     *
     * @param   port    The port number on which the server is waiting for incoming connections
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public HangmanServer(int port) {
        this.port = port;
        this.exec = Executors.newCachedThreadPool();
        this.listening = true;
        this.highscores = new HighscoreList();
    }

    /**
     * Starts the server by calling accept() on the ServerSocket.
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public void startServer() {
        try {
            this.socket = new ServerSocket(this.port);

            System.out.println("Server started on port " + this.socket.getLocalPort());

            while (this.listening) this.exec.submit(new HangmanServerSession(this.socket.accept(), this.highscores));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
