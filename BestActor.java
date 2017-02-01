/** This program defines the actor object.
* 
* @author Ryan Crumpler
* @version 1.31.11
*/
public class BestActor extends Nomination {
   private String movie;
   private String percent;
   private boolean actress;
   private boolean supporting;
   
   /**
   * @param nameIn is first and last name of actor/actress
   * @param coefficentIn is constant used to determine actors 
   * liklihood of winning calculated through other awards
   * @param movieIn is the movie that the actor/actress appeared in
   * @param percentIn is the actor/actress' chance of winning an Oscar
   * @param actressIn boolean to tell if actor or actress
   * @param supportingIn boolean to tell if actor/actress 
   * was in lead or supporting role
   */
   public BestActor(String nameIn, double coefficentIn, 
      String movieIn, String percentIn, 
      boolean actressIn, boolean supportingIn) {
   super(nameIn, coefficentIn);
      movie = movieIn;
      percent = percentIn;
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
   * @return the chance of actor/actress winning an Oscar.
   */
   public String getPercent() {
      return percent;
   }
   /**
   * sets chance of actor/actress winning an Oscar.
   * @param percentIn is percent chance of winning as calculated
   */
   public void setPercent(String percentIn) {
      percent = percentIn;
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
   *  prints out the actor/actress, the movie they were in 
   * and their chance of winning an Oscar.
   * @return string containing all data of actor/actress
   */
   public String toString() {
      String hisHer = "his";
      String supportingStr = "Actor";
      if (actress) {
         hisHer = "her";
      }
      String output = getName() + " has a " 
         + getPercent() + "% chance of winning for "
         + hisHer + " role in \"" + getMovie() + "\"\n";
      return output;
   }
}