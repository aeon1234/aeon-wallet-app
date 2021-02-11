#!/usr/bin/env bash
set -e

source script/env.sh

cd $EXTERNAL_LIBS_BUILD_ROOT

url="https://github.com/aeonix/aeon"
version="v0.14.1.0-aeon"

if [ ! -d "aeon" ]; then
  git clone ${url} -b ${version}
  cd aeon
  git submodule update --recursive --init
else
  cd aeon
  git fetch
  git checkout ${version}
  git submodule update --recursive --init
fi
