#Gnuplot script for plotting basilisk .score files
set title "Location"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "location-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "location-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "location-behltvw.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "location-behltvw-diffscore.score" every 10 using 2:1 title 'ba-m+' with linespoints, \
     "location-behlntvw-diffscore.score" every 10 using 2:1 title 'ba-m+-none' with linespoints