#!/bin/bash

# Include utility
. parse_yaml.sh

# --------------------
# Verify API endpoints: Crosswalk, Campus Solutions, Hub
# https://jira.ets.berkeley.edu/jira/browse/CLC-6123
# --------------------

# Load YAML file
yaml_filename="${1}"
eval $(parse_yaml ${yaml_filename} "yml_")

# --------------------
# IDs
export LEGACY_UID=61889
export LEGACY_SID=11667051
export CAMPUS_SOLUTIONS_ID=11667051

echo "--------------------"
echo "Verify Crosswalk API"; echo

API_CALLS=(
  "/LEGACY_SIS_STUDENT_ID/${LEGACY_SID}"
  "/UID/${LEGACY_UID}"
)

for path in ${API_CALLS[@]}; do
  curl -k --digest \
    -u "${yml_calnet_crosswalk_proxy_username//\'}:${yml_calnet_crosswalk_proxy_password//\'}" \
    "${yml_calnet_crosswalk_proxy_base_url//\'}${path}"
  echo; echo " Exit code ${?} for path: ${path}"; echo
done

echo "--------------------"
echo "Verify Campus Solutions API"; echo

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
)

for path in ${API_CALLS[@]}; do
  curl -k -u "${yml_campus_solutions_proxy_username//\'}:${yml_campus_solutions_proxy_password//\'}" \
    "${yml_campus_solutions_proxy_base_url//\'}${path}"
  echo; echo " Exit code ${?} for path: ${path}"; echo
done

echo "--------------------"
echo "Verify Hub API"; echo

API_CALLS=(
  "/${CAMPUS_SOLUTIONS_ID}/affiliation"
  "/${CAMPUS_SOLUTIONS_ID}/contacts"
  "/${CAMPUS_SOLUTIONS_ID}/demographic"
  "/${CAMPUS_SOLUTIONS_ID}/all"
  "/${CAMPUS_SOLUTIONS_ID}/work-experiences"
)

for path in ${API_CALLS[@]}; do
  curl -k -H "Accept:application/json" \
    -u "${yml_hub_edos_proxy_username//\'}:${yml_hub_edos_proxy_password//\'}" \
    --header "app_id: ${yml_hub_edos_proxy_app_id//\'}" \
    --header "app_key: ${yml_hub_edos_proxy_app_key//\'}"  \
    "${yml_hub_edos_proxy_base_url//\'}${path}"
  echo; echo " Exit code ${?} for path: ${path}"; echo
done

echo; echo "--------------------"; echo

exit 0
