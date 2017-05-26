/*******************************************************************************
 * Copyright (c) 2014 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.lightzone.internal;

public class JpegException extends RuntimeException
{

 	private static final long serialVersionUID = 8884812770288976151L;

	public JpegException(String message)
    {
        super(message);
    }

    public JpegException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JpegException(Throwable cause)
    {
        super(cause);
    }
}
