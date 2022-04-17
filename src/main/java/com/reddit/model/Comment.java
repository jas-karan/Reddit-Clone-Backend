package com.reddit.model;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Comment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotEmpty
	private String text;
	
	//many comments on one post
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="postId",referencedColumnName="postId")
	private Post post;
	
	private Instant createdDate;
	
	//many comments by one user (on any post)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="userId",referencedColumnName="userId")
	private User user;
}
