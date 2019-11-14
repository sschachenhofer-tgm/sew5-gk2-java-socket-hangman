package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class for handling scores reached in the Hangman game. This class provides access to the highscore table, which
 * records the top 10 scores. It is also responsible for writing the table to a file.
 *
 * @author  Simon Schachenhofer
 * @version 2019-11-13
 */
public class HighscoreList {

    private ArrayList<Integer> highscores;
    private Lock highscoresLock;
    private File highscoresFile;

    /**
     * Creates a new instance of HighscoreList to manage the highscore file.
     *
     * This constructor will try to read the already existing highscores from a file called highscore.txt. If that
     * file does not exist, it will be created. The file may consist only of highscore lines, without any headers or
     * titles. Each line may only contain a number and nothing else. Names of the players who played the game will not
     * be recorded.
     *
     * @author  Simon Schachenhofer
     * @since   2019-11-13
     */
    public HighscoreList() {
        this.highscores = new ArrayList<>();
        this.highscoresLock = new ReentrantReadWriteLock().writeLock();

        try {
            this.highscoresFile = new File("scores.txt");
            if (highscoresFile.exists()) {
                // Read the highscores from the file
                RandomAccessFile highscoresRAF = new RandomAccessFile(this.highscoresFile, "rw");

                String hsLine;
                while ((hsLine = highscoresRAF.readLine()) != null) {
                    try {
                        this.highscores.add(Integer.parseInt(hsLine));
                    } catch (NumberFormatException e) {
                        System.out.println("The following line in the highscores file will be skipped:");
                        System.out.println(hsLine);
                    }
                }

                System.out.println(String.format("%d scores read from highscores.txt file:", this.highscores.size()));
                for (int score : this.highscores) System.out.println(score);

            } else {
                // Create a new highscores file
                this.highscoresFile.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a new score reached by a client. If the score is among the top ten scores, it is written to the
     * highscores file. Otherwise, it is simply ignored.
     *
     * @param   score   The score the client reached (the number of tries)
     * @author  Simon Schachenhofer
     * @since   2019-11-08
     */
    public void handleScore(int score) {

        this.highscores.add(score);  // Add the score
        this.highscores.sort(null);  // Sort the highscores

        // Remove all but the top-ten scores from the list
        if (this.highscores.size() > 10) this.highscores.subList(10, this.highscores.size()).clear();

        StringBuilder output = new StringBuilder();
        for (int highscore : this.highscores) {
            output.append(highscore);
            output.append("\n");
        }

        try {
            FileWriter highscoresFileWriter = new FileWriter(this.highscoresFile, false);
            this.highscoresLock.tryLock(10, TimeUnit.SECONDS);  // Acquire the lock
            highscoresFileWriter.write(output.toString());
            highscoresFileWriter.flush();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.highscoresLock.unlock();
        }
    }
}
