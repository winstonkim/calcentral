#!/bin/bash

# cd to directory of this script
dir_of_this_script="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${dir_of_this_script}"

# Include utility
. sis_script_utils.sh

# --------------------
# Verify API endpoints: Crosswalk, Campus Solutions, Hub
# https://jira.ets.berkeley.edu/jira/browse/CLC-6123
# --------------------

if [[ $# -eq 0 ]] ; then
  echo; echo "USAGE"; echo "    ${0} [Path to YAML file]"; echo
  exit 0
fi

# Load YAML file
yaml_filename="${1}"

if [[ ! -f "${yaml_filename}" ]] ; then
  echo; echo "ERROR"; echo "    YAML file not found: ${yaml_filename}"; echo
  exit 0
fi

eval $(parse_yaml ${yaml_filename} "yml_")

# --------------------
LOG_DIRECTORY="${dir_of_this_script}/../../log/sis_api_endpoint_verification_$(date +"%Y-%m-%d_%H%M%S")"
LEGACY_UID=61889
LEGACY_SID=11667051
CAMPUS_SOLUTIONS_ID=11667051

echo "DESCRIPTION"
echo "    Verify API endpoints: Crosswalk, Campus Solutions, Hub"; echo

echo "OUTPUT"
echo "    ${LOG_DIRECTORY} directory will have a log file per API verification"; echo

echo "--------------------"
echo "VERIFY: CROSSWALK API"; echo
mkdir -p "${LOG_DIRECTORY}/calnet_crosswalk"

API_CALLS=(
  "/LEGACY_SIS_STUDENT_ID/${LEGACY_SID}"
  "/UID/${LEGACY_UID}"
)

for path in ${API_CALLS[@]}; do
  log_file="${LOG_DIRECTORY}/calnet_crosswalk/${path//\//_}.log"
  http_code=$(curl -k -w "%{http_code}\n" \
    -so "${log_file}" \
    --digest \
    -u "${yml_calnet_crosswalk_proxy_username//\'}:${yml_calnet_crosswalk_proxy_password//\'}" \
    "${yml_calnet_crosswalk_proxy_base_url//\'}${path}")
  report_response_code "${path}" "${http_code}"
  validate_api_response "${log_file}"
done

echo "--------------------"
echo "VERIFY: CAMPUS SOLUTIONS API"; echo
mkdir -p "${LOG_DIRECTORY}/campus_solutions"

API_CALLS=(
  "/UC_CC_ADDR_LBL.v1/get?COUNTRY=ESP"
  "/UC_CC_ADDR_TYPE.v1/getAddressTypes/"
  "/UC_CC_CHECKLIST.v1/get/checklist?EMPLID=${CAMPUS_SOLUTIONS_ID}"
  "/UC_COUNTRY.v1/country/get"
  "/UC_CC_CURRENCY_CD.v1/Currency_Cd/Get"
  "/UC_CC_COMM_DB_URL.v1/dashboard/url/"
  "/UC_CC_SS_ETH_SETUP.v1/GetEthnicitytype/"
  "/UC_OB_HIGHER_ONE_URL_GET.v1/get?EMPLID=${CAMPUS_SOLUTIONS_ID}"
  "/UC_CC_SERVC_IND.v1/Servc_ind/Get?/EMPLID=${CAMPUS_SOLUTIONS_ID}"
  "/UC_CC_LANGUAGES.v1/get/languages/"
  "/UC_CC_NAME_TYPE.v1/getNameTypes/"
  "/UC_CC_COMM_PEND_MSG.v1/get/pendmsg?EMPLID=${CAMPUS_SOLUTIONS_ID}"
  "/UC_SIR_CONFIG.v1/get/sir/config/?INSTITUTION=UCB01"
  "/UC_STATE_GET.v1/state/get/?COUNTRY=ESP"
  "/UC_CM_XLAT_VALUES.v1/GetXlats?FIELDNAME=PHONE_TYPE"
  "/UC_CC_DELEGATED_ACCESS.v1/DelegatedAccess/get?SCC_DA_PRXY_OPRID=${LEGACY_UID}"
  "/UC_CC_DELEGATED_ACCESS_URL.v1/get"
  "/UC_DA_T_C.v1/get"
)

for path in ${API_CALLS[@]}; do
  log_file="${LOG_DIRECTORY}/campus_solutions/${path//\//_}.log"
  http_code=$(curl -k -w "%{http_code}\n" \
    -so "${log_file}" \
    -u "${yml_campus_solutions_proxy_username//\'}:${yml_campus_solutions_proxy_password//\'}" \
    "${yml_campus_solutions_proxy_base_url//\'}${path}")
  report_response_code "${path}" "${http_code}"
  validate_api_response "${log_file}"
done

echo "--------------------"
echo "VERIFY: HUB API"; echo
mkdir -p "${LOG_DIRECTORY}/hub_edos"

API_CALLS=(
  "/${CAMPUS_SOLUTIONS_ID}/affiliation"
  "/${CAMPUS_SOLUTIONS_ID}/contacts"
  "/${CAMPUS_SOLUTIONS_ID}/demographic"
  "/${CAMPUS_SOLUTIONS_ID}/all"
  "/${CAMPUS_SOLUTIONS_ID}/work-experiences"
)

for path in ${API_CALLS[@]}; do
  log_file="${LOG_DIRECTORY}/hub_edos/${path//\//_}.log"
  http_code=$(curl -k -w "%{http_code}\n" \
    -so "${log_file}" \
    -H "Accept:application/json" \
    -u "${yml_hub_edos_proxy_username//\'}:${yml_hub_edos_proxy_password//\'}" \
    --header "app_id: ${yml_hub_edos_proxy_app_id//\'}" \
    --header "app_key: ${yml_hub_edos_proxy_app_key//\'}"  \
    "${yml_hub_edos_proxy_base_url//\'}${path}")
  report_response_code "${path}" "${http_code}"
  validate_api_response "${log_file}"
done

echo; echo "--------------------"; echo
echo "DONE"
echo "    Results can be found in the directory: ${LOG_DIRECTORY}"
echo; echo

exit 0
