/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal.lire;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ai.IAiService;

public class AiAlgorithm extends Algorithm {

	/**
	 * @param id - algorithm ID
	 * @param name - algorithm internal name
	 * @param label - algorithm display name
	 * @param description - algorithm description
	 * @param essential - true if algorithm is always shown
	 * @param providerId - ID of AI provider module
	 */
	public AiAlgorithm(int id, String name, String label, String description, boolean essential, String providerId) {
		super(id, name, label, description, essential);
		this.providerId = providerId;
	}

	/* (nicht-Javadoc)
	 * @see com.bdaum.zoom.core.internal.lire.Algorithm#isEnabled()
	 */
	public boolean isEnabled() {
		IAiService aiService = CoreActivator.getDefault().getAiService();
		if (aiService != null) 
			return aiService.isEnabled();
		return false;
	}

	/* (nicht-Javadoc)
	 * @see com.bdaum.zoom.core.internal.lire.Algorithm#isAccountValid()
	 */
	public boolean isAccountValid() {
		IAiService aiService = CoreActivator.getDefault().getAiService();
		if (aiService != null)
			return aiService.isAccountValid(providerId);
		return false;
	}
	
	/* (nicht-Javadoc)
	 * @see com.bdaum.zoom.core.internal.lire.Algorithm#isAi()
	 */
	public boolean isAi() {
		return true;
	}

}
