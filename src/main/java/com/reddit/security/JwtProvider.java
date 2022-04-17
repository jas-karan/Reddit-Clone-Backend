// ------ NIMBUS JOSE+JWT -------------
package com.reddit.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtProvider {
	
	private final JwtEncoder jwtEncoder;
	
	@Value("${jwt.expiration.time}")
	private Long jwtExpirationInMillis;
	
	public String generateToken(Authentication authentication) {
		//authentication object is received from authentication manager
		//after authenticating the user
		//authentication manager uses a different User object than our user model
		User principal = (User) authentication.getPrincipal();
		
		return generateTokenWithUsername(principal.getUsername());
	}
	
	public String generateTokenWithUsername(String username) {
		//compose the JWT claims set
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("self")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusMillis(jwtExpirationInMillis))
				.subject(username)
				.claim("scope","ROLE_USER")
				.build();
		
		return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
	
}
