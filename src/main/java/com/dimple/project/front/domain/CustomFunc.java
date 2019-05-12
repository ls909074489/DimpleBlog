package com.dimple.project.front.domain;

import lombok.Data;

/**
 * 自定义菜单
 * @author 90907
 *
 */
@Data
public class CustomFunc {

	private String funcHref;
	private String funcText;
	
	public CustomFunc() {
	}
	public CustomFunc(String funcHref, String funcText) {
		this.funcHref = funcHref;
		this.funcText = funcText;
	}
	
	
}
