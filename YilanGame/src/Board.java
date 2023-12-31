import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

// Board sınıfı, JPanel sınıfından türetiliyor ve ActionListener arayüzünü uyguluyor.
// Bu, Board'un bir grafik bileşen olduğu ve zamanlayıcıdan gelen olaylara yanıt verebileceği anlamına gelir.
public class Board extends JPanel implements ActionListener {

    private final int B_WIDTH = 300; // Oyun alanının genişliği (piksel cinsinden)
    private final int B_HEIGHT = 300; // Oyun alanının yüksekliği (piksel cinsinden)
    private final int DOT_SIZE = 10; // Yılanın her bir parçasının boyutu (piksel cinsinden)
    private final int ALL_DOTS = 900; // Yılanın maksimum uzunluğu (parça cinsinden)
    private final int RAND_POS = 19; // Yemin rastgele konumlandırılması için kullanılan sabit
    private final int DELAY = 100; // Zamanlayıcının gecikme süresi (milisaniye cinsinden)
    private int score;//BU SATIRI DAVUT EKLEDİ.

    private final int x[] = new int[ALL_DOTS]; // Yılanın x koordinatlarını tutan dizi
    private final int y[] = new int[ALL_DOTS]; // Yılanın y koordinatlarını tutan dizi

    private int dots; // Yılanın mevcut uzunluğu (parça cinsinden)
    private int apple_x; // Yemin x koordinatı
    private int apple_y; // Yemin y koordinatı

    private int pineApple_x; // Ananasın x koordinatı
    private int pineApple_y; // Ananasın y koordinatı

    private  int bomb_x;
    private int bomb_y;
    private boolean leftDirection = false; // Yılanın sola dönüş durumu
    private boolean rightDirection = true; // Yılanın sağa dönüş durumu
    private boolean upDirection = false; // Yılanın yukarı dönüş durumu
    private boolean downDirection = false; // Yılanın aşağı dönüş durumu
    private boolean inGame = true; // Oyunun devam etme durumu

    private Timer timer; // Zamanlayıcı nesnesi
    private Image ball; // Yılanın her bir parçasının resmi
    private Image apple; // Yemin resmi

    private Image pineApple; // Ananasın resmi
    private Image head; // Yılanın başının resmi
    private Image bgImage;//BU SATIRI DAVUT EKLEDİ.
    private Image bomb;
    private int bytesRead;

    public Board() { // Board sınıfının kurucu metodu

        initBoard(); // Oyun alanını başlatan metodu çağırır
    }
    private void initBoard() { // Oyun alanını başlatan metod

        this.addKeyListener(new TAdapter()); // Klavye kontrolleri için bir dinleyici ekler
        this.setFocusable(true); // Klavye kontrolleri için odaklanabilirliği açar
        this.setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT)); // Oyun alanının boyutunu ayarlar
        this.loadImages(); // Resimleri yükleyen metodu çağırır
        this.initGame(); // Oyunu başlatan metodu çağırır
        startScreen();
    }
    public void startScreen() {
        int result = JOptionPane.showOptionDialog(null,
                " Yılan Oyununa hoş geldiniz :) \n",
                "Giriş Ekranı",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"Oyuna Başla"},
                "Oyuna Başla");

        if (result == JOptionPane.YES_OPTION) {
            // Eğer kullanıcı "Oyuna Başla" derse, oyun başlasın
            timer.start();
        } else {
            // Eğer kullanıcı pencereyi kapatırsa, program kapatılır
            System.exit(0);
        }
    }
    private void gameOver(Graphics g) { // Oyun bittiğini gösteren metot


        String msg = "KAYBETTİNİZ :("   ; // Oyun bittiğini belirten mesaj
        String m_score = "PUANINIZ: "+ score;
        Font small = new Font("İtalic", Font.BOLD, 20); // Mesajın fontu
        FontMetrics metr = getFontMetrics(small); // Mesajın metrikleri

        g.setColor(Color.black); // Mesajın rengi
        g.setFont(small); // Mesajın fontu
        g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2); // Mesajı ekrana yazar
        g.drawString(m_score, (B_WIDTH - metr.stringWidth(m_score)) / 2, (B_HEIGHT / 2)+20); // Mesajı ekrana yazar

    }
    /*--------------------------------------------*/
    private void initGame() { // Oyunu başlatan metot
        score = 0; //BU SATIRI DAVUT EKLEDİ
        dots = 4; // Yılanın başlangıç uzunluğunu ayarlar

        for (int z = 0; z < dots; z++) { // Yılanın başlangıç konumunu belirler
            x[z] = 50 - z * 10; // Yılanın x koordinatlarını sırayla azaltır
            y[z] = 50; // Yılanın y koordinatlarını sabit tutar
        }

        locateApple(); // Yemin konumunu belirleyen metodu çağırır
        locatePineApple();
        locateBomb();
        // Timer'ı sadece locatePineApple metodu için ekliyoruz.
        Timer pineAppleTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inGame) {
                    locatePineApple();
                                    }
            }
        });
        pineAppleTimer.start();

        Timer bombTimer = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inGame) {
                    locateBomb();
                }
            }
        });
        bombTimer.start();


        timer = new Timer(DELAY, this); // Zamanlayıcı nesnesini oluşturur ve gecikme süresi ve dinleyici olarak Board sınıfını verir
        timer.start(); // Zamanlayıcıyı başlatır
    }
    @Override
    public void paintComponent(Graphics g) { // Oyun alanını çizen metot
        super.paintComponent(g); // Üst sınıfın metodunu çağırır
        g.drawImage(bgImage, 0, 0,getWidth(),getHeight(),this);     //BU SATIRI DAVUT EKLEDİ
        doDrawing(g); // Oyun alanını çizen yardımcı metodu çağırır
    }
    private void doDrawing(Graphics g) { // Oyun alanını çizen yardımcı metot

        if (inGame) { // Eğer oyun devam ediyorsa

            g.drawImage(apple, apple_x, apple_y, this); // Yemin resmini yeminin konumunda çizer

            g.drawImage(pineApple, pineApple_x, pineApple_y, this); // Yemin resmini yeminin konumunda çizer

            g.drawImage(bomb, bomb_x, bomb_y, this); // Yemin resmini yeminin konumunda çizer

            String msg = "Skor: " + score; // Skoru gösteren mesaj
            Font small = new Font("Helvetica", Font.BOLD, 14); // Mesajın fontu
            FontMetrics metr = getFontMetrics(small); // Mesajın metrikleri
            g.setColor(Color.white); // Mesajın rengi
            g.setFont(small); // Mesajın fontu
            g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) - 5, 15); // Mesajı ekrana yazar

            for (int z = 0; z < dots; z++) { // Yılanın her bir parçası için
                if (z == 0) { // Eğer ilk parça ise
                    g.drawImage(head, x[z], y[z], this); // Yılanın başının resmini çizer
                } else { // Eğer ilk parça değilse
                    g.drawImage(ball, x[z], y[z], this); // Yılanın diğer parçalarının resmini çizer
                }
            }

            Toolkit.getDefaultToolkit().sync(); // Grafik sistemi ile senkronize eder

        } else { // Eğer oyun bitmişse

            gameOver(g); // Oyun bittiğini gösteren metodu çağırır
        }
    }
    private void move() { // Yılanın hareket etmesini sağlayan metot

        for (int z = dots; z > 0; z--) { // Yılanın parçalarının koordinatlarını günceller
            x[z] = x[(z - 1)]; // Bir sonraki parçanın x koordinatını alır
            y[z] = y[(z - 1)]; // Bir sonraki parçanın y koordinatını alır
        }

        if (leftDirection) { // Eğer yılan sola dönüyorsa
            x[0] -= DOT_SIZE; // Başının x koordinatını azaltır
        }

        if (rightDirection) { // Eğer yılan sağa dönüyorsa
            x[0] += DOT_SIZE; // Başının x koordinatını arttırır
        }

        if (upDirection) { // Eğer yılan yukarı dönüyorsa
            y[0] -= DOT_SIZE; // Başının y koordinatını azaltır
        }

        if (downDirection) { // Eğer yılan aşağı dönüyorsa
            y[0] += DOT_SIZE; // Başının y koordinatını arttırır
        }
    }
    private void checkCollision() { // Yılanın kendisine veya duvarlara çarpmasını kontrol eden metot

        for (int z = dots; z > 0; z--) { // Yılanın her bir parçası için

            if ((z > 4) && (x[0] == x[z]) && (y[0] == y[z])) { // Eğer baş, vücudun bir parçasına çarpmışsa
                inGame = false;// Oyunu bitirir
                playSound("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/bomba.wav");

            }
        }

        if (y[0] >= B_HEIGHT) { // Eğer baş, alt duvara çarpmışsa
            y[0] = 0; // Başın y koordinatını sıfırlar
        }

        if (y[0] < 0) { // Eğer baş, üst duvara çarpmışsa
            y[0] = B_HEIGHT; // Başın y koordinatını oyun alanının yüksekliği yapar
        }

        if (x[0] >= B_WIDTH) { // Eğer baş, sağ duvara çarpmışsa
            x[0] = 0; // Başın x koordinatını sıfırlar
        }

        if (x[0] < 0) { // Eğer baş, sol duvara çarpmışsa
            x[0] = B_WIDTH; // Başın x koordinatını oyun alanının genişliği yapar
        }

        if (!inGame) { // Eğer oyun bitmişse
            timer.stop(); // Zamanlayıcıyı durdurur
        }
    }
    /*--------------------------------------------*/
    private void loadImages() { // Resimleri yükleyen metod

        ImageIcon iib= new ImageIcon("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources\\background.jpg");
        bgImage= iib.getImage();


        ImageIcon iid = new ImageIcon("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/dot.png"); // Yılanın her bir parçasının resmini yükler
        ball = iid.getImage();

        ImageIcon iia = new ImageIcon("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/appleV2.jpg"); // Yemin resmini yükler
        apple = iia.getImage();

        ImageIcon iip = new ImageIcon("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/pineAppleV2.png"); // Yemin resmini yükler
        pineApple = iip.getImage();

        ImageIcon iih = new ImageIcon("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/head.png"); // Yılanın başının resmini yükler
        head = iih.getImage();

        ImageIcon iibomb = new ImageIcon("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/bombv2.jpg"); // Yemin resmini yükler
        bomb = iibomb.getImage();

    }
    private void locateApple() { // Yemin konumunu rastgele belirleyen metot

        int r = (int) (Math.random() * RAND_POS); // 0 ile RAND_POS arasında rastgele bir sayı üretir
        apple_x = ((r * DOT_SIZE)); // Yemin x koordinatını, yılanın boyutuna göre ayarlar

        r = (int) (Math.random() * RAND_POS); // 0 ile RAND_POS arasında rastgele bir sayı üretir
        apple_y = ((r * DOT_SIZE)); // Yemin y koordinatını, yılanın boyutuna göre ayarlar
    }
    private void locatePineApple() { // Yemin konumunu rastgele belirleyen metot

        int n = (int) (Math.random() * RAND_POS); // 0 ile RAND_POS arasında rastgele bir sayı üretir
        pineApple_x = ((n * DOT_SIZE)); // Yemin x koordinatını, yılanın boyutuna göre ayarlar

        n = (int) (Math.random() * RAND_POS); // 0 ile RAND_POS arasında rastgele bir sayı üretir
        pineApple_y = ((n * DOT_SIZE)); // Yemin y koordinatını, yılanın boyutuna göre ayarlar
    }
    private void locateBomb() { // Yemin konumunu rastgele belirleyen metot

        int n = (int) (Math.random() * RAND_POS); // 0 ile RAND_POS arasında rastgele bir sayı üretir
        bomb_x = ((n * DOT_SIZE)); // Yemin x koordinatını, yılanın boyutuna göre ayarlar

        n = (int) (Math.random() * RAND_POS); // 0 ile RAND_POS arasında rastgele bir sayı üretir
        bomb_y = ((n * DOT_SIZE)); // Yemin y koordinatını, yılanın boyutuna göre ayarlar
    }
    private void checkApple() { // Yılanın yemi yiyip yemediğini kontrol eden metot

        if ((x[0] == apple_x) && (y[0] == apple_y)) { // Eğer yemi yemişse

            dots++; // Yılanın uzunluğunu arttırır
            score += 10; // Yılan her yem yediğinde skoru 10 puan arttırır
            locateApple(); // Yeni bir yem konumu belirler

            playSound("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/yemeSesi.wav");
        }
    }
    private void checkPineApple() { // Yılanın yemi yiyip yemediğini kontrol eden metot

        if ((x[0] == pineApple_x) && (y[0] == pineApple_y)) { // Eğer yemi yemişse

            dots+=3; // Yılanın uzunluğunu arttırır
            score +=30;    //BU SATIRI DAVUT EKLEDİ
            locatePineApple(); // Yeni bir yem konumu belirler
            playSound("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/yemeSesi.wav");
        }
    }
    private void checkBomb() { // Yılanın yemi yiyip yemediğini kontrol eden metot
        if ((x[0] == bomb_x) && (y[0] == bomb_y)) { // Eğer yemi yemişse
            inGame =false;
            locatePineApple(); // Yeni bir yem konumu belirler
            playSound("C:\\Users\\Ahmet\\Desktop\\Drive\\2023GrafikYilan\\YilanGame\\src\\resources/bomba.wav");
        }
    }
    /*--------------------------------------------*/
    private void playSound(String s) { // Ses dosyasını oynatan metot

        try {
            File soundFile = new File(s); // Ses dosyasını açar
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile); // Ses dosyasını AudioInputStream nesnesine dönüştürür
            AudioFormat format = audioIn.getFormat(); // Ses dosyasının formatını alır
            SourceDataLine line = AudioSystem.getSourceDataLine(format); // Ses dosyasının formatına uygun bir SourceDataLine nesnesi alır
            line.open(format); // SourceDataLine nesnesini açar
            line.start(); // SourceDataLine nesnesini başlatır
            byte[] buffer = new byte[1024]; // Ses dosyasının verilerini tutmak için bir byte dizisi oluşturur
            //int bytesRead = 0; // Okunan byte sayısını tutmak için bir değişken tanımlar

            // Ses dosyasını oynatmak için yeni bir Thread oluşturur
            new Thread(() -> {
                try {
                    while ((bytesRead = audioIn.read(buffer, 0, buffer.length)) != -1) { // AudioInputStream nesnesinden veri okur
                        line.write(buffer, 0, bytesRead); // Okunan veriyi SourceDataLine nesnesine yazar
                    }
                    line.drain(); // SourceDataLine nesnesinin tamponunu boşaltır
                    line.close(); // SourceDataLine nesnesini kapatır
                    audioIn.close(); // AudioInputStream nesnesini serbest bırakır
                } catch (Exception e) {
                    e.printStackTrace(); // Hata olursa ekrana yazdırır
                }
            }).start(); // Thread'i başlatır

        } catch (Exception e) {
            e.printStackTrace(); // Hata olursa ekrana yazdırır
        }
    }
    /*--------------------------------------------*/
    @Override
    public void actionPerformed(ActionEvent e) { // Zamanlayıcıdan gelen olaylara yanıt veren metot

        if (inGame) { // Eğer oyun devam ediyorsa

            checkApple(); // Yılanın yemi yiyip yemediğini kontrol eden metodu çağırır
            checkPineApple();
            checkBomb();
            checkCollision(); // Yılanın kendisine veya duvarlara çarpmasını kontrol eden metodu çağırır
            move(); // Yılanın hareket etmesini sağlayan metodu çağırır
        }

        repaint(); // Oyun alanını yeniden çizen metodu çağırır
    }
    /*--------------------------------------------*/

    private class TAdapter extends KeyAdapter { // Klavye kontrollerini sağlayan iç sınıf

        @Override
        public void keyPressed(KeyEvent e) { // Klavyeden basılan tuşlara göre yılanın yönünü değiştiren metot

            int key = e.getKeyCode(); // Basılan tuşun kodunu alır

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) { // Eğer sol ok tuşu basılmışsa ve yılan sağa gitmiyorsa
                leftDirection = true; // Yılanın sola dönüş durumunu açar
                upDirection = false; // Yılanın yukarı dönüş durumunu kapatır
                downDirection = false; // Yılanın aşağı dönüş durumunu kapatır
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) { // Eğer sağ ok tuşu basılmışsa ve yılan sola gitmiyorsa
                rightDirection = true; // Yılanın sağa dönüş durumunu açar
                upDirection = false; // Yılanın yukarı dönüş durumunu kapatır
                downDirection = false; // Yılanın aşağı dönüş durumunu kapatır
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) { // Eğer yukarı ok tuşu basılmışsa ve yılan aşağı gitmiyorsa
                upDirection = true; // Yılanın yukarı dönüş durumunu açar
                rightDirection = false; // Yılanın sağa dönüş durumunu kapatır
                leftDirection = false; // Yılanın sola dönüş durumunu kapatır
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) { // Eğer aşağı ok tuşu basılmışsa ve yılan yukarı gitmiyorsa
                downDirection = true; // Yılanın aşağı dönüş durumunu açar
                rightDirection = false; // Yılanın sağa dönüş durumunu kapatır
                leftDirection = false; // Yılanın sola dönüş durumunu kapatır
            }
        }
    }
}

