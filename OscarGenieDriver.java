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
   protected String name;
   protected String award;
   protected String movie;
   protected String nominee;
   private int count;
   private boolean actress, supporting;
   private HashMap<String, BestActor> actorMap;
   private HashMap<String, Integer> countMap;
   private HashMap<String, Double> totalMap;
   private HashMap<String, BestPicture> pictureMap;
   private HashMap<String, Nomination> nomineeMap;
   private HashMap<String, Calculations> calcMap;
   private HashMap<String, Integer> calcCount;
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
   * calcCount defines a hashmap that contains the count of 
   * number of coefficents each nominee will have summed
   */
   public OscarGenieDriver() {
      actorMap = new HashMap<>();
      pictureMap = new HashMap<>();
      totalMap = new HashMap<>();
      countMap = new HashMap<>();
      calcMap = new HashMap<>();
      calcCount = new HashMap<>();
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
   public void readProbability(String fileNameIn) throws IOException {
      Scanner scanFile = new Scanner(new File(fileNameIn));
      String orginization = "";
      count = 0;
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
            count++; 
            calcCount.put(award, count);      
         }
      }
   }
   /**
   * uses calcCount to iterate through calcMap and the coefficent is 
   * updated for each nminee.
   * @param calcNameIn is name of the award that the 
   * coefficents of the nominees will be added to
   */
   public void writeProbability(String calcNameIn) {
      double totalCoeff = 0;
      double coeff = 0;
      Calculations c = new Calculations("", "", 0);
      BestActor a = new BestActor("", 0, "", "", false, false);
      BestPicture p = new BestPicture("", 0, "");
      if (calcNameIn.compareTo("Best Actor") == 0
            || calcNameIn.compareTo("Best Supporting Actor") == 0 
            || calcNameIn.compareTo("Best Actress") == 0 
            || calcNameIn.compareTo("best Supporting Actress") == 0) {
         for (int j = 0; j < countMap.get(calcNameIn); j++) {
            String countStringJ = String.valueOf(j);
            a = actorMap.get(calcNameIn + countStringJ);
            for (int i = 0; i < calcCount.get(calcNameIn); i++) {
               String countStringI = String.valueOf(i);
               c = calcMap.get(award + countStringI);
               if (c.getName().compareTo(a.getName()) == 0) {
                  coeff = a.getCoefficent();
                  coeff += c.getCoefficent();
                  totalCoeff += c.getCoefficent();
                  a.setCoefficent(coeff);
                  totalMap.put(calcNameIn, totalCoeff);
               }
            }
         }  
      }
      else {
         for (int j = 0; j < countMap.get(calcNameIn); j++) {
            String countStringJ = String.valueOf(j);
            p = pictureMap.get(calcNameIn + countStringJ);
            for (int i = 0; i < calcCount.get(calcNameIn); i++) {
               String countStringI = String.valueOf(i);
               c = calcMap.get(award + countStringI);
               if (c.getName().compareTo(p.getName()) == 0) {
                  coeff = p.getCoefficent();
                  coeff += c.getCoefficent();
                  totalCoeff += c.getCoefficent();
                  p.setCoefficent(coeff);
                  totalMap.put(calcNameIn, totalCoeff);
               }
            }
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
      Scanner scanFile = new Scanner(new File(fileNameIn));
      double totalCoeff = 0;
      count = 0; 
      award = scanFile.nextLine();
      
      if (award.compareTo("Best Actor,,") == 0
         || award.compareTo("Best Supporting Actor,,") == 0 
         || award.compareTo("Best Actress,,") == 0 
         || award.compareTo("best Supporting Actress,,") == 0) {
         award = award.substring(0, award.length() - 2);
         countMap.put(award, 0);
         if (award.compareTo("Best Supporting Actor") == 0 
            || award.compareTo("Best Supporting Actress") == 0) {
            supporting = true;
         }
         if (award.compareTo("Best Supporting Actres") == 0 
            || award.compareTo("Best Actress") == 0) {
            actress = true;
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
                  percent, actress, supporting);
               if (actress) {
                  n.setActress(true);
               }
               if (supporting) {
                  n.setSupporting(true);
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
         BestActor p = new BestActor("", 0, "", "", false, false);
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
      calcCount.clear();
   }  
   
}