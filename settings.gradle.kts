rootProject.name = "live-projects-service-template"

include("service-template-util")

include("service-template-test-keycloak")
include("service-template-test-util")
include("service-template-test-data")
include("service-template-test-containers")

include("service-template-domain")
include("service-template-config")
include("service-template-persistence")
include("service-template-web")
include("service-template-web-security")
include("service-template-health-check")
include("service-template-metrics")
include("service-template-distributed-tracing")

include("service-template-main")

