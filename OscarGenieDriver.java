import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ArrayList;

/** This program is the driver that carries out all methods 
* to determine chance ofwinning an oscar.
* 
* @author Ryan Crumpler
* @version 1.31.11
*/
public class OscarGenieDriver {
   private double coefficent;
   protected String name, award, movie, nominee;
   private HashMap<String, Double> totalMap;
   private HashMap<String, ArrayList<Calculations>> calcMap;
   private HashMap<String, HashMap<String, Nomination>> nomineeMap;
   private HashMap<String, File> yearHash;
   protected String percent;
   protected Nomination highestNominee;
   private HashMap<String, Nomination> winnerMap;
   protected String[] awardList;
   /**
   * This program reads a CSV file that shows the category 
   * of the award and all the nominees and stores all the nominees as 
   * either bestActor or bestPicture objects. It then reads a CSV file that
   * shows a list of criteria used to measure oscar nominee chance of winning as
   * well as how effective that metric is at 
   * predicting oscar winners. It stores 
   * all nominee objects in an ArrarList and puts that ArrayList in a hashMap
   * and the hashmap of ArrayLists in a hashmap 
   * it stores the total coefficent
   * for each category in a hashmap, this is used to 
   * calculate percent chance of winning.
   * The number of nominees is kept in a hashmap and 
   * this is used to iterate through nomine hashmaps.
   * nomineeMap defines a hashmap of Nomination ArrayList objects
   * totalMap defines a hashmap containing the sum of coefficents for
   * each category
   * calcMap defines a hashmap of doubles of the 
   * individual coefficents that are summed to give 
   * each nominee their total coefficent used in calculations
   * winnerMap is a hashMap of all projected winners for each category
   */
   public OscarGenieDriver() {
      totalMap = new HashMap<>();
      calcMap = new HashMap<>();
      yearHash = new HashMap<>();
      nomineeMap = new HashMap<>();
      highestNominee = new Nomination("", 0, "");
      winnerMap = new HashMap<>();
      awardList = new String[]{"Best Picture", "Best Actor", 
         "Best Actress", "Best Supporting Actor", "Best Supporting Actress", 
         "Best Animated Feature", "Best Cinematography", "Best Costume Design", 
         "Best Director", "Best Documentary Feature", "Best Film Editing",
         "Best Foreign Language Film", "Best Makeup and Hairstyling", 
         "Best Original Score", "Best Original Song", "Best Production Design", 
         "Best Visual Effects", "Best Original Screenplay", 
         "Best Adapted Screenplay"};
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
   */
   public String[] getAwardList() {
      return awardList;
   }
   /**
   * reads CSV that contains the nominee and any info 
   * exclusive to bestActor objects.
   * Adds nominees to an arrayList and stores that list in a hashmap 
   * and adds that hashmap to another hashMap nomineeMap
   * @param fileNameIn is the CSV file that will be read 
   * @throws IOException for scanner
   */
   public void readNominee(String fileNameIn) throws IOException {
      Scanner scanFile = new Scanner((yearHash.get(fileNameIn)));
      HashMap <String, Nomination> nomMap = new HashMap<String, Nomination>();
      boolean nextLine = true;
      award = scanFile.nextLine();
      String a = award;
      boolean song = false;
      boolean actress = false;
      boolean supporting = false;
      if (award.equals("Best Actor,,")
         || award.equals("Best Supporting Actor,,") 
         || award.equals("Best Actress,,")
         || award.equals("Best Supporting Actress,,")
         || award.equals("Best Original Song,,")) {
         award = award.substring(0, award.length() - 2);
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
               coefficent = Double.parseDouble(scanNominee.next());
               movie = scanNominee.next();
               percent = "0";
               BestActor n = new BestActor(name, coefficent, percent, 
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
         award = award.substring(0, award.length() - 1);
         while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine(); 
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNextLine()) {
               name = scanNominee.next();
               coefficent = Double.parseDouble(scanNominee.next());
               Nomination n = new Nomination(name, coefficent, percent);
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
   * the calcMap hashmap and 
   * @param fileNameIn is the CSV file that will be read 
   * @throws IOException for scanner
   */
   public void getProbability(String fileNameIn) throws IOException {  
      Scanner scanFile = new Scanner((yearHash.get(fileNameIn)));
      String orginization = "";
      boolean isNom = false;
      double totalCoeff = 0;
      ArrayList<Calculations> calcList = new ArrayList<Calculations>();
      Nomination p = new Nomination("", 0, ""); 
      award = scanFile.nextLine(); 
      award = award.substring(0, award.length() - 2);
      HashMap <String, Nomination> nomMap = nomineeMap.get(award);
      while (scanFile.hasNextLine()) {  
         nominee = scanFile.nextLine();
         Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
         while (scanProbability.hasNextLine()) { 
            name = scanProbability.next();
            orginization = scanProbability.next();
            coefficent = Double.parseDouble(scanProbability.next());
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
                  isNom = true;
               } 
            }      
         }
      }      
   }
   /**
   * Prints out bestActor objects and their probability of winning an Oscar.
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
   * generates a list of all winners.
   * @return string of winners
   */
   public String generateAll() {
      String output = "";
      double perc = 0;
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