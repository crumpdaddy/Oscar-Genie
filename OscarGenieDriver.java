import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

/** This program is the driver that carries out all methods 
* to determine chance of winning an oscar.
* 
* @author Ryan Crumpler
* @version 14.8.17
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
     * List or organizations to be used to try to predict winners.
     */
    protected static String[] orgList = new String[]{"New York Film Critics",
            "LA Film Critics", "DC Film Critics", "Boston Film Critics",
            "San Francisco Critics", "Las Vegas Critics", "Houston Critics",
            "Screen Actors Guild", "Writers Guild", "Producers Guild",
            "Directors Guild", "Visual Effects Society", "Critics Choice",
            "Golden Globes", "Golden Globes-Musical or Comedy",
            "Golden Globes-Drama"};

    /**
     * Static int that defines the max number of times the program will adjust the filter
     */
    private static int maxCount = 25;

    /**
     * integers to define years to begin and end calculations on.
     */
    private int minYear, maxYear;

    private double threshold, coefficent, scalar, calcCount;
    protected String name, award, movie, nominee, organization, percent;

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
    private HashMap<Integer, HashMap<String, HashMap<String, Nomination>>> nomineesByYear;

    /**
     * HashMap structure for storing actual and calculated winners.
     * Key is year of Oscars
     * Stores a HashMap for the actual winners where key is award and the data is the winner.
     */
    private HashMap<Integer, HashMap<String, Nomination>> calculatedWinners;
    private HashMap<Integer, HashMap<String, String>> allWinners;

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


    /**
     * This program reads a CSV file that contains every Oscar nominee and winner
     * it then reads a CSV that contains a set of calculations based off of which
     * nominees won awards from other orginizations and adds the calculations and
     * nominees to hashmaps. It then kalman filters the calculations to find the
     * optimal formula to predict Oscar winners
     */
    public OscarGenieDriver() {
        nomineesByYear = new HashMap<>();
        allWinners = new HashMap<>();
        kalmanMap = new HashMap<>();
        orgCount = new HashMap<>();
        calculatedWinners = new HashMap<>();

    }

    /**
     * Method to run the initial setup for driver program.
     * Calls on all required methods to produce accurate results.
     * @param minYear first year to start predicting results for
     * @param maxYear last year to predict results for
     * @throws IOException for scanner
     */
    public void setup(int minYear, int maxYear, double threshold) throws IOException{
        setMinMaxYear(1991, 2017);
        setScalar(.07);
        setThreshold(threshold);
        readNominee("all_nominations.csv");
        readCalculations("all_calculations.csv");
        setCoefficients();
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
     * @return String[[] of list of awards orginizations
     * used in OscarGenie to iterate through awards orginizations
     */
    public String[] getOrgList() {
        return orgList;
    }

    /**
     * Sets the range of time to preform calculations on
     *
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
     *                    defines a winner.
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
     *
     * @throws IOException for scanner
     */
    public void readNominee(String fileNameIn) throws IOException {
        Scanner scanFile = new Scanner(new File(fileNameIn));
        HashMap<String, Nomination> nomMap = new HashMap<String, Nomination>();
        HashMap<String, Nomination> cloneMap = new HashMap<String, Nomination>();
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, String> winnersClone = new HashMap<String, String>();
        HashMap<String, HashMap<String, Nomination>> nomineeMap = new HashMap<String, HashMap<String, Nomination>>();
        HashMap<String, HashMap<String, Nomination>> cloneMap2 = new HashMap<String, HashMap<String, Nomination>>();
        ArrayList<String> awardOrg = new ArrayList<String>();
        boolean nextLine = true;
        award = scanFile.nextLine();
        String won = "";
        String notes = "";
        int year = minYear;
        int pastYear = minYear;
        String prevAward = "";
        while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine();
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNext()) {
                year = Integer.parseInt(scanNominee.next());
                if (year != pastYear) {
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
                if (award.equals("Actor - Leading Role") || award.equals("Actor - Supporting Role")
                        || award.equals("Actress - Leading Role") || award.equals("Actress - Supporting Role")
                        || award.equals("Song")) {
                    if (award.compareTo("Song") == 0) {
                        song = true;
                    }
                    percent = "0.0";
                    BestActor n = new BestActor(name, 0, percent, awardOrg,  movie, actress, song);
                    if (award.compareTo("Actress - Supporting Role") == 0
                            || award.compareTo("Actress - Leading Role") == 0) {
                        n.setActress(true);
                    }
                    nomMap.put(name, n);
                }
                else {
                    Nomination n = new Nomination(name, 0, percent, awardOrg);
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
     *
     * @param fileNameIn is the CSV file that will be read
     * @throws IOException for scanner
     */
    public void readCalculations(String fileNameIn) throws IOException {
        Scanner scanFile = new Scanner(new File(fileNameIn));
        HashMap<String, Nomination> nominees = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, Nomination> cloneMap1 = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Nomination>> cloneMap2 = new HashMap<String, HashMap<String, Nomination>>();
        ArrayList<String> tempArr = new ArrayList<String>();
        ArrayList<String> awardOrg = new ArrayList<String>();
        awardOrg = new ArrayList<>();
        int year = minYear;
        int prevYear = minYear;
        award = scanFile.nextLine();
        String kalKey = "";
        double kal = 0;
        double kalCount = 0;
        String prevAward = "Actor - Leading Role";
        while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine();
            Scanner scanProbability = new Scanner(nominee).useDelimiter(",");
            while (scanProbability.hasNext()) {
                year = Integer.parseInt(scanProbability.next());
                nominationsByAward = nomineesByYear.get(year);
                if (year != prevYear) {
                    prevYear = year;
                }
                award = scanProbability.next();
                if (!award.equalsIgnoreCase(prevAward)) {
                    try {
                        cloneMap1 = new HashMap<String, Nomination>(nominees);
                        nominationsByAward.put(prevAward, cloneMap1);
                        nominees.clear();
                    }
                    catch (NullPointerException e) {
                    }
                    prevAward = award;
                }
                if (award.equalsIgnoreCase("Z Filler Data")) {
                    try {
                        cloneMap2 = new HashMap<String, HashMap<String, Nomination>>(nominationsByAward);
                        nomineesByYear.put(prevYear, cloneMap2);
                        nominationsByAward.clear();
                    }
                    catch (NullPointerException e) {
                    }
                }
                try {
                    nominees = nominationsByAward.get(award);
                }
                catch (NullPointerException e) {
                }
                name = scanProbability.next();
                organization = scanProbability.next();
                kalKey = organization + " " + award;
                try {
                    kalCount = orgCount.get(kalKey);
                    kalCount += 1;
                }
                catch (NullPointerException e) {
                    kalCount = 1;
                }
                orgCount.put(kalKey, kalCount);
                if (nominees != null && nominees.containsKey(name)) {
                    Nomination n = new Nomination("", 0, "", awardOrg);
                    n = nominees.get(name);
                    awardOrg = n.getAwardOrg();
                    awardOrg.add(organization);
                    tempArr = new ArrayList<String>(awardOrg);
                    n.setAwardOrg(tempArr);
                    nominees.put(name, n);
                    awardOrg.clear();
                    winners = allWinners.get(year);
                    if (!award.equals("AA Filler Data") && winners.get(award).equalsIgnoreCase(name)) {
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
        try {
            cloneMap2 = new HashMap<String, HashMap<String, Nomination>>(nominationsByAward);
            nomineesByYear.put(year, cloneMap2);
        }
        catch (NullPointerException e) {
        }
    }


    // Set methods for setting initial nominee coefficients.

    /**
     * Finds the percent accuracy an organization is at predicting a particular award
     * winner and stores that percent in the kalmanMap HashMap
     */
    public void setCoefficients() {
        String kalKey = "";
        double kalCount = 0;
        double kalmanCoeff = 0;
        double newCoeff = 0;
        for (int i = 0; i < awardList.length; i++) {
            for (int j = 0; j < orgList.length; j++) {
                kalKey = orgList[j] + " " + awardList[i];
                try {
                    kalCount = orgCount.get(kalKey);
                    kalmanCoeff = kalmanMap.get(kalKey);
                    kalmanCoeff = kalmanCoeff / kalCount;
                    kalmanMap.put(kalKey, kalmanCoeff);
                }
                catch (NullPointerException e) {
                }
            }
        }
    }

    /**
     * Sets all the calculations in a given year by
     * scanning both the Nominations awardOrg ArrayLists and setting the coefficients
     * to their respective nominee.
     * @param awardIn is the award to set
     */
    public void setCalculationsCoef(String awardIn) {
        String kalKey = "";
        double getCoeff = 0;
        double kalmanCoeff = 0;
        HashMap<String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        ArrayList<String> awardOrg = new ArrayList<String>();
        for (int i = minYear; i <= maxYear; i++) {
            //resetNominees(awardIn, i);
            nominationsByAward = nomineesByYear.get(i);
            nominationsByAward = nomineesByYear.get(i);
            nominations = nominationsByAward.get(awardIn);
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = new Nomination("", 0, "", null);
                    n = nominations.get(key);
                    awardOrg = n.getAwardOrg();
                    double coef = 0;
                    for (int j = 0; j < awardOrg.size(); j++) {
                        kalKey = awardOrg.get(j) + " " + awardIn;
                        coef += kalmanMap.get(kalKey);
                    }
                    n.setCoefficient(coef);
                    nominations.put(n.getName(), n);
                }
            }
            catch (NullPointerException e) {
            }
            nominationsByAward.put(awardIn, nominations);
            nomineesByYear.put(i, nominationsByAward);
        }
    }

    /**
     * resets the coefficients for all nominees for a given award and year.
     *
     * @param awardIn is award for nominees
     * @param year    is award year
     */
    public void resetNominees(String awardIn, int year) {
        HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        HashMap<String, Nomination> nominations = new HashMap<String, Nomination>();
        nominationsByAward = nomineesByYear.get(year);
        nominations = nominationsByAward.get(awardIn);
        try {
            for (String key : nominations.keySet()) {
                Nomination n = new Nomination("", 0, "", null);
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
        String kalKey = "";
        int count = 0;
        for (int j = 0; j < awardList.length; j++) {
            award = awardList[j];
            setCalculationsCoef(awardList[j]);
            winners = generateWinByAward(awardList[j]);
            calcCount = findAccuracy(awardList[j], winners);
            prevCalcCount = calcCount;
            for (int k = 0; k < orgList.length; k++) {
                kalKey = orgList[k] + " " + awardList[j];
                adjusted = adjustKalmanMap(kalKey, true);
                setCalculationsCoef(awardList[j]);
                winners = generateWinByAward(awardList[j]);
                calcCount = findAccuracy(awardList[j], winners);
                count = 0;
                boolean go = true;
                while (go) {
                    if (calcCount == prevCalcCount) {
                        while (calcCount == prevCalcCount && count <= maxCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMap(kalKey, true);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count++;
                        }
                        if (calcCount < prevCalcCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMap(kalKey, false);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count = 0;
                            while (calcCount >= prevCalcCount && adjusted) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMap(kalKey, false);
                                setCalculationsCoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            if (adjusted) {
                                adjusted = adjustKalmanMap(kalKey, true);
                                setCalculationsCoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                            }
                            prevCalcCount = calcCount;
                        }
                        else if (calcCount > prevCalcCount) {
                            count = 0;
                            while (calcCount >= prevCalcCount && count < maxCount) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMap(kalKey, true);
                                setCalculationsCoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            adjusted = adjustKalmanMap(kalKey, false);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            prevCalcCount = calcCount;
                        }
                    }
                    else if (calcCount > prevCalcCount) {
                        count = 0;
                        while (calcCount >= prevCalcCount && count < maxCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMap(kalKey, true);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count++;
                        }
                        adjusted = adjustKalmanMap(kalKey, false);
                        setCalculationsCoef(awardList[j]);
                        winners = generateWinByAward(awardList[j]);
                        calcCount = findAccuracy(awardList[j], winners);
                        prevCalcCount = calcCount;
                    }
                    else if (calcCount < prevCalcCount) {
                        prevCalcCount = calcCount;
                        adjusted = adjustKalmanMap(kalKey, false);
                        setCalculationsCoef(awardList[j]);
                        winners = generateWinByAward(awardList[j]);
                        calcCount = findAccuracy(awardList[j], winners);
                        count = 0;
                        while (calcCount >= prevCalcCount && adjusted) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMap(kalKey, false);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count++;
                        }
                        if (adjusted) {
                            adjusted = adjustKalmanMap(kalKey, true);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                        }
                        prevCalcCount = calcCount;
                    }
                    go = false;
                }
            }
        }
    }

    /**
     * Adjusts the values in the kalmanMap by a given scalar.
     *
     * @param kalKey   is the kalmanKey that stores the coefficient in the kalmanMap HashMap
     * @param increase is used to set how the coefficient is adjuste true = increase false = decrease
     * @return a boolean that is true if coefficient was adjusted and false if coefficients 0
     */
    public boolean adjustKalmanMap(String kalKey, boolean increase) {
        double kalmanCoeff = 0;
        double newCoeff = 0;
        try {
            kalmanCoeff = kalmanMap.get(kalKey);
            if (!increase) {
                newCoeff = kalmanCoeff - scalar;
                if (kalmanCoeff <= 0.0) {
                    kalmanMap.put(kalKey, 0.0);
                    return false;
                }
                else {
                    kalmanMap.put(kalKey, newCoeff);
                    return true;
                }
            }
            if (increase) {
                newCoeff = kalmanCoeff + scalar;
                if (kalmanCoeff == 0) {
                    kalmanMap.put(kalKey, kalmanCoeff);
                    return false;
                }
                else {
                    kalmanMap.put(kalKey, newCoeff);
                    return true;
                }
            }
        }
        catch (NullPointerException e) {
        }
        return false;
    }

    /**
     * Sets all the calculations in a given award by
     * scanning both the calculations HashMaps and setting the coefficients
     * to their respective nominee.
     *
     * @param awardIn is the name of the award to set coefficients to
     */
    public HashMap<Integer, String> generateWinByAward(String awardIn) {
        HashMap<Integer, String> output = new HashMap<Integer, String>();
        HashMap<String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        for (int j = minYear; j <= maxYear; j++) {
            nominationsByAward = nomineesByYear.get(j);
            nominations = nominationsByAward.get(awardIn);
            Nomination highestCoeff = new Nomination("", 0, "", null);
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = new Nomination("", 0, "", null);
                    n = nominations.get(key);
                    if (highestCoeff.getName().equals("")) {
                        highestCoeff = n;
                    }
                    if (n.getCoefficient() > highestCoeff.getCoefficient()) {
                        highestCoeff = n;
                    }
                }
                output.put(j, highestCoeff.getName());
            }
            catch (NullPointerException e) {
            }
        }
        return output;
    }

    /**
     * Determines how accurate a prediction is by comparing a
     * HashMap of predicted winners to a HashMap of known winners.
     *
     * @param awardIn   is the name of the award that will be compared to the winner
     * @param calcInput is a HashMap of predicted winners supplied by the
     *                  generateWinByAward method
     * @return a double representing a percent of correctly predicted winners
     */
    public double findAccuracy(String awardIn, HashMap<Integer, String> calcInput) {
        HashMap<String, String> winners = new HashMap<String, String>();
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
     * Returns all winners in a given year.
     * The winners are stored in a HashMap where the key is the award and the
     * data is the name of the nominee.
     *
     * @param year is the year for which to calculate the winners
     * @return a HashMap of of all the winners
     * in a year key is the award and data is the winner
     */
    public HashMap<String, String> returnWinnersByYear(int year) {
        HashMap<String, String> output = new HashMap<String, String>();
        HashMap<String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        nominationsByAward = nomineesByYear.get(year);
        for (int i = 0; i < awardList.length; i++) {
            nominations = nominationsByAward.get(awardList[i]);
            Nomination highestCoeff = new Nomination("", 0, "", null);
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = new Nomination("", 0, "", null);
                    n = nominations.get(key);
                    if (highestCoeff.getName().equals("")) {
                        highestCoeff = n;
                    }
                    if (n.getCoefficient() > highestCoeff.getCoefficient()) {
                        highestCoeff = n;
                    }
                }
                if (highestCoeff.getCoefficient() < threshold) {
                    highestCoeff.setName("");
                }
                output.put(awardList[i], highestCoeff.getName());
            }
            catch (NullPointerException e) {
            }
        }
        return output;
    }

    /**
     * Uses the coefficients to calculate the percents as well as well as the the most likely nominee to win.
     */
    public void getProbability() {
        HashMap<String, Nomination> nominations = new HashMap<String, Nomination>();
        HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
        ArrayList<String> awardOrg = new ArrayList<String>();
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        for (int i = minYear; i <= maxYear; i++) {
            nominationsByAward = nomineesByYear.get(i);
            HashMap<String, Nomination> highest = new HashMap<String, Nomination>();
            for (int j = 0; j < awardList.length; j++) {
                nominations = nominationsByAward.get(awardList[j]);
                Nomination n = new Nomination("", 0, "", null);
                Nomination highestNominee = new Nomination("", 0, "", null);
                double totalcoef = 0;
                try {
                    for (String key : nominations.keySet()) {
                        n = nominations.get(key);
                        totalcoef += n.getCoefficient();
                    }
                }
                catch (NullPointerException e) {
                }
                try {
                    double perc = 0;
                    for (String key : nominations.keySet()) {
                        n = nominations.get(key);
                        awardOrg = n.getAwardOrg();
                        if (awardOrg.size() == 1 && n.getCoefficient() < 1) {
                            perc = n.getCoefficient() * 100;
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
                } catch (NullPointerException e) {
                }
                highest.put(awardList[j], highestNominee);
                nominationsByAward.put(awardList[j], nominations);
            }
            nomineesByYear.put(i, nominationsByAward);
            calculatedWinners.put(i, highest);
        }
    }


    //Methods to print various results.

    /**
     * Output all contents of nominees in winnerMap toString()
     * if there is no winner for a category it outputs that it can't predict a winner.
     *
     * @param year is year to generate all winners for
     * @return string of winners
     */
    public String generateAll(int year) {
        HashMap<String, Nomination> highest = new HashMap<String, Nomination>();
        String output = "";
        highest = calculatedWinners.get(year);
        for (int i = 0; i < awardList.length; i++) {
            Nomination n = highest.get(awardList[i]);
            output += awardList[i] + ": ";
            if (n.getName().equals("")) {
                output += "Cannot calculate winners because there is insufficient data\n";
            } else {
                output += n.toString() + "\n";
            }
        }
        return output;
    }

    /**
     * Calculates the percent correct predictions program made.
     *
     * @return String output that prints percent correct
     */
    public String getTotalPercent() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        DecimalFormat countFmt = new DecimalFormat("#00");
        String output = "";
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, String> predictedWinners = new HashMap<String, String>();
        double count = 0;
        double correct = 0;
        double perc = 0;
        for (int j = minYear; j <= maxYear; j++) {
            predictedWinners = returnWinnersByYear(j);
            winners = allWinners.get(j);
            for (int i = 0; i < awardList.length; i++) {
                try {
                    if (predictedWinners.get(awardList[i]).equalsIgnoreCase(
                            winners.get(awardList[i]))) {
                        correct++;
                        count++;
                    } else if (!predictedWinners.get(awardList[i]).equals("")) {
                        count++;
                    }
                } catch (NullPointerException e) {
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
     *
     * @return a String of awards and percent correct predicted for each award.
     */
    public String awardAccuracy() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        String output = "";
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, Nomination> highest = new HashMap<String, Nomination>();
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
                    } else if (Double.parseDouble(highest.get(awardList[i]).getPercent()) > threshold) {
                        count++;
                    }
                } catch (NullPointerException e) {
                }
            }
            perc = correct / count;
            perc = perc * 100;
            output += awardList[i] + ": " + numFmt.format(perc) + "%\n";
        }
        return output;
    }

    /**
     * Prints actor/nomination and all awards it won based off calculations.
     *
     * @param category defines category of award to be calculated
     * @param year is the year to calculate
     * @return string projected  winner and all previous awards
     */
    public String getDetail(int year, String category) {
        String output = "";
        HashMap<String, Nomination> highest = new HashMap<String, Nomination>();
        ArrayList<String> awardOrg = new ArrayList<String>();
        highest = calculatedWinners.get(year);
        Nomination x = highest.get(category);
        awardOrg = x.getAwardOrg();
        if (x.getCoefficient() < threshold) {
            output = "Oscar Genie cannot accurately predict the winner "
                    + "due to lack of data.\nThe best guess is: " + x.getName()
                    + "as it also won awards from:\n";
        }
        else {
            output = x.getName() + " is most "
                    + "likely to win an Oscar\n" + x.getName()
                    + " also won awards this year from:\n\n";
        }

        for (int i = 0; i < awardOrg.size(); i++) {
            output += awardOrg.get(i) + "\n";
        }
        return output;
    }

    /**
     * returns string of each nominee toString() for a given category
     * If there are no winners the user is notified.
     *
     * @param category is award category
     * @param year     is year to calculate
     * @return toString of all nominees
     */
    public String returnResults(int year, String category) {
        String output = category + ":\n";
        HashMap<String, Nomination> highest = new HashMap<String, Nomination>();
        highest = calculatedWinners.get(year);
        if (highest.get(category).getPercent().equals("0.0")) {
            output = "\nCannot calculate winners because there is insufficent "
                    + "data for " + category;
        } else {
            HashMap<String, Nomination> nominees = new HashMap<String, Nomination>();
            HashMap<String, HashMap<String, Nomination>> nominationsByAward = new HashMap<String, HashMap<String, Nomination>>();
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
     * Compares predicted winners to actual winners and outputs
     * correct winners and percent of winners correctly predicted.
     * Calculates the percent accuracy of the program.that year
     *
     * @param year is year to get details for
     * @return String output which prints all data
     */
    public String allDetails(int year) {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        String output = "";
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, String> predictedWinners = new HashMap<String, String>();
        predictedWinners = returnWinnersByYear(year);
        winners = allWinners.get(year);
        double count = 0;
        double correct = 0;
        double perc = 0;
        for (int i = 0; i < awardList.length; i++) {
            try {
                output += "The actual winner for " + awardList[i] + " is "
                        + winners.get(awardList[i]) + ":\n";
                if (predictedWinners.get(awardList[i]).equals("")) {
                    output += "Oscar Genie did not have enough data "
                            + "to predict this\n";
                } else if (predictedWinners.get(awardList[i]).equalsIgnoreCase(
                        winners.get(awardList[i]))) {
                    output += " Oscar Genie correctly predicted this winner\n";
                    correct++;
                    count++;
                } else {
                    output += " Oscar Genie did not predict this winner\n";
                    count++;
                }
            } catch (NullPointerException e) {
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
     * prints help text.
     *
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
     *
     * @return String output of info text
     */
    public String printInfo() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        String output = "This program was written by Ryan "
                + "Crumpler\nIt can predict each category with the given accuracies:\n"
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
                + "%coefficient Oscar Genie considers\nthat award "
                + "to have insufficient data";
        return output;
    }

}