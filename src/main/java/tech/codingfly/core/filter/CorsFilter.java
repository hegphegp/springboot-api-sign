package tech.codingfly.core.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class CorsFilter extends OncePerRequestFilter {

	private static Logger logger = LoggerFactory.getLogger(CorsFilter.class);

	private String allowOrigin;

	private String[] allowMethods;

	private String allowHeaders;

	private long maxAge;

	public CorsFilter(String allowOrigin, String[] allowMethods, String allowHeaders, long maxAge) {
		this.allowOrigin = allowOrigin;
		this.allowMethods = allowMethods;
		this.allowHeaders = allowHeaders;
		this.maxAge = maxAge;
	}

	private CorsProcessor processor = new DefaultCorsProcessor();

	private CorsConfiguration config;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (CorsUtils.isCorsRequest(request)) {
			CorsConfiguration corsConfiguration = getCorsConfiguration(request);
			if (corsConfiguration != null) {
				boolean isValid = this.processor.processRequest(corsConfiguration, request, response);
				if (!isValid || CorsUtils.isPreFlightRequest(request)) {
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	protected CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
		if (config == null) {
			config = new CorsConfiguration();
			config.setAllowCredentials(true);
			config.addAllowedOrigin(allowOrigin); 
			config.addAllowedHeader(allowHeaders);
			config.setAllowedMethods(Arrays.asList(allowMethods));
			config.setMaxAge(maxAge);
		}

		return config;
	}
}