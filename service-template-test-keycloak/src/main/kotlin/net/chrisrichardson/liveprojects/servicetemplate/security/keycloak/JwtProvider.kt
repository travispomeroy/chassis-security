package net.chrisrichardson.liveprojects.servicetemplate.security.keycloak

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.UserRepresentation
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.net.URI

@Component
@ConditionalOnProperty("keycloak.auth-server-url")
class JwtProvider @Autowired constructor(
        @Value("\${keycloak.auth-server-url}") val keycloakUrl: String,
        @Value("\${keycloak.realm}") val realm: String,
        @Value("\${keycloak.resource}") val clientId: String,
        val httpProxy : URI? = null
) {

    val jwts : MutableMap<String, String> = mutableMapOf()
    val ids : MutableMap<String, String> = mutableMapOf()

    fun getUserId(userName: String) : String = ids[userName]!!

    fun getJwt(userName: String, password: String, role: String) : String {
        if (!jwts.containsKey(userName)) {
            ensureUserExists(userName, password, role)

            jwts[userName] = fetchJwt(clientId, userName, password)

        }
        System.out.println("Issued jwt for $userName = ${jwts[userName]}")
        return jwts[userName]!!
    }

    private fun ensureUserExists(userName: String, password: String, role: String) {
        val keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm("master")
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build()

        val realmResource = keycloak.realm(realm)
        val usersResource = realmResource.users()

        val userr = UserRepresentation()
        userr.username = userName
        userr.isEnabled = true

        val createUserResponse = usersResource.create(userr)

        assertThat(createUserResponse.status).isIn(201, 409)

        val userId = usersResource.search(userName)[0].id

        val userResource: UserResource = usersResource.get(userId)

        ids[userName] = userId

        val credr = CredentialRepresentation()
        credr.type = CredentialRepresentation.PASSWORD
        credr.value = password
        credr.isTemporary = false

        userResource.resetPassword(credr);

        val rolesResource = realmResource.roles()

        val serviceTemplateRealmRole: RoleRepresentation = rolesResource.get(role).toRepresentation()
        userResource.roles().realmLevel().add(Arrays.asList(serviceTemplateRealmRole))
    }

    /*


    curl -X POST \
           -d "grant_type=password&username=foo&password=foopassword&client_id=service-template" \
           http://localhost:8091/realms/service-template/protocol/openid-connect/token

     */
    private fun fetchJwt(clientId: String, userName: String, password: String): String {
        var ra = RestAssured.given()
        if (httpProxy != null) {
            ra = ra.proxy(httpProxy)
        }
        System.out.println(keycloakUrl)
        return ra
                .urlEncodingEnabled(true)
                .param("client_id", clientId)
                .param("username", userName)
                .param("password", password)
                .param("grant_type", "password")
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("${keycloakUrl}/realms/${realm}/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .assertThat()
                .body("access_token", CoreMatchers.not(CoreMatchers.nullValue()))
                .extract()
                .path("access_token")
    }

    fun jwtForAuthorizedUser() =
            getJwt(TestUserCredentials.userName, TestUserCredentials.password, "service-template-user")

    fun jwtForOtherAuthorizedUser() =
            getJwt(TestUserCredentials.otherAuthorizedUserName, TestUserCredentials.password, "service-template-user")

    fun jwtForUserInSomeOtherRole() =
            getJwt(TestUserCredentials.userNameOther, TestUserCredentials.passwordOther, "some-other-role")


}


object TestUserCredentials {
    val userName = "foo"
    val password = "foopassword"
    val otherAuthorizedUserName = "fooOtherAuthorized"

    val userNameOther = "fooOther"
    val passwordOther = "foopasswordOther"

}

object RestAssuredUtils {
    fun givenJwt(jwt: String?) = RestAssured.given()
            .header("Authorization", "Bearer ${jwt}")
}
