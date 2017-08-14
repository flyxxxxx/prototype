package org.prototype.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Data
public class User {

	
	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(length=20,nullable=false)
	private String name;
	
	
	private Dict type;
}
