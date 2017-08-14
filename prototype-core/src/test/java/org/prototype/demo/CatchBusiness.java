package org.prototype.demo;

import org.prototype.annotation.Catch;
import org.prototype.core.Prototype;
import lombok.Getter;

@Prototype
public class CatchBusiness {

	@Getter
	private int value;

	@Catch
	public void execute() {
		throw new java.lang.RuntimeException();
	}
	

	void executeException(UnsupportedOperationException exception) {
		value = -3;
	}

	void executeException(Exception exception) {
		value = -1;
	}

	void executeException(RuntimeException exception) {
		value = -2;
	}
}
