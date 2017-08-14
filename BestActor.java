import java.util.ArrayList;

/** This program defines the actor object.
 *
 * @author Ryan Crumpler
 * @version 14.8.17
 */
import java.util.ArrayList;

public class BestActor extends Nomination {
   protected String movie;
   protected boolean actress, supporting, song;
   protected ArrayList<String> awardOrg;
   /**
    * @param nameIn is first and last name of actor/actress
    * @param coefficientIn is constant used to determine actors
    * likelihood of winning calculated through other awards
    * @param movieIn is the movie that the actor/actress appeared in
    * @param percentIn is the actor/actress' chance of winning an Oscar
    * @param actressIn boolean to tell if actor or actress
    * @param songIn boolean is object is best song or actorwas in lead or supporting role
    */
   public BestActor(String nameIn, double coefficientIn, String percentIn, ArrayList<String> awardOrgIn,
                    String movieIn, boolean actressIn, boolean songIn) {
      super(nameIn, coefficientIn, percentIn, awardOrgIn);
      movie = movieIn;
      percent = percentIn;
      song = songIn;
   }

   /**
    * @return the movie actor/actress appeared in
    */
   public String getMovie() {
      return movie;
   }

   /**
    * sets movie for actor/actress.
    * @param movieIn is movie that actor/actress was in
    */
   public void setMovie(String movieIn) {
      movie = movieIn;
   }

   /**
    * defines person as actor or actress.
    * @param actressIn boolean defines if is actress true representing actress
    */
   public void setActress(boolean actressIn) {
      actress = actressIn;
   }

   /**
    * @return if actor or actress
    */
   public boolean getActress() {
      return actress;
   }

   /**
    * defines if it is best song or an actor.
    * @param songIn boolean defines if is original song
    */
   public void setSong(boolean songIn) {
      song = songIn;
   }

   /**
    * @return if original song
    */
   public boolean getSong() {
      return song;
   }

   /**
    * prints out the actor/actress, the movie they were in and their chance of winning an Oscar.
    * @return string containing all data of actor/actress
    */
   public String toString() {
      String hisHer = "his role in \"";
      String output = "";
      if (actress) {
         hisHer = "her role in \"";
      }
      if (song) {
         output = "The Song \"" + getName() + "\" in the movie "
                 + getMovie() + " has a " + getPercent() + "% chance of winning";
      }
      else {
         output = getName() + " has a "
                 + getPercent() + "% chance of winning an Oscar for "
                 + hisHer + getMovie() + "\"";
      }
      return output;
   }
}