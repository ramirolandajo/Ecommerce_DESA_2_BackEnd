package ar.edu.uade.ecommerce.Security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthenticationFilterTest {
    @org.junit.jupiter.api.BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        // Simula un request sin token
        filter.doFilter(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithAuthentication() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);
        filter.doFilter(request, response, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithHeaderNoBearer() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abcdefg");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        java.lang.reflect.Field jwtUtilField = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(filter, jwtUtil);
        java.lang.reflect.Field udsField = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(filter, uds);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidtoken");
        Mockito.when(jwtUtil.extractUsername("invalidtoken")).thenReturn(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithValidTokenUserNotFound() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        java.lang.reflect.Field jwtUtilField = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(filter, jwtUtil);
        java.lang.reflect.Field udsField = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(filter, uds);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        Mockito.when(jwtUtil.extractUsername("validtoken")).thenReturn("user@email.com");
        Mockito.when(uds.loadUserByUsername("user@email.com")).thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("not found"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithValidTokenUserFoundTokenInvalid() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        java.lang.reflect.Field jwtUtilField = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(filter, jwtUtil);
        java.lang.reflect.Field udsField = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(filter, uds);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        Mockito.when(jwtUtil.extractUsername("validtoken")).thenReturn("user@email.com");
        UserDetails userDetails = User.withUsername("user@email.com").password("1234").roles("USER").build();
        Mockito.when(uds.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        Mockito.when(jwtUtil.validateToken("validtoken", "user@email.com")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithValidTokenUserFoundTokenValid() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        java.lang.reflect.Field jwtUtilField = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(filter, jwtUtil);
        java.lang.reflect.Field udsField = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(filter, uds);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        Mockito.when(jwtUtil.extractUsername("validtoken")).thenReturn("user@email.com");
        UserDetails userDetails = User.withUsername("user@email.com").password("1234").roles("USER").build();
        Mockito.when(uds.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        Mockito.when(jwtUtil.validateToken("validtoken", "user@email.com")).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user@email.com", auth.getName());
    }

    @Test
    void testDoFilterInternalWithValidTokenAndAuthenticationAlreadySet() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        java.lang.reflect.Field jwtUtilField = JwtAuthenticationFilter.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(filter, jwtUtil);
        java.lang.reflect.Field udsField = JwtAuthenticationFilter.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(filter, uds);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        Mockito.when(jwtUtil.extractUsername("validtoken")).thenReturn("user@email.com");
        Authentication auth = new UsernamePasswordAuthenticationToken("user@email.com", "1234");
        SecurityContextHolder.getContext().setAuthentication(auth);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        // Verifica que la autenticación no se sobrescribió
        assertEquals(auth, SecurityContextHolder.getContext().getAuthentication());
    }
}
