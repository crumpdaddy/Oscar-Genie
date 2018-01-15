import java.io.Serializable;

/**
 * This program defines the BestActress object
 * @author Ryan Crumpler
 * @version 15.1.18
 */
public class BestActress extends Nomination implements Serializable {
    private String movie;
    /**
     * @param nameIn is first and last name of actress
     * likelihood of winning calculated through other awards
     * @param movieIn is the movie that the actor/actress appeared in
     */
    public BestActress(String nameIn, String movieIn) {
        super(nameIn);
        movie = movieIn;
    }

    /**
     * @return the movie actress appeared in
     */
    public String getMovie() {
        return movie;
    }

    /**
     * sets movie actress was in.
     * @param movieIn is movie that actress was in
     */
    public void setMovie(String movieIn) {
        movie = movieIn;
    }

    /**
     * prints out the actress, the movie she was in and her chance of winning an Oscar.
     * @return string containing all data of actress
     */
    public String toString() {
        String output = "";
        output = getName() + " has a "
                + getPercent() + "% chance of winning an Oscar for her role in \""
                + getMovie() + "\"";
        return output;
    }

}
