# Oscar-Genie
Predicts Oscar Winners by looking at all awards that a film won 
for a particular year and a film's keywords and weights how effective 
of a tool that particular award or keyword is at predicting the Oscar winners and selects 
the film with the highest score. Uses machine learning to better weigh each award.

Now uses TMDB API found here https://github.com/Omertron/api-themoviedb

# Important note for using the program
Because the TMDB API limits a user to 40 queries every 10 seconds the program takes about 30 min to make all queries. The allievate this I exported all data to allWinners.ser and nomineesByYear.ser. When running the jar have both ser files in the same directory and run the program with no command line arguments. Run  the jar with the command line argument -t to start the program without the ser files and query the database. 

Developed by Ryan Crumpler
