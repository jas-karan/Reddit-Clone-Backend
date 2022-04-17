package com.reddit.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.reddit.dto.AuthenticationResponse;
import com.reddit.dto.LoginRequest;
import com.reddit.dto.RegisterRequest;
import com.reddit.exception.RedditException;
import com.reddit.model.NotificationEmail;
import com.reddit.model.User;
import com.reddit.model.VerificationToken;
import com.reddit.repository.UserRepository;
import com.reddit.repository.VerificationTokenRepository;

import lombok.AllArgsConstructor;
import com.reddit.security.JwtProvider;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {
	
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final VerificationTokenRepository verificationTokenRepository;
	private final MailService mailService;
	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);
		userRepository.save(user);
		
		String token = generateVerificationToken(user); 
		
		mailService.sendMail(new NotificationEmail("Please Activate you Account",
				user.getEmail(),"Thank you for signing up to Reddit 2.0, "+
				"please click on the below url to activate your account : "+
				"http://localhost:5000/api/auth/accountVerification/"+token));
		//next thing is to activate this account 
	}
	
	//token generated will be used for verification of account
	//at the time of registering, since user may verify the account
	//after some days, we need to pertain the token in database (VerificationToken)
	private String generateVerificationToken(User user) {
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);
		
		verificationTokenRepository.save(verificationToken);
		return token;
	}
	
	public void verifyAccount(String token) {
		Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
		verificationToken.orElseThrow(() -> new RedditException("Invalid Token"));
		fetchUserAndEnable(verificationToken.get());
	}
	
	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {
		String username  = verificationToken.getUser().getUsername();
		User user = userRepository.findByUsername(username).orElseThrow(()->new RedditException("User: "+ username +"Not found"));
		user.setEnabled(true);
		userRepository.save(user);
	}
	
	//generates the jwt token and use authentication manager
	public AuthenticationResponse login(LoginRequest loginRequest) {
		//authenticate the user using usernamePasswordToken (by authentication manager)
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
																					loginRequest.getPassword())); 
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		String token = jwtProvider.generateToken(authenticate);
		
		//class for authentication response - dto
		return new AuthenticationResponse(token,loginRequest.getUsername());
		
	}
	
	@Transactional
    public User getCurrentUser() {
        Jwt principal = (Jwt) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principal.getSubject())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getSubject()));
    }
	
	 public boolean isLoggedIn() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
	    }
}
