/**
 * Represents a program with title, description, start time,
 * end time, and optional attributes like subtitle and image URL.
 */
public class Program {
    private String title;         // Title of the program
    private String subtitle;      // Subtitle of the program (optional)
    private String description;   // Description of the program
    private String startTime;     // Start time of the program
    private String endTime;       // End time of the program

    private int id;               // Unique identifier for the program
    private String imageUrl;      // URL of the image associated with the program (optional)

    /**
     * Constructor to create a Program object with essential attributes.
     * @param title The title of the program.
     * @param description The description of the program.
     * @param startTime The start time of the program.
     * @param endTime The end time of the program.
     */
    public Program(String title, String description, String startTime, String endTime){
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = null;  // Initializing imageUrl as null initially
    }

    /**
     * Getter method to retrieve the title of the program.
     * @return The title of the program.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter method to retrieve the description of the program.
     * @return The description of the program.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method to retrieve the start time of the program.
     * @return The start time of the program.
     */
    public String getStartTime(){
        return startTime;
    }

    /**
     * Getter method to retrieve the end time of the program.
     * @return The end time of the program.
     */
    public String getEndTime(){
        return endTime;
    }

    /**
     * Getter method to retrieve the name of the program (same as title).
     * @return The name of the program.
     */
    public String getName() {
        return title;
    }

    /**
     * Getter method to retrieve the subtitle of the program.
     * @return The subtitle of the program.
     */
    public String getSubtitle(){
        return subtitle;
    }

    /**
     * Setter method to set the subtitle of the program.
     * @param subtitle The subtitle to set.
     */
    public void setSubtitle(String subtitle){
        this.subtitle = subtitle;
    }

    /**
     * Setter method to set the image URL of the program.
     * @param url The URL of the image.
     */
    public void setImageUrl(String url){
        this.imageUrl = url;
    }

    /**
     * Getter method to retrieve the image URL of the program.
     * @return The image URL of the program.
     */
    public String getImageUrl(){
        return imageUrl;
    }

    /**
     * Getter method to retrieve the ID of the program.
     * @return The ID of the program.
     */
    public int getId(){
        return id;
    }
}
