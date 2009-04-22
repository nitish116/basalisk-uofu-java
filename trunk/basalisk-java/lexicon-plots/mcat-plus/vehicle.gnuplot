#Gnuplot script for plotting basilisk .score files
set title "Vehicle"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set output "vehicle-plot.ps"
cd "../../lexicon-scores/mcat-plus/"
plot "vehicle.score" every 5 using 2:1 title 'Vehicle' with points
