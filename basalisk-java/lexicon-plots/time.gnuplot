#Gnuplot script for plotting basilisk .score files
set title "Time"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set output "time-plot.ps"
cd "../lexicon-scores/"
plot "time.score" every 5 using 2:1 title 'Time' with points
