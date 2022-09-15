#!/bin/bash

filename="./config.gradle"
version_code=""
version_name=""

usage(){
   echo "Usage: $0 [ -n version_name ] [ -c version_code ]" 1>&2 
}

while getopts :n:c: options;do
    case $options in
      n)
          version_name=${OPTARG}
          ;;
      c)
          version_code=${OPTARG}
          re_isanum='^[1-9][0-9]*$'
          if ! [[ $version_code =~ $re_isanum ]] ; then
              echo "Error: version_code must be number."
              exit 1
          fi
          ;;
      :)
          echo "Error: -${OPTARG} requires an argument."
          exit 1
          ;;
      *)
          usage
          exit 1
          ;;
      esac
done
      
check_input(){
    if [ -z ${version_name} ] || [ -z ${version_code} ];then
       usage
       exit 1
    fi
}         
    
change_version_code(){
    sed -i 's#^[[:space:]]*versionCode[[:space:]]*\:[[:space:]]*[0-9]*,#            versionCode              : '${version_code}',#g' ${filename}
    echo "change version code to ${version_code}"
}

change_version_name(){
    sed -i 's#^[[:space:]]*versionName[[:space:]]*\:[[:space:]]*\"[0-9,a-z,.]*\",#            versionName              : "'${version_name}'",#g' ${filename}
    echo "change version name to ${version_name}"
}


main(){
  check_input
  change_version_code 
  change_version_name
}

main
