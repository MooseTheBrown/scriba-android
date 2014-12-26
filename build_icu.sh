#!/bin/sh

#
# Copyright (C) 2014 Mikhail Sapozhnikov
#
# This file is part of scriba-android.
#
# scriba-android is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# scriba-android is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with scriba-android. If not, see <http://www.gnu.org/licenses/>.
#
#

if [ "$1" = "--help" ]; then
    echo "Build static ICU library for Android and copy it to scriba-android tree."
    echo "This script should be run from the root of scriba-android source tree."
    echo "Variables used and their default values:"
    echo "TOOLCHAIN_VARIANT     [arm-linux-androideabi]"
    echo "TOOLCHAIN_VER         [4.8]"
    echo "TOOLCHAIN_PATH        [./../toolchain/\$TOOLCHAIN_VARIANT-\$TOOLCHAIN_VER]"
    echo "ICU_SRC               [./../icu]"
    echo "NUM_THREADS           [6]"

    exit 0
fi

if ( ! echo $PWD | grep ".*scriba-android$" ) > /dev/null; then
    echo "This script should be run from the root of scriba-android source tree."
    exit 1
fi

SCRIBAANDROID_ROOT="$PWD"

if [ -z "$TOOLCHAIN_VARIANT" ]; then
    TOOLCHAIN_VARIANT=arm-linux-androideabi
fi
if [ -z "$TOOLCHAIN_VER" ]; then
    TOOLCHAIN_VER=4.8
fi

if [ -z "$TOOLCHAIN_PATH" ]; then
    TOOLCHAIN_PATH="$SCRIBAANDROID_ROOT/../toolchain/$TOOLCHAIN_VARIANT-$TOOLCHAIN_VER"
fi

TOOLCHAIN_SYSROOT="$TOOLCHAIN_PATH/sysroot"

if [ -z "$ICU_SRC" ]; then
    ICU_SRC="$SCRIBAANDROID_ROOT/../icu"
fi

if [ -z "$NUM_THREADS" ]; then
    NUM_THREADS=6
fi

# Build host version of ICU
echo "Building host version of ICU"
export CXXFLAGS="-DU_USING_ICU_NAMESPACE=0 -DU_CHARSET_IS_UTF8=1"

if [ -e "$ICU_SRC/build_linux" ]; then
    rm -rf "$ICU_SRC/build_linux"
fi
mkdir "$ICU_SRC/build_linux"
cd "$ICU_SRC/build_linux"
sh "$ICU_SRC/source/runConfigureICU" Linux --prefix="$ICU_SRC/build_linux" --enable-static --enable-shared=no --enable-tests=no --enable-samples=no --disable-dyload --with-data-packaging-mode=static
make -j"$NUM_THREADS"
make install

# Build Android version of ICU
export CC="$TOOLCHAIN_PATH/bin/$TOOLCHAIN_VARIANT-gcc"
export CXX="$TOOLCHAIN_PATH/bin/$TOOLCHAIN_VARIANT-g++"
echo "Building Android version of ICU"
export CXXFLAGS="-DU_USING_ICU_NAMESPACE=0 -DU_CHARSET_IS_UTF8=1 -DU_HAVE_NL_LANGINFO_CODESET=0 -I$TOOLCHAIN_SYSROOT/usr/include"

if [ -e "$ICU_SRC/build_android" ]; then
    rm -rf "$ICU_SRC/build_android"
fi
mkdir "$ICU_SRC/build_android"

if [ -e "$ICU_SRC/install_android" ]; then
    rm -rf "$ICU_SRC/install_android"
fi
mkdir "$ICU_SRC/install_android"

cd "$ICU_SRC/build_android"
"$ICU_SRC/source/configure" --with-cross-build="$ICU_SRC/build_linux" --prefix="$ICU_SRC/install_android" --host="$TOOLCHAIN_VARIANT" --enable-static --enable-shared=no --enable-tests=no --enable-samples=no --enable-extras=no --disable-dyload --with-data-packaging-mode=static
make -j"$NUM_THREADS"
make install

# copy ICU headers to JNI directory
JNI_DIR="$SCRIBAANDROID_ROOT/jni"
if [ ! -e "$JNI_DIR" ]; then
    mkdir "$JNI_DIR"
fi
if [ -e "$JNI_DIR/layout" ]; then rm -rf "$JNI_DIR/layout"; fi
if [ -e "$JNI_DIR/unicode" ]; then rm -rf "$JNI_DIR/unicode"; fi
cp -R "$ICU_SRC/install_android/include/layout" "$JNI_DIR/"
cp -R "$ICU_SRC/install_android/include/unicode" "$JNI_DIR/"

# copy ICU static libraries
ICU_LIBS="libicudata.a libicui18n.a libicuio.a libicule.a libiculx.a libicutu.a libicuuc.a"
for LIB in $ICU_LIBS; do
    cp "$ICU_SRC/install_android/lib/$LIB" "$JNI_DIR/"
done
