import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Set the look and feel to the system look and feel
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacDarkLaf");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                     | UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }
            Gui GUI = null;
            try {
                GUI = new Gui();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new ActionHandler(new ApiParser(), GUI);
            // Make the GUI visible
            GUI.setVisible(true);
        });
    }
}