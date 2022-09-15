#!/bin/bash
set -e

BASE_DIR=$(cd $(dirname $0); pwd)
WORK_DIR=$(pwd)
OUT_DIR=${WORK_DIR}/out

AVRP_DIR=${BASE_DIR}/avr-protect-deploy
AVRP_SCRIPT_DIR=${BASE_DIR}/avr-protect-scripts

CONFIDENTIAL_PATH=/avr/.confidential

rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

R=\\033[31m; G=\\033[32m; Y=\\033[33m; B=\\033[34m; P=\\033[35m; C=\\033[36m; D=\\033[m
echo -e | grep -q -- -e && ESCAPE= || ESCAPE=-e
info() {
    echo $ESCAPE ${C}[ ${P}`date +"%H:%M:%S"` ${Y}* ${C}] ${Y}"$@"${D}
}
success() {
    echo $ESCAPE ${C}[ ${P}`date +"%H:%M:%S"` ${P}+ ${C}] ${P}"$@"${D}
}
error() {
    echo $ESCAPE ${C}[ ${P}`date +"%H:%M:%S"` ${R}- ${C}] ${R}"$@"${D}
}
execute() {
    echo $ESCAPE ${C}[ ${P}`date +"%H:%M:%S"` ${G}$ ${C}] ${G}"$@"${D}
    "$@"
    RETURN_CODE=$?
    if [ "$RETURN_CODE" != 0 ]; then exit $RETURN_CODE; fi
}
get_prop(){
    grep "${2}" ${1} | cut -d'=' -f2 | sed 's/\r//'
}
set_prop(){
    sed -i "s#^${2}=.*#${2}=${3}#g" ${1}
}
sign_aab() {
    ${CONFIDENTIAL_PATH}/txai_sign_aab.sh $1 $2
}
sign_apk() {
    ${CONFIDENTIAL_PATH}/txai_sign_apk.sh $1 $2
}
exportapk () {
    TMP_DIR=$(mktemp -d)
    AAB_INPUT=$1
    APKS_OUTPUT=${TMP_DIR}/out.apks
    APK_OUTPUT_UNSIGNED=${TMP_DIR}/out.apk
    APK_OUTPUT=$2
    java -jar /avr/tools/bundletool-all-1.11.0.jar build-apks --bundle="${AAB_INPUT}" --output="${APKS_OUTPUT}" --mode=universal
    unzip -p "${APKS_OUTPUT}" universal.apk > "${APK_OUTPUT_UNSIGNED}"

    sign_apk ${APK_OUTPUT_UNSIGNED} ${APK_OUTPUT}

    rm -rf ${TMP_DIR}
}
get_app_name() {
  curday=`date +%m%d%H%M`
  versionName=`grep "versionName\ " config.gradle |cut -d \" -f 2`
  versionCode=`grep "versionCode\ " config.gradle| tr -s ' '|cut -d ' ' -f 4 |cut -d ',' -f 1`
  appName="texi_release_v${versionName}.${versionCode}_${curday}"
  echo ${appName}
}

ORI_APP_NAME=`get_app_name`

echo ""
echo "####################################################################################################################"
echo "# change properties ..."
echo "####################################################################################################################"

new_mapbox_key=`get_prop ${CONFIDENTIAL_PATH}/txai.properties MAPBOX_DOWNLOADS_TOKEN`
set_prop ${BASE_DIR}/gradle.properties MAPBOX_DOWNLOADS_TOKEN ${new_mapbox_key}

echo ""
echo "####################################################################################################################"
echo "# start building ..."
echo "####################################################################################################################"

execute ${BASE_DIR}/gradlew clean bundleRelease -Dorg.gradle.daemon=false -Djava.security.egd=file:/dev/urandom

echo ""
echo "####################################################################################################################"
echo "# Copy output to ${OUT_DIR} ..."
echo "####################################################################################################################"

execute cp ${BASE_DIR}/app/build/outputs/mapping/release/mapping.txt ${OUT_DIR}/
execute cp ${BASE_DIR}/app/build/outputs/bundle/release/app-release.aab ${OUT_DIR}/${ORI_APP_NAME}_un_protect_unsigned.aab

execute sign_aab ${OUT_DIR}/${ORI_APP_NAME}_un_protect_unsigned.aab ${OUT_DIR}/${ORI_APP_NAME}_un_protect.aab
execute rm -rf ${OUT_DIR}/${ORI_APP_NAME}_un_protect_unsigned.aab

echo ""
echo "####################################################################################################################"
echo "# protect apk ..."
echo "####################################################################################################################"

export NDK_ROOT=/opt/android-ndk-r20b/

(
    cd ${AVRP_DIR}

    execute ./avr-protectw                                               \
        -i ${OUT_DIR}/${ORI_APP_NAME}_un_protect.aab                     \
        -o ${OUT_DIR}/${ORI_APP_NAME}_unsigned.aab                       \
        -c ${AVRP_SCRIPT_DIR}/avr-protect-cfg.json                       \
        -ext pkgList=${AVRP_SCRIPT_DIR}/avr-protect-cfg-pkg-list.txt     \

    # execute cp ${AAB_SIGNED} ${AAB_PROTECTED}

    if [ ! -e ${OUT_DIR}/${ORI_APP_NAME}_unsigned.aab ]; then
        error "Protect failed, please check!"
        exit -1
    fi

    execute sign_aab ${OUT_DIR}/${ORI_APP_NAME}_unsigned.aab ${OUT_DIR}/${ORI_APP_NAME}.aab
    execute rm -rf ${OUT_DIR}/${ORI_APP_NAME}_unsigned.aab
)

echo ""
echo "####################################################################################################################"
echo "# export apk ..."
echo "####################################################################################################################"

execute exportapk ${OUT_DIR}/${ORI_APP_NAME}_un_protect.aab ${OUT_DIR}/${ORI_APP_NAME}_un_protect.apk
execute exportapk ${OUT_DIR}/${ORI_APP_NAME}.aab ${OUT_DIR}/${ORI_APP_NAME}.apk

echo ""
echo "####################################################################################################################"
echo "# Done."
echo "####################################################################################################################"