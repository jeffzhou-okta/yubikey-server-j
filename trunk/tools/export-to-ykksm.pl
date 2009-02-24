#!/usr/bin/perl

# Written by Simon Josefsson <simon@josefsson.org>.
# Copyright (c) 2009 Yubico AB
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
#   * Redistributions of source code must retain the above copyright
#     notice, this list of conditions and the following disclaimer.
#
#   * Redistributions in binary form must reproduce the above
#     copyright notice, this list of conditions and the following
#     disclaimer in the documentation and/or other materials provided
#     with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

use strict;
use DBI;
use MIME::Base64;

sub usage {
    print "Usage: $0 [--verbose] [--help]\n";
    print "          [--database DBI] [--db-user USER] [--db-passwd PASSWD]\n";
    print "\n";
    print "  Tool to export keys from Java server to the YKKSM-KEYPROV format.\n";
    print "\n";
    print "  DBI: Database identifier, see http://dbi.perl.org/\n";
    print "       defaults to a MySQL database ykksm on localhost,\n";
    print "       i.e., DBI::mysql:yubico.\n";
    print "\n";
    print "  USER: Database username to use, defaults to empty string.\n";
    print "\n";
    print "  PASSWD: Database password to use, defaults to empty string.\n";
    print "\n";
    print "Usage example:\n";
    print "\n";
    print "  ./ykksm-export.pl --db-user foo --db-passwd pass123 |\n";
    print "     gpg -a --encrypt -r 1D2F473E > keys.txt\n";
    print "\n";
    exit 1;
}

my $verbose = 0;
my $db = "DBI:mysql:yubico";
my $dbuser;
my $dbpasswd;
while ($ARGV[0] =~ m/^-(.*)/) {
    my $cmd = shift @ARGV;
    if (($cmd eq "-v") || ($cmd eq "--verbose")) {
	$verbose = 1;
    } elsif (($cmd eq "-h") || ($cmd eq "--help")) {
	usage();
    } elsif ($cmd eq "--database") {
	$db = shift;
    } elsif ($cmd eq "--db-user") {
	$dbuser = shift;
    } elsif ($cmd eq "--db-passwd") {
	$dbpasswd = shift;
    }
}

if ($#ARGV>=0) {
    usage();
}

sub b642hex {
    my $b64 = shift;
    my $bin = decode_base64($b64);
    return unpack("H*", $bin);
}

sub b64ToModhex {
    my $b64 = shift;
    my $bin = decode_base64($b64);
    $_ = unpack("H*", $bin);
    tr/0123456789abcdef/cbdefghijklnrtuv/;
    return $_;
}

my $dbh = DBI->connect($db, $dbuser, $dbpasswd, {'RaiseError' => 1});
my $sth = $dbh->prepare
    ('SELECT id, created, accessed, tokenId, userId, secret FROM yubikeys')
    or die "Couldn't prepare statement: " . $dbh->errstr;
$sth->execute()
    or die "Couldn't execute statement: " . $sth->errstr;

my $row;
while ($row = $sth->fetchrow_hashref()) {
    if ($verbose) {
	print "# ";
	foreach my $key (keys %$row) {
	    print $key . "=" . $row->{$key} . " ";
	}
	print "\n";
    }
    print $row->{'id'} . ",";
    print b64ToModhex($row->{'tokenId'}) . ",";
    print b642hex($row->{'userId'}) . ",";
    print b642hex($row->{'secret'}) . ",";
    print "000000000000,";
    print "created=" . $row->{'created'} . ",";
    print "accessed=" . $row->{'accessed'} . "\n";
}

if ($sth->rows == 0) {
    print "No data?!\n\n";
}

$sth->finish;
$dbh->disconnect();

exit 0;
