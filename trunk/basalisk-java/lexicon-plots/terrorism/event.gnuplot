#Gnuplot script for plotting basilisk .score files
set title "Event"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set key left top
set output "event-plot.ps"
cd "../../lexicon-scores/terrorism/"
plot "event-scat.score" every 10 using 2:1 title 'ba-1' with linespoints, \
     "event-behltvw.score" every 10 using 2:1 title 'ba-m' with linespoints, \
     "event-behltvw-diffscore.score" every 10 using 2:1 title 'ba-m+' with linespoints, \
     "event-behlntvw-diffscore.score" every 10 using 2:1 title 'ba-m+-none' with linespoints