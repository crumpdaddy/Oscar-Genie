import java.util.Scanner;
import java.io.IOException;
import java.io.File;
/** This has the main class that has UI and runs 
* OscarGenieDriver methods and outpus results. 
* @author Ryan Crumpler
* @version 2.26.17
*/
public class OscarGenie {
   /**
   * @throws IOException for scanner
   * @param args coomand line arguments
   */
   public static void main(String[] args) throws IOException {
      File file = new File("");
      String year, goBack, info, category, aString, back = "";
      int award = 0;
      Scanner scan = new Scanner(System.in);
      OscarGenieDriver myNoms = new OscarGenieDriver();
      myNoms.scanCoefficents();
      String[] awardList = myNoms.getAwardList();
      System.out.println("Welcome to Oscar Genie by Ryan Crumpler \n"); 
      do {
         System.out.println("Please enter a year 2013-2017 to predict"
            + " Press 'Q' to Quit");
         year = scan.nextLine();
         myNoms.clearAll();
         if (year.equals("2017") || year.equals("2016") || year.equals("2015") 
            || year.equals("2014") || year.equals("2013")) {
            myNoms.yearSet(year); 
            String menu = "";
            menu += "Enter title of the award or its "
                  + "corresponding number you want to predict:\n"; 
            for (int i = 0; i < awardList.length; i++) {
               myNoms.readNominee(awardList[i] + "Nominations.csv");
               myNoms.getProbability(awardList[i] + "Calculations.csv");
               myNoms.generateProbability(awardList[i]);
               menu += i + 1 + " - " + awardList[i] + "\n";
            }
            do {
               menu += "20 - All to generate all winners\n21 - Help for Help"
                  + "\n22 - Info for Info about this program\n"
                  + "23 - Year to select a new year";
               System.out.println(menu);
               aString = scan.nextLine();
               try {
                  award = Integer.parseInt(aString);     
               }
               catch (NumberFormatException e) {
                  boolean isAward = false;
                  boolean isFound = false;
                  if (aString.equalsIgnoreCase("All")) {
                     award = 20; isAward = true;
                  }
                  if (aString.equalsIgnoreCase("Help")) {
                     award = 21; isAward = true;
                  }
                  if (aString.equalsIgnoreCase("Info")) {
                     award = 22; isAward = true;
                  }
                  if (aString.equalsIgnoreCase("Year")) {
                     award = 23; isAward = true;
                  }
                  if (!isAward && aString.length() > 4) {
                     String awardSub = aString.substring(5);
                     if (aString.substring(0, 5).equalsIgnoreCase("Best ")) {
                        aString = aString.substring(5, aString.length());
                     }                   
                     while (!isFound) {
                        for (int i = 0; i < awardList.length; i++) {
                           String listSubString = awardList[i].substring(5);
                           if (aString.equalsIgnoreCase(listSubString)) {
                              isAward = true;
                              isFound = true;
                              award = i + 1;
                           }  
                           if (i == 18) {
                              isFound = true;
                           } 
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
               if (award >= 1 && award <= 19) {
                  category = awardList[award - 1 ];
                  System.out.println(myNoms.returnResults(category));
                  do {
                     System.out.println("Press 'I' for more info");
                     System.out.println("Press 'E' to enter different award");
                     info = scan.nextLine().toUpperCase();
                     if (info.equals("I")) {
                        do {
                           System.out.println(myNoms.generateDetails(category));
                           System.out.println("Press 'E' to go Back");
                           goBack = scan.nextLine().toUpperCase();
                        }
                        while (!goBack.equals("E"));
                     }
                  }
                  while (!info.equals("E"));
               }
               else if (award == 20) {
                  do {
                     System.out.println(myNoms.generateAll()); 
                     System.out.println("Press 'E' to go Back");
                     goBack = scan.nextLine().toUpperCase();
                  }
                  while (!goBack.equals("E"));  
               }
               else if (award == 21) {
                  do {
                     System.out.println("Enter either the number "
                        + "corresponding to the selection you wish to "
                        + "predict or\nyou can enter the name of the award\n"
                        + "You don't have to add the word 'best'"
                        + " before every award. Case does not matter\n"
                        + "please email any bugs or errors "
                        + "to rjcrumpler@gmail.com");
                     System.out.println("Press 'B' to go "
                        + "back to award selection");
                     goBack = scan.nextLine().toUpperCase();          
                  }
                  while (!goBack.equals("B"));
               }
               else if (award == 22) {
                  do {
                     System.out.println("This program was written by Ryan "
                        + "Crumpler\nIt works by scanning scources such "
                        + "as movie critic assosciationsx\n and determining "
                        + "how effective each source is at predicting Oscar "
                        + "winners\nfor each category It then weights each "
                        + "source and makes a prediction\nbased on all the "
                        + "sources and outputs the results.\n***Please Note: "
                        + "This Proggram is unable to predict the results for:"
                        + "\nBest Animated Short\nBest Short Documentary\n"
                        + "Best Sound Mixing\nBest Sound Editing\n"
                        + "Best Live Action Short\nDue to insufficent data\n"
                        + "Generally an output of\n0.00% indicates insufficent"
                        + "\ndata for that nominee\nPless 'E' to go back");
                     goBack = scan.nextLine().toUpperCase();
                  }
                  while (!goBack.equals("E"));
               }    
            }    
            while (award != 23 || !aString.equalsIgnoreCase("year"));         
         }    
         else {
            System.out.println("Invalid Year");      
         }
         if (year.equalsIgnoreCase("Q"))  {
            System.exit(0);
         }
      }     
      while (!year.equalsIgnoreCase("Q")); 
   }
}