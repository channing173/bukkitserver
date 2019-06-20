#!/bin/sh
BINDIR=$(dirname "$(readlink -fn "$0")")
cd "$BINDIR"
java -Xms8G -Xmx8G -jar craftbukkit.jar -o true
