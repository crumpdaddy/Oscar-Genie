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
         System.out.println("Please enter a year 2010-2017 to predict");
         year = scan.nextLine();
         myNoms.clearAll();
         if (year.equals("2010") || year.equals("2011")
            || year.equals("2012") || year.equals("2013")
            || year.equals("2014") || year.equals("2015") 
            || year.equals("2016") || year.equals("2017")) {
            
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
               if (award.equalsIgnoreCase("Best Picture")) {
                  myNoms.readNominee(year + "bestPictureNominations.csv");
                  myNoms.readProbability(year + "bestPictureCalculations.csv");
                  myNoms.writeProbability("Best Picture");
                  System.out.println(myNoms.generatePictureProbability(
                     "Best Picture"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));
               }
               else if (award.equalsIgnoreCase("Best Actor")) {
                  myNoms.readNominee(year + "bestActorNominations.csv");
                  myNoms.readProbability(year + "bestActorCalculations.csv");
                  myNoms.writeProbability("Best Actor");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Actor"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Actress")) {
                  myNoms.readNominee(year + "bestActressNominations.csv");
                  myNoms.readProbability(year 
                     + "bestActressCalculations.csv");
                  myNoms.writeProbability("Best Actress");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Actress"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Supporting Actress")) {
                  myNoms.readNominee(year 
                     + "bestSupportingActressNominations.csv");
                  myNoms.readProbability(year 
                     + "bestSupportingActressCalculations.csv");
                  myNoms.writeProbability("Best Actress");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Supporting Actress"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Supporting Actor")) {
                  myNoms.readNominee(year 
                     + "bestSupportingActorNominations.csv");
                  myNoms.readProbability(year 
                     + "bestSupportingActorsCalculations.csv");
                  myNoms.writeProbability("Best Supporting Actor");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Supporting Actor"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Director")) {
                  myNoms.readNominee(year + "bestDirectorNominations.csv");
                  myNoms.readProbability(year 
                     + "bestDirectorCalculations.csv");
                  myNoms.writeProbability("Best Director");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Director"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Cinematography")) {
                  myNoms.readNominee(year 
                     + "bestCinematographyNominations.csv");
                  myNoms.readProbability(year 
                     + "bestCinematographyCalculations.csv");
                  myNoms.writeProbability("Best Cinematography");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Cinematography"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Costume Design")) {
                  myNoms.readNominee(year + "bestCostumeNominations.csv");
                  myNoms.readProbability(year 
                     + "bestCostumeCalculations.csv");
                  myNoms.writeProbability("Best Costume");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Costume"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Film Editing")) {
                  myNoms.readNominee(year + "bestEditingNominations.csv");
                  myNoms.readProbability(year 
                     + "bestEditingCalculations.csv");
                  myNoms.writeProbability("Best Editing");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Editing"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Visual Effects")) {
                  myNoms.readNominee(year + "bestEffectsNominations.csv");
                  myNoms.readProbability(year 
                     + "bestEffectsCalculations.csv");
                  myNoms.writeProbability("Best Effects");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Effects"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Animated Feature")) {
                  myNoms.readNominee(year + "bestAnimatedNominations.csv");
                  myNoms.readProbability(year 
                     + "bestAnimatedCalculations.csv");
                  myNoms.writeProbability("Best Animated");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Animated"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Original Song")) {
                  myNoms.readNominee(year + "bestSongNominations.csv");
                  myNoms.readProbability(year 
                     + "bestSongsCalculations.csv");
                  myNoms.writeProbability("Best Song");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Song"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Original Score")) {
                  myNoms.readNominee(year + "bestScoreNominations.csv");
                  myNoms.readProbability(year 
                     + "bestScoreCalculations.csv");
                  myNoms.writeProbability("Best Score");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Score"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Documentary Feature")) {
                  myNoms.readNominee(year + "bestDocumentaryNominations.csv");
                  myNoms.readProbability(year 
                     + "bestDocumentaryCalculations.csv");
                  myNoms.writeProbability("Best Documentary");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Documentary"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Original Screenplay")) {
                  myNoms.readNominee(year + "bestOscreenNominations.csv");
                  myNoms.readProbability(year 
                     + "bestOscreenCalculations.csv");
                  myNoms.writeProbability("Best Original Screenplay");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Original Screenplay"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Adapted Screenplay")) {
                  myNoms.readNominee(year + "bestAscreenNominations.csv");
                  myNoms.readProbability(year 
                     + "bestAscreenCalculations.csv");
                  myNoms.writeProbability("Best Adapted Screenplay");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Adapted Screenplay"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("Best Production Design")) {
                  myNoms.readNominee(year + "bestProductionNominations.csv");
                  myNoms.readProbability(year 
                     + "bestProductionCalculations.csv");
                  myNoms.writeProbability("Best Production Design");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Production Design"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }
               else if (award.equalsIgnoreCase("year")) {
                  break;
               }
               else if (award.equalsIgnoreCase("Best Makeup and Hairstyling")) {
                  myNoms.readNominee(year + "bestMakeupNominations.csv");
                  myNoms.readProbability(year 
                     + "bestMakeupCalculations.csv");
                  myNoms.writeProbability("Best Makeup");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Makeup"));
                  do {
                     System.out.println("Press 'E' to enter different award");
                     goBack = scan.nextLine();
                  }
                  while (!goBack.equalsIgnoreCase("E"));         
               }     
               else if (award.equalsIgnoreCase("Best Foreign Language Film")) {
                  myNoms.readNominee(year + "bestForeignNominations.csv");
                  myNoms.readProbability(year 
                     + "bestForeignCalculations.csv");
                  myNoms.writeProbability("Best Foreign Language");
                  System.out.println(myNoms.generateActorProbability(
                     "Best Foreign Language"));
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
                        + "to insufficent data");
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