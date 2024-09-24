import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Random;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class GameObject {
    protected int x, y;
    protected int speedX, speedY;

    public GameObject(int x, int y, int speedX, int speedY) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void move(ArrayList<Asteroid> otherObjects) {
        x += speedX;
        y += speedY;
    }

    public abstract void draw(Graphics g); // วาดวัตถุ

    public boolean checkCollision(GameObject other) {
        int thisSize = 50; // ขนาดของอุกกาบาต
        int otherSize = 50; // ขนาดของวัตถุอื่น (ปรับตามความเหมาะสม)

        return (this.x < other.x + otherSize &&
                this.x + thisSize > other.x &&
                this.y < other.y + otherSize &&
                this.y + thisSize > other.y);
    }

    protected void changeSpeed() {
        Random rand = new Random();
        this.speedX = rand.nextInt(20) - 10; // เปลี่ยนความเร็วในแกน X
        this.speedY = rand.nextInt(20) - 10; // เปลี่ยนความเร็วในแกน Y
    }
}

class Asteroid extends GameObject {
    private Image image;

    public Asteroid(int x, int y, int speedX, int speedY, Image image) {
        super(x, y, speedX, speedY);
        this.image = image;
    }

    @Override
    public void move(ArrayList<Asteroid> otherObjects) {
        super.move(otherObjects); // เรียกใช้เมธอด move ของ GameObject

        // ตรวจสอบการชนกับขอบของเฟรม
        int asteroidSize = 50;

        if (x < 0) {
            x = 0; // ตั้งค่า x เมื่อชนขอบซ้าย
            speedX = -speedX; // เปลี่ยนทิศทาง
            changeSpeed(); // เปลี่ยนความเร็ว
        }
        if (x > 800 - asteroidSize) {
            x = 800 - asteroidSize; // ตั้งค่า x เมื่อชนขอบขวา
            speedX = -speedX; // เปลี่ยนทิศทาง
            changeSpeed(); // เปลี่ยนความเร็ว
        }
        if (y < 0) {
            y = 0; // ตั้งค่า y เมื่อชนขอบบน
            speedY = -speedY; // เปลี่ยนทิศทาง
            changeSpeed(); // เปลี่ยนความเร็ว
        }
        if (y > 600 - asteroidSize) {
            y = 600 - asteroidSize; // ตั้งค่า y เมื่อชนขอบล่าง
            speedY = -speedY; // เปลี่ยนทิศทาง
            changeSpeed(); // เปลี่ยนความเร็ว
        }

        // ตรวจสอบการชนกับอุกกาบาตอื่น
        for (GameObject other : otherObjects) {
            if (other != this && this.checkCollision(other)) {
                // เปลี่ยนทิศทางของอุกกาบาตเมื่อชนกัน
                this.speedX = -this.speedX; // เปลี่ยนทิศทางเมื่อชน
                this.speedY = -this.speedY; // เปลี่ยนทิศทางเมื่อชน
                this.changeSpeed(); // เปลี่ยนความเร็ว

                // อัปเดตตำแหน่ง
                this.x += this.speedX;
                this.y += this.speedY;
                ((Asteroid) other).x += ((Asteroid) other).speedX;
                ((Asteroid) other).y += ((Asteroid) other).speedY;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, 50, 50, null);
    }
}

// คลาสสำหรับเอฟเฟกต์ระเบิด
class Explosion {
    private int x, y;
    private Image[] frames; // อาร์เรย์สำหรับภาพเฟรม
    private int duration; // จำนวนเฟรมที่แสดง
    private int currentFrame; // เฟรมปัจจุบัน

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
        this.frames = new Image[] {
                new ImageIcon("images/bomb.gif").getImage(),
                new ImageIcon("images/bomb.gif").getImage(),
                new ImageIcon("images/bomb.gif").getImage(),
                // เพิ่มเฟรมอื่น ๆ ตามต้องการ
        };
        this.duration = frames.length; // กำหนดจำนวนเฟรมจากอาร์เรย์
        this.currentFrame = 0;
    }

    public void draw(Graphics g) {
        if (currentFrame < duration) {
            g.drawImage(frames[currentFrame], x - 25, y - 25, 100, 100, null); // ปรับขนาดและตำแหน่ง
            currentFrame++;
        }
    }

    public boolean isFinished() {
        return currentFrame >= duration; // เช็คว่าเสร็จสิ้นแล้วหรือไม่
    }
}

class MovementThread extends Thread {
    private Asteroid asteroid;
    private ArrayList<Asteroid> asteroids;

    public MovementThread(Asteroid asteroid, ArrayList<Asteroid> asteroids) {
        this.asteroid = asteroid;
        this.asteroids = asteroids;
    }

    @Override
    public void run() {
        while (true) {
            asteroid.move(asteroids);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class GamePanel extends JPanel {
    private ArrayList<Asteroid> asteroids;
    private ArrayList<Explosion> explosions;
    private Image backgroundImage; // อาร์เรย์สำหรับเอฟเฟกต์ระเบิด
    private Random rand = new Random();

    public GamePanel(int asteroidCount) {
        asteroids = new ArrayList<>();
        explosions = new ArrayList<>(); // สร้างอาร์เรย์สำหรับเอฟเฟกต์ระเบิด
        backgroundImage = new ImageIcon("images/back.jpg").getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);

        for (int i = 0; i < asteroidCount; i++) {
            // สุ่มเลือกภาพจาก 10 รูป
            int randomImageIndex = rand.nextInt(10) + 1; // สุ่มเลข 1-10
            Image asteroidImage = new ImageIcon("images/" + randomImageIndex + ".png").getImage();

            // สุ่มตำแหน่ง
            int x = rand.nextInt(750); // ค่าพิกัด X
            int y = rand.nextInt(550); // ค่าพิกัด Y

            // กำหนดทิศทางการเคลื่อนที่ (4 ทิศทาง)
            int direction = rand.nextInt(4); // 0-3 สำหรับทิศทางทั้ง 4

            int speedX = 0;
            int speedY = 0;

            switch (direction) {
                case 0: // ขึ้น
                    speedX = 0;
                    speedY = -(rand.nextInt(5) + 1); // เคลื่อนที่ขึ้น
                    break;
                case 1: // ลง
                    speedX = 0;
                    speedY = rand.nextInt(5) + 1; // เคลื่อนที่ลง
                    break;
                case 2: // ซ้าย
                    speedX = -(rand.nextInt(5) + 1); // เคลื่อนที่ซ้าย
                    speedY = 0;
                    break;
                case 3: // ขวา
                    speedX = rand.nextInt(5) + 1; // เคลื่อนที่ขวา
                    speedY = 0;
                    break;
            }
            asteroids.add(new Asteroid(x, y, speedX, speedY, asteroidImage));
        }

        for (Asteroid asteroid : asteroids) {
            new MovementThread(asteroid, asteroids).start();
        }

        // เพิ่ม MouseListener สำหรับการดับเบิ้ลคลิก
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) { // ตรวจสอบว่าดับเบิ้ลคลิก
                    for (int i = 0; i < asteroids.size(); i++) {
                        Asteroid asteroid = asteroids.get(i);
                        if (asteroid.checkCollision(new GameObject(e.getX(), e.getY(), 0, 0) {
                            @Override
                            public void draw(Graphics g) {
                                // ไม่จำเป็นต้องทำอะไร
                            }
                        })) {
                            explosions.add(new Explosion(asteroid.x, asteroid.y)); // เพิ่มเอฟเฟกต์ระเบิด
                            asteroids.remove(i); // ลบอุกกาบาตที่ถูกคลิก
                            break; // ออกจากลูป
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // วาดภาพพื้นหลังขนาด 800x600
        g.drawImage(backgroundImage, 0, 0, null); // วาดภาพพื้นหลังที่มีขนาดถูกปรับให้พอดีกับเฟรม

        // วาดอุกกาบาต
        for (Asteroid asteroid : asteroids) {
            asteroid.draw(g);
        }

        // วาดเอฟเฟกต์ระเบิด
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.draw(g);
            if (explosion.isFinished()) {
                explosions.remove(i);
            }
        }
    }
}

class GameMain {
    public static void main(String[] args) {
        // กำหนดจำนวนอุกกาบาตเริ่มต้น
        int nummeteor = 10; // ค่าเริ่มต้นถ้าไม่มีการป้อนค่า

        // ตรวจสอบว่ามีการป้อนค่าใน args หรือไม่
        if (args.length > 0) {
            try {
                nummeteor = Integer.parseInt(args[0]); // แปลงค่าที่ป้อนให้เป็นจำนวนเต็ม
                if (nummeteor < 1) { // ตรวจสอบว่าค่าที่ป้อนไม่ต่ำกว่า 1
                    System.out.println("Please enter a valid number of asteroids (greater than 0).");
                    return; // ออกจากโปรแกรมถ้าค่าผิดพลาด
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid integer.");
                return; // ออกจากโปรแกรมถ้าค่าผิดพลาด
            }
        }

        JFrame frame = new JFrame("Asteroid Game");
        GamePanel gamePanel = new GamePanel(nummeteor); // ส่งจำนวนอุกกาบาตที่ต้องการ
        frame.add(gamePanel);
        frame.setSize(810, 635);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        while (true) {
            gamePanel.repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
