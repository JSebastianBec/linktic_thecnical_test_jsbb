package com.jsbb.productservice.config;

import com.jsbb.productservice.util.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = List.of(
            ApiConstants.SWAGGER_UI_PATH,
            ApiConstants.API_DOCS_PATH,
            ApiConstants.ACTUATOR_PATH
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${security.api-key}")
    private String apiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(ApiConstants.API_KEY_HEADER);
        if (header == null || !header.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(ApiConstants.UNAUTHORIZED_BODY);
            return;
        }
        chain.doFilter(request, response);
    }
}
