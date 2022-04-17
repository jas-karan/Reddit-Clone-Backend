package com.reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reddit.dto.SubredditDto;
import com.reddit.exception.RedditException;
import com.reddit.mapper.SubredditMapper;
import com.reddit.model.Subreddit;
import com.reddit.repository.SubredditRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubredditService {
	
	private final SubredditRepository subredditRepository;
	private final SubredditMapper subredditMapper;
	
	@Transactional
	public SubredditDto save(SubredditDto subredditDto) {
		Subreddit subreddit = mapSubredditDto(subredditDto);  //map from dto to subreddit object 
		Subreddit save = subredditRepository.save(subreddit);
		subredditDto.setId(save.getId());
		return subredditDto;
	}
	
	private Subreddit mapSubredditDto(SubredditDto subredditDto) {
		return Subreddit.builder().name(subredditDto.getName())
				.description(subredditDto.getDescription())
				.build();
	}
	
	@Transactional
	public List<SubredditDto> getAll() {
		return subredditRepository.findAll()
							.stream()
							.map(this::mapToDto)
							.collect(Collectors.toList());
	}
	
	private SubredditDto mapToDto(Subreddit subreddit) {
		return SubredditDto.builder().name(subreddit.getName())
								.id(subreddit.getId())
								.numberOfPosts(subreddit.getPosts().size())
								.build();
	}
	
	public SubredditDto getSubreddit(Long id) {
		Subreddit subreddit = subredditRepository.findById(id)
				.orElseThrow(()-> new RedditException("No Subreddit found with id: "+id));
		return subredditMapper.mapSubredditToDto(subreddit);
	}
}
