#!/bin/bash

set -e

mvn clean package
rm -rf /usr/local/share/devmagic
mkdir -p /usr/local/share/devmagic
tar xfz target/devmagic-1.0-SNAPSHOT-bin.tar.gz -C /usr/local/share/devmagic --strip 1
#rm -f /usr/local/bin/devmagic
#sudo ln -s /usr/local/bin/devmagic ${DIR}/devmagic.sh
