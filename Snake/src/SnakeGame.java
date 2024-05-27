import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 25;
    private final int BOARD_WIDTH = 800;
    private final int BOARD_HEIGHT = 600;
    private final int TOTAL_TILES = (BOARD_WIDTH * BOARD_HEIGHT) / (TILE_SIZE * TILE_SIZE);

    private List<Point> snake;
    private List<Fruit> fruits;
    private List<Obstacle> obstacles;
    private char direction;
    private boolean running;
    private Timer timer;
    private BufferedImage appleImage;
    private BufferedImage bananaImage;

    public SnakeGame() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
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
        initGame();
        loadImages();
    }

    private void loadImages() {
        try {
            appleImage = ImageIO.read(getClass().getResourceAsStream("resources/apple-removebg-preview.png"));
            bananaImage = ImageIO.read(getClass().getResourceAsStream("resources/banana-removebg-preview.png"));
            if (appleImage == null) {
                System.out.println("appleImage not loaded");
            } else {
                System.out.println("appleImage loaded successfully");
            }
            if (bananaImage == null) {
                System.out.println("bananaImage not loaded");
            } else {
                System.out.println("bananaImage loaded successfully");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        snake = new ArrayList<>();
        fruits = new ArrayList<>();
        obstacles = new ArrayList<>();
        snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        direction = 'R';
        placeFruits();
        running = true;
        timer = new Timer(100, this);
        timer.start();
    }

    private void placeFruits() {
        fruits.clear();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(BOARD_WIDTH / TILE_SIZE) * TILE_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / TILE_SIZE) * TILE_SIZE;
            boolean isApple = random.nextBoolean();
            fruits.add(new Fruit(new Point(x, y), isApple));
        }
    }

    private void placeObstacles() {
        obstacles.clear();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            int x = random.nextInt(BOARD_WIDTH / TILE_SIZE) * TILE_SIZE;
            int y = random.nextInt(BOARD_HEIGHT / TILE_SIZE) * TILE_SIZE;
            obstacles.add(new Obstacle(new Point(x, y), randomDirection()));
        }
    }

    private char randomDirection() {
        char[] directions = {'L', 'R', 'U', 'D'};
        Random random = new Random();
        return directions[random.nextInt(directions.length)];
    }

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
        snake.add(0, head);
        for (int i = 0; i < fruits.size(); i++) {
            if (head.equals(fruits.get(i).position)) {
                fruits.remove(i);
                placeFruits();
                return;
            }
        }
        snake.remove(snake.size() - 1);
    }

    private void moveObstacles() {
        for (Obstacle obstacle : obstacles) {
            switch (obstacle.direction) {
                case 'L':
                    obstacle.position.x -= TILE_SIZE;
                    if (obstacle.position.x < 0) obstacle.position.x = BOARD_WIDTH - TILE_SIZE;
                    break;
                case 'R':
                    obstacle.position.x += TILE_SIZE;
                    if (obstacle.position.x >= BOARD_WIDTH) obstacle.position.x = 0;
                    break;
                case 'U':
                    obstacle.position.y -= TILE_SIZE;
                    if (obstacle.position.y < 0) obstacle.position.y = BOARD_HEIGHT - TILE_SIZE;
                    break;
                case 'D':
                    obstacle.position.y += TILE_SIZE;
                    if (obstacle.position.y >= BOARD_HEIGHT) obstacle.position.y = 0;
                    break;
            }
        }
    }

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
        for (Obstacle obstacle : obstacles) {
            if (head.equals(obstacle.position)) {
                running = false;
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            for (Fruit fruit : fruits) {
                if (fruit.isApple) {
                    if (appleImage != null) {
                        g.drawImage(appleImage, fruit.position.x, fruit.position.y, TILE_SIZE, TILE_SIZE, this);
                    } else {
                        System.out.println("appleImage is null");
                    }
                } else {
                    if (bananaImage != null) {
                        g.drawImage(bananaImage, fruit.position.x, fruit.position.y, TILE_SIZE, TILE_SIZE, this);
                    } else {
                        System.out.println("bananaImage is null");
                    }
                }
            }
            g.setColor(Color.RED);
            for (Obstacle obstacle : obstacles) {
                g.fillRect(obstacle.position.x, obstacle.position.y, TILE_SIZE, TILE_SIZE);
            }
            for (Point point : snake) {
                g.setColor(Color.GREEN);
                g.fillRect(point.x, point.y, TILE_SIZE, TILE_SIZE);
            }
        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        String msg = "Game Over";
        Font font = new Font("Helvetica", Font.BOLD, 50);
        FontMetrics metrics = getFontMetrics(font);
        g.setColor(Color.RED);
        g.setFont(font);
        g.drawString(msg, (BOARD_WIDTH - metrics.stringWidth(msg)) / 2, BOARD_HEIGHT / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            moveObstacles();
            checkCollision();
        }
        repaint();
    }

    private static class Fruit {
        Point position;
        boolean isApple;

        Fruit(Point position, boolean isApple) {
            this.position = position;
            this.isApple = isApple;
        }
    }

    private static class Obstacle {
        Point position;
        char direction;

        Obstacle(Point position, char direction) {
            this.position = position;
            this.direction = direction;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.placeObstacles();
    }
}
