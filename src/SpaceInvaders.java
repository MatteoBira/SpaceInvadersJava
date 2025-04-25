import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_WIDTH = 70;
    private static final int PLAYER_HEIGHT = 60;
    private static final int ALIEN_SIZE = 40;
    private static final int BULLET_WIDTH = 15;
    private static final int BULLET_HEIGHT = 10;
    private static double ALIEN_SPEED = 1;
    private static final int BULLET_COOLDOWN = 350;

    private int playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
    private int playerY = HEIGHT - PLAYER_HEIGHT - 20;
    public int lives = 2;
    private ArrayList<Rectangle> playerBullets = new ArrayList<>();
    private ArrayList<Rectangle> alienBullets = new ArrayList<>();
    private Rectangle[][] aliens = new Rectangle[10][5];

    private Timer timer;
    private Timer alienShootTimer;
    private int score = 0;
    private int highScore = 0;
    private int alienMoveCounter = 0;
    private boolean movingRight = true;
    private long lastBulletTime = 0;
    private int deadAliens = 0;

    private Image playerImage;
    private Image alienImage1;
    private Image alienImage2;
    private Image alienImage3;
    private Image bulletImage;
    private Image backgroundImage;

    public SpaceInvaders() {
        loadHighScore();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
        setFocusable(true);

        playerImage = new ImageIcon("Immagini/navicella2.png").getImage().getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_SMOOTH);
        alienImage1 = new ImageIcon("Immagini/Alieni/alieno1.png").getImage().getScaledInstance(ALIEN_SIZE, ALIEN_SIZE, Image.SCALE_SMOOTH);
        alienImage2 = new ImageIcon("Immagini/Alieni/alieno2.png").getImage().getScaledInstance(ALIEN_SIZE, ALIEN_SIZE, Image.SCALE_SMOOTH);
        alienImage3 = new ImageIcon("Immagini/Alieni/alieno3.png").getImage().getScaledInstance(ALIEN_SIZE, ALIEN_SIZE, Image.SCALE_SMOOTH);
        bulletImage = new ImageIcon("Immagini/bullet.png").getImage().getScaledInstance(BULLET_WIDTH * 3, BULLET_HEIGHT * 3, Image.SCALE_SMOOTH);
        backgroundImage = new ImageIcon("Immagini/spazio.png").getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5; j++) {
                aliens[i][j] = new Rectangle(100 + i * (ALIEN_SIZE + 10), 50 + j * (ALIEN_SIZE + 10), ALIEN_SIZE, ALIEN_SIZE);
            }
        }

        timer = new Timer(10, this);
        timer.start();

        alienShootTimer = new Timer(1000, e -> alienShooting());
        alienShootTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveBullets();
        moveAliens();
        checkCollisions();
        repaint();
    }

    private void moveBullets() {
        for (int i = 0; i < playerBullets.size(); i++) {
            Rectangle bullet = playerBullets.get(i);
            bullet.y -= 5;
            if (bullet.y < 0) {
                playerBullets.remove(i);
                i--;
            }
        }

        for (int i = 0; i < alienBullets.size(); i++) {
            Rectangle bullet = alienBullets.get(i);
            bullet.y += 3;
            if (bullet.y > HEIGHT) {
                alienBullets.remove(i);
                i--;
            }
        }
    }

    private void moveAliens() {
        if (alienMoveCounter % 2 == 0) {
            boolean changeDirection = false;

            for (int i = 0; i < aliens.length; i++) {
                for (int j = 0; j < aliens[i].length; j++) {
                    Rectangle alien = aliens[i][j];
                    if (alien != null) {
                        if (movingRight) {
                            alien.x += ALIEN_SPEED;
                            if (alien.x + ALIEN_SIZE >= WIDTH) {
                                changeDirection = true;
                            }
                        } else {
                            alien.x -= ALIEN_SPEED;
                            if (alien.x <= 0) {
                                changeDirection = true;
                            }
                        }
                    }
                }
            }

            if (changeDirection) {
                movingRight = !movingRight;
                for (int i = 0; i < aliens.length; i++) {
                    for (int j = 0; j < aliens[i].length; j++) {
                        if (aliens[i][j] != null) {
                            aliens[i][j].y += ALIEN_SIZE;
                        }
                    }
                }
            }
        }
        alienMoveCounter++;
    }

    private void alienShooting() {
        List<Rectangle> shootableAliens = new ArrayList<>();

        for (int i = 0; i < aliens.length; i++) {
            for (int j = 0; j < aliens[i].length; j++) {
                Rectangle alien = aliens[i][j];
                if (alien != null) {
                    boolean canShoot = true;
                    for (int k = j + 1; k < aliens[i].length; k++) {
                        if (aliens[i][k] != null) {
                            canShoot = false;
                            break;
                        }
                    }
                    if (canShoot) {
                        shootableAliens.add(alien);
                    }
                }
            }
        }

        if (!shootableAliens.isEmpty()) {
            Rectangle selectedAlien = shootableAliens.get(new Random().nextInt(shootableAliens.size()));
            alienBullets.add(new Rectangle(selectedAlien.x + ALIEN_SIZE / 2 - BULLET_WIDTH / 2, selectedAlien.y + ALIEN_SIZE, BULLET_WIDTH, BULLET_HEIGHT));
        }
    }

    private void checkCollisions() {
        ArrayList<Rectangle> toRemovePlayerBullets = new ArrayList<>();

        for (Rectangle bullet : playerBullets) {
            for (int i = 0; i < aliens.length; i++) {
                for (int j = 0; j < aliens[i].length; j++) {
                    Rectangle alien = aliens[i][j];
                    if (alien != null && bullet.intersects(alien)) {
                        toRemovePlayerBullets.add(bullet);
                        aliens[i][j] = null;
                        score += 10;
                        deadAliens++;
                        if (deadAliens % 10 == 0) {
                            ALIEN_SPEED += 0.5;
                        }
                        break;
                    }
                }
            }
        }

        for (Rectangle bullet : alienBullets) {
            Polygon playerTriangle = new Polygon(
                    new int[]{playerX + PLAYER_WIDTH / 2, playerX + 2, playerX + PLAYER_WIDTH - 5},
                    new int[]{playerY, playerY + PLAYER_HEIGHT, playerY + PLAYER_HEIGHT},
                    3
            );
            if (playerTriangle.intersects(bullet)) {
                if(lives == 0)
                {
                    System.out.println("dio cane");
                    gameOver();
                }else{
                    alienBullets.remove(bullet);
                    System.out.println("meno una vita");
                    lives--;
                }
                return;
            }
        }

        playerBullets.removeAll(toRemovePlayerBullets);

        for (Rectangle[] alienRow : aliens) {
            for(Rectangle alien  : alienRow){
                Polygon playerTriangle = new Polygon(
                        new int[]{playerX + PLAYER_WIDTH / 2, playerX + 2, playerX + PLAYER_WIDTH - 5},
                        new int[]{playerY, playerY + PLAYER_HEIGHT, playerY + PLAYER_HEIGHT},
                        3
                );
                try{
                    if (playerTriangle.intersects(alien)) {
                    gameOver();
                    return;

                    }
                }catch (NullPointerException e){};

            }
        }
    }

    private void gameOver() {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over\nHai realizzato: " + score + " punti", "Game Over", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage, 0, 0, this);

        g.drawImage(playerImage, playerX, playerY, this);

        for (Rectangle bullet : playerBullets) {
            g.drawImage(bulletImage, bullet.x - 5, bullet.y - 5, this);
        }

        for (Rectangle bullet : alienBullets) {
            g.drawImage(bulletImage, bullet.x - 5, bullet.y - 5, this);
        }

        for (int i = 0; i < aliens.length; i++) {
            for (int j = 0; j < aliens[i].length; j++) {
                Rectangle alien = aliens[i][j];
                if (alien != null) {
                    if (j >= 3) {
                        g.drawImage(alienImage1, alien.x, alien.y, this);
                    } else if (j < 3 && j >= 1) {
                        g.drawImage(alienImage2, alien.x, alien.y, this);
                    } else {
                        g.drawImage(alienImage3, alien.x, alien.y, this);
                    }
                }
            }
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("High Score: " + highScore, WIDTH - 150, 20);
        g.drawString("Lives: " + (lives + 1), WIDTH / 2, 20);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= 10;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += 10;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            shootBullet();
        }
    }

    private void shootBullet() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBulletTime >= BULLET_COOLDOWN) {
            playerBullets.add(new Rectangle(playerX + PLAYER_WIDTH / 3 - BULLET_WIDTH / 2, playerY, BULLET_WIDTH, BULLET_HEIGHT));
            lastBulletTime = currentTime;
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders");
        SpaceInvaders game = new SpaceInvaders();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
