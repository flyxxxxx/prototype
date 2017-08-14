package org.prototype.io;

import java.io.InputStream;

public interface Input {
	Class<?> getSupportedType();
	InputStream getInputStream();	
}
