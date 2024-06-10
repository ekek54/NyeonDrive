package com.example.nyeondrive.auth;

import java.util.Collection;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);
        String principalClaimValue = jwt.getClaimAsString(JwtClaimNames.SUB);
        return new CustomJwtAuthenticationToken(principalClaimValue, authorities);
    }

    static class CustomJwtAuthenticationToken extends AbstractAuthenticationToken {
        private final UUID userId;

        public CustomJwtAuthenticationToken(String principal, Collection<? extends GrantedAuthority> authorities) {
            //Empty Authorities
            super(authorities);
            this.setAuthenticated(true);
            userId = UUID.fromString(principal);
        }

        @Override
        public Object getCredentials() {
            return userId;
        }

        @Override
        public Object getPrincipal() {
            return userId;
        }
    }
}
