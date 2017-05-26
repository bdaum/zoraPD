package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 */
public class ConstraintException extends Exception {

	/**
     * 
     */
    private static final long serialVersionUID = 6233114900959182414L;

    /**
	 * Constructor for ConstraintException.
	 */
	public ConstraintException() {
		super();
	}

	/**
	 * Constructor for ConstraintException.
	 * @param message - the error message
	 */
	public ConstraintException(String message) {
		super(message);
	}

	/**
	 * Constructor for ConstraintException.
	 * @param message - the error message
	 * @param cause - a nested Throwable instance 
	 */
	public ConstraintException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ConstraintException.
	 * @param cause - a nested Throwable instance
	 */
	public ConstraintException(Throwable cause) {
		super(cause);
	}

}
