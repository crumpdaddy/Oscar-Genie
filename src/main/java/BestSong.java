import java.io.Serializable;

/**
 * This program defines the BestSong object
 * @author Ryan Crumpler
 * @version 15.1.18
 */
public class BestSong extends Nomination  implements Serializable {
    protected String movie;

    public BestSong(String nameIn, String movieIn) {
        super(nameIn);
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
