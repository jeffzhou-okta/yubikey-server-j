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
use POSIX qw(strftime);
use MIME::Base64;

sub usage {
    print "Usage: $0 [--verbose] [--help]\n";
    print "          [--database DBI] [--db-user USER] [--db-passwd PASSWD]\n";
    print "\n";
    print "  Import key data to Java server from the YKKSM-KEYPROV format.\n";
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
    print "  ./ykksm-import.pl < keys.txt\n";
    print "\n";
    exit 1;
}

my $verbose = 0;
my $db = "dbi:mysql:yubico";
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

my $infilename = "tmp.$$";
my $verify_status;
my $encrypted_to;
my $signed_by;

# Read input into temporary file.
open TMPFILE, ">$infilename"
    or die "Cannot open $infilename for writing";
while (<>) {
    print TMPFILE $_;
}
close TMPFILE;

END { unlink $infilename; }

# Get status
open(GPGV, "gpg --status-fd 1 --output /dev/null < $infilename 2>&1 |")
    or die "Cannot launch gpg";
while (<GPGV>) {
    $verify_status .= $_;
    $encrypted_to = $1 if m,^\[GNUPG:\] ENC_TO ([0-9A-F]+) ,;
    $signed_by = $1 if m,^\[GNUPG:\] VALIDSIG ([0-9A-F]+) ,;
}
close GPGV;

print "Verification output:\n" . $verify_status;
print "encrypted to: " . $encrypted_to . "\n";
print "signed by: " . $signed_by . "\n";

die "Input not signed?" if !$signed_by;

my $dbh = DBI->connect($db, $dbuser, $dbpasswd, {'RaiseError' => 1});
my $inserth = $dbh->prepare_cached(qq{
INSERT INTO yubikeys (client_id, active, created, accessed,
                      tokenId, userId, secret)
VALUES (1, true, ?, null, ?, ?, ?)
});
my $now = strftime "%Y-%m-%dT%H:%M:%S", localtime;

sub hex2b64 {
    my $hex = shift;
    my $bin = pack("H*", $hex);
    $_ = encode_base64($bin);
    chop;
    return $_;
}

sub modhex2b64 {
    $_ = shift;
    tr/cbdefghijklnrtuv/0123456789abcdef/;
    return hex2b64($_);
}

open(GPGV, "gpg < $infilename 2>/dev/null |")
    or die "Cannot launch gpg";
while (<GPGV>) {
    next if m:^#:;
    my ($serialNr, $publicName, $internalName, $aesKey,
	$lockCode, $created, $accessed) =
	  m%^([0-9]+),([cbdefghijklnrtuv]+),([0-9a-f]+),([0-9a-f]+),([0-9a-f]+),([T:0-9 -]*),([T:0-9 -]*)%;
    if ($verbose) {
	print "line: $_";
    }
    print "\tserialnr $serialNr publicName $publicName " .
	"internalName $internalName aesKey $aesKey lockCode $lockCode " .
	"created $created accessed $accessed eol";
    if ($verbose) {
	print "\n";
    } else {
	print "\r";
    }

    $created = $now if !$created;

    print "tokenId $publicName userId $internalName secret $aesKey\n";

    $publicName = modhex2b64($publicName);
    $internalName = hex2b64($internalName);
    $aesKey = hex2b64($aesKey);

    print "tokenId $publicName userId $internalName secret $aesKey\n";

    $inserth->execute($created, $publicName, $internalName, $aesKey)
	or die "Database insert error: " . $dbh->errstr;
}
print "\n";

close GPGV;
$dbh->disconnect();

exit 0;
