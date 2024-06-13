import java.io.*;

public class Highscore {
    public static int readHighscore(String filename) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            return Integer.parseInt(line.trim());
        } catch (FileNotFoundException e) {
            // If the file does not exist, return 0
            return 0;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static void writeHighscore(String filename, int score) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(String.valueOf(score));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static int readSinglePlayerHighscore() throws IOException {
        return readHighscore("single_player_highscore.txt");
    }

    public static void writeSinglePlayerHighscore(int score) throws IOException {
        writeHighscore("single_player_highscore.txt", score);
    }

    public static int readPlayWithComputerHighscore() throws IOException {
        return readHighscore("play_with_computer_highscore.txt");
    }

    public static void writePlayWithComputerHighscore(int score) throws IOException {
        writeHighscore("play_with_computer_highscore.txt", score);
    }
}
