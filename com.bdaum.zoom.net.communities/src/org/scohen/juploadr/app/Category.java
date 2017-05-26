package org.scohen.juploadr.app;

import java.util.Comparator;

public class Category {

	protected String id;
	protected String title;

	public Category() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public class TitleComparator implements Comparator<Category> {

		
		public int compare(Category p1, Category p2) {
			return p1.getTitle().compareTo(p2.getTitle());
		}

	}

}