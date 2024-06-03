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
    private List<Point> enemySnake;
    private List<Fruit> fruits;
    private List<Obstacle> obstacles;
    private char direction;
    private boolean running;
    private Timer timer;
    private BufferedImage appleImage;
    private BufferedImage bananaImage;
    private int score;
    private int initialDelay = 200; // Initial delay for slow mode
    private int delayDecrease = 5;  // Amount to decrease delay per point

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
        enemySnake = new ArrayList<>();
        fruits = new ArrayList<>();
        obstacles = new ArrayList<>();
        snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        enemySnake.add(new Point(BOARD_WIDTH / 4, BOARD_HEIGHT / 4)); // Initialize enemy snake
        direction = 'R';
        placeFruits();
        placeObstacles();
        running = true;
        timer = new Timer(initialDelay, this);
        timer.start();
        score = 0;
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
                score++;
                int newDelay = Math.max(50, initialDelay - score * delayDecrease); // Speed up the game
                timer.setDelay(newDelay);
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

    private void moveEnemy() {
        if (enemySnake.isEmpty()) return;

        Point head = new Point(enemySnake.get(0));
        Point nearestFruit = findNearestFruit(head);

        if (nearestFruit == null) {
            return;
        }

        // Determine the direction based on the nearest fruit
        int dx = nearestFruit.x - head.x;
        int dy = nearestFruit.y - head.y;

        // Priority order of directions
        char[] directions = new char[4];
        if (Math.abs(dx) > Math.abs(dy)) {
            directions[0] = dx > 0 ? 'R' : 'L';
            directions[1] = dy > 0 ? 'D' : 'U';
            directions[2] = dy > 0 ? 'U' : 'D';
            directions[3] = dx > 0 ? 'L' : 'R';
        } else {
            directions[0] = dy > 0 ? 'D' : 'U';
            directions[1] = dx > 0 ? 'R' : 'L';
            directions[2] = dx > 0 ? 'L' : 'R';
            directions[3] = dy > 0 ? 'U' : 'D';
        }

        // Find a safe direction
        for (char dir : directions) {
            Point newHead = new Point(head);
            switch (dir) {
                case 'L':
                    newHead.x -= TILE_SIZE;
                    break;
                case 'R':
                    newHead.x += TILE_SIZE;
                    break;
                case 'U':
                    newHead.y -= TILE_SIZE;
                    break;
                case 'D':
                    newHead.y += TILE_SIZE;
                    break;
            }
            if (isSafe(newHead)) {
                head = newHead;
                break;
            }
        }

        enemySnake.add(0, head);
        for (int i = 0; i < fruits.size(); i++) {
            if (head.equals(fruits.get(i).position)) {
                fruits.remove(i);
                placeFruits();
                return;
            }
        }
        enemySnake.remove(enemySnake.size() - 1);
    }

    private Point findNearestFruit(Point head) {
        Point nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Fruit fruit : fruits) {
            double dist = head.distance(fruit.position);
            if (dist < minDist) {
                minDist = dist;
                nearest = fruit.position;
            }
        }
        return nearest;
    }

    private boolean isSafe(Point p) {
        for (Obstacle obstacle : obstacles) {
            if (p.equals(obstacle.position)) {
                return false;
            }
        }
        for (Point part : snake) {
            if (p.equals(part)) {
                return false;
            }
        }
        for (Point part : enemySnake) {
            if (p.equals(part)) {
                return false;
            }
        }
        return true;
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
        for (Point part : enemySnake) {
            if (head.equals(part)) {
                running = false;
                break;
            }
        }

        // Check for collision of enemy snake with itself
        Point enemyHead = enemySnake.get(0);
        for (int i = 1; i < enemySnake.size(); i++) {
            if (enemyHead.equals(enemySnake.get(i))) {
                running = false;
                break;
            }
        }

        // Check for collision of enemy snake with obstacles
        for (Obstacle obstacle : obstacles) {
            if (enemyHead.equals(obstacle.position)) {
                running = false;
                break;
            }
        }

        // Check for collision of enemy snake with main snake
        for (Point part : snake) {
            if (enemyHead.equals(part)) {
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
            g.setColor(Color.BLUE);
            for (Point point : enemySnake) {
                g.fillRect(point.x, point.y, TILE_SIZE, TILE_SIZE);
            }
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 10);
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
            moveEnemy();
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
