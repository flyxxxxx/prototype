package org.prototype.web;

@interface WebDefine {
	String getHeaders() default "getHeaders";

	// String getLastModified() default "getLastModified";
	String getCookies() default "getCookies";

	String getSessionAttributes() default "getSessionAttributes";
}
