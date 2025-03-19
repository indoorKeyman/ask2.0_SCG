package com.example.SCG;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;

import java.util.List;

@Configuration
public class CorsGlobalConfiguration {

//    @Bean
//    public WebFluxConfigurer corsConfigurer() {
//        return new WebFluxConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry corsRegistry) {
//                corsRegistry.addMapping("/api/**")
//                        .allowedOrigins("http://43.201.219.118:3000") // 슬래시 제거
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("*")
//                        .allowCredentials(true);
//            }
//        };
//    }


    @Bean
    public WebFluxConfigurer corsConfigurer() {
        return new WebFluxConfigurerComposite() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/**")
                        .allowedOrigins("http://43.201.219.118:3000") // 프론트엔드 주소
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://43.201.219.118:3000"); // 프론트엔드 주소
        corsConfig.addAllowedHeader("*"); // 모든 헤더 허용
        corsConfig.addAllowedMethod("*"); // 모든 메서드 허용 (GET, POST 등)
        corsConfig.setAllowCredentials(true); // 쿠키/인증 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // 모든 API에 대해 적용
        return new CorsWebFilter(source);
    }




}
