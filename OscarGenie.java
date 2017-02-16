import java.util.Scanner;
import java.io.IOException;
import java.io.File;
/** This program is the driver that carries out all methods 
* to determine chance ofwinning an oscar.
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
      File file = new File("bestActressNominations.csv");
      String year = "";
      String award = "";
      String goBack = "";
      Scanner scan = new Scanner(System.in);
      OscarGenieDriver myNoms = new OscarGenieDriver();
      System.out.println("Welcome to Oscar Genie by Ryan Crumpler \n"); 
      do {
         System.out.println("Please enter a year 2016-2017 to predict");
         year = scan.nextLine();
         myNoms.clearAll();
         if (year.equals("2016") || year.equals("2017")) {
            myNoms.yearSet(year);  
            do {
               System.out.println("Enter award you want to predict:\n"
                  + "Best Picture\nBest Actor\nBest "
                  + "Actress\nBest Supporting Actor\n"
                  + "Best Supporting Actress\nBest "
                  + "Director\nBest Cinematography\n"
                  + "Best Costume Design\nBest Documentary "
                  + "Feature\nBest Animated Feature\n"
                  + "Best Foreign Language Film\nBest Makeup "
                  + "and Hairstyling\n"
                  + "Best Original Score\nBest Original Song\n"
                  + "Best Visual Effect\n"
                  + "Best Film Editing\nBest Production Design\n"
                  + "Best Original Screenplay\n"
                  + "Best Adapted Screenplay\nYear to Enter new Year\n"
                  + "Help for help\nInfo for additional Info\n");
               award = scan.nextLine();
               award = award.toUpperCase();
               if (award.equalsIgnoreCase("BEST PICTURE")
                  || award.equals("BEST CINEMATOGRAPHY")
                  || award.equals("BEST DOCUMENTARyY FEATURE")
                  || award.equals("BEST ANIMATED FEATURE")
                  || award.equals("BEST FOREIGN LANGUAGE FILM")
                  || award.equals("BEST MAKEUP AND HAIRSTYLING")
                  || award.equals("BEST COSTUME DESIGN")
                  || award.equals("BEST ORIGINAL SCORE")
                  || award.equals("BEST VISUAL EFFECTS")
                  || award.equals("BEST FILM EDITING")
                  || award.equals("BEST PRODUCTION DESIGN")
                  || award.equals("BEST ORIGINAL SCREENPLAY")
                  || award.equals("BEST ADAPTED SCREENPLAY")
                  || award.equals("BEST DIRECTOR")
                  || award.equals("BEST COSTUME DESIGN")) {
                  myNoms.readNominee(award + "Nominations.csv");
                  myNoms.readProbability(award + "Calculations.csv");
                  myNoms.writeProbability(award);
                  System.out.println(myNoms.generatePictureProbability(award));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));
               }
               else if (award.equalsIgnoreCase("BEST ACTOR")
                  || award.equals("BEST SUPPORTING ACTOR")
                  || award.equals("BEST ACTRESS")
                  || award.equals("BEST SUPPORTING ACTRESS")
                  || award.equals("BEST ORIGINAL SONG")) {
                  myNoms.readNominee(award + "Nominations.csv");
                  myNoms.readProbability(award + "Calculations.csv");
                  myNoms.writeProbability(award);
                  System.out.println(myNoms.generateActorProbability(award));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));        
               }
               else if (award.equalsIgnoreCase("help")) {
                  do {
                     System.out.println("Case does not matter "
                        + "when entering award "
                        + "but make sure input is "
                        + "spelled correctly.");
                     System.out.println("Press 'B' to go "
                        + "back to award selection");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("b"));
               }
               else if (award.equalsIgnoreCase("info")) {
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
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("b"));
               }     
            }    
            while (!award.equalsIgnoreCase("Year"));         
         }  
         else {
            System.out.println("Invalid Year");      
         }       
      } 
      while (!year.equalsIgnoreCase("Q"));      
   }
}