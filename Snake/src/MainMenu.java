import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainMenu extends JPanel {
    // Tryby gry
    public enum Mode {
        SINGLE_PLAYER,
        PLAY_WITH_COMPUTER,
        NONE
    }

    private JFrame parentFrame;
    private Mode selectedMode = Mode.NONE;

    // Konstruktor klasy MainMenu
    public MainMenu(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Ustawienie układu na BoxLayout z osiami Y
        setBackground(Color.BLACK); // Ustawienie tła na czarne

        // Tytuł gry
        JLabel title = new JLabel("Snake Game");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Helvetica", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        // Przyciski menu
        JButton singlePlayerButton = new JButton("Single Player");
        JButton playWithComputerButton = new JButton("Play with Computer");
        JButton highscoreButton = new JButton("Highscore");

        singlePlayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playWithComputerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        highscoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Akcja przycisku dla trybu gry jednoosobowej
        singlePlayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMode = Mode.SINGLE_PLAYER;
                startGame();
            }
        });

        // Akcja przycisku dla trybu gry z komputerem
        playWithComputerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMode = Mode.PLAY_WITH_COMPUTER;
                startGame();
            }
        });

        // Akcja przycisku do wyświetlania wyników
        highscoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHighscore();
            }
        });

        // Dodawanie elementów do panelu
        add(Box.createRigidArea(new Dimension(0, 50)));
        add(title);
        add(Box.createRigidArea(new Dimension(0, 50)));
        add(singlePlayerButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(playWithComputerButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(highscoreButton);
    }

    // Metoda do rozpoczęcia gry
    private void startGame() {
        if (selectedMode == Mode.SINGLE_PLAYER) {
            Normal game = new Normal(parentFrame); // Tworzenie obiektu gry jednoosobowej
            parentFrame.setContentPane(game);
            parentFrame.revalidate();
            game.requestFocusInWindow();
        } else if (selectedMode == Mode.PLAY_WITH_COMPUTER) {
            SnakeGame game = new SnakeGame(true, parentFrame); // Tworzenie obiektu gry z komputerem
            parentFrame.setContentPane(game);
            parentFrame.revalidate();
            game.requestFocusInWindow();
        }
    }

    // Metoda do wyświetlania wyników
    private void showHighscore() {
        try {
            int singlePlayerHighscore = Highscore.readSinglePlayerHighscore();
            int playWithComputerHighscore = Highscore.readPlayWithComputerHighscore();
            String message = String.format("Single Player:    %d%nwith Computer:  %d",
                    singlePlayerHighscore, playWithComputerHighscore);
            JOptionPane.showMessageDialog(parentFrame, message, "Highscore", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame, "Failed to read highscore.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getter dla wybranego trybu gry
    public Mode getSelectedMode() {
        return selectedMode;
    }
}
