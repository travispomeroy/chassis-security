echo Copy a script to the keycloak container to perform an export
docker cp keycloak-export-docker.sh live-projects-skeleton_keycloak_1:/tmp/keycloak-export-docker.sh
echo Execute the script inside of the container
docker exec -it live-projects-skeleton_keycloak_1 bash -c '/tmp/keycloak-export-docker.sh'
echo Grab the finished export from the container
docker cp live-projects-skeleton_keycloak_1:/tmp/realms-export-single-file.json .
