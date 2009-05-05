#Gnuplot script for plotting basilisk .score files
set title "None"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "none-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "none-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "none-behltvw.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "none-behltvw-diffscore.score" every 10 using 2:1 title 'ba-m+' with linespoints