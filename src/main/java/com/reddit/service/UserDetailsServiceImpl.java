package com.reddit.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.reddit.model.User;
import com.reddit.repository.UserRepository;

import lombok.AllArgsConstructor;

//core part of user authentication
//will be used by authentication manager to see if user exist

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) {
		Optional<User> userOptional = userRepository.findByUsername(username);
		User user = userOptional.orElseThrow(()->new UsernameNotFoundException("No username found "+
						"with username: "+username));
		
		return new org.springframework
				.security.core.userdetails.User(user.getUsername(), user.getPassword(),
						user.isEnabled(),true,true,true,getAuthorities("USER"));
		//this user is different from user model
		
	}
	
	private Collection<? extends GrantedAuthority> getAuthorities(String role){
		return Collections.singletonList(new SimpleGrantedAuthority(role));
	}
}
