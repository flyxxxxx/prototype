package org.prototype.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 数据词典表
 * @author lj
 *
 */
@Data
@Entity
@Table(name=Dict.TABLE_NAME)
public class Dict {
	public static final String TABLE_NAME="sys_dict";
	
	
	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(length=20,nullable=false)
	private String name;
	
	public Dict(){}
	public Dict(String name){
		this.name=name;
	}
	public Dict(Integer id,String name){
		this.id=id;
		this.name=name;
	}
	
}
