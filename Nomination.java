/** This program defines the abstract nomination object.
*  This is used for actors as well as film objects
* @author Ryan Crumpler
* @version 1.31.11
*/
public abstract class Nomination {
   
   protected String name;
   protected double coefficent;
    /**
   * @param nameIn is title of film or name of actor/actress
   * @param coefficentIn is constant used to determine  
   * liklihood of winning calculated through other awards
   */
   public Nomination(String nameIn, double coefficentIn) {
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
}