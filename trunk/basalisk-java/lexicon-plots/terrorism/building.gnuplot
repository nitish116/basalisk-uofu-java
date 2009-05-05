#Gnuplot script for plotting basilisk .score files
set title "Building"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "building-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "building-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "building-behltvw.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "building-behltvw-diffscore.score" every 10 using 2:1 title 'ba-m+' with linespoints, \
     "building-behlntvw-diffscore.score" every 10 using 2:1 title 'ba-m+-none' with linespoints     