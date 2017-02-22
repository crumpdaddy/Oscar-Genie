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
   private int count;
   private boolean actress, supporting;
   private HashMap<String, Integer> countMap;
   private HashMap<String, Double> totalMap;
   private HashMap<String, Nomination> nomineeMap;
   private HashMap<String, ArrayList<Calculations>> calcMap;
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
   * all nominee objects in their respictive 
   * hashMap and stores the total coefficent
   * for each category in a hashmap, this is used to 
   * calculate percent chance of winning.
   * The number of nominees is kept in a hashmap and 
   * this is used to iterate through nomine hashmaps.
   * nomineeMap defines a hashmap of Nomination objects
   * countMap defines a hashmap of that contains the total 
   * number of nominees for each category
   * totalMap defines a hashmap containing the sum of coefficents for
   * each category
   * calcMap defines a hashmap of doubles of the 
   * individual coefficents that are summed to give 
   * each nominee their total coefficent used in calculations
   */
   public OscarGenieDriver() {
      totalMap = new HashMap<>();
      countMap = new HashMap<>();
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
   * Reads CSV file that contains award in first line followed
   * by nominee, the institution that gave an award to the nominee
   * and the coefficent that is a percent that shows how often the 
   * the institutional award winner receives an oscar.
   * The coefficents each nominee receives are summed and stored in
   * the calcMap hashmap and the number of awards a nominee receives
   * is counted in calcCOunt.
   * @param fileNameIn is the CSV file that will be read 
   * @throws IOException for scanner
   */
   public void getProbability(String fileNameIn) throws IOException {  
      Scanner scanFile = new Scanner((yearHash.get(fileNameIn)));
      String orginization = "";
      count = 0;
      double totalCoeff = 0;
      ArrayList<Calculations> calcList = new ArrayList<Calculations>();
      Nomination a = new BestActor("", 0, "", "", false, false, false);
      Nomination p = new Nomination("", 0, ""); 
      award = scanFile.nextLine(); 
      award = award.substring(0, award.length() - 2);
      while (scanFile.hasNextLine()) {  
         nominee = scanFile.nextLine();
         Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
         while (scanProbability.hasNextLine()) { 
            name = scanProbability.next();
            orginization = scanProbability.next();
            coefficent = Double.parseDouble(scanProbability.next());
            String countString = String.valueOf(count);
            Calculations n = new Calculations(name, orginization, coefficent);
            calcList.add(n);
            totalCoeff += coefficent;
            totalMap.put(award, totalCoeff);
            calcMap.put(award, calcList);
            if (award.equals("Best Actor")
               || award.equals("Best Supporting Actor")
               || award.equals("Best Actress")
               || award.equals("Best Supporting Actress")
               || award.equals("Best Original Song")) {  
               for (int i = 0; i < countMap.get(award); i++) {
                  String iString = String.valueOf(i);
                  a = nomineeMap.get(award + iString);
                  if (name.equals(a.getName())) {
                     a.setCoefficent(a.getCoefficent() + coefficent);
                  }
                  if (i == 0) {
                     highestNominee = a;
                     winnerMap.put(award, a);
                  }
                  else if (a.getCoefficent()
                     > highestNominee.getCoefficent()) {
                     highestNominee = a;
                     winnerMap.put(award, a);
                  }
               }
            }  
            else {
               for (int i = 0; i < countMap.get(award); i++) {
                  String iString = String.valueOf(i);
                  p = nomineeMap.get(award + iString);
                  if (name.equals(p.getName())) {
                     p.setCoefficent(p.getCoefficent() + coefficent);
                  }
                  if (i == 0) {
                     highestNominee = p;
                     winnerMap.put(award, p);
                  }
                  else if (p.getCoefficent() > highestNominee.getCoefficent()) {
                     highestNominee = p;
                     winnerMap.put(award, p);
                  }
               }
            }
            count++; 
         }
      }      
   }
   /**
   * reads CSV that contains the nominee and any info 
   * exclusive to bestActor objects.
   * If coefficents are present in the file they are 
   * summed and added to countMap.
   * The file is read and converted to bestActor or bestPicture objects 
   * and stored in then stored in their respictive hashmaps
   * @param fileNameIn is the CSV file that will be read 
   * @throws IOException for scanner
   */
   public void readNominee(String fileNameIn) throws IOException {
      Scanner scanFile = new Scanner((yearHash.get(fileNameIn)));
      double totalCoeff = 0;
      count = 0; 
      boolean nextLine = true;
      award = scanFile.nextLine();
      String a = award;
      boolean song = false;
      if (award.equals("Best Actor,,")
         || award.equals("Best Supporting Actor,,") 
         || award.equals("Best Actress,,")
         || award.equals("Best Supporting Actress,,")
         || award.equals("Best Original Song,,")) {
         award = award.substring(0, award.length() - 2);
         countMap.put(award, 0);
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
               totalCoeff += coefficent;
               String countString = String.valueOf(count);
               nomineeMap.put(award + countString, n);
               totalMap.put(award, totalCoeff);
               count++;
               countMap.put(award, count);   
            }
         }
      }
      else {
         award = award.substring(0, award.length() - 1);
         countMap.put(award, 0);
         while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine(); 
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNextLine()) {
               name = scanNominee.next();
               coefficent = Double.parseDouble(scanNominee.next());
               Nomination n = new Nomination(name, coefficent, percent);
               totalCoeff += coefficent;
               String countString = String.valueOf(count);
               nomineeMap.put(award + countString, n);
               totalMap.put(award, totalCoeff);
               count++;
               countMap.put(award, count);   
            }
         }        
      }
   }
   /**
   Prints out bestActor objects and their probability of winning an Oscar.
   * @param category defines category of award to be calculated
   */
   public void generateProbability(String category) {
      double perc = 0;
      DecimalFormat numFmt = new DecimalFormat("#00.00");
      for (int i = 0; i < countMap.get(category); i++) {
         Nomination p = new BestActor("", 0, "", "", false, false, false);
         p = nomineeMap.get(award + String.valueOf(i));
         perc = (p.getCoefficent() / totalMap.get(category)) * 100;  
         p.setPercent(numFmt.format(perc));
      }   
   }
   /**
   Prints actor/nomination and all awards it won based off calculations.
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
   * generates a list of all winners.
   * @return string of winners
   */
   public String generateAll() {
      String output = "";
      double perc = 0;
      for (int i = 0; i < awardList.length; i++) {
         Nomination n = winnerMap.get(awardList[i]);
         if (Double.parseDouble(n.getPercent()) == 0) {
            output += "Cannot calculate winners because there is "
               + "insufficent date";
         }
         else {
            output += n.toString() + " an Oscar for " + awardList[i];
         }
      }
      return output;
   }
   /**
   * @return String[[] of list of awards
   */
   public String[] getAwardList() {
      return awardList;
   }
   /**
   clears all hashmaps, used when calculating different years.
   */
   public void clearAll() {
      nomineeMap.clear();
      countMap.clear();
      totalMap.clear();
      calcMap.clear();
   }
   /** 
   * @param category is award category
   * @return toString of all nominees
   */
   public String returnResults(String category) {
      String output = "";
      if (Double.parseDouble(winnerMap.get(category).getPercent()) == 0) {
         output = "Cannot calculate winners because there is insufficent "
            + "data for " + category;
      }
      else {
         for (int i = 0; i < countMap.get(category); i++) {
            Nomination p = new Nomination("", 0, "");
            p = nomineeMap.get(category + String.valueOf(i));
            output += p.toString() +  " an Oscar for " + category;
         } 
      }
      return output;
   } 
   
}