import com.efarm.efarmbackend.security.jwt.AuthTokenFilter
import com.efarm.efarmbackend.security.jwt.JwtUtils
import com.efarm.efarmbackend.security.services.UserDetailsServiceImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.FilterChain

class AuthTokenFilterSpec extends Specification {

    def jwtUtils = Mock(JwtUtils)
    def userDetailsService = Mock(UserDetailsServiceImpl)
    def filter = new AuthTokenFilter(jwtUtils: jwtUtils, userDetailsService: userDetailsService)
    def request = Mock(HttpServletRequest)
    def response = Mock(HttpServletResponse)
    def filterChain = Mock(FilterChain)

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "should authenticate user if JWT token is valid"() {
        given:
        String jwt = 'valid-token'
        String username = 'user'
        UserDetails userDetails = Mock(UserDetails)
        jwtUtils.getJwtFromCookies(request) >> jwt
        jwtUtils.validateJwtToken(jwt) >> true
        jwtUtils.getUserNameFromJwtToken(jwt) >> username
        userDetailsService.loadUserByUsername(username) >> userDetails

        when:
        filter.doFilterInternal(request, response, filterChain)

        then:
        1 * userDetails.getAuthorities() >> []
        1 * filterChain.doFilter(request, response)
        assert SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken
    }

    def "should not authenticate user if JWT token is invalid"() {
        given:
        jwtUtils.getJwtFromCookies(request) >> 'invalid-token'
        jwtUtils.validateJwtToken(_) >> false

        when:
        filter.doFilterInternal(request, response, filterChain)

        then:
        1 * filterChain.doFilter(request, response)
        assert SecurityContextHolder.getContext().getAuthentication() == null
    }

}
