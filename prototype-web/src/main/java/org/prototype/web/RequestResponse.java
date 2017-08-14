package org.prototype.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Data;

/**
 * 请求与响应. <br>
 * 用于输出HTTP请求与响应.
 * 
 * @author lj
 *
 */
@Data
class RequestResponse implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2822262653112705447L;

	/**
	 * id
	 */
	private String id;

	/**
	 * 用时
	 */
	private long useTime;

	/**
	 * 结果对象
	 */
	private Object result;

	private Request request;

	private Response response;

	/**
	 * 构造
	 * 
	 * @param result
	 *            响应结果对象
	 */
	public RequestResponse(Object result) {
		this.result = result;
		HttpPrototypeStatus status = HttpPrototypeStatus.getStatus(HttpPrototypeStatus.class);
		id = status.getId();
		useTime = System.currentTimeMillis() - status.getStartTime();
		request = new Request(status.getRequest());
		response = new Response(status.getResponse());
	}

	@Data
	class Response {

		private String contentType;
		private String characterEncoding;
		private int status;
		private Locale locale;

		Response(HttpServletResponse response) {
			contentType = response.getContentType();
			characterEncoding = response.getCharacterEncoding();
			status = response.getStatus();
			locale = response.getLocale();
		}

	}

	@Data
	class Request {

		private String url;

		private String remoteAddr;

		private Map<String, String> headers = new HashMap<>();
		private Map<String, Object> parameters = new HashMap<>();

		private String remoteHost;

		private String remoteUser;

		private int remotePort;

		private List<Cookie> cookies;

		private String authType;

		private String characterEncoding;

		private String contentType;

		private String contextPath;

		private List<Locale> locales = new ArrayList<>();

		private String method;

		private String protocol;

		private String sessionId;

		Request(HttpServletRequest request) {
			authType = request.getAuthType();
			characterEncoding = request.getCharacterEncoding();
			contentType = request.getContentType();
			contextPath = request.getContextPath();
			Enumeration<Locale> e = request.getLocales();
			while (e.hasMoreElements()) {
				locales.add(e.nextElement());
			}
			method = request.getMethod();
			sessionId = request.getSession(true).getId();
			remoteAddr = request.getRemoteAddr();
			remoteHost = request.getRemoteHost();
			remoteUser = request.getRemoteUser();
			remotePort = request.getRemotePort();
			parseHeaders(request);
			parseParameters(request);
			Cookie[] cks = request.getCookies();
			if (cks == null) {
				cks = new Cookie[0];
			}
			cookies = Arrays.asList();
		}

		private void parseParameters(HttpServletRequest request) {
			Map<String, String[]> map = request.getParameterMap();
			for (Map.Entry<String, String[]> entry : map.entrySet()) {
				String[] parameter = entry.getValue();
				if (parameter.length == 1) {
					parameters.put(entry.getKey(), parameter[0]);
				} else {
					parameters.put(entry.getKey(), Arrays.asList(parameters));
				}
			}
		}

		private void parseHeaders(HttpServletRequest request) {
			Enumeration<String> e = request.getHeaderNames();
			while (e.hasMoreElements()) {
				String name = e.nextElement();
				headers.put(name, request.getHeader(name));
			}
		}
	}

}
