#!/bin/sh
BINDIR=$(dirname "$(readlink -fn "$0")")
cd "$BINDIR"
java -Xms12G -Xmx12G -jar craftbukkit.jar -o true
