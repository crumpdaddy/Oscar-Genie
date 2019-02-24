import com.omertron.themoviedbapi.MovieDbException;
import java.util.Scanner;
/** This has the main class that has UI and runs
* OscarGenieDriver methods and outputs results.
* @author Ryan Crumpler
* @version 16.2.18
*/
public class OscarGenie {

    /**
     * * @throws IOException for scanner
     * * @param args command line arguments
     * */
    public static void main(String[] args) throws MovieDbException {
        int maxYear = 2017;
        int minYear = 1991;
        int calculatingYear = 2018;
        String yearIn, info, category, aString, back;
        int award = 0;
        Scanner scan = new Scanner(System.in);
        OscarGenieDriver myNoms = new OscarGenieDriver();
        System.out.println("Loading...");
        if (args.length == 0) {
            myNoms.setup(minYear, maxYear, calculatingYear, false);
        }
        else if (args.length == 1){
            if (args[0].equalsIgnoreCase("-t")) {
                myNoms.initialSetup(minYear, maxYear, calculatingYear);
            }
            else if (args[0].equalsIgnoreCase("-i")) {
                myNoms.setup(minYear, maxYear, calculatingYear, true);
            }
            else {
                System.out.println(args[0] + " is not a recognized parameter");
                myNoms.setup(minYear, maxYear, calculatingYear, false);
            }
        }
        else if (args.length == 3) {
            minYear = Integer.parseInt((args[0]));
            maxYear = Integer.parseInt((args[1]));
            calculatingYear = Integer.parseInt((args[2]));
            myNoms.setup(minYear, maxYear, calculatingYear, false);

        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("-t")) {
                minYear = Integer.parseInt((args[1]));
                maxYear = Integer.parseInt((args[2]));
                calculatingYear = Integer.parseInt((args[3]));
                myNoms.initialSetup(minYear, maxYear, calculatingYear);

            }
            else if (args[0].equalsIgnoreCase("-i")) {
                minYear = Integer.parseInt((args[1]));
                maxYear = Integer.parseInt((args[2]));
                calculatingYear = Integer.parseInt((args[3]));
                myNoms.setup(minYear, maxYear, calculatingYear, true);
            }
            else {
                System.out.println(args[0] + " is not a recognized parameter");
                minYear = Integer.parseInt((args[1]));
                maxYear = Integer.parseInt((args[2]));
                calculatingYear = Integer.parseInt((args[3]));
                myNoms.setup(minYear, maxYear, calculatingYear, false);
            }
        }
        String[] awardList = myNoms.getAwardList();
        System.out.println("\n\n\nWelcome to Oscar Genie by Ryan Crumpler\n");
        do {
            System.out.println("Please enter a year " + minYear + "-"
                    + calculatingYear + " to predict"
                    + "\nPress 'I' for Info about this program and it's total accuracy\n"
                    + "Press 'H' for Help\nPress 'Q' to Quit");
            yearIn = scan.nextLine();
            int year = 0;
            try {
                year = Integer.parseInt(yearIn);
            }
            catch (NumberFormatException ignored) {
            }
            if (year >= minYear && year <= calculatingYear) {
                StringBuilder menu = new StringBuilder();
                menu.append("Enter title of the award or its corresponding number you want to predict:\n");
                for (int i = 0; i < awardList.length; i++) {
                    menu.append(i + 1).append(" - ").append(awardList[i]).append("\n");
                }
                menu.append(awardList.length + 1).append(" - All to generate all winners\n").append(awardList.length + 2).append(" - Year to select new year");
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
                        if (year <= maxYear + 1) {
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
                        else {
                            do {
                                System.out.println("Press 'E' to go Back");
                                info = scan.nextLine().toUpperCase();
                            }
                            while (!info.equals("E"));
                        }
                    }
                }
                while (award != awardList.length + 2 || !aString.equalsIgnoreCase("year"));
            }
            else if (yearIn.equalsIgnoreCase("H")) {
                System.out.println(myNoms.printHelp());
                do {
                    System.out.println("Press 'E' to go Back");
                    yearIn = scan.nextLine().toUpperCase();
                }
                while (!yearIn.equals("E"));
            }
            else if (yearIn.equalsIgnoreCase("I")) {
                System.out.println(myNoms.printInfo());
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
            else {
                System.out.println("Invalid Year");
            }
        }
        while (!yearIn.equalsIgnoreCase("Q"));
    }
}