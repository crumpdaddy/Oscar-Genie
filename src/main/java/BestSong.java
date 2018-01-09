import java.io.Serializable;
import java.util.ArrayList;

/**
 * This program defines the BestSong object
 * @author Ryan Crumpler
 * @version 30.12.17
 */
public class BestSong extends Nomination  implements Serializable {
    protected String movie;

    public BestSong(String nameIn, double coefficientIn, String percentIn, ArrayList<String> awardOrgIn,
                    String movieIn) {
        super(nameIn, coefficientIn, percentIn, awardOrgIn);
        movie = movieIn;
    }

    /**
     * @return the movie actor/actress appeared in
     */
    public String getMovie() {
        return movie;
    }

    /**
     * sets movie for the song.
     * @param movieIn is movie that song was in
     */
    public void setMovie(String movieIn) {
        movie = movieIn;
    }

    /**
     * prints out the song name, the movie the song was in and its chance of winning an Oscar.
     * @return string containing all data of song
     */
    public String toString() {
        String output = "";
        output = "The Song \"" + getName() + "\" in the movie "
                + getMovie() + " has a " + getPercent() + "% chance of winning";
        return output;
    }
}
