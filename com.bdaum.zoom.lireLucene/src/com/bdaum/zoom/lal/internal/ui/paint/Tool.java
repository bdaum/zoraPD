/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.lal.internal.ui.paint;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.ToolItem;

public class Tool {
	public int id;
	public String name;
	public String group;
	public int type;
	public Runnable action;
	public Image image = null;
	public Object data;
	public String key;
	public ToolItem item;
	
	public Tool(int id, String key, String group, int type) {
		super();
		this.id = id;
		this.name = PaintExample.getResourceString("PaintExample."+key); //$NON-NLS-1$
		this.group = group;
		this.type = type;
		this.key = key;
	}

	public Tool(int id, String key, String group, int type, Object data) {
		this(id, key, group, type);
		this.data = data;
	}
}
