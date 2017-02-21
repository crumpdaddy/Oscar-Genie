import java.util.Scanner;
import java.io.IOException;
import java.io.File;
/** This program is the driver that carries out all methods 
* to determine chance of winning an oscar.
* 
* @author Ryan Crumpler
* @version 1.31.11
*/
public class OscarGenie {
   /**
   * @throws IOException for scanner
   * @param args coomand line arguments
   */
   public static void main(String[] args) throws IOException {
      File file = new File("");
      String year = "";
      String goBack = "";
      String info = "";
      String category = "";
      int award = 0;
      String awardString = "";
      Scanner scan = new Scanner(System.in);
      OscarGenieDriver myNoms = new OscarGenieDriver();
      System.out.println("Welcome to Oscar Genie by Ryan Crumpler \n"); 
      do {
         System.out.println("Please enter a year 2016-2017 to predict"
           + "Press 'Q' to Quit");
         year = scan.nextLine();
         myNoms.clearAll();
         if (year.equals("2017") || year.equals("2016")) {
            myNoms.yearSet(year);  
            String[] awardList = myNoms.getAwardList();
            for (int i = 0; i < awardList.length; i++) {
               myNoms.readNominee(awardList[i] + "Nominations.csv");
               myNoms.getProbability(awardList[i] + "Calculations.csv");
               myNoms.generateProbability(awardList[i]);
            }
            do {
               System.out.println("Enter number for award you "
                  + "want to predict:\n"
                  + "1 - Best Picture\n2 - Best Actor\n"
                  + "3 - Best Actress\n4 - Best Supporting Actor\n"
                  + "5 - Best Supporting Actress\n6 - Best Animated Feature\n"
                  + "7 - Best Cinematography\n8 - Best Costume Design\n" 
                  + "9 - Best Director\n10 - Best Documentary Feature\n" 
                  + "11 - Best Film Editing\n12 - Best Foreign Language Film\n" 
                  + "13 - Best Makeup and Hairstyling\n"
                  + "14 - Best Original Score\n" 
                  + "15 - Best Original Song\n16 - Best Production Design\n" 
                  + "17 - Best Visual Effects\n18 - Best Original Screenplay\n"
                  + "19 - Best Adapted Screenplay\n"
                  + "20 - All to generate all winners\n"
                  + "21 - Help for Help\n22 - Info for Info\n"
                  + "23 - Select new Year");
               awardString = scan.nextLine();
               try {
                  award = Integer.parseInt(awardString);     
               }
               catch (NumberFormatException e) {
                  boolean isAward = false;
                  if (awardString.equalsIgnoreCase("All")) {
                     award = 20;
                     isAward = true;
                  }
                  if (awardString.equalsIgnoreCase("Help)")) {
                     award = 21;
                     isAward = true;
                  }
                  if (awardString.equalsIgnoreCase("Info)")) {
                     award = 22;
                     isAward = true;
                  }
                  if (awardString.equalsIgnoreCase("Year)")) {
                     award = 23;
                     isAward = true;
                  }
                  if (!isAward && awardString.length() > 4) {
                     String awardSub = awardString.substring(5);
                     if (awardString.substring(0, 
                        5).equalsIgnoreCase("Best ")) {
                        awardString = awardString.substring(5, 
                           awardString.length());
                     }
                      
                     while (!isAward) {
                        for (int i = 0; i < awardList.length; i++) {
                           String listSubString = awardList[i].substring(5);
                           if (awardString.equalsIgnoreCase(listSubString)) {
                              isAward = true;
                              award = i + 1;
                           }   
                        }
                     }
                  }
                  if (!isAward) {
                     System.out.println("Error: Please enter number "
                        + "corresponding to command");
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
                           System.out.println("Press 'E to go Back");
                           goBack = scan.nextLine().toUpperCase();
                        }
                        while (!goBack.equals("E"));
                     }
                  }
                  while (!goBack.equals("E"));
               }
               else if (award == 20) {
                  do {
                     System.out.println(myNoms.generateAll()); 
                     System.out.println("Press 'E to go Back");
                     goBack = scan.nextLine().toUpperCase();
                  }
                  while (!goBack.equals("E"));  
               }
               else if (award == 21) {
                  do {
                     System.out.println("Enter just the number for the "
                        + "selection  you wish to make with no spaces\n"
                         + "please email any bugs to rjcrumpler@gmail.com");
                     System.out.println("Press 'B' to go "
                        + "back to award selection");
                     goBack = scan.nextLine().toUpperCase();          
                  }
                  while (!goBack.equals("B"));
               }
               else if (award == 22) {
                  do {
                     System.out.println("This program was "
                        + "written by Ryan Crumpler\n"
                        + "It works by scanning scources "
                        + "such as movie critic "
                        + "assosciationsx\n and determining "
                        + "how effective each source "
                        + "is at predicting the Oscar "
                        + "winners\nfor each category."
                        + "It then weights each "
                        + "source and makes a prediction\n"
                        + "based on all the sources "
                        + "and outputs the results.\n"
                        + "***Please Note: This Proggram "
                        + "is unable to effictivly "
                        + "predict the results for:\nBest "
                        + "Animated Short\nBest Short "
                        + "Documentary\nBest Sound "
                        + "Mixing\nBest Sound Editing\n"
                        + "Best Live Action Short\nDue "
                        + "to insufficent data\n"
                        + "Generall an output of\n"
                        + "0.00% indicates insufficent\n"
                        + "data for that nominee");
                     goBack = scan.nextLine().toUpperCase();
                  }
                  while (!goBack.equals("E"));
               }    
            }    
            while (award != 23 
               || !awardString.equalsIgnoreCase("year"));         
         }
         if (year.equalsIgnoreCase("Q"))  {
            System.exit(0);
         }  
         else {
            System.out.println("Invalid Year");      
         }
      }     
      while (!year.equalsIgnoreCase("Q")); 
   }
}