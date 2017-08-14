package org.prototype.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.prototype.core.PrototypeStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.Getter;

/**
 * HTTP原型状态.
 * 
 * @author lj
 *
 */
public class HttpPrototypeStatus extends PrototypeStatus {

	@Getter
	private HttpServletRequest request;

	@Getter
	private HttpServletResponse response;

	/**
	 * 构造
	 * 
	 * @param request
	 *            请求
	 * @param response
	 *            响应
	 */
	HttpPrototypeStatus() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		request = attributes.getRequest();
		response = attributes.getResponse();
	}

	/**
	 * 获取Locale
	 * 
	 * @return Locale
	 */
	@Override
	public Locale getLocale() {
		return request.getLocale();
	}
}
