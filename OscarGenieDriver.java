
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/** This program is the driver that carries out all methods
 * to determine chance of winning an oscar.
 * The general flow of the program is as follows:
 * Initial Setup -> set global variables -> Read CSV files and store data ->
 * Generate and set initial coefficient approximations ->
 * Kalman Filter calculations -> Generate probabilities based on calculations ->
 * Generate winners based on probabilities.
 * @author Ryan Crumpler
 * @version 9.9.17
 */
public class OscarGenieDriver {
    /**
     * List of all awards Oscar Genie will attempt to predict
     */
    protected static String[] awardList = new String[]{"Best Picture",
            "Actor - Leading Role", "Actress - Leading Role",
            "Actor - Supporting Role", "Actress - Supporting Role",
            "Animated Feature", "Cinematography", "Costume Design",
            "Director", "Documentary", "Film Editing",
            "Foreign Language Film", "Production Design",
            "Score", "Screenplay - Adapted", "Screenplay - Original",
            "Song", "Visual Effects"};

    /**
     *  List or organizations to be used to try to predict winners.
     */
    protected static String[] orgList = new String[]{"New York Film Critics",
            "LA Film Critics", "DC Film Critics", "Boston Film Critics",
            "San Francisco Critics", "Las Vegas Critics", "Houston Critics",
            "Screen Actors Guild", "Writers Guild", "Producers Guild",
            "Directors Guild", "Visual Effects Society", "Critics Choice",
            "Golden Globes", "Golden Globes-Comedy or Musical",
            "Golden Globes-Drama"};

    /**
     * Static int that defines the max number of times the program will adjust the filter
     */
    private static int maxCount = 20;
    protected String name, award, movie, nominee, organization, percent, kalKey;
    private double coefficient, threshold, scalar, calcCount;

    /**
     * integers to define years to begin and end calculations on.
     */
    private int minYear, maxYear;

    /**
     * HashMap structure for storing actual and calculated winners.
     *  Key is year of Oscars
     *  Stores a HashMap for the actual winners where key is award and the data is the winner.
     */
    private HashMap<Integer, HashMap<String, String>> allWinners;
    private HashMap<Integer, HashMap<String, Nomination>> calculatedWinners;

    /**
     * HashMap Structures for  all calculations and all nominees
     * The top level HashMap has a key that is the year.
     * The data is a set of HashMaps where the key is the award title,
     * The data of the 2nd level HashMap is a 3rd level HashMap
     * The key to the 3rd level Nomination HashMap the name of the nominee,
     * with the data being the Nomination object
     * The key to the 3rd level Calculation HashMap the name of the organization,
     * with the data being the Calculation object
     */
    private HashMap<Integer, HashMap<String, HashMap<String, Calculations>>> calculationByYear;
    private HashMap<Integer, HashMap<String, HashMap<String, Nomination>>> nomineesByYear;

    /**
     * HashMap that stores coefficients that will be adjusted and then set to nominee and calculation objects
     */
    private HashMap<String, Double> kalmanMap;

    /**
     * HashMap that is used to get initial kalman coefficients for kalmanMap
     * It's kay is a string that contains the organization + the award,
     * the data is an integer that shows how many nominees that organization 
     * correctly predicted to win a particular Oscar award.
     */
    private HashMap<String, Double> orgCount;

    public OscarGenieDriver() {
        nomineesByYear = new HashMap<>();
        calculationByYear = new HashMap<>();
        allWinners = new HashMap<>();
        kalmanMap = new HashMap<>();
        orgCount = new HashMap<>();
        calculatedWinners = new HashMap<>();
    }

    //Initial Setup

    /**
     * Runs initial setup necessary for main OscarGenie program.
     * Sets min and max years, scalar, and threshold -> reads CSVs ->
     * sets coefficients and calculation coefficients -> runs kalman filter ->
     * gets probability of winners
     * @param minYear is first year to start calculating
     * @param maxYear is last year to start calculating
     * @throws IOException for scanner
     */
    public void initialSetup(int minYear, int maxYear) throws IOException {
        setMinMaxYear(minYear, maxYear);
        setScalar(.05);
        setThreshold(0);
        readNominee("all_nominations.csv");
        readCalculations("all_calculations.csv");
        setCoefficients();
        for (int i = 0; i < awardList.length; i++) {
            setCalculationsCoef(awardList[i]);
        }
        kalmanFilter();
        getProbability();
    }
    // Get and Set Methods for variables.

    /**
     * @return String[[] of list of awards
     * used in OscarGenie to iterate through awards
     */
    public String[] getAwardList() {
        return awardList;
    }

    /**
     * @return String[[] of list of awards organizations
     * used in OscarGenie to iterate through awards organizations
     */
    public String[] getOrgList() {
        return orgList;
    }

    /**
     * Sets the range of time to preform calculations on
     * @param minYearIn first year to preform calculations on
     * @param maxYearIn last year to preform calculations on
     */
    public void setMinMaxYear(int minYearIn, int maxYearIn) {
        minYear = minYearIn;
        maxYear = maxYearIn;
    }

    /**
     * Sets the kalman scalar
     * @param scalarIn is the scalar for the filter
     */
    public void setScalar(double scalarIn) {
        scalar = scalarIn;
    }

    /**
     * @param thresholdIn is threshold to define what % certainty
     * defines a winner.
     *
     */
    public void setThreshold(double thresholdIn) {
        threshold = thresholdIn;
    }

    // Methods to read CSV files and add contents to HashMaps.

    /**
     * Reads as CSV that contains all nominees and adds data to layered HashMaps.
     * The CSV contains the year, award, nominee, notes and if the nominee won an oscar.
     * This data is added to Nomination objects and stored
     * If the nominee won an Oscar it is added to the allWinners HashMap.
     * CloneMaps were created as shallow copies of the layered HashMaps, 
     * this is done because clearing a HashMap after putting it inside another HashMap
     * clears the data from the top layer HashMap as well.
     * @throws IOException for scanner
     */
    public void readNominee(String fileNameIn) throws IOException {
        Scanner scanFile = new Scanner(new File(fileNameIn));
        HashMap <String, Nomination> nomMap = new HashMap<String, Nomination>();
        HashMap <String, Nomination> cloneMap = new HashMap<String, Nomination>();
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, String> winnersClone = new HashMap<String, String>();
        HashMap <String, HashMap<String, Nomination>> nomineeMap = new HashMap <String, HashMap<String, Nomination>>();
        HashMap <String, HashMap<String, Nomination>> cloneMap2 = new HashMap<String, HashMap<String, Nomination>>();
        boolean nextLine = true;
        award = scanFile.nextLine();
        String won = "";
        String notes = "";
        int year = maxYear;
        int pastYear = maxYear;
        String prevAward = "";
        while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine();
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNext()) {
                year = Integer.parseInt(scanNominee.next());
                if (year < pastYear) {
                    winnersClone = new HashMap<String, String>(winners);
                    allWinners.put(pastYear, winnersClone);
                    cloneMap2 = new HashMap<String, HashMap<String, Nomination>>(nomineeMap);
                    nomineesByYear.put(pastYear, cloneMap2);
                    pastYear = year;
                    nomMap.clear();
                    nomineeMap.clear();
                }
                award = scanNominee.next();
                if (!award.equalsIgnoreCase(prevAward)) {
                    cloneMap = new HashMap<String, Nomination>(nomMap);
                    nomineeMap.put(prevAward, cloneMap);
                    nomMap.clear();
                    prevAward = award;
                }
                name = scanNominee.next();
                movie = scanNominee.next();
                boolean song = false;
                boolean actress = false;
                if (award.equals("Actor - Leading Role")
                        || award.equals("Actor - Supporting Role")
                        || award.equals("Actress - Leading Role")
                        || award.equals("Actress - Supporting Role")
                        || award.equals("Song")) {
                    if (award.compareTo("Song") == 0) {
                        song = true;
                    }
                    percent = "0.0";
                    BestActor n = new BestActor(name, 0, percent,
                            movie, actress, song);
                    if (award.compareTo("Actress - Supporting Role") == 0
                            || award.compareTo("Actress - Leading Role") == 0) {
                        n.setActress(true);
                    }
                    nomMap.put(name, n);
                }
                else {
                    Nomination n = new Nomination(name, 0, percent);
                    nomMap.put(name, n);
                }
                won = scanNominee.next();
                if (won.equalsIgnoreCase("Yes")) {
                    winners.put(award, name);
                }
            }
        }
    }

    /**
     * Reads as CSV that contains all calculations and stores the data in layered HashMaps.
     * The CSV contains the year, name of award, name of nominee, and calculation organization.
     * This data is added to Calculation objects and stored.
     * * CloneMaps were created as shallow copies of the layered HashMaps, 
     * this is done because clearing a HashMap after putting it inside another HashMap
     * clears the data from the top layer HashMap as well.
     * Before adding the calculation to the calculationByOrg a check is preformed
     * to see if the name of the name of the  Calculation object was actually nominated
     * if it was nominated it is added to the HashMap if not it is ignored
     * It also populated the orgCount HashMap. If the organization correctly predicted 
     * winner for that award the value is incremented.
     * @param fileNameIn is the CSV file that will be read
     * @throws IOException for scanner
     */
    public void readCalculations(String fileNameIn) throws IOException {
        Scanner scanFile = new Scanner(new File(fileNameIn));
        HashMap<String, Calculations> calculationByOrg = new HashMap<String, Calculations>();
        HashMap<String, HashMap<String, Calculations>> calculationByAward = new HashMap<String, HashMap<String, Calculations>>();
        HashMap<String, Calculations> cloneMap1 = new HashMap<String, Calculations>();
        HashMap<String, HashMap<String, Calculations>> cloneMap2 = new HashMap<String, HashMap<String, Calculations>>();
        HashMap<String, String> winners = new HashMap<String, String>();
        int year = maxYear;
        int prevYear = maxYear;
        award = scanFile.nextLine();
        String kalKey = "";
        double kal = 0;
        double kalCount = 0;
        String prevAward = "Actor - Leading Role";
        Nomination p = new Nomination("", 0, "");
        while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine();
            Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
            while (scanProbability.hasNext()) {
                year = Integer.parseInt(scanProbability.next());
                if (year < prevYear) {
                    cloneMap2 = new HashMap<String, HashMap<String, Calculations>>(calculationByAward);
                    calculationByYear.put(prevYear, cloneMap2);
                    prevYear = year;
                    calculationByAward.clear();
                }
                HashMap<String, HashMap<String, Nomination>> map1 = new HashMap<String, HashMap<String, Nomination>>();
                HashMap<String, Nomination> map2 = new HashMap<String, Nomination>();
                map1 = nomineesByYear.get(year);
                award = scanProbability.next();
                if (!award.equalsIgnoreCase(prevAward)) {
                    cloneMap1 = new HashMap<String, Calculations>(calculationByOrg);
                    calculationByAward.put(prevAward, cloneMap1);
                    calculationByOrg.clear();
                    prevAward = award;
                }
                try {
                    map2 = map1.get(award);
                }
                catch (NullPointerException e) {
                }
                name = scanProbability.next();
                organization = scanProbability.next();
                Calculations c = new Calculations(name, organization, 0);
                kalKey = organization + " " + award;
                try {
                    kalCount = orgCount.get(kalKey);
                    kalCount += 1;
                }
                catch (NullPointerException e) {
                    kalCount = 1;
                }
                orgCount.put(kalKey, kalCount);
                if (map2 != null && map2.containsKey(name)) {
                    calculationByOrg.put(organization, c);
                    winners = allWinners.get(year);
                    if (!award.equals("AA Filler Data")
                            && winners.get(award).equalsIgnoreCase(name)) {
                        try {
                            kal = kalmanMap.get(kalKey);
                            kal += 1;
                        }
                        catch (NullPointerException e) {
                            kal = 1;
                        }
                        kalmanMap.put(kalKey, kal);
                    }
                }
            }
        }
        cloneMap2 = new HashMap<String, HashMap<String, Calculations>>(calculationByAward);
        calculationByYear.put(prevYear, cloneMap2);
    }


    // Set methods for setting initial nominee coefficients.

    /**
     * Finds the percent accuracy an organization is at predicting a particular award
     * winner and stores that percent in the kalmanMap HashMap
     */
    public void setCoefficients() {
        double kalCount = 0;
        double kalmancoef = 0;
        double newcoef = 0;
        for (int i = 0; i < awardList.length; i++) {
            for (int j = 0; j < orgList.length; j++) {
                kalKey = orgList[j] + " " + awardList[i];
                try {
                    kalCount = orgCount.get(kalKey);
                    kalmancoef = kalmanMap.get(kalKey);
                    kalmancoef = kalmancoef / kalCount;
                    kalmanMap.put(kalKey, kalmancoef);
                }
                catch (NullPointerException e) {
                }
            }
        }
    }

    /**
     * Sets all the calculations in a given award by
     * scanning both the calculations HashMaps and setting the coefficients
     * to their respective nominee.
     * @param awardIn is the name of the award to set coefficients to
     */
    public void setCalculationsCoef(String awardIn) {
        double kalmancoef = 0.0;
        HashMap<String, Calculations> coefByOrg = new HashMap<String, Calculations>();
        HashMap<String, HashMap<String, Calculations>> coefByAward = new HashMap<String, HashMap<String, Calculations>>();
        HashMap <String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        for (int i = minYear; i <= maxYear; i++) {
            resetNominees(awardIn, i);
            coefByAward = calculationByYear.get(i);
            nominationsByAward = nomineesByYear.get(i);
            coefByOrg = coefByAward.get(awardIn);
            nominations = nominationsByAward.get(awardIn);
            for (int j = 0; j < orgList.length; j++) {
                kalKey = orgList[j] + " " + awardIn;
                Calculations c = new Calculations("", "", 0);
                Nomination n = new Nomination("", 0, "");
                try {
                    c = coefByOrg.get(orgList[j]);
                    double coef = kalmanMap.get(kalKey);
                    kalmancoef = coef + c.getCoefficient();
                    c.setCoefficient(coef);
                    coefByOrg.put(orgList[j], c);
                    try {
                        name = c.getName();
                        n = nominations.get(name);
                        double totalcoef = n.getCoefficient() + coef;
                        n.setCoefficient(totalcoef);
                    }
                    catch (NullPointerException e) {
                    }
                }
                catch (NullPointerException e) {
                }
                coefByAward.put(awardIn, coefByOrg);
            }
            calculationByYear.put(i, coefByAward);
        }
    }

    /**
     * resets the coefficients for all nominees for a given award and year.
     * @param awardIn is award for nominees
     * @param year is award year
     */
    public void resetNominees(String awardIn, int year) {
        HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        HashMap <String, Nomination> nominations = new HashMap<String, Nomination>();
        nominationsByAward = nomineesByYear.get(year);
        nominations = nominationsByAward.get(awardIn);
        try {
            for (String key : nominations.keySet()) {
                Nomination n = new Nomination("", 0, "");
                n = nominations.get(key);
                n.setCoefficient(0);
                nominations.put(key, n);
            }
        }
        catch (NullPointerException e) {
        }
        nominationsByAward.put(awardIn, nominations);
        nomineesByYear.put(year, nominationsByAward);
    }

    /**
     * Sets all the calculations in a given year by
     * scanning both the calculations HashMaps and setting the coefficients
     * to their respective nominee.
     * @param year is the year of the calculations to set
     */
    public void setAllCalculationscoef(int year) {
        double getcoef = 0;
        double kalmancoef = 0;
        HashMap<String, Calculations> coefByOrg = new HashMap<String, Calculations>();
        HashMap<String, HashMap<String, Calculations>> coefByAward = new HashMap<String, HashMap<String, Calculations>>();
        coefByAward = calculationByYear.get(year);
        HashMap <String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        coefByAward = calculationByYear.get(year);
        nominationsByAward = nomineesByYear.get(year);
        for (int i = 0; i < awardList.length; i++) {
            coefByOrg = coefByAward.get(awardList[i]);
            nominations = nominationsByAward.get(awardList[i]);
            for (int j = 0; j < orgList.length; j++) {
                kalKey = orgList[j] + " " + awardList[i];
                Calculations c = new Calculations("", "", 0);
                Nomination n = new Nomination("", 0, "");
                try {
                    c = coefByOrg.get(orgList[j]);
                    kalmancoef = kalmanMap.get(kalKey);
                    kalmancoef = kalmancoef + c.getCoefficient();
                    c.setCoefficient(kalmancoef);
                    coefByOrg.put(orgList[j], c);
                    try {
                        name = c.getName();
                        n = nominations.get(name);
                        n.setCoefficient(kalmancoef);
                    }
                    catch (NullPointerException e) {
                    }
                }
                catch (NullPointerException e) {
                }
                coefByAward.put(awardList[i], coefByOrg);
            }
            calculationByYear.put(year, coefByAward);
        }
    }


    // KalMan Filtering Methods

    /**
     * Recursive method to adjust the initial coefficients to better predict the winners.
     * It gets a value from the kalman map and increases it by the scalar,
     * then finds the accuracy of the new coefficient, if the accuracy increases it increases
     * the coefficients again until the coefficient is optimal.
     * If the accuracy decreases it decreases the coefficient until the coefficient is optimal
     * The while loops use maxCount to prevent infinite loops
     */
    public void kalmanFilter() {
        HashMap<Integer, String> winners = new HashMap<Integer, String>();
        double prevCalcCount = 0;
        boolean adjusted = true;
        for (int i = 0; i < awardList.length; i++) {
            //setCalculationsCoef(awardList[i]);
            winners = generateWinByAward(awardList[i]);
            calcCount = findAccuracy(awardList[i], winners);
            prevCalcCount = calcCount;
            for (int j = 0; j < orgList.length; j++) {
                kalKey = orgList[j] + " " + awardList[i];
                boolean go = true;
                while (go) {
                    calcCount = kalmanAdjust(true, awardList[i], kalKey);
                    adjusted = kalmanCheck(calcCount);
                    if (calcCount == prevCalcCount) {
                        int count = 0;
                        while (count < maxCount && adjusted && calcCount == prevCalcCount) {
                            prevCalcCount = calcCount;
                            calcCount = kalmanAdjust(true, awardList[i], kalKey);
                            adjusted = kalmanCheck(calcCount);
                            count++;
                        }
                        if (calcCount > prevCalcCount) {
                            count = 0;
                            while (count < maxCount && adjusted && calcCount >= prevCalcCount) {
                                prevCalcCount = calcCount;
                                calcCount = kalmanAdjust(true, awardList[i], kalKey);
                                adjusted = kalmanCheck(calcCount);
                                count++;
                            }
                            calcCount = kalmanAdjust(false, awardList[i], kalKey);
                            adjusted = kalmanCheck(calcCount);
                            prevCalcCount = calcCount;
                            go = false;
                        }
                        else if (calcCount < prevCalcCount) {
                            calcCount = kalmanAdjust(false, awardList[i], kalKey);
                            adjusted = kalmanCheck(calcCount);
                            prevCalcCount = calcCount;
                            count = 0;
                            while (count < maxCount && adjusted && calcCount >= prevCalcCount) {
                                prevCalcCount = calcCount;
                                calcCount = kalmanAdjust(false, awardList[i], kalKey);
                                adjusted = kalmanCheck(calcCount);
                                count++;
                            }
                            if (adjusted) {
                                calcCount = kalmanAdjust(true, awardList[i], kalKey);
                                adjusted = kalmanCheck(calcCount);
                                prevCalcCount = calcCount;
                            }
                        }
                    }
                    else if (calcCount > prevCalcCount) {
                        int count = 0;
                        while (count < maxCount && adjusted && calcCount >= prevCalcCount) {
                            prevCalcCount = calcCount;
                            calcCount = kalmanAdjust(true, awardList[i], kalKey);
                            adjusted = kalmanCheck(calcCount);
                            count++;
                        }
                        calcCount = kalmanAdjust(false, awardList[i], kalKey);
                        adjusted = kalmanCheck(calcCount);
                        prevCalcCount = calcCount;
                        go = false;
                    }
                    else if (calcCount < prevCalcCount) {
                        calcCount = kalmanAdjust(false, awardList[i], kalKey);
                        adjusted = kalmanCheck(calcCount);
                        prevCalcCount = calcCount;
                        int count = 0;
                        while (count < maxCount && adjusted && calcCount >= prevCalcCount) {
                            prevCalcCount = calcCount;
                            calcCount = kalmanAdjust(false, awardList[i], kalKey);
                            adjusted = kalmanCheck(calcCount);
                            count++;
                        }
                        if (adjusted) {
                            calcCount = kalmanAdjust(true, awardList[i], kalKey);
                            adjusted = kalmanCheck(calcCount);
                            prevCalcCount = calcCount;
                        }
                    }
                    go = false;
                }
            }
        }
    }

    /**
     * Adjusts the values in the kalmanMap by a given scalar.
     * @param kalKey is the kalmanKey that stores the coefficient in the kalmanMap HashMap
     * @param increase is used to set how the coefficient is adjuste true = increase false = decrease
     * @return a boolean that is true if coefficient was adjusted and false if coefficients 0
     */
    public boolean adjustKalmanMap(String kalKey, boolean increase) {
        double kalmancoef = 0;
        double newcoef = 0;
        try {
            kalmancoef = kalmanMap.get(kalKey);
            if (!increase) {
                newcoef = kalmancoef - scalar;
                if (kalmancoef <= 0.0) {
                    kalmanMap.put(kalKey, 0.0);
                    return false;
                }
                else {
                    kalmanMap.put(kalKey, newcoef);
                    return true;
                }
            }
            if (increase) {
                newcoef = kalmancoef + scalar;
                if (kalmancoef == 0) {
                    kalmanMap.put(kalKey, kalmancoef);
                    return false;
                }
                else {
                    kalmanMap.put(kalKey, newcoef);
                    return true;
                }
            }
        }
        catch (NullPointerException e) {
        }
        return false;
    }

    /**
     * Macro method that calls on other methods to adjust calculations for filtering
     * Calls all the methods needed to adjust the kalman filter coefficient and determine if
     * accuracy increases or decreases.
     * @param direction determines to increase or decrease filter
     * @param award is the award to filter
     * @param kalKey is the key for the Kalman HashMap
     * @return the calcCount. If it is negative it was not adjusted
     */
    public double kalmanAdjust (boolean direction, String award, String kalKey) {
        boolean adjusted = adjustKalmanMap(kalKey, direction);
        setCalculationsCoef(award);
        HashMap<Integer, String> winners = generateWinByAward(award);
        calcCount = findAccuracy(award, winners);
        if (!adjusted) {
            calcCount = calcCount * -1;
        }
        return calcCount;
    }

    /**
     * Method to invert a negative calcCount and returns boolean if it was inverted
     * @param calcCount calcCount to be tested for negative value
     * @return false if negative calcCount, true if positive
     */
    public boolean kalmanCheck(double calcCount) {
        if (calcCount <= 0) {
            calcCount = calcCount * -1;
            return false;
        }
        return true;
    }

    /**
     * Generates the winners based off the nominee with the highest coefficient.
     * Scans all the nominees across all years for a particular award and finds the
     * nominee with the highest coefficient. The predicted winners are stored in a HashMap
     * where the key is the year and the data is the name of the winner.
     * @param awardIn is the award to generate winners for
     * @return a HashMap of winners where key is year and the
     * data is the winner's name
     */
    public HashMap<Integer, String> generateWinByAward(String awardIn) {
        HashMap<Integer, String> output = new HashMap<Integer, String>();
        HashMap <String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        for (int j = minYear; j <= maxYear; j++) {
            nominationsByAward = nomineesByYear.get(j);
            nominations = nominationsByAward.get(awardIn);
            Nomination highestcoef = new Nomination("", 0, "");
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = new Nomination("", 0, "");
                    n = nominations.get(key);
                    if (highestcoef.getName().equals("")) {
                        highestcoef = n;
                    }
                    if (n.getCoefficient() > highestcoef.getCoefficient()) {
                        highestcoef = n;
                    }
                }
                output.put(j, highestcoef.getName());
            }
            catch (NullPointerException e) {
            }
        }
        return output;
    }

    /**
     * Determines how accurate a prediction is by comparing a
     * HashMap of predicted winners to a HashMap of known winners.
     * @param awardIn is the name of the award that will be compared to the winner
     * @param calcInput is a HashMap of predicted winners supplied by the
     * generateWinByAward method
     * @return a double representing a percent of correctly predicted winners
     */
    public double findAccuracy(String awardIn, HashMap<Integer, String> calcInput) {
        HashMap<String, String> winners = new HashMap<String, String>();
        Nomination n = new Nomination("", 0, "");
        String calcName = "";
        double calcCount = 0;
        for (int i = minYear; i <= maxYear; i++) {
            winners = allWinners.get(i);
            calcName = calcInput.get(i);
            try {
                if (calcName.equalsIgnoreCase(winners.get(awardIn))) {
                    calcCount++;
                }
            }
            catch (NullPointerException e) {
            }
        }
        double range = maxYear - minYear + 1;
        calcCount = calcCount / range;
        return calcCount;
    }

    // Methods to calculate final results.

    /**
     * Uses the coefficients to calculate the percents as well as well as the the most likely nominee to win.
     */
    public void getProbability() {
        HashMap <String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        HashMap<String, Calculations> calcByOrg = new HashMap<String, Calculations>();
        HashMap<String, HashMap<String, Calculations>> calcByAward = new HashMap<String, HashMap<String, Calculations>>();
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        for (int i = minYear; i <= maxYear; i++) {
            calcByAward = calculationByYear.get(i);
            nominationsByAward = nomineesByYear.get(i);
            HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
            for (int j = 0; j < awardList.length; j++) {
                nominations = nominationsByAward.get(awardList[j]);
                Nomination n = new Nomination("", 0, "");
                Nomination highestNominee = new Nomination("", 0, "");
                double totalcoef = 0;
                try {
                    for (Map.Entry<String, Nomination> entry  : nominations.entrySet()) {
                        n = entry.getValue();
                        totalcoef += n.getCoefficient();
                    }
                }
                catch (NullPointerException e) {
                }
                try {
                    double perc = 0;
                    for (String key : nominations.keySet()) {
                        n = nominations.get(key);
                        if (calcByOrg.size() == 1 && n.getCoefficient() < 1) {
                            perc =  n.getCoefficient() * 100;
                            n.setPercent(numFmt.format(perc));
                        }
                        else {
                            perc = n.getCoefficient() / totalcoef;
                            perc = perc * 100;
                        }
                        n.setPercent(numFmt.format(perc));
                        if (n.getCoefficient() > highestNominee.getCoefficient()) {
                            highestNominee = n;
                        }
                        nominations.put(n.getName(), n);
                    }
                }
                catch (NullPointerException e) {
                }
                highest.put(awardList[j], highestNominee);
                nominationsByAward.put(awardList[j], nominations);
            }
            nomineesByYear.put(i, nominationsByAward);
            calculatedWinners.put(i, highest);
        }
    }

    /**
     * Returns all winners in a given year.
     * The winners are stored in a HashMap where the key is the award and the
     * data is the name of the nominee.
     * @param year is the year for which to calculate the winners
     * @return  a HashMap of of all the winners
     * in a year key is the award and data is the winner
     */
    public HashMap<String, String> returnWinnersByYear(int year) {
        HashMap<String, String> output = new HashMap<String, String>();
        HashMap <String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        nominationsByAward = nomineesByYear.get(year);
        for (int i = 0; i < awardList.length; i++) {
            nominations = nominationsByAward.get(awardList[i]);
            Nomination highestcoef = new Nomination("", 0, "");
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = new Nomination("", 0, "");
                    n = nominations.get(key);
                    if (highestcoef.getName().equals("")) {
                        highestcoef = n;
                    }
                    if (n.getCoefficient() > highestcoef.getCoefficient()) {
                        highestcoef = n;
                    }
                }
                if (Double.parseDouble(highestcoef.getPercent()) < threshold) {
                    highestcoef.setName("");
                }
                output.put(awardList[i], highestcoef.getName());
            }
            catch (NullPointerException e) {
            }
        }
        return output;
    }


    //Methods to print various results.

    /**
     * returns string of each nominee toString() for a given category
     * If there are no winners the user is notified.
     * @param category is award category
     * @param year is year to calculate
     * @return toString of all nominees
     */
    public String returnResults(int year, String category) {
        String output = category + ":\n";
        HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
        highest = calculatedWinners.get(year);
        if (highest.get(category).getPercent().equals("0.0")) {
            output = "\nCannot calculate winners because there is insufficient "
                    + "data for " + category;
        }
        else {
            HashMap <String, Nomination> nominees = new HashMap<String, Nomination>();
            HashMap <String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
            nominationsByAward = nomineesByYear.get(year);
            nominees = nominationsByAward.get(category);
            for (String key : nominees.keySet()) {
                Nomination p = nominees.get(key);
                output += p.toString() + "\n";
            }
        }
        return output;
    }

    /**
     * Prints actor/nomination and all awards it won based off calculations.
     * @param category defines category of award to be calculated
     * @param year is the year to calculate
     * @return string projected  winner and all previous awards
     */
    public String getDetail(int year, String category) {
        String output = "";
        HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
        highest = calculatedWinners.get(year);
        HashMap<String, Calculations> calcByOrg = new HashMap<String, Calculations>();
        HashMap<String, HashMap<String, Calculations>> calcByAward = new HashMap<String, HashMap<String, Calculations>>();
        calcByAward = calculationByYear.get(year);
        Nomination x = highest.get(category);
        calcByOrg = calcByAward.get(category);
        if (Double.parseDouble(x.getPercent()) < threshold) {
            output = "Oscar Genie cannot accurately predict the winner "
                    + "due to lack of data.\nThe best guess is: " + x.getName()
                    + "as it also won awards from:\n";
        }
        else {
            output = x.getName() + " is most "
                    + "likely to win an Oscar\n" + x.getName()
                    + " also won awards this year from:\n\n";
        }
        for (String key : calcByOrg.keySet()) {
            if (calcByOrg.get(key).getName().equals(x.getName())) {
                output += calcByOrg.get(key).getOrganization() + "\n";
            }
        }
        return output;
    }

    /**
     * Output all contents of nominees in winnerMap toString()
     * if there is no winner for a category it outputs that it can't predict a winner.
     * @param year is year to generate all winners for
     * @return string of winners
     */
    public String generateAll(int year) {
        HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
        String output = "";
        highest = calculatedWinners.get(year);
        for (int i = 0; i < awardList.length; i++) {
            Nomination n = highest.get(awardList[i]);
            output += awardList[i] + ": ";
            if (n.getName().equals("")) {
                output += "Cannot calculate "
                        + "winners because there is insufficient data\n";
            }
            else {
                output += n.toString() + "\n";
            }
        }
        return output;
    }

    /**
     * Compares predicted winners to actual winners and outputs
     * correct winners and percent of winners correctly predicted.
     * Calculates the percent accuracy of the program.that year
     * @param year is year to get details for
     * @return String output which prints all data
     */
    public String allDetails(int year) {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        String output = "";
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
        highest = calculatedWinners.get(year);
        winners = allWinners.get(year);
        double count = 0;
        double correct = 0;
        double perc = 0;
        for (int i = 0; i < awardList.length; i++) {
            try {
                output += "The actual winner for " + awardList[i] + " is "
                        + winners.get(awardList[i]) + ":\n";
                if (Double.parseDouble(highest.get(awardList[i]).getPercent()) < threshold) {
                    output += "Oscar Genie did not have enough data "
                            + "to predict this\n";
                }
                else if (highest.get(awardList[i]).getName().equalsIgnoreCase(
                        winners.get(awardList[i]))) {
                    output += " Oscar Genie correctly predicted this winner\n";
                    correct++;
                    count++;
                }
                else {
                    output += " Oscar Genie did not predict this winner\n";
                    count++;
                }
            }
            catch (NullPointerException e) {
            }
        }
        perc = correct / count;
        perc = perc * 100;
        output += "Oscar Genie was able to predict " + correct + " out of "
                + count + " which is " + numFmt.format(perc)
                + "% of winners it had data for\n";
        return output;
    }

    /**
     * Calculates the percent correct predictions program made.
     * @return String output that prints percent correct
     */
    public String getTotalPercent() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        DecimalFormat countFmt = new DecimalFormat("#00");
        String output = "";
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Calculations>> calcByAward = new HashMap<String, HashMap<String, Calculations>>();
        double count = 0;
        double correct = 0;
        double perc = 0;
        for (int j = minYear; j <= maxYear; j++) {
            calcByAward = calculationByYear.get(j);
            highest = calculatedWinners.get(j);
            winners = allWinners.get(j);
            for (int i = 0; i < awardList.length; i++) {
                try {
                    if (highest.get(awardList[i]).getName().equalsIgnoreCase(
                            winners.get(awardList[i]))
                            && Double.parseDouble(highest.get(awardList[i]).getPercent()) > threshold) {
                        correct++;
                        count++;
                    }
                    else if (Double.parseDouble(highest.get(awardList[i]).getPercent()) > threshold) {
                        count++;
                    }
                }
                catch (NullPointerException e) {
                }
            }
        }

        perc = correct / count;
        perc = perc * 100;
        output += "Oscar Genie was able to predict " + countFmt.format(correct)
                + " winners out of " + countFmt.format(count)
                + " nominees it had data for\nWhich is "
                + numFmt.format(perc) + "% accuracy\n\n";
        return output;
    }

    /**
     * Generates a percent accuracy the program is at calculating each category
     * across all years.
     * @return a String of awards and percent correct predicted for each award.
     */
    public String awardAccuracy () {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        String output = "";
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap <String, Nomination> highest = new HashMap<String, Nomination>();
        for (int i = 0; i < awardList.length; i++) {
            double count = 0;
            double correct = 0;
            double perc = 0;
            for (int j = minYear; j <= maxYear; j++) {
                highest = calculatedWinners.get(j);
                winners = allWinners.get(j);
                try {
                    if (highest.get(awardList[i]).getName().equalsIgnoreCase(winners.get(awardList[i]))
                            && Double.parseDouble(highest.get(awardList[i]).getPercent()) > threshold) {
                        correct++;
                        count++;
                    }
                    else if (Double.parseDouble(highest.get(awardList[i]).getPercent()) > threshold) {
                        count++;
                    }
                }
                catch (NullPointerException e) {
                }
            }
            perc = correct / count;
            perc = perc * 100;
            output += awardList[i] + ": " + numFmt.format(perc) + "%\n";
        }
        return output;
    }


    //General print methods

    /**
     * prints help text.
     * @return String output of help text
     */
    public String printHelp() {
        String output = "Enter either the number "
                + "corresponding to the selection you wish to "
                + "predict or\nyou can enter the name of the award\n"
                + "Case does not matter\n"
                + "please email any bugs or errors "
                + "to rjcrumpler1@me.com";
        return output;
    }

    /**
     * prints info text.
     * @return String output of info text
     */
    public String printInfo() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        String output = "This program was written by Ryan "
                + "Crumpler\nIt can predict each category with the given accuracies "
                + awardAccuracy() + "\n"
                + getTotalPercent()
                + "It works by scanning sources such "
                + "as movie critic associations\n and determining "
                + "how effective each source is at predicting Oscar "
                + "winners\nfor each category It then weights each "
                + "source and makes a prediction\nbased on all the "
                + "sources and outputs the results.\n***Please Note***\n"
                + "Generally an output of\n0.00% indicates insufficient"
                + "\ndata for that nominee\nIf there is only data for 1"
                + " nominee and\nthe nominee has less than a "
                + numFmt.format(threshold)
                + "% chance of winning Oscar Genie considers\nthat award "
                + "to have insufficient data";
        return output;
    }
}
