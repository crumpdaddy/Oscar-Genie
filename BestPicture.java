/** This program defines the picture object.
* 
* @author Ryan Crumpler
* @version 1.31.11
*/
public class BestPicture extends Nomination {
   private String percent;
   /**
   * @param nameIn is title of film
   * @param coefficentIn is constant used to determine film's 
   * liklihood of winning calculated through other awards
   * @param percentIn is the actor/actress' chance of winning an Oscar
   */
   public BestPicture(String nameIn, double coefficentIn,
       String percentIn) {
   super(nameIn, coefficentIn);
      percent = percentIn;
   }
   /**
   * @return the chance of film winning an Oscar.
   */
   public String getPercent() {
      return percent;
   }
   /**
   * sets chance of film winning an Oscar.
   * @param percentIn is percent chance of winning as calculated
   */
   public void setPercent(String percentIn) {
      percent = percentIn;
   }
   /**
   *  prints out the info of film, the title of the film
   * and its chance of winning an Oscar.
   * @return string containing all data of film
   */
   public String toString() {
      String output = "\"" + getName() + "\" as a "
          + getPercent() + "% chance of winning\n";
      return output;
   }
}