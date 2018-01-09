import java.io.Serializable;
import java.util.ArrayList;

/** This program defines the BestActor object.
 *
 * @author Ryan Crumpler
 * @version 30.12.17
 */

public class BestActor extends Nomination implements Serializable {
   protected String movie;
   /**
    * @param nameIn is first and last name of the actor
    * @param coefficientIn is constant used to determine actor's
    * likelihood of winning calculated through other awards
    * @param movieIn is the movie that the actor appeared in
    * @param percentIn is the actor's chance of winning an Oscar
    */
   public BestActor(String nameIn, double coefficientIn, String percentIn, ArrayList<String> awardOrgIn,
                    String movieIn) {
      super(nameIn, coefficientIn, percentIn, awardOrgIn);
      movie = movieIn;
   }

   /**
    * @return the movie actor appeared in
    */
   public String getMovie() {
      return movie;
   }

   /**
    * sets movie for actor.
    * @param movieIn is movie that actor was in
    */
   public void setMovie(String movieIn) {
      movie = movieIn;
   }

   /**
    * prints out the actor, the movie he was in and his chance of winning an Oscar.
    * @return string containing all data of actor
    */
   public String toString() {
      String output = "";
         output = getName() + " has a "
                 + getPercent() + "% chance of winning an Oscar for his role in \""
                 + getMovie() + "\"";
      return output;
   }
}