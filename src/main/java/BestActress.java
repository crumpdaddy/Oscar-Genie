import java.io.Serializable;
import java.util.ArrayList;

public class BestActress extends Nomination implements Serializable {
    protected String movie;
    /**
     * @param nameIn is first and last name of actress
     * @param coefficientIn is constant used to determine actress'
     * likelihood of winning calculated through other awards
     * @param movieIn is the movie that the actor/actress appeared in
     * @param percentIn is the actress' chance of winning an Oscar
     */
    public BestActress(String nameIn, double coefficientIn, String percentIn, ArrayList<String> awardOrgIn,
                     String movieIn) {
        super(nameIn, coefficientIn, percentIn, awardOrgIn);
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
