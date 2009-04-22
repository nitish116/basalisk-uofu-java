#Gnuplot script for plotting basilisk .score files
set title "Time"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "time-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "time-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "time-mcat.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "time-mcat-plus.score" every 10 using 2:1 title 'ba-m+' with linespoints