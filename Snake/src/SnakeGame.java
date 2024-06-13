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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SnakeGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 25;
    private final int BOARD_WIDTH = 800;
    private final int BOARD_HEIGHT = 600;
    private final int TOTAL_TILES = (BOARD_WIDTH * BOARD_HEIGHT) / (TILE_SIZE * TILE_SIZE);

    private List<Point> snake;
    private List<Point> enemySnake;
    private List<Point> secondEnemySnake;
    private List<Fruit> fruits;
    private List<Obstacle> obstacles;
    private char direction;
    private boolean running;
    private Timer timer;
    private BufferedImage appleImage;
    private BufferedImage bananaImage;
    private int score;
    private int initialDelay = 200; // Zwiększono opóźnienie początkowe
    private int delayDecrease = 5;
    private JButton restartButton;
    private JButton menuButton;
    private boolean playWithComputer;
    private Thread playerThread;
    private Thread enemyThread;
    private Thread secondEnemyThread;
    private SnakeMover playerSnakeMover;
    private SnakeMover enemySnakeMover;
    private SnakeMover secondEnemySnakeMover;
    private JFrame parentFrame;
    private final Lock lock = new ReentrantLock();

    public SnakeGame(boolean playWithComputer, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.playWithComputer = playWithComputer;
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

    private void initRestartButton() {
        restartButton = new JButton("Restart");
        restartButton.setFocusable(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopThreads();
                initGame();
                restartButton.setVisible(false);
                menuButton.setVisible(false);
            }
        });
        restartButton.setVisible(false);
    }

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

    private void loadImages() {
        try {
            appleImage = ImageIO.read(getClass().getResourceAsStream("/resources/apple-removebg-preview.png"));
            bananaImage = ImageIO.read(getClass().getResourceAsStream("/resources/banana-removebg-preview.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        snake = new ArrayList<>();
        enemySnake = new ArrayList<>();
        secondEnemySnake = new ArrayList<>();
        fruits = new ArrayList<>();
        obstacles = new ArrayList<>();
        snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        if (playWithComputer) {
            enemySnake.add(new Point(BOARD_WIDTH / 4, BOARD_HEIGHT / 4));
            secondEnemySnake.add(new Point(3 * BOARD_WIDTH / 4, 3 * BOARD_HEIGHT / 4));
        }
        direction = 'R';
        placeFruits();
        placeObstacles();
        running = true;
        score = 0; // Resetowanie wyniku
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(initialDelay, this);
        timer.start();

        int snakeDelay = initialDelay; // Ustawienie początkowego opóźnienia dla wszystkich węży

        playerSnakeMover = new SnakeMover(snake, this::move, snakeDelay);
        enemySnakeMover = new SnakeMover(enemySnake, this::moveEnemy, snakeDelay);
        secondEnemySnakeMover = new SnakeMover(secondEnemySnake, this::moveSecondEnemy, snakeDelay);

        playerThread = new Thread(playerSnakeMover);
        enemyThread = new Thread(enemySnakeMover);
        secondEnemyThread = new Thread(secondEnemySnakeMover);

        playerThread.start();
        enemyThread.start();
        secondEnemyThread.start();
    }

    private void stopThreads() {
        if (playerSnakeMover != null) {
            playerSnakeMover.stop();
        }
        if (enemySnakeMover != null) {
            enemySnakeMover.stop();
        }
        if (secondEnemySnakeMover != null) {
            secondEnemySnakeMover.stop();
        }
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
        lock.lock();
        try {
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
                    int newDelay = Math.max(200, initialDelay - score * delayDecrease); // Zmniejszono minimalne opóźnienie
                    timer.setDelay(newDelay);
                    playerSnakeMover.setDelay(newDelay);
                    enemySnakeMover.setDelay(newDelay);
                    secondEnemySnakeMover.setDelay(newDelay);
                    return;
                }
            }
            snake.remove(snake.size() - 1); // Usuń ostatni element tylko raz
        } finally {
            lock.unlock();
        }
    }

    private void moveObstacles() {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    private void moveEnemy() {
        moveSingleEnemy(enemySnake, findNearestFruit(enemySnake.get(0)));
    }

    private void moveSecondEnemy() {
        if (secondEnemySnake.isEmpty()) return;

        Point head = new Point(secondEnemySnake.get(0));
        Point nearestFruit = isFruitNearby(head, 5);

        if (nearestFruit != null) {
            moveSingleEnemy(secondEnemySnake, nearestFruit);
        } else {
            moveTowardsPlayer(secondEnemySnake);
        }
    }

    private void moveSingleEnemy(List<Point> enemy, Point target) {
        lock.lock();
        try {
            if (enemy.isEmpty()) return;

            Point head = new Point(enemy.get(0));
            if (target == null) return;

            int dx = target.x - head.x;
            int dy = target.y - head.y;

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

            enemy.add(0, head);
            for (int i = 0; i < fruits.size(); i++) {
                if (head.equals(fruits.get(i).position)) {
                    fruits.remove(i);
                    placeFruits();
                    return;
                }
            }
            enemy.remove(enemy.size() - 1);
        } finally {
            lock.unlock();
        }
    }

    private Point isFruitNearby(Point head, int distance) {
        lock.lock();
        try {
            for (Fruit fruit : fruits) {
                if (Math.abs(head.x - fruit.position.x) <= distance * TILE_SIZE &&
                        Math.abs(head.y - fruit.position.y) <= distance * TILE_SIZE) {
                    return fruit.position;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    private void moveTowardsPlayer(List<Point> enemy) {
        lock.lock();
        try {
            if (enemy.isEmpty()) return;

            Point head = new Point(enemy.get(0));
            Point target = snake.get(0); // target is the head of the player's snake

            int dx = target.x - head.x;
            int dy = target.y - head.y;

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

            enemy.add(0, head);
            enemy.remove(enemy.size() - 1);
        } finally {
            lock.unlock();
        }
    }

    private Point findNearestFruit(Point head) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    private boolean isSafe(Point p) {
        lock.lock();
        try {
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
            for (Point part : secondEnemySnake) {
                if (p.equals(part)) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void checkCollision() {
        lock.lock();
        try {
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
            for (Point part : secondEnemySnake) {
                if (head.equals(part)) {
                    running = false;
                    break;
                }
            }

            Point enemyHead = enemySnake.get(0);
            for (int i = 1; i < enemySnake.size(); i++) {
                if (enemyHead.equals(enemySnake.get(i))) {
                    running = false;
                    break;
                }
            }

            for (Obstacle obstacle : obstacles) {
                if (enemyHead.equals(obstacle.position)) {
                    running = false;
                    break;
                }
            }

            for (Point part : snake) {
                if (enemyHead.equals(part)) {
                    running = false;
                    break;
                }
            }

            Point secondEnemyHead = secondEnemySnake.get(0);
            for (int i = 1; i < secondEnemySnake.size(); i++) {
                if (secondEnemyHead.equals(secondEnemySnake.get(i))) {
                    running = false;
                    break;
                }
            }

            for (Obstacle obstacle : obstacles) {
                if (secondEnemyHead.equals(obstacle.position)) {
                    running = false;
                    break;
                }
            }

            for (Point part : snake) {
                if (secondEnemyHead.equals(part)) {
                    running = false;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            lock.lock();
            try {
                for (Fruit fruit : fruits) {
                    if (fruit.isApple) {
                        g.drawImage(appleImage, fruit.position.x, fruit.position.y, TILE_SIZE, TILE_SIZE, this);
                    } else {
                        g.drawImage(bananaImage, fruit.position.x, fruit.position.y, TILE_SIZE, TILE_SIZE, this);
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
                if (playWithComputer) {
                    g.setColor(Color.BLUE);
                    for (Point point : enemySnake) {
                        g.fillRect(point.x, point.y, TILE_SIZE, TILE_SIZE);
                    }
                    g.setColor(Color.MAGENTA);
                    for (Point point : secondEnemySnake) {
                        g.fillRect(point.x, point.y, TILE_SIZE, TILE_SIZE);
                    }
                }
                g.setColor(Color.WHITE);
                g.drawString("Score: " + score, 10, 10);
            } finally {
                lock.unlock();
            }
        } else {
            gameOver(g);
            restartButton.setVisible(true);
            menuButton.setVisible(true);
        }
    }

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
            int highscore = Highscore.readPlayWithComputerHighscore();
            if (score > highscore) {
                Highscore.writePlayWithComputerHighscore(score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // usunięto wywołanie funkcji move, aby uniknąć podwójnego ruchu
        if (running) {
            // Sprawdzanie kolizji i ruch przeszkód
            if (playWithComputer) {
                moveObstacles();
                checkCollision();
            }
        }
        repaint();
    }

    public boolean isRunning() {
        return running;
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

    private static class SnakeMover implements Runnable {
        private final List<Point> snake;
        private final Runnable moveMethod;
        private volatile boolean running = true;
        private int delay;

        SnakeMover(List<Point> snake, Runnable moveMethod, int delay) {
            this.snake = snake;
            this.moveMethod = moveMethod;
            this.delay = delay;
        }

        void setDelay(int delay) {
            this.delay = delay;
        }

        void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    running = false; // Exit the loop if interrupted
                }
                moveMethod.run();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game - Play with Computer");
        SnakeGame game = new SnakeGame(true, frame);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
