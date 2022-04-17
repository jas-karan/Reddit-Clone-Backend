package com.reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.reddit.dto.PostRequest;
import com.reddit.dto.PostResponse;
import com.reddit.exception.PostNotFoundException;
import com.reddit.exception.SubredditNotFoundException;
import com.reddit.mapper.PostMapper;
import com.reddit.model.Post;
import com.reddit.model.Subreddit;
import com.reddit.model.User;
import com.reddit.repository.PostRepository;
import com.reddit.repository.SubredditRepository;
import com.reddit.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class PostService {
	
	private final SubredditRepository subredditRepository;
	private final AuthService authService;
	private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
	
	public void save(PostRequest postRequest) {
		Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName())
				.orElseThrow(()-> new SubredditNotFoundException(postRequest.getSubredditName()));
		User currentUser = authService.getCurrentUser();
		
		postRepository.save(postMapper.map(postRequest,subreddit, currentUser));
	}
	
	@Transactional
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id.toString()));
        return postMapper.mapToDto(post);
    }
	
	 @Transactional
	    public List<PostResponse> getAllPosts() {
	        return postRepository.findAll()
	                .stream()
	                .map(postMapper::mapToDto)
	                .collect(Collectors.toList());
	    }
	 
	 @Transactional
	    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
	        Subreddit subreddit = subredditRepository.findById(subredditId)
	                .orElseThrow(() -> new SubredditNotFoundException(subredditId.toString()));
	        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
	        return posts.stream().map(postMapper::mapToDto).collect(Collectors.toList());
	    }

	    @Transactional
	    public List<PostResponse> getPostsByUsername(String username) {
	        User user = userRepository.findByUsername(username)
	                .orElseThrow(() -> new UsernameNotFoundException(username));
	        return postRepository.findByUser(user)
	                .stream()
	                .map(postMapper::mapToDto)
	                .collect(Collectors.toList());
	    }
}
