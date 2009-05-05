#Gnuplot script for plotting basilisk .score files
set title "Weapon"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "weapon-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "weapon-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "weapon-behltvw.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "weapon-behltvw-diffscore.score" every 10 using 2:1 title 'ba-m+' with linespoints, \
     "weapon-behlntvw-diffscore.score" every 10 using 2:1 title 'ba-m+-none' with linespoints