#!/usr/bin/perl

# =====================================================================
#                     USAGE
# =====================================================================

#     union-caseframes.pl <filename1> <filename2> ... <filenameN>
#
# Example:
#     union-caseframes.pl cfs1.txt cfs2.txt cfs3.txt > merged_cfs123.txt
#
# Can take an unlimited number of caseframe files on the command line
# (requires at least two) and then prints out the union of these files 
# to standard output. 
# 

# =====================================================================
#                     DATA STRUCTURES
# =====================================================================

use Class::Struct; 

struct cfinfo => { 
    lines => '$',   # this is a pointer to a list of lines (strings) that define the cf
                    # (if you print these out in sequence, the cf structure is reconstructed)
    cfname => '$'
    };


# =====================================================================
#                     MAIN CODE
# =====================================================================

$numargs = @ARGV;
if ($numargs < 2) {
    print "ERROR: Must be at least 2 input files!\n";
} 
else { 
    $newcount = 0;
    $dupecount = 0;

    # Create hash table with caseframe entries from file1
    $file1 = $ARGV[0];
    $ht1 = create_ht();
    read_file($file1, $ht1);
    
    $count = 1;
    while ($count < $numargs) {
	# Add caseframes from each subsequent file to the same hash table, 
	# being careful to avoid adding a duplicate entry.
	#
	$nextfile = $ARGV[$count];
	read_file($nextfile, $ht1);
	$count++;
    }

    print_keysorted_ht($ht1);
    print stderr "\nFinished!\n  Found $newcount unique caseframe entries\n";
    print stderr "  Found $dupecount duplicate caseframe entries\n\n";
}


sub read_file {
    my($filename, $ht) = @_; 
    my(@lines, $cfname);
    $cfname = "";   # initialize so counts are right (see below)

    open(instream, "$filename") || die "Can't open file: $filename\n";
    while ($line = <instream>) {
	if ($line =~ /^CF:\s*$/i) {    # found new cf
	    if ($cfname) {  # should be "" initially (see above)
		$cfdata = create_cf($cfname, @lines);
		$found_dupe = add_value_to_ht($cfname, $cfdata, $ht);
		if ($found_dupe) {	$dupecount++; }
		else { $newcount++; }
	    }
	    @lines = ();    # initialize new cf
	    push(@lines, $line);
	}
	else {
	    if ($line =~ /^Name:\s+(.*?)\s*$/i) {
		$cfname = $1;
		if ($cfname =~ /(.*)_\d+$/) {   
		    $cfname = $1;  # strip off trailing number for comparison purposes
		}
	    }
	    if ($line !~ /^\s*$/) {   # don't bother saving blank lines
		push(@lines, $line);
	    }
	}
    }
    # Don't forget to process the last cf in the file!
    if ($cfname) {   # just in case we get an empty file
	$cfdata = create_cf($cfname, @lines);
	$found_dupe = add_value_to_ht($cfname, $cfdata, $ht);
	if ($found_dupe) {	$dupecount++; }
	else { $newcount++; }
    }
}


sub create_cf {
    my($cfname, @lines) = @_;
    my($cfdata);

    $cfdata = cfinfo->new();
    $cfdata->lines(\@lines);
    $cfdata->cfname($cfname);
    return($cfdata);
}


sub create_ht {
    my(%ht) = ();

    return(\%ht);
}



# Adds value to the ht entry with the given key
# If an entry with that same key already exist, just ignore the new one
# (assumes this must be a duplicate entry because the key is a unique
# feature). 
#
sub add_value_to_ht {
    my($key, $value, $ht) = @_;  # ptr to hash table
    my($found_dupe);

    $found_dupe = 0;
    if ($$ht{$key}) {
	$found_dupe = 1;
    }
    else {
	$$ht{$key} = $value;
    }
    return($found_dupe);
}



# Prints the hash table sorted w.r.t. alphabetized keys
# The pad_value parameter gives the width to which the keys should be 
# padded when printing. Ex: pad_value = 40 says to pad each key
# to have width 40. 
#
sub print_keysorted_ht {
    my($ht) = @_;   # ptr to hash table
    my($entry, $lines, $line);

    foreach $key (sort keys %$ht) {
	$entry = $$ht{$key};
	$lines = $entry->lines;
	foreach $line (@$lines) {
	    print "$line";     # newline should already be part of each line
	}
	print "\n";   # print newline between each cf
                      # (will produce an extra newline between cfs in normal cf files
                      #  but better 2 lines than none, to be safe)
    }
}

