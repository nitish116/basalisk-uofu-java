#Gnuplot script for plotting basilisk .score files
set title "Vehicle"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "vehicle-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "vehicle-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "vehicle-behltvw.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "vehicle-behltvw-diffscore.score" every 10 using 2:1 title 'ba-m+' with linespoints, \
     "vehicle-behlntvw-diffscore.score" every 10 using 2:1 title 'ba-m+-none' with linespoints