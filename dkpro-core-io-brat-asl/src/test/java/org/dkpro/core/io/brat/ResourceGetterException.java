package org.dkpro.core.io.brat;

import java.io.IOException;

public class ResourceGetterException extends Exception {

	public ResourceGetterException(Exception e) { super(e); }

	public ResourceGetterException(String mess, IOException e) { super(mess, e); }
}
