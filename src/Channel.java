import java.util.ArrayList;
import java.util.List;

/**
 * Represents a television channel with various attributes like
 * name, ID, type, image URL, about information, and a list of programs.
 */
public class Channel {
    private String name;                // Name of the channel
    private int id;                     // Unique identifier for the channel
    private String channelType;         // Type of the channel
    private String imageUrl;            // URL of the channel's image
    private String about;               // Information about the channel

    private boolean programCached;      // Indicates whether programs for this channel are cached

    private List<Program> programmes;   // List of programs scheduled on the channel

    /**
     * Constructor to create a Channel object with essential attributes.
     * @param name The name of the channel.
     * @param id The unique identifier of the channel.
     * @param channelType The type of the channel.
     * @param imageUrl The URL of the channel's image.
     * @param about Information about the channel.
     */
    public Channel(String name, int id, String channelType, String imageUrl, String about) {
        this.name = name;
        this.id = id;
        this.channelType = channelType;
        this.imageUrl = imageUrl;
        this.about = about;
        this.programmes = new ArrayList<>();   // Initializing the list of programs
        this.programCached = false;            // Initially, programs are not cached
    }

    /**
     * Getter method to retrieve the name of the channel.
     * @return The name of the channel.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method to retrieve the ID of the channel.
     * @return The ID of the channel.
     */
    public int getId() {
        return id;
    }

    /**
     * Getter method to retrieve the type of the channel.
     * @return The type of the channel.
     */
    public String getChannelType() {
        return channelType;
    }

    /**
     * Getter method to retrieve the image URL of the channel.
     * @return The image URL of the channel.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Getter method to retrieve information about the channel.
     * @return Information about the channel.
     */
    public String getAbout() {
        return about;
    }

    /**
     * Getter method to retrieve the list of programs scheduled on the channel.
     * @return The list of programs scheduled on the channel.
     */
    public List<Program> getProgrammes() {
        return programmes;
    }

    /**
     * Setter method to set whether programs for this channel are cached.
     * @param programCached Indicates whether programs for this channel are cached.
     */
    public void setProgramCached(boolean programCached) {
        this.programCached = programCached;
    }

    /**
     * Getter method to check if programs for this channel are cached.
     * @return True if programs for this channel are cached, otherwise false.
     */
    public boolean isProgramCached() {
        return programCached;
    }
}
