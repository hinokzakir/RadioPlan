/**
 * ActionHandler class implements ActionListener and ListSelectionListener interfaces
 * to handle actions and selections in the GUI.
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ActionHandler implements ActionListener, ListSelectionListener {

    private Gui gui;
    private ApiParser api;

    private Channel latestChannel;

    private Timer timer;

    private Boolean programselected;

    private Boolean update;
    private Boolean network;
    private Boolean downloaded;

    /**
     * Constructor for ActionHandler class.
     *
     * @param api ApiParser instance to interact with API.
     * @param gui Gui instance for GUI operations.
     */
    public ActionHandler(ApiParser api, Gui gui) {
        this.gui = gui;
        this.api = api;
        this.update = false;
        this.network = true;
        parseChannels();
        gui.setUpOptionMenu(this);
        gui.addListenerToTable(this);
        timer = new Timer(60 * 60 * 1000, e -> {
            if(!update){
                parseScheduleEpisodes();
            }
        });
        timer.setInitialDelay(60 * 60 * 1000);
        timer.start();
    }

    /**
     * Parses the list of channels from the API.
     */
    public void parseChannels() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String xmlUrl = "http://api.sr.se/api/v2/channels/?pagination=false";
                    URL url = new URL(xmlUrl);

                    // Create a new instance of a document builder factory
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    // Attempt to parse the URL
                    Document doc = dBuilder.parse(url.openStream());
                    doc.getDocumentElement().normalize();

                    NodeList nodeList = doc.getElementsByTagName("channel");

                    // Loop through the list of channels
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String channelName = node.getAttributes().getNamedItem("name").getNodeValue();
                            int channelId = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
                            String channelType = api.getNodeValue(node, "channeltype");
                            String imageUrl = api.getNodeValue(node, "image");
                            String about = api.getNodeValue(node, "tagline");

                            // Create a Channel object
                            Channel channel = new Channel(channelName, channelId, channelType, imageUrl, about);

                            // Add the Channel to the HashMap using channel ID as the key
                            api.getChannelsMap().put(channelId, channel);
                        }
                    }
                    downloaded = true;
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        gui.displayPopupMessage("ERROR: Gick inte att hämta kanaler: " + e.getMessage());
                    });
                    downloaded = false;
                }
                return null;
            }
            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    addChannelstoGui();
                });
            }
        };
        worker.execute();
    }


    public void addChannelstoGui() {
        // Add the actionlistener to all the menuitems in the gui
        for (Map.Entry<Integer, Channel> entry : api.getChannelsMap().entrySet()) {
            if (entry.getValue().getChannelType().equals("Rikskanal")) {
                SwingUtilities.invokeLater(() -> gui.addChannelRiksKanaler(entry.getValue().getName(), this));
            } else if (entry.getValue().getChannelType().equals("Lokal kanal")) {
                SwingUtilities.invokeLater(() -> gui.addChannelLokalKanaler(entry.getValue().getName(), this));
            } else if (entry.getValue().getChannelType().equals("Extrakanaler")) {
                SwingUtilities.invokeLater(() -> gui.addChannelExtrakanaler(entry.getValue().getName(), this));
            } else if (entry.getValue().getChannelType().equals("Fler kanaler")) {
                SwingUtilities.invokeLater(() -> gui.addChannelFlerkanaler(entry.getValue().getName(), this));
            } else if (entry.getValue().getChannelType().equals("Minoritet och språk")) {
                SwingUtilities.invokeLater(() -> gui.addChannelMinoritet(entry.getValue().getName(), this));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // Get the selected channel from the channels map

        Channel selectedChannel = findChannelByName(api.getChannelsMap(), command);

        // Update the GUI with the image URL of the selected channel
        if (selectedChannel != null) {
            latestChannel = selectedChannel;
            String imageUrl = selectedChannel.getImageUrl();
            gui.updateInfoPanel(imageUrl, selectedChannel.getAbout());
            if (!selectedChannel.isProgramCached()) {
                parseChannelProgram(selectedChannel);
            } else {
                gui.updateSchedulePanel(selectedChannel.getProgrammes());
            }
        }
        if ("update".equals(command)) {
            if(downloaded==true){
                parseScheduleEpisodes();
            }
            else{
                parseChannels();
            }
        }
        if ("about".equals(command)) {
            gui.displayPopupMessage("Programmerat av Hinok Zakir Saleh 2024");
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int selectedRow = gui.getScheduleTable().getSelectedRow();
            if (selectedRow != -1) {
                String selectedProgramName = gui.getScheduleTable().getValueAt(selectedRow, 0).toString();
                Program selectedProgram = getProgramByName(latestChannel.getProgrammes(), selectedProgramName);
                if (selectedProgram != null) {
                    // Display information in a popup window
                    displayInfoPopup(selectedProgram);
                    gui.lockTable();
                }
            }
        }
        gui.unlockTable();
    }

    /**
     * Retrieves a Program object by its name from a list of programs.
     *
     * @param programs    List of programs.
     * @param programName Name of the program to retrieve.
     * @return Program object if found, null otherwise.
     */
    private static Program getProgramByName(List<Program> programs, String programName) {
        for (Program program : programs) {
            if (program.getName().equals(programName)) {
                return program;
            }
        }
        return null;
    }

    /**
     * Finds a Channel by its name from a map of channels.
     *
     * @param channelMap   Map of channels.
     * @param channelName  Name of the channel to find.
     * @return Channel object if found, null otherwise.
     */
    private static Channel findChannelByName(Map<Integer, Channel> channelMap, String channelName) {
        for (Map.Entry<Integer, Channel> entry : channelMap.entrySet()) {
            Channel channel = entry.getValue();
            if (channelName.equals(channel.getName())) {
                return channel; // Found the channel with the given name
            }
        }
        return null; // Channel not found
    }

    /**
     * Parses the program schedule of a channel.
     *
     * @param channel Channel whose program schedule needs to be parsed.
     */
    public void parseChannelProgram(Channel channel) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    String channelId = String.valueOf(channel.getId());

                    // Get the current date and time
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    // Determine the URLs for the current date and 12 hours ahead
                    LocalDateTime twelveHoursPast = now.minusHours(12);
                    String scheduleUrl1 = "http://api.sr.se/v2/scheduledepisodes?channelid=" + channelId + "&date=" + twelveHoursPast.format(formatter) + "&pagination=false";
                    LocalDateTime twelveHoursLater = now.plusHours(12);
                    String scheduleUrl2 = "http://api.sr.se/v2/scheduledepisodes?channelid=" + channelId + "&date=" + twelveHoursLater.format(formatter) + "&pagination=false";
                    // Fetch data from the first URL
                    URL url1 = new URL(scheduleUrl1);
                    isInternetAvailable();
                    if (url1 != null && network) {
                        parseSchedule(url1, channel);
                    }

                    // Fetch data from the second URL
                    URL url2 = new URL(scheduleUrl2);
                    if (url2 != null && network) {
                        parseSchedule(url2, channel);
                    }

                } catch (Exception e) {
                    //handle exception
                }
                channel.setProgramCached(true);
                return null;
            }

            @Override
            protected void done() {
                try {
                    SwingUtilities.invokeLater(() -> {
                        gui.updateSchedulePanel(channel.getProgrammes());
                        isInternetAvailable();
                    });
                } catch (Exception e) {
                    //handle exception
                }
            }
        };
        worker.execute();
    }




    /**
     * Parses the episodes of the program schedule for all channels.
     */
    public synchronized void parseScheduleEpisodes() {
        gui.lockUpdate();
        update=true;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    for (Channel channel : api.getChannelsMap().values()) {
                        if (channel.isProgramCached()) {
                            String channelId = String.valueOf(channel.getId());

                            // Get the current date and time
                            LocalDateTime now = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                            // Determine the URLs for the current date and 12 hours ahead
                            LocalDateTime twelveHoursPast = now.minusHours(12);
                            String scheduleUrl1 = "http://api.sr.se/v2/scheduledepisodes?channelid=" + channelId + "&date=" + twelveHoursPast.format(formatter) + "&pagination=false";
                            LocalDateTime twelveHoursLater = now.plusHours(12);
                            String scheduleUrl2 = "http://api.sr.se/v2/scheduledepisodes?channelid=" + channelId + "&date=" + twelveHoursLater.format(formatter) + "&pagination=false";
                            isInternetAvailable();
                            if (channel.getProgrammes().size() > 0 && network) {
                                channel.getProgrammes().clear();
                            }
                            // Fetch data from the first URL
                            URL url1 = new URL(scheduleUrl1);
                            if(network){
                                parseSchedule(url1, channel);
                            }

                            // Fetch data from the second URL
                            URL url2 = new URL(scheduleUrl2);
                            if(network){
                                parseSchedule(url2, channel);
                            }
                        }
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> gui.displayPopupMessage("Kunde inte uppdatera tablån"));
                }
                return null;
            }

            @Override
            protected void done() {
                if (latestChannel != null) {
                    if(latestChannel.getProgrammes() == null){
                    }
                    else{
                        gui.updateSchedulePanel(latestChannel.getProgrammes());
                        isInternetAvailable();
                        if(network){
                            gui.updateInfoPanel(latestChannel.getImageUrl(), latestChannel.getAbout());
                        }
                        else{
                            gui.displayPopupMessage("ERROR: Tablån kunde ej uppdateras\n kontrollera internet anslutning");
                        }
                    }
                } else {
                    gui.displayPopupMessage("Tablå kan inte uppdateras \n pga inga kanaler har valts");
                }
                update=false;
                gui.unlockUpdate();
            }
        };

        worker.execute(); // Start the background task
    }

    /**
     * Displays information about a program in a popup window.
     *
     * @param program Program object to display information about.
     */
    public void displayInfoPopup(Program program) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (program.getImageUrl() == null) {
                        try {
                            String channelId = String.valueOf(latestChannel.getId());

                            String scheduleUrl = "http://api.sr.se/api/v2/scheduledepisodes?channelid=" + channelId + "&pagination=false";
                            URL url = new URL(scheduleUrl);

                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            Document doc = dBuilder.parse(url.openStream());
                            doc.getDocumentElement().normalize();

                            NodeList nodeList = doc.getElementsByTagName("scheduledepisode");

                            for (int i = 0; i < nodeList.getLength(); i++) {
                                Node node = nodeList.item(i);
                                if (node.getNodeType() == Node.ELEMENT_NODE) {
                                    Element episodeElement = (Element) node;
                                    String test1 = episodeElement.getElementsByTagName("title").item(0).getTextContent();
                                    String test2 = program.getTitle();
                                    if (test1.equals(test2)) {
                                        NodeList imageUrlNodes = episodeElement.getElementsByTagName("imageurl");
                                        if (imageUrlNodes.getLength() > 0) {
                                            String imageUrl = imageUrlNodes.item(0).getTextContent();
                                            if (!imageUrl.isEmpty()) {
                                                program.setImageUrl(imageUrl);
                                            } else {
                                                // Handle case where imageUrl is empty
                                            }
                                        } else {
                                            // Handle case where "imageurl" element is missing
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (IOException ioException) {
                            api.handleIOException(latestChannel);
                        }

                    }
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void done() {
                ImageIcon icon;
                if (program.getImageUrl() != null) {
                    icon = createImageIcon(program.getImageUrl());
                } else {
                    icon = createImageIcon("https://i3.radionomy.com/radios/400/c16d64a1-3ef8-473e-94f1-13651dcfa1f2.jpg");
                }
                JOptionPane.showMessageDialog(gui, program.getDescription(), program.getTitle(), JOptionPane.INFORMATION_MESSAGE, icon);
            }
        };

        worker.execute();
    }

    /**
     * Creates an ImageIcon from the given image URL.
     *
     * @param imageUrl URL of the image.
     * @return ImageIcon object if image is successfully loaded, null otherwise.
     */
    private static ImageIcon createImageIcon(String imageUrl) {
        try {
            // Fetch the image from the URL
            Image image = new ImageIcon(new URL(imageUrl)).getImage();

            // Scale the image to fit within 100x100 pixels
            Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

            // Create and return the ImageIcon
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses the schedule from the given URL and adds programs to the specified channel.
     *
     * @param url     URL to parse schedule from.
     * @param channel Channel to add programs to.
     */
    private void parseSchedule(URL url, Channel channel) {
        try {
            Document doc = api.getDocumentFromUrl(url);
            NodeList nodeList = doc.getElementsByTagName("scheduledepisode");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element episodeElement = (Element) node;
                    api.parseAndAddProgram(channel, episodeElement);
                }
            }
        } catch (IOException ioException) {
            // Handle IO exception
            SwingUtilities.invokeLater(() -> gui.displayPopupMessage("Error: kan inte skapa anslutning till server"));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
    public void isInternetAvailable() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            network = true;
            connection.disconnect();
        } catch (Exception e) {
            network = false;
        }
    }
}
