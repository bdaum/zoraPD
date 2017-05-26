/**
 * 
 */
package com.bdaum.zoom.job;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Controls the concurrent execution of jobs
 * Jobs with the equal operation type are prohibited from running concurrently
 * Jobs with conflicting operation profile are prohibited from running concurrently
 * Profiles conflict when their ANDed bitmasks are not zero. 
 */
public class ProfiledSchedulingRule implements ISchedulingRule {


	private final Object operationType;
	private final int profile;

	/**
	 * Constructor
	 * @param operationType - operation type
	 * @param profile - job profile
	 */
	public ProfiledSchedulingRule(Object operationType, int profile) {
		this.operationType = operationType;
		this.profile = profile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	
	public boolean isConflicting(ISchedulingRule rule) {
		// reflexive
		if (rule == this)
			return true;
		// No conflict with other jobs
		if (!(rule instanceof ProfiledSchedulingRule))
			return false;
		ProfiledSchedulingRule profiledSchedulingRule = (ProfiledSchedulingRule) rule;
		// Not the same operation at the same time
		if (profiledSchedulingRule.getOperationType().equals(
				getOperationType()))
			return true;
		// Profiles must not match
		return ((profiledSchedulingRule.getProfile() & getProfile()) != 0);
	}

	private Object getOperationType() {
		return operationType;
	}

	/**
	 * Returns job profile
	 * @return job profile
	 */
	public int getProfile() {
		return profile;
	}

}