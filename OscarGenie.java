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
      String yearIn, goBack, info, category, aString, back = "";
      int award = 0;
      Scanner scan = new Scanner(System.in);
      OscarGenieDriver myNoms = new OscarGenieDriver();
      myNoms.readNominee("all_nominations.csv", 2017);
      myNoms.readCalculations("all_calculations.csv", 2017); 
      myNoms.setCoefficents(); 
      myNoms.kalmanFilter(1991, 2017); 
      myNoms.getProbability(1991, 2017);
      String[] awardList = myNoms.getAwardList();
      System.out.println("Welcome to Oscar Genie by Ryan Crumpler\n"); 
      do {
         System.out.println("Please enter a year 1991-2017 to predict"
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
         
         if (year >= 1991 && year <= 2017) {
            String menu = "";
            menu += "Enter title of the award or its "
                  + "corresponding number you want to predict:\n"; 
            for (int i = 0; i < awardList.length; i++) {
               menu += i + 1 + " - " + awardList[i] + "\n";
            }
            menu += "20 - All to generate all winners\n"
               + "21 - Year to select new year";
            do {
               System.out.println(menu);
               aString = scan.nextLine();
               try {
                  award = Integer.parseInt(aString);     
               }
               catch (NumberFormatException e) {
                  boolean isAward = false;
                  if (aString.equalsIgnoreCase("All")) {
                     award = 20; isAward = true;
                  }
                  if (aString.equalsIgnoreCase("Year")) {
                     award = 21; isAward = true;
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
               if (award == 21) {
                  aString = "year";
               }
               if (award >= 1 && award <= 19) {
                  category = awardList[award - 1];
                  boolean start = true;
                  if (start) {
                     System.out.println(myNoms.returnResults(year, category));
                     start = false;
                  }
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
                     if (info.equals("E")) {
                        start = true;
                     }
                  }
                  while (!info.equals("E"));
               }
               else if (award == 20) {
                  boolean start = true;
                  if (start) {
                     System.out.println(myNoms.generateAll(year)); 
                     System.out.println("Press 'I' to to see correct " 
                        + "winners and to see Oscar Genie accuracy");
                     start = false;
                  }
                  do {
                     System.out.println("Press 'E' to go Back");
                     info = scan.nextLine().toUpperCase();          
                     if (info.equals("I")) {
                        start = false;
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
            while (award != 21 || !aString.equalsIgnoreCase("year"));         
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
            System.out.println(myNoms.printInfo());
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