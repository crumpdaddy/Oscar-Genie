/** This program is the driver that carries out all methods 
* to determine chance ofwinning an oscar.
* 
* @author Ryan Crumpler
* @version 1.31.11
*/
public class Calculations {
   protected String name;
   protected double coefficent;
   protected String orginization;
   /**
   * Difines a Calculation object that is used in calculation total coefficents.
   * @param nameIn is name of nominee
   * @param orginizationIn is name of orginization that gave award to nominee
   * @param coefficentIn is how good an orginization is at prediting Oscars
   */
   public Calculations(String nameIn, String orginizationIn,
       double coefficentIn) {
      name = nameIn;
      orginization = orginizationIn;
      coefficent = coefficentIn;
   }
   /**
   * @return the title of film or actor
   */
   public String getName() {
      return name;
   }
   /**
   * sets name of nominee.
   * @param nameIn is movie nominated.
   */
   public void setName(String nameIn) {
      name = nameIn;
   }
   /**
   * @return the title of orginization that gave award to film
   */
   public String getOrginization() {
      return orginization;
   }
   /**
   * @param orginizationIn is the title of 
   * orginization that gave award to film
   */
   public void setOrginization(String orginizationIn) {
      orginization = orginizationIn;
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