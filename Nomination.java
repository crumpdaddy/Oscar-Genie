/** This program defines the  nomination object.
 *  This is used for actors as well as film objects
 * @author Ryan Crumpler
 * @version 13.8.17
 */
public class Nomination {

   protected String name, percent;
   protected double coefficient;
   /**
    * @param nameIn is title of film or name of actor/actress
    * @param coefficientIn is constant used to determine
    * likelihood of winning calculated through other awards
    * @param percentIn is percent chance of winning
    */
   public Nomination(String nameIn, double coefficientIn, String percentIn) {
      name = nameIn;
      coefficient = coefficientIn;
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
    * @return the coefficient used to calculate chance of winning
    */
   public double getCoefficient() {
      return coefficient;
   }

   /**
    * sets the coefficient used to calculate chance of winning.
    * @param coefficientIn is coefficient for calculations
    */
   public void setCoefficient(double coefficientIn) {
      coefficient = coefficientIn;
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
      String output = "\"" + getName() + "\" has a "
              + getPercent() + "% chance of winning an Oscar";
      return output;
   }
}