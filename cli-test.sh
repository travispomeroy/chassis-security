#! /bin/bash -e

# Stop using bash

export ADMIN_JWT=$(http --form POST   http://localhost:8091/realms/master/protocol/openid-connect/token  client_id=admin-cli username=admin  password=admin grant_type=password | jq -r .access_token)

USER_ID=userId$(date +"%Y%m%d_%H%M%S")


export USER_RESOURCE=$(curl -i -H "Authorization: bearer ${ADMIN_JWT}" \
   -H "Content-Type: application/json" \
   --data "{\"username\":\"${USER_ID}\",\"enabled\":true, \"credentials\":[{\"type\":\"password\",\"value\":\"foopassword\"}]}" \
   http://localhost:8091/admin/realms/service-template/users | grep -i location: | cut -f2 -d\ | tr -d '\n' | tr -d '\r')

curl -H "Content-Type: application/json" \
  -H "Authorization: bearer $ADMIN_JWT" \
  $USER_RESOURCE/role-mappings | jq .

curl -H "Content-Type: application/json" \
  -H "Authorization: bearer $ADMIN_JWT" \
  http://localhost:8091/admin/realms/service-template/roles | jq .

echo foo ${USER_RESOURCE}/role-mappings/realm

curl -H "Content-Type: application/json" \
  -H "Authorization: bearer $ADMIN_JWT" \
  --data '[{"id": "3edfce29-8d27-4879-aa2f-be000209a31f", "name":"service-template-user"}]'  ${USER_RESOURCE}/role-mappings/realm

#  "${USER_RESOURCE}/role-mappings"


export JWT=$(http --form POST   http://localhost:8093/realms/service-template/protocol/openid-connect/token  client_id=service-template username=${USER_ID}  password=foopassword grant_type=password  | jq -r .access_token )

curl -v -H "Authorization: Bearer $JWT" http://localhost:8080/accounts

