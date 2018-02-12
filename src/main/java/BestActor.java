import java.io.Serializable;

/** This program defines the BestActor object.
 *
 * @author Ryan Crumpler
 * @version 20.1.18
 */

public class BestActor extends Nomination implements Serializable {
   private String movie;
   /**
    * @param nameIn is first and last name of the actor
    * likelihood of winning calculated through other awards
    * @param movieIn is the movie that the actor appeared in
    */
   BestActor(String nameIn, String movieIn) {
      super(nameIn);
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
   @SuppressWarnings("unused")
   public void setMovie(String movieIn) {
      movie = movieIn;
   }

   /**
    * prints out the actor, the movie he was in and his chance of winning an Oscar.
    * @return string containing all data of actor
    */
   public String toString() {
         return getName() + " has a "
                 + getPercent() + "% chance of winning an Oscar for his role in \""
                 + getMovie() + "\"";
   }
}