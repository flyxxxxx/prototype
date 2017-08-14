package org.prototype.io;

import java.io.IOException;

public interface Output<T> {
	Class<?> getSupportedType();
	T create(Stream stream) throws IOException;
	Object output(T object) throws IOException;
}
