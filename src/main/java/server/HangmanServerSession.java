package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.TreeSet;

/**
 * This class handles the actual hangman game between a server and a single client. It chooses a random word and then
 * makes the client guess that word. All communication happens over a TCP socket connection.
 *
 * The HangmanServerSession extends the java.lang.Thread class. To enable the server to host multiple games of hangman
 * at once, run this class concurrently by creating a new instance and then calling start() on it.
 *
 * @author      Simon Schachenhofer
 * @version     2019-11-07
 */
public class HangmanServerSession extends Thread {

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;

    private String word;
    private TreeSet<Character> guessedChars;
    private int tries;
    private int remainingTries;
    private boolean gameRunning;
    private HighscoreList highscores;

    /**
     * Creates a new instance of a HangmanServerSession for hosting a game of hangman with a client.
     *
     * @param   socket  The TCP Socket object used for communicating with the client
     * @param   highscores  An instance of HighscoreList managing the highscores table
     * @since   2019-11-07
     */
    public HangmanServerSession(Socket socket, HighscoreList highscores) {
        this.clientSocket = socket;

        // Step 1: Choose a word
        this.word = new OfflineRandomWords().randomWord().toUpperCase();
        this.guessedChars = new TreeSet<>();

        this.tries = 10;
        this.remainingTries = 10;

        this.highscores = highscores;
    }

    /**
     * Plays the game of hangman. This is the main method for the hangman gameplay.
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public void run() {
        System.out.println("Starting new game for client " + this.clientSocket.getInetAddress().toString());

        this.gameRunning = true;

        try {
            this.socketIn = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.socketOut = new PrintWriter(this.clientSocket.getOutputStream());

            // Send the client the completely masked word
            this.output(String.format("%d remaining tries. %s",
                    remainingTries, HangmanServerSession.uncoverWord(word, guessedChars)));

            String guess;

            while (true) {
                if (!this.gameRunning) break;

                guess = socketIn.readLine();
                if (guess == null) break;

                if (guess.length() == 1) this.handleLetterGuess(Character.toUpperCase(guess.charAt(0)));
                else this.handleWordGuess(guess);
            }

            // The game ends as soon as the while loop is exited
            clientSocket.close();

        } catch (IOException e) {
            // TODO: Handle exception
        }

        System.out.println(String.format("Game for client %s ended. Score: %d",
                this.clientSocket.getInetAddress().toString(), this.tries - this.remainingTries));
    }

    /**
     * Handles a guess the client made to uncover a single letter.
     *
     * @param   guess   The client's guess
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    private void handleLetterGuess(char guess) {
        if (this.guessedChars.contains(guess)) {
            // The client already guessed for that character
            this.output(String.format("You already guessed '%s' - %d remaining tries. %s",
                    guess, this.remainingTries, this.uncoverWord()));

        } else {
            // The client didn't guess that character yet
            this.guessedChars.add(guess);

            if (this.word.indexOf(guess) > -1) {
                // The client successfully guessed a letter

                if (this.isWordUncovered()) {
                    // The client guessed all letters in the word
                    this.gameWon();

                } else {
                    // The client has tries left
                    this.output(String.format("%d remaining tries. %s", remainingTries,
                            HangmanServerSession.uncoverWord(word, guessedChars)));
                }

            } else {
                // The client guessed a letter that is not contained in the word
                unsuccessfulGuess();
            }
        }
    }

    /**
     * Handles a guess the client made to uncover the entire word.
     *
     * @param   guess   The client's guess
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    private void handleWordGuess(String guess) {
        if (guess.toUpperCase().equals(this.word)) {
            // The guess was successful
            this.gameWon();

        } else {
            // The guess was not successful
            this.unsuccessfulGuess();
        }
    }

    /**
     * Prints the passed String to the Socket and then flushes immediately.
     *
     * @param   message The String to print to the socket
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    private void output(String message) {
        this.socketOut.println(message);
        this.socketOut.flush();
    }

    /**
     * Uncovers all the letters the client playing the current game guessed in the word.
     *
     * @return  A String representing the word in which all guessed letters are uncovered and all other letters are
     *          replaced by an underscore.
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    private String uncoverWord() {
        return HangmanServerSession.uncoverWord(this.word, this.guessedChars);
    }

    /**
     * Uncovers all the letters the client guessed in the word.
     *
     * @param   word    The word the client needs to guess
     * @param   guessedChars    A Collection containing all characters the client guessed so far
     * @return  A String representing the word in which all guessed letters are uncovered and all other letters are
     *          replaced by an underscore.
     * @author  Simon Schachenhofer
     * @since   2019-11-07
     */
    public static String uncoverWord(String word, Collection<Character> guessedChars) {
        word = word.toUpperCase();

        String masked = new String(new char[word.length()]).replace('\0', '_');

        for (char c : guessedChars) {
            c = Character.toUpperCase(c);

            if (word.indexOf(c) > -1) {
                // If the word contains the char, iterate through the word and uncover all occurences

                int index = -1;
                do {
                    index = word.indexOf(c, index + 1);
                    if (index != -1) masked = masked.substring(0, index) + c + masked.substring(index + 1);
                } while (index != -1);
            }
        }

        return masked;
    }

    /**
     * Returns whether the client playing the current game guessed all letters contained in the word.
     *
     * @return  true if the client already guessed all letters contained in the word, false otherwise
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    private boolean isWordUncovered() {
        return HangmanServerSession.isWordUncovered(this.word, this.guessedChars);
    }

    /**
     * Returns whether the client guessed all letters contained in the word.
     *
     * @param   word    The word the client needs to guess
     * @param   guessedChars    A Collection of all char values the client guessed so far
     * @return  true if the client already guessed all letters contained in the word, false otherwise
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    public static boolean isWordUncovered(String word, Collection<Character> guessedChars) {
        for (char c : word.toCharArray()) {
            if (!guessedChars.contains(c)) return false;
        }

        // If all characters in the string are also contained in the Collection, the client has guessed all letters
        return true;
    }

    /**
     * Reacts to an unsuccessful client guess by sending back a message with the remaining tries and the word, or by
     * ending the game if the client has no tries left
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    private void unsuccessfulGuess() {
        this.remainingTries--;

        if (this.remainingTries > 0) {
            // The client has tries left
            this.output(String.format("%d remaining tries. %s", this.remainingTries,
                    HangmanServerSession.uncoverWord(this.word, this.guessedChars)));
        } else {
            // The client has no tries left and lost the game
            this.gameLost();
        }
    }

    /**
     * Sends a message informing the client that they won and then ends the game by setting gameRunning to false.
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    private void gameWon() {
        this.output(String.format("You win. The word was: %s. Press Enter to end the game.", this.word));
        this.gameRunning = false;
        this.highscores.handleScore(this.tries - this.remainingTries);
    }

    /**
     * Sends a message informing the client that they lost and then ends the game by setting gameRunning to false.
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    private void gameLost() {
        this.output(String.format("You lose. The word was: %s", this.word));
        this.gameRunning = false;
    }


}