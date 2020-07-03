export FROM_PATH=$1
export DEST_PATH=$2

export GIT_REV=$(git rev-parse HEAD)
export GIT_REV=${GIT_REV:0:6}

cd $FROM_PATH/
for d in */; do
  cd $FROM_PATH/$d
  for f in *; do
    # if filename looks like [DIRNAME].jar
    if [ $f = "${d%?}.jar" ]; then
      export OLD=$(find $DEST_PATH -regextype grep -maxdepth 1 -name "${d%?}-*.jar")
      if ! [ -z "$OLD" ]; then
        rm "$OLD"
      fi
      cp $f "$DEST_PATH/${d%?}-$GIT_REV.jar"
    fi
  done
done