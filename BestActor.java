/** This program defines the actor object.
* 
* @author Ryan Crumpler
* @version 1.31.11
*/
public class BestActor extends Nomination {
   protected String movie;
   protected boolean actress;
   protected boolean supporting;
   protected boolean song;
   
   /**
   * @param nameIn is first and last name of actor/actress
   * @param coefficentIn is constant used to determine actors 
   * liklihood of winning calculated through other awards
   * @param movieIn is the movie that the actor/actress appeared in
   * @param percentIn is the actor/actress' chance of winning an Oscar
   * @param actressIn boolean to tell if actor or actress
   * @param supportingIn boolean to tell if actor/actress 
   * @param songIn boolean is object is best song or actor
   * was in lead or supporting role
   */
   public BestActor(String nameIn, double coefficentIn, 
      String percentIn, String movieIn, boolean actressIn, 
      boolean supportingIn, boolean songIn) {
   super(nameIn, coefficentIn, percentIn);
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
   * @param actressIn boolean defines if is actress 
   * true representing actress
   */
   public void setActress(boolean actressIn) {
      actress = actressIn;
   }
   /**
   * defines person as lead or supporting role.
   * @param supportingIn boolean defines if is supporting role 
   * true representing supporting role
   */
   public void setSupporting(boolean supportingIn) {
      supporting = supportingIn;
   }
   /**
   * @return if actor or actress
   */
   public boolean getActress() {
      return actress;
   }
   /**
   * @return is in lead or supporting role
   */
   public boolean getSupporting() {
      return supporting;
   }
   /**
   * defines if it is best song or an actor.
   * @param songIn boolean defines if is original song 
   * true representing supporting role
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
   *  prints out the actor/actress, the movie they were in 
   * and their chance of winning an Oscar.
   * @return string containing all data of actor/actress
   */
   public String toString() {
      String hisHerSong = "his role in \"";
      String output = "";
      String supportingStr = "Actor";
      if (actress) {
         hisHerSong = "her role in \"";
      }
      if (song) {
         hisHerSong = "the song \"";
      }
      output = "\n" + getName() + " has a " 
         + getPercent() + "% chance of winning for "
         + hisHerSong + getMovie() + "\"";
      return output;
   }
}