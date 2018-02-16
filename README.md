# Oscar-Genie
Developed by Ryan Crumpler

Predicts Oscar Winners by looking at all awards that a film won 
for a particular year and a film's keywords and weights how effective 
of a tool that particular award or keyword is at predicting the Oscar winners and selects 
the film with the highest score. Uses machine learning to better weigh each award.

Now uses TMDB API found here https://github.com/Omertron/api-themoviedb

# Important note for using the program
Because the TMDB API limits a user to 40 queries every 10 seconds the program takes about 30 min to make all queries. The alleviate this I exported all data to allWinners.ser and nomineesByYear.ser. When running the jar have both ser files in the same directory and run the program with no command line arguments. Run  the jar with the command line argument -t to start the program without the ser files and query the database. 
The program can be run with various command line arguments:

-t is to run in initial setup mode, this will force the program to requery TMDB, this can take half an hour due to limitations imposed by TMDB

-i is to use the ser files located inside the jar, this is so you do not have to have the ser files in the same directory as the jar. It is recommended to run the program with this parameter 

The minimum year, maximum year and current year can also be passed as arguments. 

The min year is the first year to read from the CSVs, note the earliest valid year is 1991.

The max year is the latest year you now you have the winners for, this is currently 2017

The calculating year is they year you do not know the winners for yet, the current parameter is 2018 

You can simulate how well the program predicts previous years by entering the year you want to predict for the current year, and one year before the current year for the max year, and 1991 for min year. This will predict the winners for the current year you selected without checking the program against the known winners.

# Examples on how to run program:
If the ser files are in same directory as the jar:
java -jar Oscar-Genie.jar

If ser files are not in same directory as jar and you do not wish to re-query TMDB:
java -jar Oscar-Genie.jar -i

If you want to re-query TMDB (this will take a long time)
java -jar Oscar-Genie.jar -t

Predict 2017 winners with data up to and including 2016 (can be run with or without -t or -i preceding the dates):

java -jar Oscar-Genie.jar -i 1991 2016 2017

or

java -jar Oscar-Genie.jar 1991 2016 2017

or 

java -jar Oscar-Genie.jar -t 1991 2016 2017




