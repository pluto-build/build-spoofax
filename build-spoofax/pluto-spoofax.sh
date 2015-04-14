#/bin/sh

ARGS="pluto-spoofax build.pluto.buildspoofax.Main.factory build.pluto.buildspoofax.Main\$Input $@"

mvn compile exec:java -Dexec.args="$ARGS"
