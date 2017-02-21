/** This program defines the abstract nomination object.
*  This is used for actors as well as film objects
* @author Ryan Crumpler
* @version 1.31.11
*/
public class Nomination {
   
   protected String name;
   protected double coefficent;
   protected String percent;
    /**
   * @param nameIn is title of film or name of actor/actress
   * @param coefficentIn is constant used to determine  
   * liklihood of winning calculated through other awards
   * @param percentIn is percent chance of winning
   */
   public Nomination(String nameIn, double coefficentIn, String percentIn) {
      name = nameIn;
      coefficent = coefficentIn;
   }
   /**
   * @return the title of film or actor
   */
   public String getName() {
      return name;
   }
   /**
   * sets name of movie.
   * @param nameIn is movie nominated.
   */
   public void setName(String nameIn) {
      name = nameIn;
   }
   /**
   * @return the coefficent used to calculate chance of winning
   */
   public double getCoefficent() {
      return coefficent;
   }
   /**
   * sets the coefficent used to calculate chance of winning.
   * @param coefficentIn is coefficent for calculations
   */
   public void setCoefficent(double coefficentIn) {
      coefficent = coefficentIn;
   }
   /**
   * @return the percent chance of winning
   */
   public String getPercent() {
      return percent;
   }
   /**
   * sets the percent chance of winning.
   * @param percentIn is percent chance of winning
   */
   public void setPercent(String percentIn) {
      percent = percentIn;
   }
   /**
   * prints out the info of film, the title of the film
   * and its chance of winning an Oscar.
   * @return string containing all data of film
   */
   public String toString() {
      String output = "\n\"" + getName() + "\" has a "
          + getPercent() + "% chance of winning";
      return output;
   }
}