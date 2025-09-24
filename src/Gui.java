import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * A GUI class for displaying radio channel information and schedules.
 */
public class Gui extends JFrame {

    private JMenuBar menuBar;
    private JMenu menu;
    private JMenu subMenuRiksKanal;
    private JMenu subMenuLokalKanal;
    private JMenu subMenuExtrakanaler;
    private JMenu subMenuFlerkanaler;
    private JMenu subMenuMinoritet;

    private JMenuItem update;
    private JMenuItem mode;
    private JLabel imageLabel;
    private JPanel infoPanel;
    private JPanel schedulePanel;

    private JTable scheduleTable;

    private JTextArea channelTextArea;

    private Map<Integer, Channel> channels;

    /**
     * Constructor for creating a Gui object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public Gui() throws IOException {
        setUpGui();
        setUpMenuBar();
    }

    /**
     * Locks the update menu item.
     */
    public void lockUpdate() {
        update.setEnabled(false);
    }

    /**
     * Unlocks the update menu item.
     */
    public void unlockUpdate() {
        update.setEnabled(true);
    }

    /**
     * Sets up the graphical user interface.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void setUpGui() throws IOException {
        try{
            setTitle("RadioApp");
            setSize(800, 600);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            getContentPane().setBackground(new java.awt.Color(50, 50, 50));
            this.infoPanel = createInfoPanel();
            this.schedulePanel = createSchedulePanel();
            add(infoPanel, BorderLayout.WEST);
            add(schedulePanel, BorderLayout.CENTER);
        } catch (NullPointerException e){
            //do nothing
        }
    }

    public Image getPlaceholder() throws IOException {
        BufferedImage image = ImageIO.read(new URL("https://people.cs.umu.se/c22hsh/placeholder.jpg"));
        Image resizedImage = image.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
        return resizedImage;
    }

    /**
     * Creates the panel for displaying channel information.
     *
     * @return The JPanel for displaying channel information.
     * @throws IOException If an I/O error occurs.
     */
    public JPanel createInfoPanel() throws IOException {
            JPanel infoPanel = new JPanel(new BorderLayout());
            imageLabel = new JLabel();
            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setBackground(new java.awt.Color(50, 50, 50));
            //imageLabel.setIcon(new ImageIcon(resizedImage));
            imagePanel.add(imageLabel, BorderLayout.CENTER);
            infoPanel.add(imagePanel, BorderLayout.CENTER);
            channelTextArea = new JTextArea();
            channelTextArea.setLineWrap(true);
            channelTextArea.setWrapStyleWord(true);
            channelTextArea.setPreferredSize(new Dimension(300, 300));
            channelTextArea.setEditable(false);
            channelTextArea.setBackground(new java.awt.Color(30, 30, 30));
            channelTextArea.setFont(new Font("SansSerif", Font.PLAIN, 18));
            channelTextArea.setForeground(new java.awt.Color(255, 255, 255));
            channelTextArea.setText("Välj en kanal för att visa information om den. Du kan sedan välja " +
                    "ett program för att visa information om det.");
            Border thickBorder = new LineBorder(Color.BLACK, 5);
            channelTextArea.setBorder(thickBorder);
            infoPanel.add(channelTextArea, BorderLayout.SOUTH);
            return infoPanel;
    }

    /**
     * Creates the panel for displaying the schedule.
     *
     * @return The JPanel for displaying the schedule.
     */
    private JPanel createSchedulePanel() {
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBackground(new java.awt.Color(50, 50, 50));
        Object[][] data = {};
        String[] columnNames = new String[]{"Title", "Start Time", "End Time"};
        this.scheduleTable = new JTable(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Title", "Start Time", "End Time"}
        ) {
            @Override
            public boolean isCellEditable(int row, int columns) {
                return false;
            }
        });
        scheduleTable.setDefaultRenderer(Object.class, new TableRender());
        scheduleTable.setFillsViewportHeight(true);
        scheduleTable.setBackground(new java.awt.Color(30, 30, 30));
        scheduleTable.setForeground(new java.awt.Color(255, 255, 255));
        TableColumnModel columnModel = scheduleTable.getColumnModel();
        columnModel.getColumn(1).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(50);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        schedulePanel.add(scrollPane, BorderLayout.CENTER);
        return schedulePanel;
    }

    /**
     * Custom table cell renderer for adjusting font size.
     */
    class TableRender extends DefaultTableCellRenderer {
        private static final Font BIGGER_FONT = new Font("Arial", Font.PLAIN, 16);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected,

                    hasFocus, row, column);
            component.setFont(BIGGER_FONT);
            return component;
        }
    }

    /**
     * Gets the schedule table.
     *
     * @return The schedule table.
     */
    public JTable getScheduleTable() {
        return scheduleTable;
    }

    /**
     * Adds a list selection listener to the schedule table.
     *
     * @param actionListener The action listener to be added.
     */
    public void addListenerToTable(ListSelectionListener actionListener) {
        scheduleTable.getSelectionModel().addListSelectionListener(actionListener);
    }

    /**
     * Sets up the option menu.
     *
     * @param actionListener The action listener for menu items.
     */
    public void setUpOptionMenu(ActionListener actionListener) {
        JMenu tools = new JMenu("Verktyg");
        update = new JMenuItem("Uppdatera");
        update.setActionCommand("update");
        update.addActionListener(actionListener);
        JMenuItem about = new JMenuItem("Om Programmet");
        about.setActionCommand("about");
        about.addActionListener(actionListener);
        mode = new JMenuItem("Byt läge");
        tools.add(update);
        tools.add(about);
        tools.add(mode);
        menuBar.add(tools);
    }

    /**
     * Sets up the menu bar.
     */
    public void setUpMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(new java.awt.Color(50, 50, 50));
        menu = new JMenu("Kanaler");
        subMenuRiksKanal = new JMenu("Rikskanaler");
        subMenuLokalKanal = new JMenu("Lokala kanaler");
        subMenuExtrakanaler = new JMenu("Extrakanaler");
        subMenuFlerkanaler = new JMenu("Fler kanaler");
        subMenuMinoritet = new JMenu("Minoritet och språk");
        menu.add(subMenuRiksKanal);
        menu.add(subMenuLokalKanal);
        menu.add(subMenuExtrakanaler);
        menu.add(subMenuFlerkanaler);
        menu.add(subMenuMinoritet);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    /**
     * Adds a channel to the "Rikskanaler" submenu.
     *
     * @param name           The name of the channel.
     * @param actionListener The action listener for the channel.
     */
    public void addChannelRiksKanaler(String name, ActionHandler actionListener) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(name);
        menuItem.addActionListener(actionListener);
        subMenuRiksKanal.add(menuItem);
    }

    /**
     * Adds a channel to the "Lokala kanaler" submenu.
     *
     * @param name           The name of the channel.
     * @param actionListener The action listener for the channel.
     */
    public void addChannelLokalKanaler(String name, ActionHandler actionListener) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(name);
        menuItem.addActionListener(actionListener);
        subMenuLokalKanal.add(menuItem);
    }

    /**
     * Adds a channel to the "Extrakanaler" submenu.
     *
     * @param name           The name of the channel.
     * @param actionListener The action listener for the channel.
     */
    public void addChannelExtrakanaler(String name, ActionHandler actionListener) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(name);
        menuItem.addActionListener(actionListener);
        subMenuExtrakanaler.add(menuItem);
    }

    /**
     * Adds a channel to the "Fler kanaler" submenu.
     *
     * @param name           The name of the channel.
     * @param actionListener The action listener for the channel.
     */
    public void addChannelFlerkanaler(String name, ActionHandler actionListener) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(name);
        menuItem.addActionListener(actionListener);
        subMenuFlerkanaler.add(menuItem);
    }

    /**
     * Adds a channel to the "Minoritet och språk" submenu.
     *
     * @param name           The name of the channel.
     * @param actionListener The action listener for the channel.
     */
    public void addChannelMinoritet(String name, ActionHandler actionListener) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(name);
        menuItem.addActionListener(actionListener);
        subMenuMinoritet.add(menuItem);
    }

    /**
     * Updates the information panel with the provided image URL and description.
     *
     * @param imageUrl The URL of the image.
     * @param about    The description of the channel.
     */
    public void updateInfoPanel(String imageUrl, String about) {
        try {
            if (imageUrl != null) {
                BufferedImage image = ImageIO.read(new URL(imageUrl));
                Image resizedImage = image.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(resizedImage));
            } else {
                BufferedImage image = ImageIO.read(new URL("https://people.cs.umu.se/c22hsh/imagenotfound.png"));
                Image resizedImage = image.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(resizedImage));
            }
            channelTextArea.setText(about);
        } catch (IOException e) {
            displayPopupMessage("ERROR: Kan inte hämta kanal icon pga ingen internet");
            imageLabel.setIcon(null);
            channelTextArea.setText(about);
        }
    }

    /**
     * Displays a popup message with the given message.
     *
     * @param message The message to be displayed.
     */
    public void displayPopupMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Updates the schedule panel with the provided list of programs.
     *
     * @param programs The list of programs to be displayed.
     */
    public void updateSchedulePanel(List<Program> programs) {
        DefaultTableModel model = (DefaultTableModel) scheduleTable.getModel();
        model.setRowCount(0);
        for (Program program : programs) {
            LocalDateTime startTime = LocalDateTime.parse(program.getStartTime(), DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime endTime = LocalDateTime.parse(program.getEndTime(), DateTimeFormatter.ISO_DATE_TIME);
            String startt = startTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
            String endtt = endTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
            Object[] rowData = {
                    program.getTitle(),
                    startt,
                    endtt
            };
            model.addRow(rowData);
        }
    }

    /**
     * Locks the schedule table.
     */
    public void lockTable() {
        this.scheduleTable.setEnabled(false);
    }

    /**
     * Unlocks the schedule table.
     */
    public void unlockTable() {
        this.scheduleTable.setEnabled(true);
    }
}