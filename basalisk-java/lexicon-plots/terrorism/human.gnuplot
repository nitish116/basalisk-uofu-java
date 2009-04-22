#Gnuplot script for plotting basilisk .score files
set title "Human"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "human-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "human-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "human-mcat.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "human-mcat-plus.score" every 10 using 2:1 title 'ba-m+' with linespoints