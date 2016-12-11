package org.webpieces.example.mock;

import org.webpieces.example.example.SomeLibrary;

public class MockSomeLibrary extends SomeLibrary {

	private RuntimeException runtimeException;

	public void throwException(RuntimeException runtimeException) {
		this.runtimeException = runtimeException;
	}

	@Override
	public void doSomething() {
		if(runtimeException != null)
			throw runtimeException;
	}

}
