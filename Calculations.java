/** This program defines the Nomination object.
* 
* @author Ryan Crumpler
* @version 9.8.17
*/
public class Calculations {
   protected String name, organization;
   protected double coefficient;

   /**
    * Difines a Calculation object that is used in calculation total coefficients.
    * @param nameIn is name of nominee
    * @param organizationIn is name of organization that gave award to nominee
    * @param coefficientIn is how good an organization is at predicting Oscars
    */
   public Calculations(String nameIn, String organizationIn, double coefficientIn) {
      name = nameIn;
      organization = organizationIn;
      coefficient = coefficientIn;
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
    * @return the title of organization that gave award to film
    */
   public String getOrganization() {
      return organization;
   }

   /**
    * @param organizationIn is the title of organization that gave award to film
    * */
   public void setOrganization(String organizationIn) {
      organization = organizationIn;
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
    * */
   public void setCoefficient(double coefficientIn) {
      coefficient = coefficientIn;
   }
}