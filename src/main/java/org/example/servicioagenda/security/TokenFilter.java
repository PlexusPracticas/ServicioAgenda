package org.example.servicioagenda.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.servicioagenda.service.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public TokenFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("TOKEN");

        //nos indica que el TOKEN obligatorio
        if (token == null || token.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "TOKEN obligatorio");
            return;
        }

        //valida el token usando auth/decrypt
        try {
            tokenService.decrypt(token);
        } catch(Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "TOKEN inválido");
            return;
        }

        //nos permite continuar si el token es válido
        filterChain.doFilter(request, response);
    }
    }