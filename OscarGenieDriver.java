import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

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
   private HashMap<String, BestActor> actorMap;
   private HashMap<String, Integer> countMap;
   private HashMap<String, Double> totalMap;
   private HashMap<String, BestPicture> pictureMap;
   private HashMap<String, Nomination> nomineeMap;
   private HashMap<String, Calculations> calcMap;
   private HashMap<String, File> yearHash;
   protected String percent;
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
   * actorMap defines a hashmap of bestActor objects
   * pictureMap defines a hashmap of bestPicture objects
   * countMap defines a hashmap of that contains the total 
   * number of nominees for each category
   * totalMap defines a hashmap containing the sum of coefficents for
   * each category
   * calcMap defines a hashmap of doubles of the 
   * individual coefficents that are summed to give 
   * each nominee their total coefficent used in calculations
   */
   public OscarGenieDriver() {
      actorMap = new HashMap<>();
      pictureMap = new HashMap<>();
      totalMap = new HashMap<>();
      countMap = new HashMap<>();
      calcMap = new HashMap<>();
      yearHash = new HashMap<>();
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
      BestActor a = new BestActor("", 0, "", "", false, false, false);
      BestPicture p = new BestPicture("", 0, "");
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
            calcMap.put(award + countString, n);
            totalCoeff += coefficent;
            totalMap.put(award, totalCoeff);
            if (award.compareTo("BEST ACTOR") == 0
               || award.compareTo("BEST SUPPORTING ACTOR") == 0 
               || award.compareTo("BEST ACTRESS") == 0 
               || award.compareTo("BEST SUPPORTING ACTRESS") == 0
               || award.compareTo("BEST ORIGINAL SONG") == 0) {  
               for (int i = 0; i < countMap.get(award); i++) {
                  String iString = String.valueOf(i);
                  a = actorMap.get(award + iString);
                  if (name.equals(a.getName())) {
                     a.setCoefficent(a.getCoefficent() + coefficent);
                  }
               }
            }  
            else {
               for (int i = 0; i < countMap.get(award); i++) {
                  String iString = String.valueOf(i);
                  p = pictureMap.get(award + iString);
                  if (name.equals(p.getName())) {
                     p.setCoefficent(p.getCoefficent() + coefficent);
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
      award = scanFile.nextLine();
      boolean song = false;
      
      if (award.compareTo("BEST ACTOR,,") == 0
         || award.compareTo("BEST SUPPORTING ACTOR,,") == 0 
         || award.compareTo("BEST ACTRESS,,") == 0 
         || award.compareTo("BEST SUPPORTING ACTRESS,,") == 0
         || award.compareTo("BEST ORIGINAL SONG,,") == 0) {
         award = award.substring(0, award.length() - 2);
         countMap.put(award, 0);
         if (award.compareTo("BEST SUPPORTING ACTOR") == 0 
            || award.compareTo("BEST SUPPORTING ACTRESS") == 0) {
            supporting = true;
         }
         if (award.compareTo("BEST SUPPORTING ACTRESS") == 0 
            || award.compareTo("BEST ACTRESS") == 0) {
            actress = true;
         }
         if (award.compareTo("BEST ORIGINAL SONG") == 0) {
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
               BestActor n = new BestActor(name, coefficent, movie, 
                  percent, actress, supporting, song);
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
               actorMap.put(award + countString, n);
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
               BestPicture n = new BestPicture(name, coefficent, percent);
               totalCoeff += coefficent;
               String countString = String.valueOf(count);
               pictureMap.put(award + countString, n);
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
   * @return string of all objects and their probability
   */
   public String generateActorProbability(String category) {
      double perc = 0;
      String output = "The odds for " + category + ":\n";
      DecimalFormat numFmt = new DecimalFormat("#00.00");
      for (int i = 0; i < countMap.get(category); i++) {
         BestActor p = new BestActor("", 0, "", "", false, false, false);
         p = actorMap.get(award + String.valueOf(i));
         perc = (p.getCoefficent() / totalMap.get(category)) * 100;  
         p.setPercent(numFmt.format(perc));
         output += p.toString();
      }   
      return output;
   }
   /**
   Prints out bestPicture objects and their probability of winning an Oscar.
   * @param category defines category of award to be calculated
   * @return string of all objects and their probability
   */
   public String generatePictureProbability(String category) {
      double perc = 0;
      String output = "The odds for " + category + ":\n";
      DecimalFormat numFmt = new DecimalFormat("#00.00");
      for (int i = 0; i < countMap.get(category); i++) {
         BestPicture p = new BestPicture("", 0, "");
         p = pictureMap.get(category + String.valueOf(i));
         perc = (p.getCoefficent() / totalMap.get(category)) * 100;  
         p.setPercent(numFmt.format(perc));
         output += p.toString();
      }      
      return output;
   }
   /**
   clears all hashmaps, used when calculating different years.
   */
   public void clearAll() {
      actorMap.clear();
      pictureMap.clear();
      countMap.clear();
      totalMap.clear();
      calcMap.clear();
   }  
   
}