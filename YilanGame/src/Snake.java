
import java.awt.EventQueue;
import javax.swing.JFrame;

public class Snake extends JFrame { // Snake sınıfı, JFrame sınıfından türetiliyor

    public Snake() { // Snake sınıfının kurucu metodu
        initUI(); // Pencereyi başlatan metodu çağırır
    }

    private void initUI() { // Pencereyi başlatan metod

        add(new Board()); // Oyun alanını temsil eden Board nesnesini pencereye ekler

        setResizable(false); // Pencerenin boyutunun değiştirilememesini sağlar
        pack(); // Pencerenin boyutunu içerdiği bileşenlere göre ayarlar

        setTitle("Yılan Oyunu"); // Pencerenin başlığını belirler
        setLocationRelativeTo(null); // Pencerenin ekranın ortasında konumlanmasını sağlar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Pencere kapatıldığında programın sonlanmasını sağlar
    }


    public static void main(String[] args) { // Programın başlangıç noktası

        EventQueue.invokeLater(() -> { // Olay kuyruğunda bir görev oluşturur
            JFrame ex = new Snake(); // Snake nesnesini oluşturur
            ex.setVisible(true); // Pencereyi görünür yapar
        });
    }
}
