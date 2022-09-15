#!/bin/bash

set -ex

SHDIR=$(cd `dirname $0`; pwd)
PRODIR=$SHDIR/../
PULL="$SHDIR/docker/avr-pull.sh"

# ===== fetch avr-protect
rm -rf avr-protect-deploy
$PULL "avr-protect" avr-protect
tar -xzf ${PRODIR}/avr-protect/avr-protect.tar.gz
rm -rf avr-protect
# ===== end of fetch avr-protect

## ===== fetch sdk-ss
#$PULL "sdk-ss" sdk-ss
#cp -r -f sdk-ss/release/* Storage/library/libs
#rm -rf sdk-ss
## ===== end of fetch sdk-ss