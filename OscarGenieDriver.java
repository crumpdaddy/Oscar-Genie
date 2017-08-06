import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ArrayList;

/** This program is the driver that carries out all methods 
* to determine chance of winning an oscar.
* 
* @author Ryan Crumpler
* @version 7.30.17
*/
public class OscarGenieDriver {
   private double coefficent;
   protected String name, award, movie, nominee, orginization, percent;
   private HashMap<Integer, HashMap<String, String>> allWinners;
   private HashMap<Integer, HashMap<String, 
      HashMap<String, Calculations>>> calculationByYear;
   private HashMap<Integer, HashMap<String, 
      HashMap<String, Nomination>>> allNominees;
   private HashMap<Integer, HashMap<String, 
      Nomination>> calculatedWinners;
   private HashMap<String, Double> kalmanMap;
   private HashMap<String, Double> orgCount;
   protected static String[] awardList = new String[]{"Best Picture",  
      "Actor - Leading Role", "Actress - Leading Role", 
      "Actor - Supporting Role",  
      "Actress - Supporting Role", "Animated Feature",   
      "Cinematography", "Director", "Documentary", 
      "Costume Design", "Film Editing", 
      "Foreign Language Film", "Score",  
      "Song", "Production Design",   
      "Visual Effects", "Screenplay - Original",
      "Screenplay - Adapted"};
   protected static String[] orgList = new String[]{"Critics Choice", 
      "DC Film Critics",  "Golden Globes-Comedy or Musical", 
      "Golden Globes-Drama", "Houston Critics",
      "New York Film Critics", "Screen Actors Guild", 
      "Golden Globes", "Producers Guild", 
      "LA Film Critics", "Directors Guild", 
      "Writers Guild", "Visual Effects Society"};
   /**
   * This program reads a CSV file that contains every Oscar nominee and winner
   * it then reads a CSV that contains a set of calculations based off of which
   * nominees won awards from other orginizations and adds the calculations and
   * nominees to hashmaps. It then kalman filters the calculations to find the 
   * optimal formula to predict Oscar winners
   *
   */
   public OscarGenieDriver() {
      allNominees = new HashMap<>();
      calculationByYear = new HashMap<>();
      allWinners = new HashMap<>();
      kalmanMap = new HashMap<>();
      orgCount = new HashMap<>();
      calculatedWinners = new HashMap<>();
   }
   /**
   * @return String[[] of list of awards
   * used in OscarGenie to iterate through awards
   */
   public String[] getAwardList() {
      return awardList;
   }
   
   /**
   * @return String[[] of list of awards orginizations
   * used in OscarGenie to iterate through awards orginizations
   */
   public String[] getOrgList() {
      return orgList;
   }
   
   /**
   * Reads in a CSB that contains all Orscar winners
   * Adds the inwwers to a series of HashMaps as follows:
   * allWinners | - key is integer of year
   *            -> winners | - key is name of award
   *                       -?  String of name of winner
   * The layering of objects allows a quick searching of winners
   * @param fileNameIn is the name of the CSV that contains all winners
   * @param yearIn is for year and prev year
   * @throws IOException for scanner  
   */
   public void readWinners(String fileNameIn, int yearIn) throws IOException {  
      Scanner scanFile = new Scanner(new File(fileNameIn));
      int year = yearIn;
      int prevYear = yearIn;
      award = scanFile.nextLine();
      HashMap<String, String> winners = new HashMap<String, String>();
      HashMap<String, String> cloneMap = new HashMap<String, String>(); 
      while (scanFile.hasNextLine()) {  
         nominee = scanFile.nextLine();
         Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
         while (scanProbability.hasNextLine()) { 
            year = Integer.parseInt(scanProbability.next());
            if (year < prevYear) {
               cloneMap = new HashMap<String, String>(winners);
               allWinners.put(prevYear, cloneMap);
               prevYear = year;
               winners.clear();
            }
            award = scanProbability.next();
            name = scanProbability.next();
            winners.put(award, name);
         }
      }      
   } 
   /**
   * Reads as CSV that contains all nominees from 1991 onward. 
   * Adds each nominee to a series of layered hashmaps as follows:
   * allNominees | - key is integer of year
   *             -> nomineesByAward | - key is name of award
   *                                -> nomineeMap | - key is name of nominee
   ^                                              -> Nominee object
   * This layering of HashMaps inside HashMaps allows for groups of Nominees
   * to be accessed by year, by award, or by name very quickly
   * @param fileNameIn is the CSV file that will be read 
   * @param yearIn is for year and prev year
   * @throws IOException for scanner
   */
   public void readNominee(String fileNameIn, int yearIn) throws IOException {
      Scanner scanFile = new Scanner(new File(fileNameIn));
      HashMap <String, Nomination> nomMap = new HashMap<String, Nomination>();
      HashMap <String, Nomination> cloneMap = new HashMap<String, Nomination>();
      HashMap<String, String> winners = new HashMap<String, String>();
      HashMap<String, String> winnersClone = new HashMap<String, String>(); 
      HashMap <String, HashMap<String, Nomination>> nomineeMap = 
         new HashMap <String, HashMap<String, Nomination>>();
      HashMap <String, HashMap<String, Nomination>> cloneMap2 = 
         new HashMap<String, HashMap<String, Nomination>>();
      boolean nextLine = true;
      award = scanFile.nextLine();
      boolean song = false;
      boolean actress = false;
      boolean supporting = false;
      String won = "";
      String notes = "";
      int year = yearIn;
      int pastYear = yearIn;
      String prevAward = "Actor - Leading Role";     
      while (scanFile.hasNextLine()) {
         nominee = scanFile.nextLine(); 
         Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
         while (scanNominee.hasNext()) {
            year = Integer.parseInt(scanNominee.next());
            if (year < pastYear) {
               winnersClone = new HashMap<String, String>(winners);
               allWinners.put(pastYear, winnersClone);
               if (award.equals("Visual Effects")) {
                  cloneMap = new HashMap<String, Nomination>(nomMap);        
                  nomineeMap.put(prevAward, cloneMap);
                  nomMap.clear();
                  prevAward = award;
               }
               cloneMap2 = 
                  new HashMap<String, HashMap<String, Nomination>>(nomineeMap);
               allNominees.put(pastYear, cloneMap2);
               pastYear = year;           
               nomMap.clear();
               nomineeMap.clear();
            }
            award = scanNominee.next();
            if (!award.equalsIgnoreCase(prevAward)) {
               cloneMap = new HashMap<String, Nomination>(nomMap);        
               nomineeMap.put(prevAward, cloneMap);
               nomMap.clear();
               prevAward = award;
            }
            name = scanNominee.next();
            movie = scanNominee.next();             
            if (award.equals("Actor - Leading Role")
               || award.equals("Actor - Supporting Role") 
               || award.equals("Actress - Leading Role")
               || award.equals("Actress - Supporting Role")
               || award.equals("Song")) {
               if (award.equals("Actor - Supporting Role") 
                  || award.equals("Actress - Supporting Role")) {
                  supporting = true;
               }
               if (award.equals("Actress - Supporting Role") 
                  || award.equals("Actress - Leading Role")) {
                  actress = true;
               }
               if (award.compareTo("Song") == 0) {
                  song = true;
               }
               percent = "0";
               BestActor n = new BestActor(name, 0, percent, 
                  movie, actress, supporting, song);
               if (actress) {
                  n.setActress(true);
               }
               if (supporting) {
                  n.setSupporting(true);
               }
               if (song) {
                  n.setSong(true);
               }
               nomMap.put(name, n);            
            }
            else {
               Nomination n = new Nomination(name, 0, percent);
               nomMap.put(name, n);
            }
            won = scanNominee.next(); 
            if (won.equalsIgnoreCase("Yes")) {               
               winners.put(award, name);
            }              
         }
      }
   }    
   
   /**
   * Reads as CSV that contains all calculations from 1991 onward. 
   * Adds each nominee to a series of layered hashmaps as follows:
   * calculationByYear | - key is integer of year
   *            -> calculationByAward | - key is name of award
   *                      ->  calculationByOrg | - key is name orginization name
   ^                                           -> Calculations object
   * This layering of HashMaps inside HashMaps allows for groups of Calculations
   * to be accessed by year, by award, or by name very quickly
   * Before adding the calculation to the calculationByOrg a check is preforemed
   * to see if the name of the Calculation object was actually nominated
   * if it was nominated it is added to the HashMap if not it is ignored
   * @param fileNameIn is the CSV file that will be read 
   * @param yearIn is for year and prev year
   * @throws IOException for scanner
   */
   public void readCalculations(String fileNameIn, 
      int yearIn) throws IOException {  
      Scanner scanFile = new Scanner(new File(fileNameIn));
      HashMap<String, Calculations> calculationByOrg = 
         new HashMap<String, Calculations>();
      HashMap<String, HashMap<String, Calculations>> calculationByAward = 
         new HashMap<String, HashMap<String, Calculations>>();
      HashMap<String, Calculations> cloneMap1 = 
         new HashMap<String, Calculations>();
      HashMap<String, HashMap<String, Calculations>> cloneMap2 =
          new HashMap<String, HashMap<String, Calculations>>();
      HashMap<String, String> winners = new HashMap<String, String>();
      int year = yearIn;
      int prevYear = yearIn;
      award = scanFile.nextLine();
      String kalKey = "";
      double kal = 0;
      double kalCount = 0;
      String prevAward = "Actor - Leading Role";
      Nomination p = new Nomination("", 0, ""); 
      while (scanFile.hasNextLine()) {  
         nominee = scanFile.nextLine();
         Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
         while (scanProbability.hasNext()) { 
            year = Integer.parseInt(scanProbability.next());
            if (year < prevYear) {
               cloneMap2 = new HashMap<String, 
                  HashMap<String, Calculations>>(calculationByAward);
               calculationByYear.put(prevYear, cloneMap2);
               prevYear = year;
               calculationByAward.clear();
            }
            HashMap<String, HashMap<String, Nomination>> map1 = 
               new HashMap<String, HashMap<String, Nomination>>();
            HashMap<String, Nomination> map2 = 
               new HashMap<String, Nomination>();
            map1 = allNominees.get(year);
            award = scanProbability.next();
            if (!award.equalsIgnoreCase(prevAward)) {
               cloneMap1 = new HashMap<String, Calculations>(calculationByOrg);
               calculationByAward.put(prevAward, cloneMap1);
               calculationByOrg.clear();
               prevAward = award;
               
            }
            try {
               map2 = map1.get(award);
            }
            catch (NullPointerException e) {
            }
            name = scanProbability.next();           
            orginization = scanProbability.next();
            Calculations c = new Calculations(name, orginization, 0);
            kalKey = orginization + " " + award;
            try {
               kalCount = orgCount.get(kalKey);
               kalCount += 1;              
            }
            catch (NullPointerException e) {
               kalCount = 1;
            }
            orgCount.put(kalKey, kalCount);
            if (map2 != null && map2.containsKey(name)) {
               calculationByOrg.put(orginization, c);
               winners = allWinners.get(year);
               if (winners.get(award).equalsIgnoreCase(name)) {
                  try {
                     kal = kalmanMap.get(kalKey);
                     kal += 1;              
                  }
                  catch (NullPointerException e) {
                     kal = 1;
                  }
                  kalmanMap.put(kalKey, kal);
               } 
            }
         }
      } 
      cloneMap2 =
         new HashMap<String, HashMap<String, Calculations>>(calculationByAward);
      calculationByYear.put(prevYear, cloneMap2);     
   }
   
   /**
   * Sets the coefficents of all nominees and puts the 
   * calculations in kalmanMap.
   */
   public void setCoefficents() {
      String kalKey = "";
      double kalCount = 0;
      double kalmanCoeff = 0;
      double newCoeff = 0;
      for (int i = 0; i < awardList.length; i++) {
         for (int j = 0; j < orgList.length; j++) {
            kalKey = orgList[j] + " " + awardList[i];
            try {
               kalCount = orgCount.get(kalKey);
               kalmanCoeff = kalmanMap.get(kalKey);
               kalmanCoeff = kalmanCoeff / kalCount;
               kalmanMap.put(kalKey, kalmanCoeff);
            }
            catch (NullPointerException e) {                
            }
         }
      }
   }
   
   /**
   * Adjusts the values in the kalmanMap. If initial is 1 it increases the value
   * if initial is -1 it decreases the value. 
   * @param kalKey is the kalmanKey that stores the coefficent 
   * in the kalmanMap hashmap
   * @param initial is used to set how the coefficent is adjusted.
   * @param scalar defines how much to scale kalman filter
   */
   public void adjustKalmanFilter(double scalar, String kalKey, int initial) {
      double kalmanCoeff = 0;
      double newCoeff = 0;
      try {
         kalmanCoeff = kalmanMap.get(kalKey);
         if (initial < 0) {          
            newCoeff = kalmanCoeff / scalar;
            if (kalmanCoeff == 0) {
               kalmanMap.put(kalKey, kalmanCoeff);
            }
            else {
               kalmanMap.put(kalKey, newCoeff);
            }             
         }
         if (initial > 0) {
            newCoeff = kalmanCoeff * scalar;
            if (kalmanCoeff == 0) {
               kalmanMap.put(kalKey, kalmanCoeff);
            }
            else {
               kalmanMap.put(kalKey, newCoeff);
            }
         } 
      }             
      catch (NullPointerException e) {                
      }
   }
   
   /**
   * Sets all the calculations in a given year by 
   * scanning both the calculations hashmaps and setting the coefficents
   * to their respective nominee.
   * @param year is the year of the calculations to set
   */
   public void setAllCalculationsCoeff(int year) {
      String kalKey = "";
      double getCoeff = 0;
      double kalmanCoeff = 0;
      HashMap<String, Calculations> coeffByOrg = 
         new HashMap<String, Calculations>();
      HashMap<String, HashMap<String, Calculations>> coeffByAward = 
         new HashMap<String, HashMap<String, Calculations>>();
      coeffByAward = calculationByYear.get(year);
      HashMap <String, Nomination> nominations =
          new HashMap<String, Nomination>();
      HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
         new HashMap<String, HashMap<String, Nomination>>();
      coeffByAward = calculationByYear.get(year);
      nominationsByAward = allNominees.get(year);
      for (int i = 0; i < awardList.length; i++) {
         coeffByOrg = coeffByAward.get(awardList[i]);
         nominations = nominationsByAward.get(awardList[i]);
         for (int j = 0; j < orgList.length; j++) {
            kalKey = orgList[j] + " " + awardList[i];
            Calculations c = new Calculations("", "", 0);
            Nomination n = new Nomination("", 0, "");
            try {
               c = coeffByOrg.get(orgList[j]); 
               kalmanCoeff = kalmanMap.get(kalKey);
               kalmanCoeff = kalmanCoeff + c.getCoefficent();
               c.setCoefficent(kalmanCoeff); 
               coeffByOrg.put(orgList[j], c);
               try {
                  name = c.getName();
                  n = nominations.get(name);
                  n.setCoefficent(kalmanCoeff);
               }
               catch (NullPointerException e) {                           
               }
            }
            catch (NullPointerException e) {                  
            }
            coeffByAward.put(awardList[i], coeffByOrg);
         }
         calculationByYear.put(year, coeffByAward);
      }   
   }
   
   /**
   * Generates the winners based off the nominee with the highest coefficent.
   * @param awardIn is the award to generate winners for
   * @param minYear is the earliest year in which the CSV files support
   * @param maxYear is the latest year that the CSVs support
   * @return a hashmap of winners where key is year and the 
   * data is the winner's name
   */
   public HashMap<Integer, String> generateWinbyAward(String awardIn, 
      int minYear, int maxYear) {
      HashMap<Integer, String> output = new HashMap<Integer, String>();
      HashMap <String, Nomination> nominations =
          new HashMap<String, Nomination>();
      HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
         new HashMap<String, HashMap<String, Nomination>>();
      for (int j = minYear; j <= maxYear; j++) {
         nominationsByAward = allNominees.get(j);
         nominations = nominationsByAward.get(awardIn);
         Nomination highestCoeff = new Nomination("", 0, "");
         try {
            for (String key : nominations.keySet()) {
               Nomination n = new Nomination("", 0, "");
               n = nominations.get(key);
               if (highestCoeff.getName().equals("")) {
                  highestCoeff = n;
               }
               if (n.getCoefficent() > highestCoeff.getCoefficent()) {
                  highestCoeff = n;
               }
            }
            output.put(j, highestCoeff.getName());
         }
         catch (NullPointerException e) {                  
         }
      }
      return output;
   }
   
   /**
   * resets the coefficents for all nominees for agiven award and year.
   * @param awardIn is award for nominees
   * @param year is award year
   */
   public void resetNominees(String awardIn, int year) {
      HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
         new HashMap<String, HashMap<String, Nomination>>(); 
      HashMap <String, Nomination> nominations =
          new HashMap<String, Nomination>();
      nominationsByAward = allNominees.get(year);
      nominations = nominationsByAward.get(awardIn);
      try {
         for (String key : nominations.keySet()) {    
            Nomination n = new Nomination("", 0, "");
            n = nominations.get(key);
            n.setCoefficent(0);
            nominations.put(key, n);
         }        
      }
      catch (NullPointerException e) {                       
      } 
      nominationsByAward.put(awardIn, nominations);  
      allNominees.put(year, nominationsByAward); 
   }
   
   /**
   * Works similarly to setAllCalculationsCoeff except insead of setting by
   * year this method sets coefficents by award.
   * @param awardIn is the name of the award to set coefficents to
   * @param minYear is the earliest year in which the CSV files support
   * @param maxYear is the latest year that the CSVs support
   */
   public void setCalculationsCoeff(String awardIn, int minYear, int maxYear) {
      String kalKey = "";
      double getCoeff = 0;
      double kalmanCoeff = 0;
      HashMap<String, Calculations> coeffByOrg = 
         new HashMap<String, Calculations>();
      HashMap<String, HashMap<String, Calculations>> coeffByAward = 
         new HashMap<String, HashMap<String, Calculations>>();
      HashMap <String, Nomination> nominations =
          new HashMap<String, Nomination>();
      HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
         new HashMap<String, HashMap<String, Nomination>>();      
      for (int i = minYear; i <= maxYear; i++) {
         resetNominees(awardIn, i);
         coeffByAward = calculationByYear.get(i);
         nominationsByAward = allNominees.get(i);
         coeffByOrg = coeffByAward.get(awardIn);
         nominations = nominationsByAward.get(awardIn);
         for (int j = 0; j < orgList.length; j++) {
            kalKey = orgList[j] + " " + awardIn;
            Calculations c = new Calculations("", "", 0);
            Nomination n = new Nomination("", 0, "");
            try {
               c = coeffByOrg.get(orgList[j]);               
               double coef = kalmanMap.get(kalKey);
               kalmanCoeff = coef + c.getCoefficent();
               c.setCoefficent(coef);
               coeffByOrg.put(orgList[j], c);
               try {
                  name = c.getName();
                  n = nominations.get(name);
                  double totalCoeff = n.getCoefficent() + coef;
                  n.setCoefficent(totalCoeff);
               }
               catch (NullPointerException e) {                           
               }
            }
            catch (NullPointerException e) {                       
            }
            coeffByAward.put(awardIn, coeffByOrg);
         }
         calculationByYear.put(i, coeffByAward);
      }   
   }
   
   /**
   * Determines how accurate a prediction is by comparing a 
   * hashmap of predicted winners to a hashmap of known winners.
   * @param awardIn is the name of the award that will be comapred to the winner
   * @param calcInput is a hashmap of predicted winners supplied by the 
   * generateWinbyAward method
   * @param minYear is the earliest year in which the CSV files support
   * @param maxYear is the latest year that the CSVs support
   * @return a double representing a percent of correctly predicted winners
   */
   public double findAccuracy(String awardIn, HashMap<Integer, 
      String> calcInput, int minYear, int maxYear) {
      HashMap<String, String> winners = new HashMap<String, String>();
      Nomination n = new Nomination("", 0, "");
      String calcName = "";
      double calcCount = 0;
      for (int i = minYear; i <= maxYear; i++) {
         winners = allWinners.get(i);
         calcName = calcInput.get(i);
         try {
            if (calcName.equalsIgnoreCase(winners.get(awardIn))) {
               calcCount++;
            }
         }
         catch (NullPointerException e) {                  
         }           
      }
      double range = maxYear - minYear;
      calcCount = calcCount / range;
      return calcCount;
   }
   
   /**
   * Returns all winners in a given year.
   * @param year is the year for which to calculate the winners
   * @return HashMap<String, String> which is a hashmap of of all the winners 
   * in a year key is the award and data is the winner
   */
   public HashMap<String, String> returnWinnersByYear(int year) {
      HashMap<String, String> output = new HashMap<String, String>();
      HashMap <String, Nomination> nominations =
          new HashMap<String, Nomination>();
      HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
         new HashMap<String, HashMap<String, Nomination>>();
      nominationsByAward = allNominees.get(year);
      for (int i = 0; i < awardList.length; i++) {   
         nominations = nominationsByAward.get(awardList[i]);
         Nomination highestCoeff = new Nomination("", 0, "");       
         try {
            for (String key : nominations.keySet()) {
               Nomination n = new Nomination("", 0, "");
               n = nominations.get(key);
               if (highestCoeff.getName().equals("")) {
                  highestCoeff = n;
               }
               if (n.getCoefficent() > highestCoeff.getCoefficent()) {
                  highestCoeff = n;
               }
            }
            if (highestCoeff.getCoefficent() <= .25) {
               highestCoeff.setName("");
            }
            output.put(awardList[i], highestCoeff.getName());
         }
         catch (NullPointerException e) {                  
         }
      }
      return output;
   }
    
   /**
   * Recursive method to adjust the initial coefficents 
   * to better predict the winners.
   * more is 
   * generally more accurate
   * @param minYear is the earliest year in which the CSV files support
   * @param maxYear is the latest year that the CSVs support
   */
   public void kalmanFilter(int minYear, int maxYear) {   
      ArrayList<HashMap<String, Double>> iterationList = 
         new ArrayList<HashMap<String, Double>>();
      HashMap<Integer, String> winners = new HashMap<Integer, String>(); 
      double calcCount = 0;
      double prevCalcCount = 0;
      String kalKey = "";
      for (int j = 0; j < awardList.length; j++) {
         setCalculationsCoeff(awardList[j], minYear, maxYear);
         winners = generateWinbyAward(awardList[j], minYear, maxYear);
         calcCount = findAccuracy(awardList[j], winners, minYear, maxYear);
         prevCalcCount = calcCount;
         for (int k = 0; k < orgList.length; k++) {
            kalKey = orgList[k] + " " + awardList[j];
            adjustKalmanFilter(1.5, kalKey, 1);
            setCalculationsCoeff(awardList[j], minYear, maxYear);
            winners = generateWinbyAward(awardList[j], minYear, maxYear);
            calcCount = findAccuracy(awardList[j], winners, 
               minYear, maxYear);
            if (calcCount >= prevCalcCount) {              
               while (calcCount > prevCalcCount) {
                  prevCalcCount = calcCount;
                  adjustKalmanFilter(1.5, kalKey, 1);
                  setCalculationsCoeff(awardList[j], minYear, maxYear);
                  winners = generateWinbyAward(awardList[j], minYear, maxYear);
                  calcCount = findAccuracy(awardList[j], 
                        winners, minYear, maxYear);
               }
               adjustKalmanFilter(1.5, kalKey, -1);           
               setCalculationsCoeff(awardList[j], minYear, maxYear);
               winners = generateWinbyAward(awardList[j], 
                  minYear, maxYear);
               calcCount = findAccuracy(awardList[j], 
                  winners, minYear, maxYear);
               prevCalcCount = calcCount;
            }
            else if (calcCount <= prevCalcCount) {              
               prevCalcCount = calcCount;
               adjustKalmanFilter(1.5, kalKey, -1);
               setCalculationsCoeff(awardList[j], minYear, maxYear);
               winners = generateWinbyAward(awardList[j], 
                  minYear, maxYear);
               calcCount = findAccuracy(awardList[j], winners, 
                  minYear, maxYear);
               while (calcCount > prevCalcCount) {
                  prevCalcCount = calcCount;
                  adjustKalmanFilter(1.5, kalKey, -1);
                  setCalculationsCoeff(awardList[j], minYear, maxYear);
                  winners = generateWinbyAward(awardList[j], 
                     minYear, maxYear);
                  calcCount = findAccuracy(awardList[j], winners, 
                     minYear, maxYear);
               }
               adjustKalmanFilter(1.5, kalKey, 1);
               setCalculationsCoeff(awardList[j], minYear, maxYear);
               winners = generateWinbyAward(awardList[j], 
                  minYear, maxYear);
               calcCount = findAccuracy(awardList[j], winners, 
                  minYear, maxYear);
               prevCalcCount = calcCount;
            }
         }
      }    
   }
   
   /**
   * Uses the coefficents to calculate the percents as well
   * as well as the the most likely nominee to win.
   * @param minYear is the mininum year to start calculating
   * @param maxYear is maximum year to start calculating
   */
   public void getProbability(int minYear, int maxYear) {
      HashMap <String, Nomination> nominations =
          new HashMap<String, Nomination>();
      HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
         new HashMap<String, HashMap<String, Nomination>>(); 
      DecimalFormat numFmt = new DecimalFormat("#00.00");
      for (int i = minYear; i <= maxYear; i++) {
         nominationsByAward = allNominees.get(i);
         HashMap <String, Nomination> highest =
            new HashMap<String, Nomination>();
         for (int j = 0; j < awardList.length; j++) {
            nominations = nominationsByAward.get(awardList[j]);
            Nomination n = new Nomination("", 0, "");
            Nomination highestNominee = new Nomination("", 0, "");
            double totalCoeff = 0;
            try {
               for (String key : nominations.keySet()) {
                  n = nominations.get(key);
                  totalCoeff += n.getCoefficent();
                  nominations.put(n.getName(), n);
               }
            }
            catch (NullPointerException e) {                  
            }
            try {
               double perc = 0;
               for (String key : nominations.keySet()) {
                  n = nominations.get(key);
                  if (n.getCoefficent() < 1) {
                     perc = n.getCoefficent() * 100;
                  }
                  else {
                     perc = n.getCoefficent() / totalCoeff;
                     perc = perc * 100;
                  }
                  n.setPercent(numFmt.format(perc));
                  if (n.getCoefficent() > highestNominee.getCoefficent()) {
                     highestNominee = n;
                  }
                  nominations.put(n.getName(), n);                  
               }
            }
            catch (NullPointerException e) {                  
            }
            highest.put(awardList[j], highestNominee);
            nominationsByAward.put(awardList[j], nominations);
         }
         allNominees.put(i, nominationsByAward);
         calculatedWinners.put(i, highest);
      }
   }
   
   /**
   * Output all contents of nominees in winnerMap toString()
   * if there is no winner for a category it outputs that it can't
   * predict a winner.
   * @param year is year to generate all winners for
   * @return string of winners
   */
   public String generateAll(int year) {
      HashMap <String, Nomination> highest =
          new HashMap<String, Nomination>();
      String output = "";
      highest = calculatedWinners.get(year);
      for (int i = 0; i < awardList.length; i++) {
         Nomination n = highest.get(awardList[i]);
         output += awardList[i] + ": ";
         if (n.getName().equals("")) {
            output += "Cannot calculate "
               + "winners because there is insufficent data\n";       
         }
         else {
            output += n.toString() + "\n";
         }
      }    
      return output;
   }
   
   /**
   * Prints actor/nomination and all awards it won based off calculations.
   * @param category defines category of award to be calculated
   * @param year is the year to calculate
   * @return string projected  winner and all previous awards
   */
   public String getDetail(int year, String category) {
      String output = "";
      HashMap <String, Nomination> highest =
         new HashMap<String, Nomination>();
      highest = calculatedWinners.get(year);
      HashMap<String, Calculations> calcByOrg = 
         new HashMap<String, Calculations>();
      HashMap<String, HashMap<String, Calculations>> calcByAward = 
         new HashMap<String, HashMap<String, Calculations>>();
      calcByAward = calculationByYear.get(year);
      Nomination x = highest.get(category);
      calcByOrg = calcByAward.get(category);
      if (category.equals("Best Actor")
         || category.equals("Best Supporting Actor")
         || category.equals("Best Actress") 
         || category.equals("Best Supporting Actress")
         || category.equals("Best Original Song")) {
         if (x.getCoefficent() <= .25) {
            output = "Oscar Genie cannot accurately predict the winner " 
               + "due to lack of data.\nThe best guess is: " + x.getName()
               + "as it also won awards from:\n";  
         }
         else {
            output = x.getName() + " is most "
               + "likely to win an Oscar because they"
               + " also won awards this year from:\n";
         }
         for (String key : calcByOrg.keySet()) {
            if (calcByOrg.get(key).getName().equals(x.getName())) {
               output += calcByOrg.get(key).getOrginization() + "\n";
            }     
         }   
      }
      else {
         if (x.getCoefficent() <= .25) {
            output = "Oscar Genie cannot accurately predict the winner " 
               + "due to lack of data.\nThe best guess is: " + x.getName()
               + "as it also won awards from:\n"; 
         }
         else {
            output = "The film " + x.getName() + " is likely to win because"
               + " it also won awards this year from:\n";
         }
         for (String key : calcByOrg.keySet()) {
            if (calcByOrg.get(key).getName().equals(x.getName())) {
               output += calcByOrg.get(key).getOrginization() + "\n";
            }          
         }
      }
      return output;
   }
   
   /** 
   * returns string of each nominee toString() for a given category
   * If there are no winners the user is notified.
   * @param category is award category
   * @param year is year to calculate
   * @return toString of all nominees
   */
   public String returnResults(int year, String category) {
      String output = category + ":\n";
      HashMap <String, Nomination> highest =
          new HashMap<String, Nomination>();
      highest = calculatedWinners.get(year);
      if (highest.get(category).getPercent().equals("0.0")) {
         output = "\nCannot calculate winners because there is insufficent "
            + "data for " + category;
      }
      else {
         HashMap <String, Nomination> nominees =
            new HashMap<String, Nomination>();
         HashMap <String, HashMap<String, Nomination>> nominationsByAward = 
            new HashMap<String, HashMap<String, Nomination>>(); 
         nominationsByAward = allNominees.get(year);
         nominees = nominationsByAward.get(category);
         for (String key : nominees.keySet()) {                     
            Nomination p = nominees.get(key);
            output += p.toString() + "\n";
         } 
      }
      return output;
   } 
   
   /**
   * Compares predicted winners to actual winners and outputs
   * correct winners and percent of winners correctly predicted.
   * @param year is year to get details for
   * @return String output which prints all data
   */
   public String allDetails(int year) {
      DecimalFormat numFmt = new DecimalFormat("#00.00");
      String output = "";
      HashMap<String, String> winners = new HashMap<String, String>();
      HashMap<String, String> predictedWinners = new HashMap<String, String>();
      predictedWinners = returnWinnersByYear(year);
      winners = allWinners.get(year);
      double count = 0;
      double correct = 0;
      double perc = 0;
      for (int i = 0; i < awardList.length; i++) {
         try {
            output += "The actual winner for " + awardList[i] + " is " 
               + winners.get(awardList[i]) + ":\n";
            if (predictedWinners.get(awardList[i]).equals("")) {
               output += "Oscar Genie did not have enough data "
                  + "to predict this\n";
            }
            else if (predictedWinners.get(awardList[i]).equalsIgnoreCase(
               winners.get(awardList[i]))) {
               output += " Oscar Genie correctly predicted this winner\n";
               correct++;
               count++;
            }
            else {
               output += " Oscar Genie did not predict this winner\n";
               count++;
            }
         }
         catch (NullPointerException e) {
         }
      }
      perc = correct / count;
      perc = perc * 100;
      output += "Oscar Genie was able to predict " + correct + " out of "
         + count + " which is " + numFmt.format(perc) 
         + "% of winners it had data for\n";
      return output;
   } 
   
   /**
   * prints help text.
   * @return String output of help text
   */
   public String printHelp() {
      String output = "Enter either the number "
         + "corresponding to the selection you wish to "
         + "predict or\nyou can enter the name of the award\n"
         + "Case does not matter\n"
         + "please email any bugs or errors "
         + "to rjcrumpler@gmail.com";
      return output;
   }
   
   /**
   * prints info text.
   * @return String output of info text
   */
   public String printInfo() {
      String output = "This program was written by Ryan "
         + "Crumpler\n" + getTotalPercent(1991, 2017)
         + "It works by scanning scources such "
         + "as movie critic assosciationsx\n and determining "
         + "how effective each source is at predicting Oscar "
         + "winners\nfor each category It then weights each "
         + "source and makes a prediction\nbased on all the "
         + "sources and outputs the results.\n***Please Note***\n"
         + "Generally an output of\n0.00% indicates insufficent"
         + "\ndata for that nominee\nIF Oscar Genie only has 1"
         + "nominee with data and\nthe nominee has less than a 25%"
         + " chance of winning Oscar Genie consideres\nthat award"
         + "to have insufficent data";
      return output;
   }
   
   /**
   * Calculates the percent correct preditions program made.
   * @param minYear is first year to srart calculating
   * @param maxYear is last year to start calculating
   * @return String output that prints percent correct
   */
   public String getTotalPercent(int minYear, int maxYear) {
      DecimalFormat numFmt = new DecimalFormat("#00.00");
      String output = "";
      HashMap<String, String> winners = new HashMap<String, String>();
      HashMap<String, String> predictedWinners = new HashMap<String, String>();
      double count = 0;
      double correct = 0;
      double perc = 0;
      for (int j = minYear; j <= maxYear; j++) {
         predictedWinners = returnWinnersByYear(j);
         winners = allWinners.get(j);
         for (int i = 0; i < awardList.length; i++) {
            try {
               if (predictedWinners.get(awardList[i]).equalsIgnoreCase(
                  winners.get(awardList[i]))) {
                  correct++;
                  count++;
               }
               else if (!predictedWinners.get(awardList[i]).equals("")) {
                  count++;
               }
            }
            catch (NullPointerException e) {
            }
         }
      }
      perc = correct / count;
      perc = perc * 100;
      output += "Oscar Genie was able to predict " + numFmt.format(perc) 
         + "% of winners it had data for.\n";
      return output;
   } 
}  