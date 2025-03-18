package com.example.SCG.component;

import com.example.SCG.JWT.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JWTUtil jwtUtil;

    public JwtAuthGlobalFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    // 제외할 경로 리스트
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/login",
            "/members/sign_up",
            "/members/login",
            "/reissue",
            "/logout",
            "/jobs/list/**",
            "/community/questions/list",
            "/community/answer/list"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // 제외 경로에 포함되어 있다면 필터를 건너뜀
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // 헤더에서 access 토큰 추출
        String accessToken = exchange.getRequest().getHeaders().getFirst("access");

        if (accessToken == null) {
            return unauthorizedResponse(exchange, "Missing access token");
        }

        try {
            // 토큰 만료 여부 확인
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            return unauthorizedResponse(exchange, "Access token expired");
        }

        // 토큰이 access인지 확인
        String category = jwtUtil.getCategory(accessToken);
        if (!"access".equals(category)) {
            return unauthorizedResponse(exchange, "Invalid access token");
        }

        // 토큰에서 username과 role 추출
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        // 요청 헤더에 username과 role 추가 (서비스로 전달하기 위해)
        exchange.getRequest().mutate()
                .header("username", username)
                .header("role", role)
                .build();

        return chain.filter(exchange);
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(("{\"error\": \"" + message + "\"}").getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -1; // 우선순위 최상위
    }
}
