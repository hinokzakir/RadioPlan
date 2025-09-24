import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Utility class for parsing API data related to television channels and programs.
 */
public class ApiParser {

    private List<String> channelNamesRiksKanal;
    private List<String> channelNamesLokalKanal;
    private List<String> channelNamesExtrakanaler;
    private List<String> channelNamesFlerkanaler;
    private List<String> channelNamesMinoritet;
    private Map<Integer, Channel> channelsMap;

    private static final long TWELVE_HOURS_IN_MILLIS = 12 * 60 * 60 * 1000; // 12 hours in milliseconds

    /**
     * Constructor to create a ApiParser object with essential attributes.
     */
    public ApiParser() {
        channelNamesRiksKanal = new ArrayList<>();
        channelNamesLokalKanal = new ArrayList<>();
        channelNamesExtrakanaler = new ArrayList<>();
        channelNamesFlerkanaler = new ArrayList<>();
        channelNamesMinoritet = new ArrayList<>();
        channelsMap = new HashMap<>();
        Date currentTime = new Date();
    }

    /**
     * Retrieves a Document object from the given URL.
     *
     * @param url The URL from which to fetch the document.
     * @return Document object representing the XML document.
     * @throws IOException                  If an I/O error occurs.
     * @throws SAXException                 If any parsing errors occur.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     */
    public Document getDocumentFromUrl(URL url) throws IOException, SAXException, ParserConfigurationException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(url.openStream());
        } catch (FileNotFoundException e) {
            // Handle error
        }
        return null;
    }

    /**
     * Parses an episode element and adds the corresponding program to the channel's program list.
     *
     * @param channel        The channel to which the program belongs.
     * @param episodeElement The XML element representing the episode.
     * @throws ParseException If an error occurs during parsing.
     */
    public void parseAndAddProgram(Channel channel, Element episodeElement) throws ParseException, ParseException {
        String title = episodeElement.getElementsByTagName("title").item(0).getTextContent();
        String subtitle = null;
        String description = getDescription(episodeElement);
        String startTimeString = episodeElement.getElementsByTagName("starttimeutc").item(0).getTextContent();
        String endTimeString = episodeElement.getElementsByTagName("endtimeutc").item(0).getTextContent();

        if (description == null) {
            description = "Kunde inte hitta beskrivning till program";
        }

        NodeList subtitleList = episodeElement.getElementsByTagName("subtitle");
        if (subtitleList.getLength() > 0) {
            subtitle = subtitleList.item(0).getTextContent();
        }
        if (subtitle != null) {
            title = title + " " + subtitle;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date startTime = dateFormat.parse(startTimeString);

        if (isWithinTimeRange(startTime)) {
            Element channelElement = (Element) episodeElement.getElementsByTagName("channel").item(0);
            String channelId = channelElement.getAttribute("id");
            Channel currentChannel = channelsMap.get(Integer.parseInt(channelId));

            if (currentChannel != null) {
                Program program = new Program(title, description, startTimeString, endTimeString);
                currentChannel.getProgrammes().add(program);
            }
        }
    }

    /**
     * Retrieves the description of an episode from the given element.
     *
     * @param episodeElement The XML element representing the episode.
     * @return Description of the episode.
     */
    private String getDescription(Element episodeElement) {
        NodeList descriptionNodes = episodeElement.getElementsByTagName("description");
        return descriptionNodes.getLength() > 0 ? descriptionNodes.item(0).getTextContent() : "";
    }

    /**
     * Handles the case of an IOException for a specific channel.
     *
     * @param channel The channel for which the IO exception occurred.
     */
    public void handleIOException(Channel channel) {
        //nothing
    }

    /**
     * Checks if the start time of an episode is within the specified time range.
     *
     * @param startTime The start time of the episode.
     * @return True if the start time is within the time range, otherwise false.
     */
    private boolean isWithinTimeRange(Date startTime) {
        // Calculate the current time
        Date currentTime = new Date();

        // Calculate the time 12 hours before and after the current time
        Date startTimeMinus12Hours = new Date(startTime.getTime() - TWELVE_HOURS_IN_MILLIS);
        Date startTimePlus12Hours = new Date(startTime.getTime() + TWELVE_HOURS_IN_MILLIS);

        // Check if the start time is within the specified time range
        return currentTime.after(startTimeMinus12Hours) && currentTime.before(startTimePlus12Hours);
    }

    /**
     * Retrieves the text content of the specified tag within a given node.
     *
     * @param node     The node containing the tag.
     * @param tagName  The name of the tag.
     * @return The text content of the tag, or null if not found.
     */
    public String getNodeValue(Node node, String tagName) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(tagName)) {
                return childNode.getTextContent();
            }
        }
        return null;
    }

    /**
     * Retrieves the map of channels.
     *
     * @return The map containing channels.
     */
    public Map<Integer, Channel> getChannelsMap() {
        return channelsMap;
    }
}
