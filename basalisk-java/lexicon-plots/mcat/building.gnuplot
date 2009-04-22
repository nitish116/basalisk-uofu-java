#Gnuplot script for plotting basilisk .score files
set title "Building"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set output "building-plot.ps"
cd "../../lexicon-scores/mcat/"
plot "building.score" every 5 using 2:1 title 'Building'with points
