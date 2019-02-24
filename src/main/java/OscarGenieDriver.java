import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import com.omertron.themoviedbapi.model.keyword.Keyword;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.*;
import static com.omertron.themoviedbapi.enumeration.SearchType.PHRASE;

/**
 * This program is the driver that carries out all methods
 * to determine chance of winning an oscar.
 *
 * @author Ryan Crumpler
 * @version 12.2.18
 */
public class OscarGenieDriver {
    /**
     * List of all awards Oscar Genie will attempt to predict
     */
    private static String[] awardList = new String[]{"Best Picture",
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
    private static String[] orgList = new String[] {"Golden Globes-Drama",  "Critics Choice", "Writers Guild",
            "Golden Globes",  "Producers Guild",   "Directors Guild",  "Screen Actors Guild",
            "New York Film Critics", "Houston Critics", "Central Ohio Film Critics",
            "Dallas Fort Worth Critics","LA Film Critics", "DC Film Critics",
            "Boston Film Critics", "San Francisco Critics", "Austin Film Critics",  "Denver Film Critics",
            "Las Vegas Critics",  "Golden Globes-Musical or Comedy", "Visual Effects Society"};


    private static String api = "8aaacb1ccfa4a6da21625ef28c28c413";

    /**
     * integers to define years to begin and end calculations on.
     */
    private int minYear, maxYear, calculatingYear;

    private boolean calculatingFuture;
    private String name, award, nominee;

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


    /**
     * This program reads a CSV file that contains every Oscar nominee and winner
     * it then reads a CSV that contains a set of calculations based off of which
     * nominees won awards from other organizations and adds the calculations and
     * nominees to hashmaps. It then kalman filters the calculations to find the
     * optimal formula to predict Oscar winners
     */
    OscarGenieDriver() {
        nomineesByYear = new HashMap<>();
        allWinners = new HashMap<>();
        kalmanMap = new HashMap<>();
        calculatedWinners = new HashMap<>();
        winnerKeywords = new HashMap<>();
    }

    /**
     * Method to run the initial setup for driver program.
     * Calls on all required methods to produce accurate results.
     * @param minYear first year to start predicting results for
     * @param maxYear last year to predict results for
     * @param calculatingYear year where winners aren't known
     * @param  internal if scanning internal ser files
     * @throws  MovieDbException for TMDB
     */
    public void setup(int minYear, int maxYear, int calculatingYear, boolean internal) throws MovieDbException {
        //setMinMaxYear(minYear, maxYear, calculatingYear);
        setMinMaxYear(minYear, 2018, 2018);
        try {
            deserializeMap(internal);
        }
        catch (ClassNotFoundException e ) {
            readNominee();
            updateExtras();
            serilizeMap();
        }
        readCalculations();
        setWinnerKeyWords();
        addAllKWcoef();
        //Iterator it = kalmanMap.entrySet().iterator();
       // while (it.hasNext()) {
         //   Map.Entry pair = (Map.Entry)it.next();
           // System.out.println(pair.getKey() + " = " + pair.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        //}
        //getWinnersNoFilter();
        kalmanFilterAwards();
        getProbability();
        int x = 0;
    }


    /**
     * Runs setup where TMDB is queried
     * @param minYear min year to calculate
     * @param maxYear max year to calculate
     * @param calculatingYear year where winners aren't known
     * @throws MovieDbException for TMDB
     */
    public void initialSetup(int minYear, int maxYear, int calculatingYear) throws MovieDbException {
        setMinMaxYear(minYear, maxYear, calculatingYear);
        readNominee();
        updateExtras();
        serilizeMap();
        readCalculations();
        setWinnerKeyWords();
        addAllKWcoef();
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
    private void setMinMaxYear(int minYearIn, int maxYearIn, int calculatingYearIn) {
        minYear = minYearIn;
        maxYear = maxYearIn;
        if (calculatingYearIn == 0 || maxYearIn == calculatingYearIn) {
            calculatingYear = maxYear;
            calculatingFuture = false;
        }
        else {
            calculatingYear = calculatingYearIn;
            calculatingFuture= true;
        }

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
     */
    private void readNominee() {
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("all_nominations.csv"));
        BufferedReader br = new BufferedReader(isr);
        HashMap<String, Nomination> nomMap = new HashMap<>();
        HashMap<String, Nomination> cloneMap;
        HashMap<String, String> winners = new HashMap<>();
        HashMap<String, String> winnersClone;
        HashMap<String, HashMap<String, Nomination>> nomineeMap = new HashMap<>();
        HashMap<String, HashMap<String, Nomination>> cloneMap2;
        Scanner scanFile = new Scanner(br);
        award = scanFile.nextLine();
        String won;
        int year;
        int pastYear = minYear;
        String prevAward = "";
        while (scanFile.hasNextLine()) {
            nominee = scanFile.nextLine();
            Scanner scanNominee = new Scanner(nominee).useDelimiter(",");
            while (scanNominee.hasNext()) {
                year = Integer.parseInt(scanNominee.next());
                if (year != pastYear) {
                    winnersClone = new HashMap<>(winners);
                    allWinners.put(pastYear, winnersClone);
                    cloneMap2 = new HashMap<>(nomineeMap);
                    nomineesByYear.put(pastYear, cloneMap2);
                    pastYear = year;
                    nomMap.clear();
                    nomineeMap.clear();
                }
                award = scanNominee.next();
                if (!award.equalsIgnoreCase(prevAward)) {
                    cloneMap = new HashMap<>(nomMap);
                    nomineeMap.put(prevAward, cloneMap);
                    nomMap.clear();
                    prevAward = award;
                }
                name = scanNominee.next();
                String movie = scanNominee.next();
                switch (award) {
                    case "Actor - Leading Role":
                    case "Actor - Supporting Role": {
                        BestActor n = new BestActor(name, movie);
                        nomMap.put(name, n);
                        break;
                    }
                    case "Actress - Leading Role":
                    case "Actress - Supporting Role": {
                        BestActress n = new BestActress(name, movie);
                        nomMap.put(name, n);
                        break;
                    }
                    case "Song": {
                        BestSong n = new BestSong(name, movie);
                        nomMap.put(name, n);
                        break;
                    }
                    default: {
                        Nomination n = new Nomination(name);
                        nomMap.put(name, n);
                        break;
                    }
                }
                won = scanNominee.next();
                if (won.equalsIgnoreCase("Yes")) {
                    if (calculatingFuture && year != calculatingYear) {
                        winners.put(award, name);
                    }
                    else if (!calculatingFuture) {
                        winners.put(award, name);
                    }
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
     */
    private void readCalculations() {
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("all_calculations.csv"));
        BufferedReader br = new BufferedReader(isr);
        Scanner scanFile = new Scanner(br);
        HashMap<String, Double> orgCount = new HashMap<>();
        HashMap<String, Nomination> nominees = new HashMap<>();
        HashMap<String, HashMap<String, Nomination>> nominationsByAward;
        HashMap<String, String> winners;
        HashMap<String, Nomination> cloneMap1;
        HashMap<String, HashMap<String, Nomination>> cloneMap2;
        ArrayList<String> tempArr;
        ArrayList<String> awardOrg;
        int year;
        int prevYear = minYear;
        award = scanFile.nextLine();
        String kalKey;
        double kal;
        double kalCount;
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
                        assert nominees != null;
                        cloneMap1 = new HashMap<>(nominees);
                        nominationsByAward.put(prevAward, cloneMap1);
                        nominees.clear();
                    }
                    catch (NullPointerException ignored) {
                    }
                    prevAward = award;
                }
                if (award.equalsIgnoreCase("Z Filler Data")) {
                    try {
                        cloneMap2 = new HashMap<>(nominationsByAward);
                        nomineesByYear.put(prevYear, cloneMap2);
                        nominationsByAward.clear();
                    }
                    catch (NullPointerException ignored) {
                    }
                }
                try {
                    nominees = nominationsByAward.get(award);
                }
                catch (NullPointerException ignored) {
                }
                name = scanProbability.next();
                String organization = scanProbability.next();
                if (Arrays.asList(orgList).contains(organization)) {
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
                        Nomination n = nominees.get(name);
                        awardOrg = n.getAwardOrg();
                        awardOrg.add(organization);
                        tempArr = new ArrayList<>(awardOrg);
                        n.setAwardOrg(tempArr);
                        nominees.put(name, n);
                        awardOrg.clear();
                        winners = allWinners.get(year);
                        if (winners.get(award).equalsIgnoreCase(name) && year != maxYear + 1) {
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
        double kalmanCoeff;
        for (String anAwardList : awardList) {
            for (String anOrgList : orgList) {
                kalKey = anOrgList + " " + anAwardList;
                try {
                    kalCount = orgCount.get(kalKey);
                    kalmanCoeff = kalmanMap.get(kalKey);
                    kalmanCoeff = kalmanCoeff / kalCount;
                    kalmanMap.put(kalKey, kalmanCoeff);
                } catch (NullPointerException ignored) {
                }
            }
        }
    }

    /**
     * Searches for and sets keywords for each ffilm
     * @param name name of film
     * @param award award film is nominated for
     * @param year year of Oscars
     * @throws MovieDbException for TMDB
     */
    private void setKeywords(String name, String award, int year) throws MovieDbException {
        TheMovieDbApi mov = new TheMovieDbApi(api);
        HashMap<String, HashMap<String, Nomination>> nomsByAward = nomineesByYear.get(year);
        HashMap<String, Nomination> noms = nomsByAward.get(award);
        Nomination n = noms.get(name);
        int id = n.getID();
        if (id > 0) {
            List<Keyword> keywords;
            keywords =  mov.getMovieKeywords(id).getResults();
            for (Keyword keyword : keywords) {
                String kw = keyword.getName();
                if (kw.length() > 0) {
                    n.addKeyword(kw);
                    String[] arr = kw.split(" ");
                    for (String s : arr) {
                        n.addKeyword(s);
                    }
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
    private void setID(MovieInfo info, String name, String award, int year) {
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
     * @throws MovieDbException for TMDB
     */
    private void updateExtras() throws MovieDbException {
        for (int i = minYear; i <= calculatingYear; i++) {
            System.out.println("Processing Data For " + i);
            for (int j = 0; j < awardList.length; j++) {
                HashMap<String, HashMap<String, Nomination>> nomsByAward = nomineesByYear.get(i);
                HashMap<String, Nomination> noms = nomsByAward.get(awardList[j]);
                try {
                    for (String key : noms.keySet()) {
                        Nomination n = noms.get(key);
                        String title;
                        switch (j) {
                            case 1:
                            case 3: {
                                BestActor a = (BestActor) n;
                                title = a.getMovie();
                                break;
                            }
                            case 2:
                            case 4: {
                                BestActress a = (BestActress) n;
                                title = a.getMovie();
                                break;
                            }
                            case 16: {
                                BestSong a = (BestSong) n;
                                title = a.getMovie();
                                break;
                            }
                            default:
                                title = n.getName();
                                break;
                        }
                        MovieInfo info = new MovieInfo();
                        if (n.getID() < 0) {
                            info = searchMovie(title, i - 1);
                        }
                        setID(info, n.getName(), awardList[j], i);
                        setKeywords(n.getName(), awardList[j], i);
                    }
                }
                catch (NullPointerException ignored) {
                }
            }
        }
    }

    //Methods for saving maps to external files and reading external files

    /**
     * Serializes and saves allWinners and nomineesByYear to a file to be read when program closes
     */
    private void serilizeMap() {
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
     * Reads serialized files and saves contents to nomineesByYear and allWinners
     * @param  internal is scanning internal ser files
     * @throws ClassNotFoundException if class inst found
     */
    @SuppressWarnings("unchecked")
    private void deserializeMap(boolean internal) throws ClassNotFoundException {
        try {
            if (internal) {
                InputStream fis = getClass().getResourceAsStream("nomineesByYear.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                nomineesByYear = (HashMap) ois.readObject();
                ois.close();
                fis.close();
            }
            else {
                FileInputStream fis = new FileInputStream("nomineesByYear.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                nomineesByYear = (HashMap) ois.readObject();
                ois.close();
                fis.close();
            }


        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (internal) {
                InputStream fis = getClass().getResourceAsStream("allWinners.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                allWinners = (HashMap) ois.readObject();
                ois.close();
                fis.close();
            }
            else {
                FileInputStream fis = new FileInputStream("allWinners.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                allWinners = (HashMap) ois.readObject();
                ois.close();
                fis.close();
            }
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
    private void setCalculationsCoef(String awardIn) {
        String kalKey;
        HashMap<String, Nomination> nominations;
        HashMap<String, HashMap<String, Nomination>> nominationsByAward;
        ArrayList<String> awardOrg;
        for (int i = minYear; i <= calculatingYear; i++) {
            nominationsByAward = nomineesByYear.get(i);
            nominations = nominationsByAward.get(awardIn);
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = nominations.get(key);
                    awardOrg = n.getAwardOrg();
                    double coef = 0;
                    for (String anAwardOrg : awardOrg) {
                        kalKey = anAwardOrg + " " + awardIn;
                        try {
                            coef += kalmanMap.get(kalKey);
                        }
                        catch (NullPointerException ignored) {
                        }
                    }
                    n.setCoefficientAward(coef);
                    n.setCoefficient();
                    nominations.put(n.getName(), n);
                }
            }
            catch (NullPointerException ignored) {
            }
            nominationsByAward.put(awardIn, nominations);
            nomineesByYear.put(i, nominationsByAward);
        }
    }

    /**
     * Adds keywords of all winners to a hashmap
     */
    private void setWinnerKeyWords() {
        for (int i = minYear; i <= maxYear; i++) {
            HashMap<String, String> winnersByAward = allWinners.get(i);
            for (String anAwardList : awardList) {
                try {
                    String title = winnersByAward.get(anAwardList);
                    HashMap<String, Double> awardKeyWords = new HashMap<>();
                    if (winnerKeywords.containsKey(anAwardList)) {
                        awardKeyWords = winnerKeywords.get(anAwardList);
                    }
                    Nomination n = nomineesByYear.get(i).get(anAwardList).get(title);
                    HashMap<String, Double> kws = n.getKeywordMap();
                    for (String word : kws.keySet()) {
                        if (awardKeyWords.containsKey(word)) {
                            awardKeyWords.put(word, awardKeyWords.get(word) + 1);
                        } else {
                            awardKeyWords.put(word, 1.0);
                        }
                    }
                    winnerKeywords.put(anAwardList, awardKeyWords);
                } catch (NullPointerException ignored) {
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
    private double getKWPercent(Nomination n, String award) {
        HashMap<String, Double> awardKWs = winnerKeywords.get(award);
        HashMap<String, Double> nomKWs = n.getKeywordMap();
        if (nomKWs.size() == 0) {
            return 0.0;
        }
        double count = 0;
        double matches = 0;
        for (String key : nomKWs.keySet()) {
            if (awardKWs.containsKey(key)) {
                n.setFrequency(key, awardKWs.get(key));
                matches += awardKWs.get(key);
            }
            count++;
        }
        if (matches / count > 1) {
            return 1;
        }
        return matches / count;
    }


    /**
     * adds key word percent to a nomination's coefficient
     */
    private void addAllKWcoef() {
        for (int i = minYear; i <= calculatingYear; i++) {
            HashMap<String, HashMap<String, Nomination>> nomineesByAward = nomineesByYear.get(i);
            for (String anAwardList : awardList) {
                HashMap<String, Nomination> noms = nomineesByAward.get(anAwardList);
                try {
                    for (Nomination n : noms.values()) {
                        n.setCoefficientKW(Math.pow(getKWPercent(n, anAwardList),2));
                        n.setCoefficient();
                    }
                } catch (NullPointerException ignored) {
                }
            }
        }
    }


    // KalMan Filtering Methods

    private void getWinnersNoFilter() {
        for (String anAwardList : awardList) {
            award = anAwardList;
            setCalculationsCoef(anAwardList);
        }
    }

    /**
     * Recursive method to adjust the initial coefficients to better predict the winners.
     * It gets a value from the kalman map and increases it by the scalar,
     * then finds the accuracy of the new coefficient, if the accuracy increases it increases
     * the coefficients again until the coefficient is optimal.
     * If the accuracy decreases it decreases the coefficient until the coefficient is optimal
     * The while loops use maxCount to prevent infinite loops
     */
    @SuppressWarnings({"Duplicates", "UnusedAssignment"})
    private void kalmanFilterAwards() {
        int maxCount = 50;
        HashMap<Integer, String> winners;
        double prevCalcCount;
        boolean adjusted;
        String kalKey;
        double calcCount;
        int count;
        for (String anAwardList : awardList) {
            award = anAwardList;
            setCalculationsCoef(anAwardList);
            winners = generateWinByAward(anAwardList);
            calcCount = findAccuracy(anAwardList, winners);
            prevCalcCount = calcCount;
            for (String anOrgList : orgList) {
                kalKey = anOrgList + " " + anAwardList;
                adjusted = adjustKalmanMapAwards(kalKey, true);
                setCalculationsCoef(anAwardList);
                winners = generateWinByAward(anAwardList);
                calcCount = findAccuracy(anAwardList, winners);
                count = 0;
                boolean go = true;
                while (go) {
                    if (calcCount == prevCalcCount) {
                        while (calcCount == prevCalcCount && count <= maxCount / 10) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, true);
                            setCalculationsCoef(anAwardList);
                            winners = generateWinByAward(anAwardList);
                            calcCount = findAccuracy(anAwardList, winners);
                            count++;
                        }
                        if (calcCount < prevCalcCount) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, false);
                            setCalculationsCoef(anAwardList);
                            winners = generateWinByAward(anAwardList);
                            calcCount = findAccuracy(anAwardList, winners);
                            count = 0;
                            while (calcCount >= prevCalcCount && adjusted) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapAwards(kalKey, false);
                                setCalculationsCoef(anAwardList);
                                winners = generateWinByAward(anAwardList);
                                calcCount = findAccuracy(anAwardList, winners);
                                count++;
                            }
                            if (adjusted) {
                                adjusted = adjustKalmanMapAwards(kalKey, true);
                                setCalculationsCoef(anAwardList);
                                winners = generateWinByAward(anAwardList);
                                calcCount = findAccuracy(anAwardList, winners);
                            }
                            prevCalcCount = calcCount;
                        }
                        else if (calcCount > prevCalcCount) {
                            count = 0;
                            while (calcCount >= prevCalcCount && count <= maxCount && adjusted) {
                                prevCalcCount = calcCount;
                                adjusted = adjustKalmanMapAwards(kalKey, true);
                                setCalculationsCoef(anAwardList);
                                winners = generateWinByAward(anAwardList);
                                calcCount = findAccuracy(anAwardList, winners);
                                count++;
                            }
                            adjusted = adjustKalmanMapAwards(kalKey, false);
                            setCalculationsCoef(anAwardList);
                            winners = generateWinByAward(anAwardList);
                            calcCount = findAccuracy(anAwardList, winners);
                            prevCalcCount = calcCount;
                        }
                    }
                    else if (calcCount > prevCalcCount) {
                        count = 0;
                        while (calcCount >= prevCalcCount && count <= maxCount && adjusted) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, true);
                            setCalculationsCoef(anAwardList);
                            winners = generateWinByAward(anAwardList);
                            calcCount = findAccuracy(anAwardList, winners);
                            count++;
                        }
                        adjusted = adjustKalmanMapAwards(kalKey, false);
                        setCalculationsCoef(anAwardList);
                        winners = generateWinByAward(anAwardList);
                        calcCount = findAccuracy(anAwardList, winners);
                        prevCalcCount = calcCount;
                    }
                    else if (calcCount < prevCalcCount) {
                        prevCalcCount = calcCount;
                        adjusted = adjustKalmanMapAwards(kalKey, false);
                        setCalculationsCoef(anAwardList);
                        winners = generateWinByAward(anAwardList);
                        calcCount = findAccuracy(anAwardList, winners);
                        count = 0;
                        while (calcCount >= prevCalcCount && adjusted) {
                            prevCalcCount = calcCount;
                            adjusted = adjustKalmanMapAwards(kalKey, false);
                            setCalculationsCoef(anAwardList);
                            winners = generateWinByAward(anAwardList);
                            calcCount = findAccuracy(anAwardList, winners);
                            count++;
                        }
                        if (adjusted) {
                            adjusted = adjustKalmanMapAwards(kalKey, true);
                            setCalculationsCoef(anAwardList);
                            winners = generateWinByAward(anAwardList);
                            calcCount = findAccuracy(anAwardList, winners);
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
     * @param increase is used to set how the coefficient is adjusted true = increase false = decrease
     * @return a boolean that is true if coefficient was adjusted and false if coefficients 0
     */
    private boolean adjustKalmanMapAwards(String kalKey, boolean increase) {
        double kalmanCoeff;
        double newCoeff;
        double scalar = .07;
        try {
            kalmanCoeff = kalmanMap.get(kalKey);
            if (!increase) {
                newCoeff = kalmanCoeff - scalar;
            }
            else {
                newCoeff = kalmanCoeff + scalar;
            }
            if (kalmanCoeff <= 0) {
                kalmanMap.put(kalKey, 0.0);
                return false;
            }
            else {
                kalmanMap.put(kalKey, newCoeff);
                return true;
            }
        }
        catch (NullPointerException ignored) {
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
    private HashMap<Integer, String> generateWinByAward(String awardIn) {
        HashMap<Integer, String> output = new HashMap<>();
        HashMap<String, Nomination> nominations;
        HashMap<String, HashMap<String, Nomination>> nominationsByAward;
        for (int j = minYear; j <= calculatingYear; j++) {
            nominationsByAward = nomineesByYear.get(j);
            nominations = nominationsByAward.get(awardIn);
            Nomination highestCoeff = new Nomination("");
            try {
                for (String key : nominations.keySet()) {
                    Nomination n = nominations.get(key);
                    if (highestCoeff.getName().equals("")) {
                        highestCoeff = n;
                    }
                    if (n.getCoefficient() > highestCoeff.getCoefficient()) {
                        highestCoeff = n;
                    }
                }
                output.put(j, highestCoeff.getName());
            }
            catch (NullPointerException ignored) {
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
    private double findAccuracy(String awardIn, HashMap<Integer, String> calcInput) {
        HashMap<String, String> winners;
        String calcName;
        double calcCount = 0;
        for (int i = minYear; i <= maxYear; i++) {
            winners = allWinners.get(i);
            calcName = calcInput.get(i);
            try {
                if (calcName.equalsIgnoreCase(winners.get(awardIn))) {
                    calcCount++;
                }
            }
            catch (NullPointerException ignored) {
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
     * @throws MovieDbException for TMDB
     */
    private MovieInfo searchMovie(String title, int year) throws MovieDbException {
        TheMovieDbApi mov = new TheMovieDbApi(api);
        ResultList<MovieInfo> results = mov.searchMovie(title,0, "", true, year, year, PHRASE);
        List<MovieInfo> movieResults;
        movieResults = results.getResults();
        if (movieResults.size() > 0) {
            return movieResults.get(0);
        }
        return null;
    }

    // Methods to calculate  and display the final results.

    /**
     * Uses the coefficients to calculate the percents as well as well as the the most likely nominee to win.
     */
    private void getProbability() {
        HashMap<String, Nomination> nominations;
        HashMap<String, HashMap<String, Nomination>> nominationsByAward;
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        for (int i = minYear; i <= calculatingYear; i++) {
            nominationsByAward = nomineesByYear.get(i);
            HashMap<String, Nomination> highest = new HashMap<>();
            for (String anAwardList : awardList) {
                nominations = nominationsByAward.get(anAwardList);
                Nomination highestNominee = new Nomination("");
                double totalcoef = 0;
                try {
                    for (String key : nominations.keySet()) {
                        Nomination n = nominations.get(key);
                        totalcoef += n.getCoefficient();
                    }
                }
                catch (NullPointerException ignored) {
                }
                try {
                    double perc;
                    for (String key : nominations.keySet()) {
                        Nomination n = nominations.get(key);
                        perc = n.getCoefficient() / totalcoef;
                        perc = perc * 100;
                        n.setPercent(numFmt.format(perc));
                        if (n.getCoefficient() > highestNominee.getCoefficient()) {
                            highestNominee = n;
                        }
                        nominations.put(n.getName(), n);
                    }
                } catch (NullPointerException ignored) {
                }
                highest.put(anAwardList, highestNominee);
                nominationsByAward.put(anAwardList, nominations);
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
        HashMap<String, Nomination> highest;
        StringBuilder output = new StringBuilder();
        highest = calculatedWinners.get(year);
        for (String anAwardList : awardList) {
            Nomination n = highest.get(anAwardList);
            output.append(anAwardList).append(": ");
            if (n.getName().equals("")) {
                output.append("Cannot calculate winners because there is insufficient data\n");
            } else {
                output.append(n.toString()).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Calculates the percent correct predictions program made.
     *
     * @return String output that prints percent correct
     */
    private String getTotalPercent() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        DecimalFormat countFmt = new DecimalFormat("#00");
        String output = "";
        HashMap<String, String> winners;
        HashMap<String, Nomination> predictedWinners;
        double count = 0;
        double correct = 0;
        double perc;
        for (int j = minYear; j <= maxYear; j++) {
            predictedWinners = calculatedWinners.get(j);
            winners = allWinners.get(j);
            for (String anAwardList : awardList) {
                try {
                    if (predictedWinners.get(anAwardList).getName().equalsIgnoreCase(
                            winners.get(anAwardList))) {
                        correct++;
                    }
                    count++;
                } catch (NullPointerException ignored) {
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
    @SuppressWarnings("unused")
    public String awardAccuracy() {
        DecimalFormat numFmt = new DecimalFormat("#00.00");
        StringBuilder output = new StringBuilder();
        HashMap<String, String> winners;
        HashMap<String, Nomination> highest;
        for (String anAwardList : awardList) {
            double count = 0;
            double correct = 0;
            double perc;
            for (int j = minYear; j <= maxYear; j++) {
                highest = calculatedWinners.get(j);
                winners = allWinners.get(j);
                try {
                    if (highest.get(anAwardList).getName().equalsIgnoreCase(winners.get(anAwardList))) {
                        correct++;
                    }
                    count++;
                } catch (NullPointerException ignored) {
                }
            }
            perc = correct / count;
            perc = perc * 100;
            output.append(anAwardList).append(": ").append(numFmt.format(perc)).append("%\n");
        }
        return output.toString();
    }

    /**
     * Prints actor/nomination and all awards it won based off calculations.
     *
     * @param category defines category of award to be calculated
     * @param year is the year to calculate
     * @return string projected  winner and all previous awards
     */
    public String getDetail(int year, String category) {
        HashMap<String, Nomination> highest;
        ArrayList<String> awardOrg;
        highest = calculatedWinners.get(year);
        Nomination x = highest.get(category);
        awardOrg = x.getAwardOrg();
        StringBuilder output = new StringBuilder(x.getName() + " is most "
                + "likely to win an Oscar\n" + x.getName()
                + " also won awards this year from:\n\n");

        for (String anAwardOrg : awardOrg) {
            output.append(anAwardOrg).append("\n");
        }
        return output.toString();
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
        StringBuilder output = new StringBuilder(category + ":\n");
        HashMap<String, Nomination> highest;
        highest = calculatedWinners.get(year);
        if (highest.get(category).getPercent().equals("0.0")) {
            output = new StringBuilder("\nCannot calculate winners because there is insufficent "
                    + "data for " + category);
        } else {
            HashMap<String, Nomination> nominees;
            HashMap<String, HashMap<String, Nomination>> nominationsByAward;
            nominationsByAward = nomineesByYear.get(year);
            nominees = nominationsByAward.get(category);
            for (String key : nominees.keySet()) {
                Nomination p = nominees.get(key);
                output.append(p.toString()).append("\n");
            }
        }
        return output.toString();
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
        StringBuilder output = new StringBuilder();
        HashMap<String, String> winners;
        HashMap<String, Nomination> predictedWinners;
        predictedWinners = calculatedWinners.get(year);
        winners = allWinners.get(year);
        double count = 0;
        double correct = 0;
        double perc;
        for (String anAwardList : awardList) {
            try {
                output.append("The actual winner for ").append(anAwardList).append(" is ").append(winners.get(anAwardList)).append(": ");
                if (predictedWinners.get(anAwardList).getName().equalsIgnoreCase(
                        winners.get(anAwardList))) {
                    output.append("Oscar Genie correctly predicted this winner\n");
                    correct++;
                    count++;
                } else {
                    output.append("Oscar Genie did not predict this winner\n");
                    count++;
                }
            } catch (NullPointerException e) {
                output.append("\n");
            }
        }
        perc = correct / count;
        perc = perc * 100;
        output.append("Oscar Genie was able to predict ").append(correct).append(
                " out of ").append(count).append(" which is ").append(numFmt.format(perc)).append(
                        "% of winners it had data for\n");
        return output.toString();
    }

    /**
     * prints help text.
     *
     * @return String output of help text
     */
    public String printHelp() {
        return "Enter either the number "
                + "corresponding to the selection you wish to "
                + "predict or ou can enter the name of the award\n"
                + "Case does not matter\n"
                + "Please email any bugs or errors "
                + "to rjcrumpler1@me.com";
    }

    /**
     * prints info text.
     *
     * @return String output of info text
     */
    public String printInfo() {
        return "This program was written by Ryan "
                + "Crumpler\nIt can predict each category with the given accuracies:\n"
                + getTotalPercent()
                + "It works by scanning sources such "
                + "as movie critic associations\n and determining "
                + "how effective each source is at predicting Oscar "
                + "winners\nfor each category It then weights each "
                + "source and makes a prediction\nbased on all the "
                + "sources and outputs the results";
    }

    /**
     * Prints all the top key words for an awrd
     * @param award name of award to get key words for
     */
    @SuppressWarnings("unused")
    public void printTopKWs(String award) {
        HashMap<String, Double> kws = winnerKeywords.get(award);
        LinkedList<String> top = new LinkedList<>();
        String maxWord = "dance";
        top.add(maxWord);
        for (String key : kws.keySet()) {
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
        for (String word : top) {
            System.out.println(word + " " + kws.get(word));
        }
    }
}