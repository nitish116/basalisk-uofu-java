#Gnuplot script for plotting basilisk .score files
set title "Location"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set output "location-plot.ps"
cd "../../lexicon-scores/mcat/"
plot "location.score" every 5 using 2:1 title 'Location' with points
