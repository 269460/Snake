import java.io.*;

public class Highscore {

    // Odczytuje wynik z pliku o podanej nazwie
    public static int readHighscore(String filename) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            return Integer.parseInt(line.trim());
        } catch (FileNotFoundException e) {
            // Jeśli plik nie istnieje, zwracamy 0
            return 0;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    // Zapisuje wynik do pliku o podanej nazwie.
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

    // Odczytuje wynik dla trybu gry jednoosobowej z domyślnego pliku.
    public static int readSinglePlayerHighscore() throws IOException {
        return readHighscore("single_player_highscore.txt");
    }

    // Zapisuje wynik dla trybu gry jednoosobowej do domyślnego pliku.
    public static void writeSinglePlayerHighscore(int score) throws IOException {
        writeHighscore("single_player_highscore.txt", score);
    }

    // Odczytuje wynik dla trybu gry z komputerem z domyślnego pliku.
    public static int readPlayWithComputerHighscore() throws IOException {
        return readHighscore("play_with_computer_highscore.txt");
    }

    // Zapisuje wynik dla trybu gry z komputerem do domyślnego pliku.
    public static void writePlayWithComputerHighscore(int score) throws IOException {
        writeHighscore("play_with_computer_highscore.txt", score);
    }
}
