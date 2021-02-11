DEFAULT_ANDROID_NDK_ROOT=/opt/android/android-ndk-r17c-linux-x86_64/android-ndk-r17c
ANDROID_NDK_ROOT="${ANDROID_NDK_ROOT:-${DEFAULT_ANDROID_NDK_ROOT}}"

export ANDROID_NDK_ROOT=`realpath $ANDROID_NDK_ROOT`

DEFAULT_EXTERNAL_LIBS_BUILD=`pwd`/build/
EXTERNAL_LIBS_BUILD="${EXTERNAL_LIBS_BUILD:-${DEFAULT_EXTERNAL_LIBS_BUILD}}"
export EXTERNAL_LIBS_BUILD=${EXTERNAL_LIBS_BUILD%/}

DEFAULT_EXTERNAL_LIBS_BUILD_ROOT=${EXTERNAL_LIBS_BUILD}/src/
EXTERNAL_LIBS_BUILD_ROOT="${EXTERNAL_LIBS_BUILD_ROOT:-${DEFAULT_EXTERNAL_LIBS_BUILD_ROOT}}"
export EXTERNAL_LIBS_BUILD_ROOT=${EXTERNAL_LIBS_BUILD_ROOT%/}

DEFAULT_EXTERNAL_LIBS_ROOT=${EXTERNAL_LIBS_BUILD}/build/
EXTERNAL_LIBS_ROOT="${EXTERNAL_LIBS_ROOT:-${DEFAULT_EXTERNAL_LIBS_ROOT}}"
export EXTERNAL_LIBS_ROOT=${EXTERNAL_LIBS_ROOT%/}

DEFAULT_NDK_TOOL_DIR=${EXTERNAL_LIBS_BUILD}/tool/
NDK_TOOL_DIR="${NDK_TOOL_DIR:-${DEFAULT_NDK_TOOL_DIR}}"
export NDK_TOOL_DIR=${NDK_TOOL_DIR%/}
