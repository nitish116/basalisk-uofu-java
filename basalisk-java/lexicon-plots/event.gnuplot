#Gnuplot script for plotting basilisk .score files
set title "Event"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set output "event-plot.ps"
cd "../lexicon-scores/"
plot "event.score" every 5 using 2:1 title 'Event' with points
