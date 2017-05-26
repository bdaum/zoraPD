package org.w3c.dom.selectors;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public interface DocumentSelector {

	public Element querySelector(String selectors);

	public Element querySelector(String selectors, NSResolver nsresolver);

	public NodeList querySelectorAll(String selectors);

	public NodeList querySelectorAll(String selectors, NSResolver nsresolver);
}
