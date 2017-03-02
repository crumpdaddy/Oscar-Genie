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
* @version 2.26.17
*/
public class OscarGenieDriver {
   private double coefficent;
   protected String name, award, movie, nominee, orginization, percent;
   private HashMap<String, Double> totalMap;
   private HashMap<String, ArrayList<Calculations>> calcMap;
   private HashMap<String, HashMap<String, Nomination>> nomineeMap;
   private HashMap<String, File> yearHash;
   protected Nomination highestNominee;
   private HashMap<String, Nomination> winnerMap;
   private HashMap<String, CoefficentCalculator> coeffMap;
   protected static String[] awardList = new String[]{"Best Picture",  
      "Best Actor", "Best Actress", "Best Supporting Actor",  
      "Best Supporting Actress", "Best Animated Feature",   
      "Best Cinematography", "Best Director", "Best Documentary Feature", 
      "Best Costume Design", "Best Film Editing", "Best Foreign Language Film",
      "Best Makeup and Hairstyling", "Best Original Score",  
      "Best Original Song", "Best Production Design",   
      "Best Visual Effects", "Best Original Screenplay",
      "Best Adapted Screenplay"};
   /**
   * This program reads a CSV file that shows the category 
   * of the award and all the nominees and stores all the nominees as 
   * either bestActor or bestPicture objects. It then reads a CSV file that
   * shows a list of criteria used to measure oscar nominee chance of winning as
   * well as how effective that metric is at 
   * predicting oscar winners. It stores 
   * all nominee objects in a hashMap with the key being their name
   * and puts that that hashmap in a hashmap with a key being the award, this 
   * ensures O(N) efficancy across all methods. 
   * It stores the total coefficent
   * for each category in a hashmap, this is used to 
   * calculate percent chance of winning.
   * nomineeMap defines a hashmap of a hashmap of Nomination  objects
   * totalMap defines a hashmap containing the sum of coefficents for
   * each category
   * calcMap defines a hashmap of doubles of the 
   * individual coefficents that are summed to give 
   * each nominee their total coefficent used in calculations
   * winnerMap is a hashMap of all projected winners for each category
   * awardList is a static String[] that contains all the award categories
   * coeffMap is hashMap of CoefficentCalculation objects
   */
   public OscarGenieDriver() {
      totalMap = new HashMap<>();
      calcMap = new HashMap<>();
      yearHash = new HashMap<>();
      nomineeMap = new HashMap<>();
      highestNominee = new Nomination("", 0, "");
      winnerMap = new HashMap<>();
      coeffMap = new HashMap<>();
   }
   /**
   * Takes reads all files in the folder of the specified year
   * and adds all file names to a hash map.
   * @param yearIn is the folder that will have its contents read
   * @throws IOException for scanner
   */
   public void yearSet(String yearIn) throws IOException {
      File folder = new File(yearIn);
      File[] fileList = folder.listFiles();
      for (int i = 0; i < fileList.length; i++) {
         File file = fileList[i];
         yearHash.put(file.getName(), file);
      }
   }
   /**
   * @return String[[] of list of awards
   * used in OscarGenie to iterate through awards
   */
   public String[] getAwardList() {
      return awardList;
   }
   /**
   * Reads CSV that contains every award orginization and their 
   * coefficent for predicting a particular Oscar category.
   * Each line is read and added to a CoefficentCalculator object and
   * those objects are added to a hashMap coeffMap that contains
   * the CoefficentCalculatior objects and uses the 
   * award + orginizations as a key
   * @throws IOException for scanner
   */
   public void scanCoefficents() throws IOException {
      Scanner scanFile = new Scanner(new File("CoefficentList.csv"));
      String line = "";
      int count = 0;
      while (scanFile.hasNextLine()) {
         line = scanFile.nextLine();
         Scanner scanLine = new Scanner(line).useDelimiter(",");
         award = scanLine.next();
         String prevAward = "";
         orginization = scanLine.next();
         coefficent = Double.parseDouble(scanLine.next());
         CoefficentCalculator n = new CoefficentCalculator(award, 
            orginization, coefficent);
         coeffMap.put(award + orginization, n);      
      }
   }
   /**
   * reads CSV that contains the nominee and any info 
   * exclusive to bestActor objects.
   * Adds nominees to a hashmap with the key being the actor/film name 
   * and adds that hashmap to another hashMap nomineeMap with a key of the award
   * @param fileNameIn is the CSV file that will be read 
   * @throws IOException for scanner
   */
   public void readNominee(String fileNameIn) throws IOException {
      Scanner scanFile = new Scanner((yearHash.get(fileNameIn)));
      HashMap <String, Nomination> nomMap = new HashMap<String, Nomination>();
      boolean nextLine = true;
      award = scanFile.nextLine();
      boolean song = false;
      boolean actress = false;
      boolean supporting = false;
      if (award.equals("Best Actor,")
         || award.equals("Best Supporting Actor,") 
         || award.equals("Best Actress,")
         || award.equals("Best Supporting Actress,")
         || award.equals("Best Original Song,")) {
         award = award.substring(0, award.length() - 1);
         if (award.equals("Best Supporting Actor") 
            || award.equals("Best Supporting Actress")) {
            supporting = true;
         }
         if (award.equals("Best Supporting Actress") 
            || award.equals("Best Actress")) {
            actress = true;
         }
         if (award.compareTo("Best Original Song") == 0) {
            song = true;
         }
         while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine(); 
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNextLine()) {
               name = scanNominee.next();
               movie = scanNominee.next();
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
               nomineeMap.put(award, nomMap);
            }
         }
      }
      else {
         while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine(); 
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNextLine()) {
               name = scanNominee.next();
               Nomination n = new Nomination(name, 0, percent);
               nomMap.put(name, n);
               nomineeMap.put(award, nomMap);
            }
         }        
      }
   }
   /**
   * Reads CSV file that contains award in first line followed
   * by nominee, the institution that gave an award to the nominee
   * and the coefficent that is a percent that shows how often the 
   * the institutional award winner receives an oscar.
   * The coefficents each nominee receives are summed and stored in
   * a calcList and at the end of scanning a file that list is added to
   * calcMap with a key of the award
   * also determines the most probable nominee by getting the nominee
   * with the highest coefficent.
   * All the coefficents are summed in totalCoefficent and 
   * the totalCoefficent is put in to totalMap which has a key of the award.
   * @param fileNameIn is the CSV file that will be read 
   * @throws IOException for scanner
   */
   public void getProbability(String fileNameIn) throws IOException {  
      Scanner scanFile = new Scanner((yearHash.get(fileNameIn)));
      double totalCoeff = 0;
      ArrayList<Calculations> calcList = new ArrayList<Calculations>();
      Nomination p = new Nomination("", 0, ""); 
      award = scanFile.nextLine(); 
      award = award.substring(0, award.length() - 1);
      HashMap <String, Nomination> nomMap = nomineeMap.get(award);
      while (scanFile.hasNextLine()) {  
         nominee = scanFile.nextLine();
         Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
         while (scanProbability.hasNextLine()) { 
            name = scanProbability.next();
            orginization = scanProbability.next();
            if (coeffMap.containsKey(award + orginization)) {
               coefficent = coeffMap.get(award + orginization).getCoefficent();
            }
            else {
               coefficent = 0;
            }
            Calculations n = new Calculations(name, 
               orginization, coefficent);          
            if (nomMap.containsKey(name)) {
               p = nomMap.get(name);
               if (name.equals(p.getName())) {
                  p.setCoefficent(p.getCoefficent() + coefficent);
                  calcList.add(n);
                  totalCoeff += coefficent;
                  totalMap.put(award, totalCoeff);
                  calcMap.put(award, calcList);
                  if (calcList.size() == 1) {
                     highestNominee = p;
                     winnerMap.put(award, p);
                  }
                  else if (p.getCoefficent() > highestNominee.getCoefficent()) {
                     highestNominee = p;
                     winnerMap.put(award, p);
                  } 
               } 
            }      
         }
      }      
   }
   /**
   * Iterates through the award category and sets the probability
   * that a nominee will win based on their coefficent divided by the
   * totalCoefficent for that category * 100 to get a perent.
   * @param category defines category of award to be calculated
   */
   public void generateProbability(String category) {
      double perc = 0;
      
      HashMap<String, Nomination> nomMap = nomineeMap.get(category);
      ArrayList<String> keyList = new ArrayList<String>(nomMap.keySet());
      for (int i = 0; i < keyList.size(); i++) {
         Nomination p = nomMap.get(keyList.get(i));
         DecimalFormat numFmt = new DecimalFormat("#00.00");
         if (totalMap.get(category) == null) {
            perc = 00.00;
         }
         else {
            perc = (p.getCoefficent() / totalMap.get(category)) * 100;  
         }
         p.setPercent(numFmt.format(perc));   
      }
   }
   /**
   * Output all contents of nominees in winnerMap toString()
   * if there is no winner for a category it outputs that it can't
   * predict a winner.
   * @return string of winners
   */
   public String generateAll() {
      String output = "";
      for (int i = 0; i < awardList.length; i++) {
         Nomination n = winnerMap.get(awardList[i]);
         output += awardList[i] + ": ";
         if (n == null) {
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
   * @return string projected  winner and all previous awards
   */
   public String generateDetails(String category) {
      String output = "";
      Nomination x = winnerMap.get(category);
      ArrayList<Calculations> cal = new ArrayList<Calculations>();
      cal = calcMap.get(category);
      if (category.equals("Best Actor")
         || category.equals("Best Supporting Actor")
         || category.equals("Best Actress") 
         || category.equals("Best Supporting Actress")
         || category.equals("Best Original Song")) {
         output = x.getName() + " is most "
               + "likely to win an Oscar because they"
               + " also won awards this year from:\n";
         for (int i = 0; i < cal.size(); i++) {
            if (cal.get(i).getName().equals(x.getName())) {
               output += cal.get(i).getOrginization() + "\n";
            }     
         }   
      }
      else {
         output = "The film " + x.getName()
            + " is likely to win because it also won awards this year from:\n";
         for (int i = 0; i < cal.size(); i++) {
            if (cal.get(i).getName().equals(x.getName())) {
               output += cal.get(i).getOrginization() + "\n";
            }          
         }
      }
      return output;
   }
   /** 
   * returns string of each nominee toString() for a given category
   * If there are no winners the user is notified.
   * @param category is award category
   * @return toString of all nominees
   */
   public String returnResults(String category) {
      String output = category + ":\n";
      if (Double.parseDouble(winnerMap.get(category).getPercent()) == 0) {
         output = "\nCannot calculate winners because there is insufficent "
            + "data for " + category;
      }
      else {
         HashMap<String, Nomination> nomMap = nomineeMap.get(category);
         ArrayList<String> keyList = new ArrayList<String>(nomMap.keySet());
         for (int i = 0; i < keyList.size(); i++) {                     
            Nomination p = nomMap.get(keyList.get(i));
            output += p.toString() + "\n";
         } 
      }
      return output;
   } 
   /**
   * clears all hashmaps, used when calculating different years.
   */
   public void clearAll() {
      nomineeMap.clear();
      totalMap.clear();
      calcMap.clear();
      winnerMap.clear();
   }          
}