import java.util.ArrayList;
import java.util.HashMap;

/** This program defines the  nomination object.
 *  This is used for actors as well as film objects
 * @author Ryan Crumpler
 * @version 28.12.17
 */
public class Nomination {
    private String name, percent;
    private double coefficient;
    private ArrayList<String> awardOrg;
    private HashMap<String, Integer> keywordMap;
    private int id;

    /**
    * @param nameIn is title of film or name of actor/actress
    * @param coefficientIn is constant used to determine the likelihood of winning calculated through other awards
    * @param percentIn is percent chance of winning
    */
    public Nomination(String nameIn, double coefficientIn, String percentIn, ArrayList<String> awardOrgIn) {
      name = nameIn;
      coefficient = coefficientIn;
      awardOrg = awardOrgIn;
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
    public HashMap<String, Integer> getKeywordMap() {
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
            keywordMap.put(keywordIn, 1);
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
    public int getFrequency(String keywordIn) {
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