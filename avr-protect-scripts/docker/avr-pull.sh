#!/bin/bash

set -e

CURDIR=$(cd `dirname $0`; pwd)

PNAME="$1"
OUT_DIR="$2"
PVER="$(cat $CURDIR/avr-dependent.txt | grep $PNAME | cut -f2 -d:)"

if [ "x$CI_REGISTRY" = x ]; then
  CI_REGISTRY=gitlab.corp.totok.co:5050
fi

fetch_files_from_docker() {
  url=$1
  dest=$2
  docker pull $url
  docker rm -f fetch-tmp || true
  docker create --name fetch-tmp $url ignore
  docker cp fetch-tmp:/out/ $dest
}

[ -e $OUT_DIR ] && mkdir -p $OUT_DIR || true
fetch_files_from_docker $CI_REGISTRY/security/store/$PNAME:$PVER $OUT_DIR/

echo "Download $CI_REGISTRY/security/store/$PNAME:$PVER to $OUT_DIR. Done!"