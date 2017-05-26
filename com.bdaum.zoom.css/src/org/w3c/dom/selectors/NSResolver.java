package org.w3c.dom.selectors;

/**
 * http://dev.w3.org/2006/webapi/selectors-api/#nsresolver
 *
 */
public interface NSResolver {

	public String lookupNamespaceURI(String prefix);
	
}
