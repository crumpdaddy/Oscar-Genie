import java.util.Scanner;
import java.io.IOException;
import java.io.File;
/** This has the main class that has UI and runs 
* OscarGenieDriver methods and outpus results. 
* @author Ryan Crumpler
* @version 8.8.17
*/
public class OscarGenie {  
   private static int maxYear = 2017;
   private static int minYear = 1991; 
   /**
   * @throws IOException for scanner
   * @param args coomand line arguments
   */ 
   public static void main(String[] args) throws IOException {
      File file = new File("");
      String yearIn, goBack, info, category, aString, back = "";
      int award = 0; 
      Scanner scan = new Scanner(System.in);
      OscarGenieDriver myNoms = new OscarGenieDriver();
      myNoms.setThreshhold(.3);
      myNoms.readNominee("all_nominations.csv", maxYear);
      myNoms.readCalculations("all_calculations.csv", maxYear); 
      myNoms.setCoefficents(); 
      myNoms.kalmanFilter(minYear, maxYear);
      myNoms.getProbability(minYear, maxYear);
      String[] awardList = myNoms.getAwardList();
      System.out.println("Welcome to Oscar Genie by Ryan Crumpler\n"); 
      do {
         System.out.println("Please enter a year " + minYear + "-" 
            + maxYear + " to predict"
            + "\n Press 'I' for Info about this program\n"
            + "Press 'H' for Help\nPress 'Q' to Quit");
         boolean invalid = true;
         yearIn = scan.nextLine();
         int year = 0;
         try {
            year = Integer.parseInt(yearIn);
         }
         catch (NumberFormatException e) {
         }        
         if (year >= minYear && year <= maxYear) {
            String menu = "";
            menu += "Enter title of the award or its "
                  + "corresponding number you want to predict:\n"; 
            for (int i = 0; i < awardList.length; i++) {
               menu += i + 1 + " - " + awardList[i] + "\n";
            }
            menu += (awardList.length + 1) + " - All to generate all winners\n"
               + (awardList.length + 2) + " - Year to select new year";
            do {
               System.out.println(menu);
               aString = scan.nextLine();
               try {
                  award = Integer.parseInt(aString);     
               }
               catch (NumberFormatException e) {
                  boolean isAward = false;
                  if (aString.equalsIgnoreCase("All")) {
                     award = awardList.length + 1; 
                     isAward = true;
                  }
                  if (aString.equalsIgnoreCase("Year")) {
                     award = awardList.length + 2;
                     isAward = true;
                  }
                  if (!isAward) {                  
                     for (int i = 0; i < awardList.length; i++) {
                        String listSubString = awardList[i];
                        if (aString.equalsIgnoreCase(listSubString)) {
                           isAward = true; award = i + 1;
                        }        
                     }
                  }
                  if (!isAward) {
                     do {
                        System.out.println("Error: Please enter number "
                           + "corresponding to command\nPress 'E' to go back");
                        back = scan.nextLine().toUpperCase();
                     }
                     while (!back.equals("E"));
                  }
               }
               if (award == awardList.length + 2) {
                  aString = "year";
               }
               if (award >= 1 && award <= awardList.length) {
                  category = awardList[award - 1];
                  System.out.println(myNoms.returnResults(year, category));
                  do {
                     System.out.println("Press 'I' for more info");
                     System.out.println("Press 'E' to enter different award");
                     info = scan.nextLine().toUpperCase();
                     if (info.equals("I")) {
                        do {
                           System.out.println(myNoms.getDetail(year, category));
                           System.out.println("Press 'E' to go Back");
                           info = scan.nextLine().toUpperCase();
                        }
                        while (!info.equals("E"));
                     }
                  }
                  while (!info.equals("E"));
               }
               else if (award == awardList.length + 1) {
                  System.out.println(myNoms.generateAll(year)); 
                  System.out.println("Press 'I' to to see correct " 
                      + "winners and to see Oscar Genie accuracy");
                  do {
                     System.out.println("Press 'E' to go Back");
                     info = scan.nextLine().toUpperCase();          
                     if (info.equals("I")) {
                        do {
                           System.out.println(myNoms.allDetails(year));
                           System.out.println("Press 'E' to go Back");
                           info = scan.nextLine().toUpperCase();
                        }
                        while (!info.equals("E"));
                     }
                  }
                  while (!info.equals("E"));  
               }                  
            }    
            while (award != awardList.length + 2 
               || !aString.equalsIgnoreCase("year"));         
         }
         else if (yearIn.equalsIgnoreCase("H")) {
            System.out.println(myNoms.printHelp());
            invalid = false;
            do {
               System.out.println("Press 'E' to go Back");
               yearIn = scan.nextLine().toUpperCase();
            }
            while (!yearIn.equals("E"));
         } 
         else if (yearIn.equalsIgnoreCase("I")) {
            System.out.println(myNoms.printInfo(minYear, maxYear));
            invalid = false;
            do {         
               System.out.println("Press 'E' to go Back");
               yearIn = scan.nextLine().toUpperCase();
            }
            while (!yearIn.equals("E"));
         }  
         else if (yearIn.equalsIgnoreCase("Q"))  {
            System.out.println("Ending program");
            System.exit(0);
         }   
         else if (invalid) {
            System.out.println("Invalid Year");      
         }     
      }     
      while (!yearIn.equalsIgnoreCase("Q")); 
   }
}