#Gnuplot script for plotting basilisk .score files
set title "Human"
set xlabel "Total Lexicon Entries"
set ylabel "Correct Lexicon Entries"
set term postscript
set output "human-plot.ps"
cd "../../lexicon-scores/mcat/"
plot "human.score" every 5 using 2:1 title 'Human' with points
