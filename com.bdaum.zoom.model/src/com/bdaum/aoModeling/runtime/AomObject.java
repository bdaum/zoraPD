package com.bdaum.aoModeling.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Berthold Daum
 * 
 *         (c) 2002-2010 Berthold Daum
 * 
 *         This is the base class for all asset implementations.
 * 
 */
@SuppressWarnings("serial")
public abstract class AomObject extends IdentifiableObject implements
		AomValueChangedNotifier, IAspectHost {

	/* Fields for operation and constraint processing */

	// Current operation mode
	protected transient int _mode = -1;

	// Operations and Constraints registered for this asset
	protected transient Aspect[] _operations;

	// Aspect registration
	private transient HashMap<Aspect, Aspect> instrumentationMap;

	/* Fields for event management */

	// Event listeners
	private transient ValueChangedListenerList changeListeners;

	/* Methods for operation and constraint processing */

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspectHost#specifyOperationMode(int)
	 */
	public void specifyOperationMode(int mode) {
		this._mode = mode;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspectHost#retrieveOperationMode()
	 */
	public int retrieveOperationMode() {
		return _mode;
	}

	/**
	 * Performs a modeless operation
	 * 
	 * @param opname
	 *            - the name of the operation to be performed
	 * @param sender
	 *            - the origin of the operation
	 * @return true, if the operation succeeds
	 */
	protected boolean performOperation(int opname, Object sender) {
		if (_operations != null) {
			Aspect operation = _operations[opname];
			if (operation != null)
				return operation.run(opname, _mode, operation._extension, this,
						sender);
		}
		return false;
	}

	/**
	 * Adds an aspect instrumentation to an instrumentation list.
	 * 
	 * @param instrumentation
	 *            The list to which a new instrumentation is added.
	 * @param point
	 *            Operation or Constraint index.
	 * @param aspect
	 *            An aspect instance to be added under the specified point.
	 * @param extension
	 *            User defined extension object.
	 */
	protected static void attachInstrumentation(
			List<Instrumentation> instrumentation, int point, Aspect aspect,
			Object extension) {
		instrumentation.add(new Instrumentation(aspect, point, extension));
	}

	/**
	 * Adds an aspect instrumentation to an instrumentation list.
	 * 
	 * @param instrumentation
	 *            The list to which a new instrumentation is added.
	 * @param point
	 *            Operation or Constraint index.
	 * @param aspect
	 *            An aspect instance to be added under the specified point.
	 */
	protected static void attachInstrumentation(
			List<Instrumentation> instrumentation, int point, Aspect aspect) {
		instrumentation.add(new Instrumentation(aspect, point, null));
	}

	/**
	 * Adds several aspect instrumentations to an instrumentation list.
	 * 
	 * @param instrumentation
	 *            The list to which a new instrumentation is added.
	 * @param asset
	 *            The asset class to which the aspect is added.
	 * @param properties
	 *            A Java-Properties map containing a list of points for this
	 *            combination of asset and aspect. Key format: Fully qualified
	 *            aspect class name followed by fully qualified assset class
	 *            name and separated by '@' Value format: Integer point values
	 *            separated by whitespace.
	 * @param aspect
	 *            An aspect instance to be added under the specified point.
	 */
	protected static void attachInstrumentation(
			List<Instrumentation> instrumentation, Class<?> asset,
			Properties properties, Aspect aspect) {
		String key = aspect.getClass().getName() + "@" + asset.getName();
		String points = properties.getProperty(key);
		if (points != null) {
			StringTokenizer st = new StringTokenizer(points);
			while (st.hasMoreTokens()) {
				try {
					instrumentation.add(new Instrumentation(aspect, Integer
							.parseInt(st.nextToken()), null));
				} catch (NumberFormatException e) {
				}
			}
		}
	}

	/**
	 * Activates an instrumentation for this asset.
	 * 
	 * @param instrumentation
	 *            The list of instrumentations. If the list contains multiple
	 *            aspects for a single point, SeqOperationAspects or
	 *            SeqConstraintAspects are created to combine these Aspects.
	 * @param opcount
	 *            - the total number of operations and constraints including the
	 *            init operation (id=0)
	 */

	protected void initializeInstrumentation(
			List<Instrumentation> instrumentation, int opcount) {
		if (instrumentation != null) {
			_operations = new Aspect[opcount];
			Iterator<Instrumentation> it = instrumentation.iterator();
			while (it.hasNext()) {
				Instrumentation instr = it.next();
				Aspect existing = _operations[instr.point];
				if (existing == null)
					_operations[instr.point] = cloneAspect(instr);
				else if (existing instanceof OperationAspect)
					_operations[instr.point] = new SeqOperationAspect(
							new OperationAspect[] { (OperationAspect) existing,
									(OperationAspect) cloneAspect(instr) });
				else
					_operations[instr.point] = new SeqConstraintAspect(
							new ConstraintAspect[] {
									(ConstraintAspect) existing,
									(ConstraintAspect) cloneAspect(instr) });
			}
			performOperation(0, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspectHost#retrieveInstrumentation()
	 */
	public Aspect[] retrieveInstrumentation() {
		return _operations;
	}

	/*
	 * (non-Javadoc) Clones the aspect of a given instrumentation. Clones are
	 * cached to avoid repeated cloning. @param instr - the instrumentation
	 * containing the aspect @return - tbe cloned aspect
	 */
	private Aspect cloneAspect(Instrumentation instr) {
		Aspect clone = null;
		if (instrumentationMap == null)
			instrumentationMap = new HashMap<Aspect, Aspect>();
		else
			clone = instrumentationMap.get(instr.aspect);
		if (clone == null) {
			clone = (Aspect) instr.aspect.clone();
			clone.setExtension(instr.extension);
			instrumentationMap.put(instr.aspect, clone);
		}
		return clone;
	}

	/* Methods for event management */

	/**
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged(AomValueChangedNotifier,
	 *      Object, Object, int, int, Object, ValueChangedEvent)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, int index,
			Object key, ValueChangedEvent wrappedEvent) {
		if (changeListeners != null && changeListeners.size() > 0
				&& !isCyclic(wrappedEvent))
			changeListeners.fireValueChangedEvent(new ValueChangedEvent(source,
					oldValue, newValue, fieldId, index, key, wrappedEvent));
	}

	/*
	 * (non-Javadoc) Checks if an event is cyclic @param wrappedEvent - the
	 * inner event to be checked for cycles @return boolean - true if the
	 * current AomValueChangedNotifier instance is in the event path
	 */
	private boolean isCyclic(ValueChangedEvent wrappedEvent) {
		while (wrappedEvent != null) {
			if (wrappedEvent.getSource() == this)
				return true;
			wrappedEvent = wrappedEvent.getWrappedEvent();
		}
		return false;
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param wrappedEvent
	 *            - an inner event in case of event bubbling or null
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId,
			ValueChangedEvent wrappedEvent) {
		fireValueChanged(source, oldValue, newValue, fieldId, -1, null,
				wrappedEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged
	 * (AomValueChangedNotifier, Object, Object, int, int)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, int index) {
		fireValueChanged(source, oldValue, newValue, fieldId, index, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#fireValueChanged
	 * (com.bdaum.aoModeling.runtime.AomValueChangedNotifier, java.lang.Object,
	 * java.lang.Object, int, java.lang.Object)
	 */
	public void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, Object key) {
		fireValueChanged(source, oldValue, newValue, fieldId, -1, key, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId) {
		fireValueChanged(source, oldValue, newValue, fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			float oldValue, float newValue, int fieldId) {
		fireValueChanged(source, new Float(oldValue), new Float(newValue),
				fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			double oldValue, double newValue, int fieldId) {
		fireValueChanged(source, new Double(oldValue), new Double(newValue),
				fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			byte oldValue, byte newValue, int fieldId) {
		fireValueChanged(source, new Byte(oldValue), new Byte(newValue),
				fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			char oldValue, char newValue, int fieldId) {
		fireValueChanged(source, new Character(oldValue), new Character(
				newValue), fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			short oldValue, short newValue, int fieldId) {
		fireValueChanged(source, new Short(oldValue), new Short(newValue),
				fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			int oldValue, int newValue, int fieldId) {
		fireValueChanged(source, new Integer(oldValue), new Integer(newValue),
				fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			long oldValue, long newValue, int fieldId) {
		fireValueChanged(source, new Long(oldValue), new Long(newValue),
				fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireValueChanged(AomValueChangedNotifier source,
			boolean oldValue, boolean newValue, int fieldId) {
		fireValueChanged(source, Boolean.valueOf(oldValue), Boolean
				.valueOf(newValue), fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId) {
		if (oldValue == newValue)
			return;
		if (oldValue == null || !oldValue.equals(newValue))
			fireValueChanged(source, oldValue, newValue, fieldId, -1, null,
					null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			Object oldValue, Object newValue, int fieldId, int index) {
		if (oldValue != newValue
				&& (oldValue == null || !oldValue.equals(newValue)))
			fireValueChanged(source, oldValue, newValue, fieldId, index, null,
					null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			boolean oldValue, boolean newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, Boolean.valueOf(oldValue), Boolean
					.valueOf(newValue), fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			boolean oldValue, boolean newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, Boolean.valueOf(oldValue), Boolean
					.valueOf(newValue), fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			float oldValue, float newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Float(oldValue), new Float(newValue),
					fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			float oldValue, float newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Float(oldValue), new Float(newValue),
					fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			double oldValue, double newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Double(oldValue),
					new Double(newValue), fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			double oldValue, double newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Double(oldValue),
					new Double(newValue), fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			byte oldValue, byte newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Byte(oldValue), new Byte(newValue),
					fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			byte oldValue, byte newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Byte(oldValue), new Byte(newValue),
					fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			char oldValue, char newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Character(oldValue), new Character(
					newValue), fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			char oldValue, char newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Character(oldValue), new Character(
					newValue), fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			short oldValue, short newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Short(oldValue), new Short(newValue),
					fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			short oldValue, short newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Short(oldValue), new Short(newValue),
					fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			int oldValue, int newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Integer(oldValue), new Integer(
					newValue), fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			int oldValue, int newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Integer(oldValue), new Integer(
					newValue), fieldId, -1, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 * @param index
	 *            - the index of the modified element if in list or array
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			long oldValue, long newValue, int fieldId, int index) {
		if (oldValue != newValue)
			fireValueChanged(source, new Long(oldValue), new Long(newValue),
					fieldId, index, null, null);
	}

	/**
	 * Notifies listeners about a value change if oldValue is unequal new value
	 * 
	 * @param source
	 *            - the origin of the change
	 * @param oldValue
	 *            - the old value
	 * @param newValue
	 *            - the new value
	 * @param fieldId
	 *            - the field id as defined in the asset class
	 */
	protected void fireIfValueChanged(AomValueChangedNotifier source,
			long oldValue, long newValue, int fieldId) {
		if (oldValue != newValue)
			fireValueChanged(source, new Long(oldValue), new Long(newValue),
					fieldId, -1, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addValueChangedListener
	 * (ValueChangedListener)
	 */
	public void addValueChangedListener(ValueChangedListener listener) {
		if (changeListeners == null)
			changeListeners = new ValueChangedListenerList(3);
		changeListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bdaum.aoModeling.runtime.AomValueChangedNotifier#
	 * removeValueChangedListener(ValueChangedListener)
	 */
	public void removeValueChangedListener(ValueChangedListener listener) {
		if (changeListeners != null)
			changeListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId) {
		addOuterChangedListener(source, fieldId, -1, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId, int index) {
		addOuterChangedListener(source, fieldId, index, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			int fieldId, Object key) {
		addOuterChangedListener(source, fieldId, -1, key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bdaum.aoModeling.runtime.AomValueChangedNotifier#addOuterChangedListener
	 * (AomValueChangedNotifier, int, int)
	 */
	public void addOuterChangedListener(AomValueChangedNotifier source,
			final int fieldId, final int index, final Object key) {
		if (isBubbled()) {
			if (changeListeners == null)
				changeListeners = new ValueChangedListenerList(3);
			if (changeListeners.findBubbledValueChangedListener(source) == null)
				changeListeners.append(new BubbledValueChangedListener(source) {
					public void valueChanged(ValueChangedEvent e) {
						owner.fireValueChanged(owner, AomObject.this,
								AomObject.this, fieldId, index, key, e);
					}
				});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bdaum.aoModeling.runtime.AomValueChangedNotifier#
	 * removeOuterChangedListeners(AomValueChangedNotifier)
	 */
	public void removeOuterChangedListeners(AomValueChangedNotifier source) {
		if (changeListeners != null)
			changeListeners.remove(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier#isBubbled()
	 */
	public boolean isBubbled() {
		return false;
	}

	/*
	 * (non-Javadoc) This method must be overriden by concrete implementations.
	 * 
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof AomObject);
	}

	/**
	 * Creates a clone of this object. Registered listeners are not cloned
	 * deeply, they are shared with the original. Registered aspects are not
	 * cloned at all. Instead, the clone is initialized with aspects as a new
	 * object would be.
	 * 
	 * @return the cloned object
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		AomObject clone = (AomObject) super.clone();
		clone.instrumentationMap = null;
		clone._operations = null;
		if (changeListeners != null)
			clone.changeListeners = (ValueChangedListenerList) changeListeners
					.clone();
		return clone;
	}

	/**
	 * Performs an abstract constraint check.
	 * 
	 * @param opname
	 *            - the id of the constrained to be performed
	 */
	protected void checkConstraint(int opname) throws ConstraintException {
		if (_operations != null) {
			Aspect operation = _operations[opname];
			if (operation != null)
				operation.check(opname, operation._extension, this);
		}
	}

	/**
	 * Utility method to compute a hash value from a double.
	 * 
	 * @param value
	 *            - input value
	 * @return int - hash key
	 */
	protected static int computeDoubleHash(double value) {
		long bits = Double.doubleToLongBits(value);
		return (int) (bits ^ (bits >>> 32));
	}

}