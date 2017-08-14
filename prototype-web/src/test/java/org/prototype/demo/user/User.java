package org.prototype.demo.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="common_user")
public class User {
	
	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(length=20,nullable=false)
	private String name;

}
