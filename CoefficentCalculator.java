/** This program defines the CoefficentCalculation object.
* @author Ryan Crumpler
* @version 3.1.17
*/
public class CoefficentCalculator {
   protected String award, orginization;
   protected double coefficent;
   /**
   * Difines a CoefficentCalculator object that is used to read coefficents for
   * all awards and orginizations.
   * @param awardIn is name of award
   * @param orginizationIn is name of orginization that gave award to nominee
   * @param coefficentIn is how good an orginization is at prediting Oscars
   */
   public CoefficentCalculator(String awardIn, String orginizationIn,
      double coefficentIn) {    
      award = awardIn;
      orginization = orginizationIn;
      coefficent = coefficentIn;
   }
   /**
   * sets name of award.
   * @param awardIn is name of award.
   */
   public void setAward(String awardIn) {
      award = awardIn;
   }
   /**
   * @return the name of award.
   */
   public String getAward() {
      return award;
   }
   /**
   * @param orginizationIn is the title of 
   * orginization that gave award to film
   */
   public void setOrginization(String orginizationIn) {
      orginization = orginizationIn;
   }
   /**
   * @return the title of orginization that gave award to film
   */
   public String getOrginization() {
      return orginization;
   }
   /**
   * sets the coefficent used to calculate chance of winning.
   * @param coefficentIn is coefficent for calculations
   */
   public void setCoefficent(double coefficentIn) {
      coefficent = coefficentIn;
   }
   /**
   * @return the coefficent used to calculate chance of winning
   */
   public double getCoefficent() {
      return coefficent;
   }
}