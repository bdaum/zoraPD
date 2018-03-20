package com.bdaum.zoom.ui.internal.dialogs;

public class RenamingTemplate {

	String label;
	String content;
	boolean system;

	public RenamingTemplate(String label, String content, boolean system) {
		this.label = label;
		this.content = content;
		this.system = system;
	}

	public String getLabel() {
		return label;
	}

	public String getContent() {
		return content;
	}

	public boolean isSystem() {
		return system;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}