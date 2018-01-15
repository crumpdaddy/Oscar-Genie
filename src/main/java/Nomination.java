import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** This program defines the  nomination object.
 * @author Ryan Crumpler
 * @version 15.1.18
 */
public class Nomination implements Serializable {
    protected String name, percent;
    protected double coefficient;
    protected ArrayList<String> awardOrg;
    protected HashMap<String, Double> keywordMap;
    protected int id;

    /**
    * @param nameIn is title of film or name of actor/actress
    */
    public Nomination(String nameIn) {
      name = nameIn;
      coefficient = 0.0;
      keywordMap = new HashMap<>();
      percent = "0.0";
      id = -1;
      awardOrg = new ArrayList<>();
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
     * @param  awardOrgIn is an ArrayList of all award organizations that picked a nomination
     */
    public void setAwardOrg(ArrayList<String> awardOrgIn) {
        awardOrg = awardOrgIn;
    }

    /**
     * @return ArrayList of all award organizations that picked a nomination
     */
    public ArrayList<String> getAwardOrg() {
        return awardOrg;
    }

    /**
     * Gets TMDB ID for a movie
     * @return TMDB ID
     */
    public int getID() {
        return id;
    }

    /**
     * Sets TMDB ID for a movie
     * @param idIn its TMDB ID for a movie
     */
    public void setID(int idIn) {
        id = idIn;
    }

    /**
     * Returns a HashMAp of all keywords for a movie and their frequency
     * @return HAshMap of keywords
     */
    public HashMap<String, Double> getKeywordMap() {
        return keywordMap;
    }

    /**
     * adds keyword to hashmap
     * @param keywordIn keyword to add to hashmap
     */
    public void addKeyword(String keywordIn) {
        if (containsKeyword(keywordIn)) {
            keywordMap.put(keywordIn, keywordMap.get(keywordIn) + 1);
        }
        else {
        keywordMap.put(keywordIn, 1.0);
        }
    }

    /**
     * Checks of hashmap contains keyword
     * @param keywordIn keyword to check if is present
     * @return boolean if keyword is present
     */
    public boolean containsKeyword(String keywordIn) {
        if (keywordMap.containsKey(keywordIn)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns keyword frequency
     * @param keywordIn keyword to check frequency of
     * @return frequency of keyword
     */
    public double getFrequency(String keywordIn) {
        if (containsKeyword(keywordIn)) {
            return keywordMap.get(keywordIn);
        }
        else {
            return 0;
        }
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