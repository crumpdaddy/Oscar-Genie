import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import com.omertron.themoviedbapi.model.keyword.Keyword;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.model.person.PersonFind;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.*;
import static com.omertron.themoviedbapi.enumeration.SearchType.PHRASE;

/** This program is the driver that carries out all methods 
* to determine chance of winning an oscar.
* 
* @author Ryan Crumpler
* @version 7.1.17
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
    protected static String[] orgList = new String[] {"Golden Globes-Drama",  "Critics Choice", "Writers Guild",
            "Golden Globes",  "Producers Guild",   "Directors Guild",  "Screen Actors Guild",
            "New York Film Critics", "Houston Critics", "Central Ohio Film Critics",
            "Dallas Fort Worth Critics","LA Film Critics", "DC Film Critics",
            "Boston Film Critics", "San Francisco Critics", "Austin Film Critics",  "Denver Film Critics",
            "Las Vegas Critics",  "Golden Globes-Musical or Comedy", "Visual Effects Society"};

    /**
     * Static int that defines the max number of times the program will adjust the filter
     */
    private static int maxCount = 50;
    private static int maxCountKW = 20;
    private static double kwScalar = 1;

    private static String api = "8aaacb1ccfa4a6da21625ef28c28c413";

    /**
     * integers to define years to begin and end calculations on.
     */
    private int minYear, maxYear, calculatingYear;

    private double scalar, calcCount;
    protected String name, award, movie, nominee, organization, percent;

    /**
     * HashMap Structures for all nominees
     * The top level HashMap has a key that is the year.
     * The data is a set of HashMaps where the key is the award title,
     * The data of the 2nd level HashMap is a 3rd level HashMap
     * The key to the 3rd level Nomination HashMap the name of the nominee,
     * with the data being the Nomination object
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
    private HashMap<String, HashMap<String, Double>> winnerKeywords;

    private HashMap<String, Double> kalmanMapKW;


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
        calculatedWinners = new HashMap<>();
        winnerKeywords = new HashMap<String, HashMap<String, Double>>();
        kalmanMapKW = new HashMap<>();
    }

    /**
     * Method to run the initial setup for driver program.
     * Calls on all required methods to produce accurate results.
     * @param minYear first year to start predicting results for
     * @param maxYear last year to predict results for
     * @throws IOException for scanner
     */
    public void setup(int minYear, int maxYear) throws IOException, ClassNotFoundException {
        setMinMaxYear(minYear, maxYear);
        setScalar(.07);
        deserializeMap();
        readCalculations("all_calculations.csv");
        setWinnerKeyWords();
        kalmanFilterAwards();
        kalmanFilterKeywords();
        getProbability();
    }

    public void initialSetup(int minYear, int maxYear) throws IOException, MovieDbException {
        setMinMaxYear(minYear, maxYear);
        setScalar(.07);
        readNominee("all_nominations.csv");
        updateExtras();
        serilizeMap();
        readCalculations("all_calculations.csv");
        kalmanFilterAwards();
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
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(fileNameIn));
        BufferedReader br = new BufferedReader(isr);
        HashMap<String, Nomination> nomMap = new HashMap<String, Nomination>();
        HashMap<String, Nomination> cloneMap = new HashMap<String, Nomination>();
        HashMap<String, String> winners = new HashMap<String, String>();
        HashMap<String, String> winnersClone = new HashMap<String, String>();
        HashMap<String, HashMap<String, Nomination>> nomineeMap = new HashMap<String, HashMap<String, Nomination>>();
        HashMap<String, HashMap<String, Nomination>> cloneMap2 = new HashMap<String, HashMap<String, Nomination>>();
        ArrayList<String> awardOrg = new ArrayList<String>();
        Scanner scanFile = new Scanner(br);
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
                percent = "0.0";
                if (award.equals("Actor - Leading Role") || award.equals("Actor - Supporting Role")) {
                    BestActor n = new BestActor(name, 0, percent, awardOrg, movie);
                    nomMap.put(name, n);
                }
                else if (award.equals("Actress - Leading Role") || award.equals("Actress - Supporting Role")) {
                    BestActress n = new BestActress(name, 0, percent, awardOrg, movie);
                    nomMap.put(name, n);
                }
                else if (award.equals("Song")) {
                    BestSong n = new BestSong(name, 0, percent, awardOrg, movie);
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
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(fileNameIn));
        BufferedReader br = new BufferedReader(isr);
        Scanner scanFile = new Scanner(br);
        HashMap<String, Double> orgCount = new HashMap<String, Double>();
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
                if (Arrays.asList(orgList).contains(organization) && year != calculatingYear) {
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
                        if (winners.get(award).equalsIgnoreCase(name)) {
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
        }
        double kalmanCoeff = 0;
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
     * Searches for and sets keywords for each ffilm
     * @param name name of film
     * @param award award film is nominated for
     * @param year year of Oscars
     * @throws MovieDbException
     */
    public void setKeywords(String name, String award, int year) throws MovieDbException {
        TheMovieDbApi mov = new TheMovieDbApi(api);
        HashMap<String, HashMap<String, Nomination>> nomsByAward = nomineesByYear.get(year);
        HashMap<String, Nomination> noms = nomsByAward.get(award);
        Nomination n = noms.get(name);
        int id = n.getID();
        if (id > 0) {
            List<Keyword> keywords = new ArrayList<Keyword>();
            keywords =  mov.getMovieKeywords(id).getResults();
            for (int i = 0; i < keywords.size(); i++) {
                String kw = keywords.get(i).getName();
                if (kw.length() > 0) {
                    n.addKeyword(kw);
                }
            }
            noms.put(name, n);
            nomsByAward.put(award, noms);
            nomineesByYear.put(year, nomsByAward);
        }
    }

    /**
     * Sets TMDB ID for all nominees
     * @param info MovieInfo object
     * @param name name of film
     * @param award award film was nominated for
     * @param year year of Oscars
     */
    public void setID(MovieInfo info, String name, String award, int year) {
        if (info != null) {
            HashMap<String, HashMap<String, Nomination>> nomsByAward = nomineesByYear.get(year);
            HashMap<String, Nomination> noms = nomsByAward.get(award);
            Nomination n = noms.get(name);
            if (n.getID() < 0) {
                n.setID(info.getId());
                noms.put(name, n);
                nomsByAward.put(award, noms);
                nomineesByYear.put(year, nomsByAward);
            }
        }
    }

    /**
     * Updates keywords and ID for all nominees
     * @throws MovieDbException
     */
    public void updateExtras() throws MovieDbException {
        for (int i = minYear; i <= maxYear; i++) {
            System.out.println("Processing Data For " + i);
            for (int j = 0; j < awardList.length; j++) {
                HashMap<String, HashMap<String, Nomination>> nomsByAward = nomineesByYear.get(i);
                HashMap<String, Nomination> noms = nomsByAward.get(awardList[j]);
                try {
                    for (String key : noms.keySet()) {
                        Nomination n = noms.get(key);
                        String title = "";
                        if (j == 1 || j == 3) {
                            BestActor a = (BestActor) n;
                            title = a.getMovie();
                        }
                        else if (j == 2 || j ==4) {
                            BestActress a = (BestActress) n;
                            title = a.getMovie();
                        }
                        else if (j == 16) {
                            BestSong a = (BestSong) n;
                            title = a.getMovie();
                        }
                        else {
                            title = n.getName();
                        }
                        MovieInfo info = new MovieInfo();
                        if (n.getID() < 0) {
                            info = searchMovie(title, i - 1);
                        }
                        setID(info, n.getName(), awardList[j], i);
                        setKeywords(n.getName(), awardList[j], i);
                    }
                }
                catch (NullPointerException e) {
                }
            }
        }
    }

    //Methods for saving maps to external files and reading external files

    /**
     * Serializes and saves allWinners and nomineesByYear to a file to be read when program closes
     */
    public void serilizeMap() {

        try {
            FileOutputStream fos = new FileOutputStream("nomineesByYear.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(nomineesByYear);
            oos.close();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream("allWinners.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(allWinners);
            oos.close();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Reads serilized files and saves contents to nomineesByYear and allWinners
     * @throws ClassNotFoundException if class inst found
     */
    public void deserializeMap() throws ClassNotFoundException {
        try {
            //InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("nomineesByYear.ser"));
            FileInputStream fis = new FileInputStream("nomineesByYear.ser");
            //InputStream fis = getClass().getResourceAsStream("nomineesByYear.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            nomineesByYear = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //InputStream fis = getClass().getResourceAsStream("allWinners.ser");
            FileInputStream fis = new FileInputStream("allWinners.ser");
            //FileInputStream fis = new FileInputStream("allWinners.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            allWinners = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Set methods for setting initial nominee coefficients.

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
                        try {
                            coef += kalmanMap.get(kalKey);
                        }
                        catch (NullPointerException e) {
                        }
                    }
                    String k = i + " " + awardIn + " " + n.getName();
                    kalmanMapKW.put(k, coef);
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
     * Adds keywords of all winners to a hashmap
     */
    public void setWinnerKeyWords() {
        for (int i = minYear; i <= maxYear; i++) {
            HashMap<String, String> winnersByAward = allWinners.get(i);
            for (int j = 0; j < awardList.length; j++) {
                try {
                    String title = winnersByAward.get(awardList[j]);
                    HashMap<String, Double> awardKeyWords = new HashMap<String, Double>();
                    if (winnerKeywords.containsKey(awardList[j])) {
                        awardKeyWords = winnerKeywords.get(awardList[j]);
                    }
                    Nomination n = nomineesByYear.get(i).get(awardList[j]).get(title);
                    HashMap<String, Double> kws = n.getKeywordMap();
                    for (String word : kws.keySet()) {
                        if (awardKeyWords.containsKey(word)) {
                            awardKeyWords.put(word, awardKeyWords.get(word) + 1);
                        }
                        else {
                            awardKeyWords.put(word, 1.0);
                        }
                    }
                    winnerKeywords.put(awardList[j], awardKeyWords);
                }
                catch (NullPointerException e) {
                }
            }
        }
    }

    /**
     * Gets thee percent of keywords a nomination shares with the winner
     * @param n is nomination
     * @param award is name of award
     * @return double of percent
     */
    public double getKWPercent(Nomination n, String award) {
        HashMap<String, Double> awardKWs = winnerKeywords.get(award);
        HashMap<String, Double> nomKWs = n.getKeywordMap();
        double count = 0;
        double matches = 0;
        for (String key : nomKWs.keySet()) {
            if (awardKWs.containsKey(key)) {
                matches += awardKWs.get(key);
            }
            count++;
        }
        return matches / count;
    }

    /**
     * adds key word percent to a nomination's coefficient
     * @param award name of award
     */
    public void addKWcoef(String award) {
        for (int i = minYear; i <= maxYear; i++) {
            HashMap<String, HashMap<String, Nomination>> nomineesByAward = nomineesByYear.get(i);
            HashMap<String, Nomination> noms = nomineesByAward.get(award);
            HashMap<String, Double> awardKWs = winnerKeywords.get(award);
            try {
                for (Nomination n : noms.values()) {
                    String k = i + " " + award + " " + n.getName();
                    n.setCoefficient(getKWPercent(n, award) + kalmanMapKW.get(k));
                }
            }
            catch (NullPointerException e) {
            }
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
    public void kalmanFilterAwards() {
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
                adjusted = adjustKalmanMapAwards(kalKey, true);
                setCalculationsCoef(awardList[j]);
                winners = generateWinByAward(awardList[j]);
                calcCount = findAccuracy(awardList[j], winners);
                count = 0;
                boolean go = true;
                while (go) {
                    if (calcCount == prevCalcCount) {
                        while (calcCount == prevCalcCount && count <= maxCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, true);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count++;
                        }
                        if (calcCount < prevCalcCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, false);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count = 0;
                            while (calcCount >= prevCalcCount && adjusted) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapAwards(kalKey, false);
                                setCalculationsCoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            if (adjusted) {
                                adjusted = adjustKalmanMapAwards(kalKey, true);
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
                                adjusted = adjustKalmanMapAwards(kalKey, true);
                                setCalculationsCoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            adjusted = adjustKalmanMapAwards(kalKey, false);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            prevCalcCount = calcCount;
                        }
                    }
                    else if (calcCount > prevCalcCount) {
                        count = 0;
                        while (calcCount >= prevCalcCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, true);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count++;
                        }
                        adjusted = adjustKalmanMapAwards(kalKey, false);
                        setCalculationsCoef(awardList[j]);
                        winners = generateWinByAward(awardList[j]);
                        calcCount = findAccuracy(awardList[j], winners);
                        prevCalcCount = calcCount;
                    }
                    else if (calcCount < prevCalcCount) {
                        prevCalcCount = calcCount;
                        adjusted = adjustKalmanMapAwards(kalKey, false);
                        setCalculationsCoef(awardList[j]);
                        winners = generateWinByAward(awardList[j]);
                        calcCount = findAccuracy(awardList[j], winners);
                        count = 0;
                        while (calcCount >= prevCalcCount && adjusted) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, false);
                            setCalculationsCoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count++;
                        }
                        if (adjusted) {
                            adjusted = adjustKalmanMapAwards(kalKey, true);
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
     * Functions same as filter for awards, just for keywords
     */
    public void kalmanFilterKeywords() {
        HashMap<Integer, String> winners = new HashMap<Integer, String>();
        double prevCalcCount = 0;
        boolean adjusted = true;
        int count = 0;
        for (int j = 0; j < awardList.length; j++) {
            award = awardList[j];
            addKWcoef(awardList[j]);
            winners = generateWinByAward(awardList[j]);
            calcCount = findAccuracy(awardList[j], winners);
            prevCalcCount = calcCount;
            HashMap<String, Double> kwByAward = winnerKeywords.get(awardList[j]);
            try {
                for (String key : kwByAward.keySet()) {
                    if (kwByAward.get(key) == 1) {
                        //continue;
                    }
                    adjusted = adjustKalmanMapKeywords(kwByAward, key, true);
                    addKWcoef(awardList[j]);
                    winners = generateWinByAward(awardList[j]);
                    calcCount = findAccuracy(awardList[j], winners);
                    count = 0;
                    boolean go = true;
                    //System.out.println(key + " " + awardList[j]);
                    while (true) {
                        if (calcCount == prevCalcCount) {
                            while (calcCount == prevCalcCount && count <= maxCountKW && adjusted) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapKeywords(kwByAward, key, true);
                                addKWcoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            if (calcCount < prevCalcCount) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapKeywords(kwByAward, key, false);
                                addKWcoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count = 0;
                                while (calcCount >= prevCalcCount && count < maxCountKW && adjusted) {
                                    prevCalcCount = calcCount;
                                    adjusted = adjustKalmanMapKeywords(kwByAward, key, false);
                                    addKWcoef(awardList[j]);
                                    winners = generateWinByAward(awardList[j]);
                                    calcCount = findAccuracy(awardList[j], winners);
                                    count++;
                                }
                                if (adjusted) {
                                    adjusted = adjustKalmanMapKeywords(kwByAward, key, true);
                                    addKWcoef(awardList[j]);
                                    winners = generateWinByAward(awardList[j]);
                                    calcCount = findAccuracy(awardList[j], winners);
                                }
                                prevCalcCount = calcCount;
                            }
                            else if (calcCount > prevCalcCount) {
                                count = 0;
                                while (calcCount >= prevCalcCount && count < maxCountKW && adjusted) {
                                    prevCalcCount = calcCount;
                                    adjusted = adjustKalmanMapKeywords(kwByAward, key, true);
                                    addKWcoef(awardList[j]);
                                    winners = generateWinByAward(awardList[j]);
                                    calcCount = findAccuracy(awardList[j], winners);
                                    count++;
                                }
                                adjusted = adjustKalmanMapKeywords(kwByAward, key, false);
                                addKWcoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                prevCalcCount = calcCount;
                            }
                        }
                        else if (calcCount > prevCalcCount) {
                            count = 0;
                            while (calcCount >= prevCalcCount && count < maxCountKW && adjusted) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapKeywords(kwByAward, key, true);
                                addKWcoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            adjusted = adjustKalmanMapKeywords(kwByAward, key, false);
                            addKWcoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            prevCalcCount = calcCount;
                        }
                        else if (calcCount < prevCalcCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapKeywords(kwByAward, key, false);
                            addKWcoef(awardList[j]);
                            winners = generateWinByAward(awardList[j]);
                            calcCount = findAccuracy(awardList[j], winners);
                            count = 0;
                            while (calcCount >= prevCalcCount && adjusted && count < maxCountKW) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapKeywords(kwByAward, key, false);
                                addKWcoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                                count++;
                            }
                            if (adjusted) {
                                adjusted = adjustKalmanMapKeywords(kwByAward, key, true);
                                addKWcoef(awardList[j]);
                                winners = generateWinByAward(awardList[j]);
                                calcCount = findAccuracy(awardList[j], winners);
                            }
                            prevCalcCount = calcCount;
                        }
                        break;
                    }
                }
            }
            catch (NullPointerException e) {
            }
        }
    }

    /**
     * Adjusts the kalman filter for the keywords, works same as for awards
     * @param kwByAward hashmap of keywords by an award
     * @param key string of key
     * @param increase increase or decrease weight
     * @return return true if changed weight
     */
    public boolean adjustKalmanMapKeywords(HashMap<String, Double> kwByAward, String key, boolean increase) {
        double kalmanCoeff = 0;
        double newCoeff = 0;
        try {
            kalmanCoeff = kwByAward.get(key);
            if (!increase) {
                newCoeff = kalmanCoeff - kwScalar;
                if (kalmanCoeff <= 0.0) {
                    kwByAward.put(key, 0.0);
                    return false;
                }
                else {
                    kwByAward.put(key, newCoeff);
                    return true;
                }
            }
            if (increase) {
                newCoeff = kalmanCoeff + kwScalar;
                if (kalmanCoeff == 0) {
                    kwByAward.put(key, kalmanCoeff);
                    return false;
                }
                else {
                    kwByAward.put(key, newCoeff);
                    return true;
                }
            }

        }
        catch (NullPointerException e) {
        }
        winnerKeywords.put(award, kwByAward);
        return false;
    }

    /**
     * Adjusts the values in the kalmanMap by a given scalar.
     *
     * @param kalKey   is the kalmanKey that stores the coefficient in the kalmanMap HashMap
     * @param increase is used to set how the coefficient is adjuste true = increase false = decrease
     * @return a boolean that is true if coefficient was adjusted and false if coefficients 0
     */
    public boolean adjustKalmanMapAwards(String kalKey, boolean increase) {
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

    //TMDB Methods

    /**
     * Searches for a movie in TMDB
     * @param title title of movie
     * @param year year movie came out
     * @return returns MovieInfo object
     * @throws MovieDbException
     */
    public MovieInfo searchMovie(String title, int year) throws MovieDbException {
        TheMovieDbApi mov = new TheMovieDbApi(api);
        ResultList<MovieInfo> results = mov.searchMovie(title,0, "", true, year, year, PHRASE);
        List<MovieInfo> movieResults = new ArrayList<MovieInfo>();
        movieResults = results.getResults();
        if (movieResults.size() > 0) {
            return movieResults.get(0);
        }
        return null;
    }

    /**
     * Searches TMDB for a person
     * @param personName name of person to search
     * @return a personFind object
     * @throws MovieDbException
     */
    public PersonFind searchPerson(String personName) throws MovieDbException {
        TheMovieDbApi mov = new TheMovieDbApi(api);
        ResultList<PersonFind> results = mov.searchPeople(personName,0, true, PHRASE);
        List<PersonFind> personResults = new ArrayList<PersonFind>();
        personResults = results.getResults();
        if (personResults.size() > 0) {
            return personResults.get(0);
        }
        return null;
    }

    // Methods to calculate  and display the final results.

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
                        if (n.getCoefficient() > 0) {
                            totalcoef += n.getCoefficient();
                        }
                        else {
                            n.setCoefficient(0.0);
                        }

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
        HashMap<String, Nomination> predictedWinners = new HashMap<String, Nomination>();
        double count = 0;
        double correct = 0;
        double perc = 0;
        for (int j = minYear; j <= maxYear; j++) {
            predictedWinners = calculatedWinners.get(j);
            winners = allWinners.get(j);
            for (int i = 0; i < awardList.length; i++) {
                try {
                    if (predictedWinners.get(awardList[i]).getName().equalsIgnoreCase(
                            winners.get(awardList[i]))) {
                        correct++;
                    }
                   // else if (predictedWinners.get(awardList[i]).getAwardOrg().size() > 1 || predictedWinners.get(awardList[i]).getCoefficient() < 10) {
                        count++;
                    //}

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
                            && highest.get(awardList[i]).getAwardOrg().size() > 1) {
                        correct++;
                       // count++;
                    }
                    count++;
                    //else if (highest.get(awardList[i]).getAwardOrg().size() > 1 || highest.get(awardList[i]).getCoefficient() < 10) {
                  //      count++;
                    //}
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
        if (x.getAwardOrg().size() <= 1) {
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
        HashMap<String, Nomination> predictedWinners = new HashMap<String, Nomination>();
        predictedWinners = calculatedWinners.get(year);
        winners = allWinners.get(year);
        double count = 0;
        double correct = 0;
        double perc = 0;
        for (int i = 0; i < awardList.length; i++) {
            try {
                output += "The actual winner for " + awardList[i] + " is "
                        + winners.get(awardList[i]) + ": ";
               // if (predictedWinners.get(awardList[i]).getAwardOrg().size() <= 1) {
                   // output += "Oscar Genie did not have enough data "
                  //          + "to predict this\n";
                //}
                if (predictedWinners.get(awardList[i]).getName().equalsIgnoreCase(
                        winners.get(awardList[i]))) {
                    output += "Oscar Genie correctly predicted this winner\n";
                    correct++;
                    count++;
                }
                else {
                    output += "Oscar Genie did not predict this winner\n";
                    count++;
                }
            }
            catch (NullPointerException e) {
                output += "\n";
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
                + "predict or ou can enter the name of the award\n"
                + "Case does not matter\n"
                + "Please email any bugs or errors "
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
               // + awardAccuracy() + "\n"
                + getTotalPercent()
                + "It works by scanning sources such "
                + "as movie critic associations\n and determining "
                + "how effective each source is at predicting Oscar "
                + "winners\nfor each category It then weights each "
                + "source and makes a prediction\nbased on all the "
                + "sources and outputs the results";
        return output;
    }

    /**
     * Prints all the top key words for an awrd
     * @param award name of award to get key words for
     */
    public void printTopKWs(String award) {
        HashMap<String, Double> kws = winnerKeywords.get(award);
        LinkedList<String> top = new LinkedList<String>();
        String maxWord = "dance";
        top.add(maxWord);
        //double max = kws.get(maxWord);
        boolean start = true;
        for (String key : kws.keySet()) {
            //if (start)
            Iterator<String> itr = top.iterator();
            int count = 0;
            while (itr.hasNext()) {
                if (kws.get(key) > kws.get(itr.next())) {
                    top.add(count, key);
                    break;
                }
                count++;
            }

        }
        System.out.println(award);
        Iterator<String> itr = top.iterator();
        while (itr.hasNext()) {
            String word = itr.next();
            System.out.println(word + " " + kws.get(word));
        }
        //System.out.println(maxWord);

    }

}