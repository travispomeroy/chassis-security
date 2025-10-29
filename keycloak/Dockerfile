FROM quay.io/keycloak/keycloak:17.0.1
COPY realm.json /realm.json
RUN /opt/keycloak/bin/kc.sh import --file /realm.json
CMD ["start-dev", "--http-port", "8091"]