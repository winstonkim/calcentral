#!/bin/sh

# --------------------------------------------
# Read YAML file from Bash script and other utilities.
# See sample usage at:
#   https://gist.github.com/pkuczynski/8665367
# --------------------------------------------

parse_yaml() {
   local prefix=$2
   local s='[[:space:]]*' w='[a-zA-Z0-9_]*' fs=$(echo @|tr @ '\034')
   sed -ne "s|^\($s\)\($w\)$s:$s\"\(.*\)\"$s\$|\1$fs\2$fs\3|p" \
        -e "s|^\($s\)\($w\)$s:$s\(.*\)$s\$|\1$fs\2$fs\3|p"  $1 |
   awk -F$fs '{
      indent = length($1)/2;
      vname[indent] = $2;
      for (i in vname) {if (i > indent) {delete vname[i]}}
      if (length($3) > 0) {
         vn=""; for (i=0; i<indent; i++) {vn=(vn)(vname[i])("_")}
         printf("%s%s%s=\"%s\"\n", "'$prefix'",vn, $2, $3);
      }
   }'
}

validate_api_response() {
  local path_to_file=$1
  error_count=$(grep -i 'error\|unable to find a routing\|not authorized' ${path_to_file} | wc -l)
  if [ "${error_count}" -ne "0" ]; then
    echo; echo "    [WARN] Errors found in ${path_to_file}:"
    cat "${path_to_file}"; echo
  fi
}

report_response_code() {
  echo "    ${1} --> response: ${2}"
}
