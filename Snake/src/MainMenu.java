import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel {
    public enum Mode {
        SINGLE_PLAYER,
        PLAY_WITH_COMPUTER,
        NONE
    }

    private JFrame parentFrame;
    private Mode selectedMode = Mode.NONE;

    public MainMenu(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK); // Ustawienie tła na czarne

        JLabel title = new JLabel("Snake Game");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Helvetica", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        JButton singlePlayerButton = new JButton("Single Player");
        JButton playWithComputerButton = new JButton("Play with Computer");

        singlePlayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playWithComputerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        singlePlayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMode = Mode.SINGLE_PLAYER;
                startGame();
            }
        });

        playWithComputerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMode = Mode.PLAY_WITH_COMPUTER;
                startGame();
            }
        });

        add(Box.createRigidArea(new Dimension(0, 50)));
        add(title);
        add(Box.createRigidArea(new Dimension(0, 50)));
        add(singlePlayerButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(playWithComputerButton);
    }

    private void startGame() {
        if (selectedMode == Mode.SINGLE_PLAYER) {
            Normal game = new Normal();
            parentFrame.setContentPane(game);
            parentFrame.revalidate();
            game.requestFocusInWindow();
        } else if (selectedMode == Mode.PLAY_WITH_COMPUTER) {
            SnakeGame game = new SnakeGame(true);
            parentFrame.setContentPane(game);
            parentFrame.revalidate();
            game.requestFocusInWindow();
        }
    }

    public Mode getSelectedMode() {
        return selectedMode;
    }
}
