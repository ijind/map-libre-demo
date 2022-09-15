#!/bin/bash
set -ex

cd `dirname $0`
CUR_PATH=$(pwd)
ROOT_PATH=$(dirname "$PWD")

function keystore_prop(){
    grep "${1}" ${ROOT_PATH}/keystore.properties | cut -d'=' -f2 | sed 's/\r//'
}

# ${CUR_PATH}/../gradlew aR --no-daemon
APK_RELEASE_DIR=${ROOT_PATH}/app/build/outputs/apk/release
APK_DIR="${ROOT_PATH}/apks"

apk_file=$(ls ${APK_RELEASE_DIR}/*.apk | sort -r | head -1)
apk_name=$(basename ${apk_file} .apk)

if [ -e ${APK_DIR} ]
then
    rm -rf ${APK_DIR}
fi

mkdir ${APK_DIR}
cp ${apk_file} ${APK_DIR}/"${apk_name}-unprotect.apk"

IN_APK_PATH="${apk_name}-unprotect.apk"
TAR_APK_PATH="${apk_name}.apk"
AVRP_DIR="${ROOT_PATH}/avr-protect-deploy"

(
cd ${AVRP_DIR}

./avr-protectw                                              \
  -i ${APK_DIR}/${IN_APK_PATH}                              \
  -o ${APK_DIR}/${TAR_APK_PATH}                             \
  -c ${CUR_PATH}/avr-protect-cfg.json                       \
  -ext pkgList=${CUR_PATH}/avr-protect-cfg-pkg-list.txt     \
)

storePassword=$(keystore_prop storePassword)
keyPassword=$(keystore_prop keyPassword)
keyAlias=$(keystore_prop keyAlias)
storeFile=$(keystore_prop storeFile)

${CUR_PATH}/resign-apk.sh "${ROOT_PATH}/apks/${apk_name}.apk" "${ROOT_PATH}/apks/${apk_name}.apk" ${ROOT_PATH}/app/$storeFile $keyAlias $storePassword $keyPassword




