package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * Copyright (c) 2003
 * 
 * This abstract aspect combines multiple sub-aspects.
 */
public abstract class ComposedOperationAspect
	extends OperationAspect
	implements IComposedAspect {

	protected OperationAspect[] operations;

	/**
	 * Construtor. 
	 * @param operations - the sub-operations to be executed
	 */
	public ComposedOperationAspect(OperationAspect[] operations) {
		this.operations = operations;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IComposedAspect#getChildren()
	 */
	public Aspect[] getChildren() {
		return operations;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IComposedAspect#accept(IComposedAspectVisitor)
	 */
	public void accept(IComposedAspectVisitor visitor) {

		if (visitor.visit(this))
			for (int i = 0; i < operations.length; i++) {
				IAspect childDelta = operations[i];
				if (childDelta instanceof IComposedAspect) {
					((IComposedAspect) childDelta).accept(visitor);
				}
			}
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspect#clone()
	 */
	@Override
	public Object clone() {
		ComposedOperationAspect clone = (ComposedOperationAspect) super.clone();
		clone.operations = new OperationAspect[operations.length];
		for (int i = 0; i < operations.length; i++) {
			OperationAspect op = operations[i];
			clone.operations[i] = (OperationAspect) op.clone();
		}
		return clone;
	}
	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspect#setExtension(Object)
	 */
	@Override
	public void setExtension(Object extension) {
		for (int i = 0; i < operations.length; i++) {
			operations[i].setExtension(extension);
		}
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspect#isStatic()
	 */
	@Override
	public boolean isStatic() {
		for (int i = 0; i < operations.length; i++) {
			OperationAspect member = operations[i];
			if (!member.isStatic())
				return false;
		}
		return true;
	}

}
