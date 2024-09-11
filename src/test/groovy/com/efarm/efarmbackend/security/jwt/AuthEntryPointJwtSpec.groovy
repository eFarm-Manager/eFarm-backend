import com.efarm.efarmbackend.security.jwt.AuthEntryPointJwt
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import spock.lang.Specification

class AuthEntryPointJwtSpec extends Specification {

    def "should send unauthorized error on authentication failure"() {
        given:
        def authEntryPointJwt = new AuthEntryPointJwt()
        def response = Mock(HttpServletResponse)
        def request = Mock(HttpServletRequest)
        def authException = Mock(AuthenticationException)

        when:
        authEntryPointJwt.commence(request, response, authException)

        then:
        1 * response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized")
    }
}
