package org.prototype.io;

import java.io.IOException;

public interface Translator<S,T> {
	Class<S> getSourceType();
	Class<T> getTargetType();
	void transfer(S source,T target,Stream stream) throws IOException;
}
