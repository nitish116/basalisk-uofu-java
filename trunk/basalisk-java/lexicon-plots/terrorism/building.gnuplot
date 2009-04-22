#Gnuplot script for plotting basilisk .score files
set title "Building"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "building-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "building-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "building-mcat.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "building-mcat-plus.score" every 10 using 2:1 title 'ba-m+' with linespoints