#Gnuplot script for plotting basilisk .score files
set title "Weapon"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "weapon-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "weapon-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "weapon-mcat.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "weapon-mcat-plus.score" every 10 using 2:1 title 'ba-m+' with linespoints