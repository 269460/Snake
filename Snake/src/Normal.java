import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Normal extends JPanel implements ActionListener {
    private final int TILE_SIZE = 25;
    private final int BOARD_WIDTH = 800;
    private final int BOARD_HEIGHT = 600;

    private List<Point> snake;
    private Point food;
    private char direction;
    private boolean running;
    private Timer timer;
    private JButton restartButton;
    private JButton menuButton;
    private BufferedImage appleImage;
    private BufferedImage bananaImage;
    private boolean isApple;
    private JFrame parentFrame;
    private int score; // Dodano zmienną przechowującą wynik

    // Konstruktor klasy Normal
    public Normal(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT && direction != 'R') {
                    direction = 'L';
                }
                if (key == KeyEvent.VK_RIGHT && direction != 'L') {
                    direction = 'R';
                }
                if (key == KeyEvent.VK_UP && direction != 'D') {
                    direction = 'U';
                }
                if (key == KeyEvent.VK_DOWN && direction != 'U') {
                    direction = 'D';
                }
            }
        });
        loadImages();
        initRestartButton();
        initMenuButton();
        initGame();
    }

    // Metoda ładowania obrazków owoców
    private void loadImages() {
        try {
            appleImage = ImageIO.read(getClass().getResourceAsStream("/resources/apple-removebg-preview.png"));
            bananaImage = ImageIO.read(getClass().getResourceAsStream("/resources/banana-removebg-preview.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inicjalizacja przycisku restartu
    private void initRestartButton() {
        restartButton = new JButton("Restart");
        restartButton.setFocusable(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initGame();
                restartButton.setVisible(false);
                menuButton.setVisible(false);
            }
        });
        restartButton.setVisible(false);
    }

    // Inicjalizacja przycisku menu
    private void initMenuButton() {
        menuButton = new JButton("Menu");
        menuButton.setFocusable(false);
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setContentPane(new MainMenu(parentFrame));
                parentFrame.revalidate();
            }
        });
        menuButton.setVisible(false);
    }

    // Inicjalizacja gry
    private void initGame() {
        snake = new ArrayList<>();
        snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        direction = 'R';
        placeFood();
        running = true;
        score = 0; // Resetowanie wyniku
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(100, this);
        timer.start();
    }

    // Umieszczanie jedzenia na planszy
    private void placeFood() {
        Random random = new Random();
        int x = random.nextInt(BOARD_WIDTH / TILE_SIZE) * TILE_SIZE;
        int y = random.nextInt(BOARD_HEIGHT / TILE_SIZE) * TILE_SIZE;
        food = new Point(x, y);
        isApple = random.nextBoolean(); // Losowo wybiera, czy ma być jabłko, czy banan
    }

    // Ruch węża
    private void move() {
        Point head = new Point(snake.get(0));
        switch (direction) {
            case 'L':
                head.x -= TILE_SIZE;
                break;
            case 'R':
                head.x += TILE_SIZE;
                break;
            case 'U':
                head.y -= TILE_SIZE;
                break;
            case 'D':
                head.y += TILE_SIZE;
                break;
        }
        if (head.equals(food)) {
            snake.add(0, head);
            placeFood();
            score++; // Zwiększanie wyniku
        } else {
            snake.add(0, head);
            snake.remove(snake.size() - 1);
        }
    }

    // Sprawdzanie kolizji
    private void checkCollision() {
        Point head = snake.get(0);
        if (head.x < 0 || head.x >= BOARD_WIDTH || head.y < 0 || head.y >= BOARD_HEIGHT) {
            running = false;
        }
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }
    }

    // Rysowanie komponentów gry
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            if (isApple) {
                g.drawImage(appleImage, food.x, food.y, TILE_SIZE, TILE_SIZE, this);
            } else {
                g.drawImage(bananaImage, food.x, food.y, TILE_SIZE, TILE_SIZE, this);
            }
            for (Point point : snake) {
                g.setColor(Color.GREEN);
                g.fillRect(point.x, point.y, TILE_SIZE, TILE_SIZE);
            }
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 10); // Wyświetlanie wyniku
        } else {
            gameOver(g);
            restartButton.setVisible(true);
            menuButton.setVisible(true);
        }
    }

    // Wyświetlanie komunikatu końca gry
    private void gameOver(Graphics g) {
        String msg = "Game Over";
        Font font = new Font("Helvetica", Font.BOLD, 50);
        FontMetrics metrics = getFontMetrics(font);
        g.setColor(Color.RED);
        g.setFont(font);
        g.drawString(msg, (BOARD_WIDTH - metrics.stringWidth(msg)) / 2, BOARD_HEIGHT / 2);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBounds((BOARD_WIDTH - 200) / 2, (BOARD_HEIGHT - 50) / 2 + 60, 200, 50);
        buttonPanel.setOpaque(false);
        buttonPanel.add(restartButton);
        buttonPanel.add(menuButton);
        this.add(buttonPanel);

        try {
            int highscore = Highscore.readSinglePlayerHighscore();
            if (score > highscore) {
                Highscore.writeSinglePlayerHighscore(score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda obsługi zdarzeń ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollision();
        }
        repaint();
    }

    public boolean isRunning() {
        return running;
    }

    // Metoda główna uruchamiająca grę
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game - Single Player");
        Normal game = new Normal(frame);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
