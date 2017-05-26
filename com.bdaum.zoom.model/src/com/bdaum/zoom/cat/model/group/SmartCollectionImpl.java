package com.bdaum.zoom.cat.model.group;

import com.bdaum.zoom.cat.model.SmartCollection_typeImpl;
import java.util.*;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset smartCollection
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SmartCollectionImpl extends SmartCollection_typeImpl implements
		SmartCollection {

	static final long serialVersionUID = -1565798882L;

	/* ----- Constructors ----- */

	public SmartCollectionImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param system - Property
	 * @param album - Property
	 * @param adhoc - Property
	 * @param network - Property
	 * @param description - Property
	 * @param colorCode - Property
	 * @param lastAccessDate - Property
	 * @param generation - Property
	 * @param perspective - Property
	 * @param postProcessor - Arc
	 */
	public SmartCollectionImpl(String name, boolean system, boolean album,
			boolean adhoc, boolean network, String description, int colorCode,
			Date lastAccessDate, int generation, String perspective,
			PostProcessor postProcessor) {
		super(name, system, album, adhoc, network, description, colorCode,
				lastAccessDate, generation, perspective);
		this.postProcessor = postProcessor;

	}

	/* ----- Initialisation ----- */

	private static List<Instrumentation> _instrumentation = new ArrayList<Instrumentation>();

	public static void attachInstrumentation(int point, Aspect aspect,
			Object extension) {
		attachInstrumentation(_instrumentation, point, aspect, extension);
	}

	public static void attachInstrumentation(int point, Aspect aspect) {
		attachInstrumentation(_instrumentation, point, aspect);
	}

	public static void attachInstrumentation(Properties properties,
			Aspect aspect) {
		attachInstrumentation(_instrumentation, SmartCollectionImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc region_album_parent *** */

	private String region_album_parent;

	/**
	 * Set value of property region_album_parent
	 *
	 * @param _value - new field value
	 */
	public void setRegion_album_parent(String _value) {
		region_album_parent = _value;
	}

	/**
	 * Get value of property region_album_parent
	 *
	 * @return - value of field region_album_parent
	 */
	public String getRegion_album_parent() {
		return region_album_parent;
	}

	/* *** Incoming Arc smartCollection_subSelection_parent *** */

	private SmartCollection smartCollection_subSelection_parent;

	/**
	 * Set value of property smartCollection_subSelection_parent
	 *
	 * @param _value - new field value
	 */
	public void setSmartCollection_subSelection_parent(SmartCollection _value) {
		smartCollection_subSelection_parent = _value;
	}

	/**
	 * Get value of property smartCollection_subSelection_parent
	 *
	 * @return - value of field smartCollection_subSelection_parent
	 */
	public SmartCollection getSmartCollection_subSelection_parent() {
		return smartCollection_subSelection_parent;
	}

	/* *** Incoming Arc group_rootCollection_parent *** */

	private String group_rootCollection_parent;

	/**
	 * Set value of property group_rootCollection_parent
	 *
	 * @param _value - new field value
	 */
	public void setGroup_rootCollection_parent(String _value) {
		group_rootCollection_parent = _value;
	}

	/**
	 * Get value of property group_rootCollection_parent
	 *
	 * @return - value of field group_rootCollection_parent
	 */
	public String getGroup_rootCollection_parent() {
		return group_rootCollection_parent;
	}

	/* *** Arc subSelection *** */

	private AomList<SmartCollection> subSelection = new FastArrayList<SmartCollection>(
			"subSelection", PackageInterface.SmartCollection_subSelection, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property subSelection
	 *
	 * @param _value - new element value
	 */
	public void setSubSelection(AomList<SmartCollection> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subSelection"));
		subSelection = _value;
		for (SmartCollection _element : _value) {
			if (_element != null)
				_element.setSmartCollection_subSelection_parent(this);

		}
	}

	/**
	 * Set value of property subSelection
	 *
	 * @param _value - new element value
	 */
	public void setSubSelection(Collection<SmartCollection> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subSelection"));
		subSelection = new FastArrayList<SmartCollection>(_value,
				"subSelection", PackageInterface.SmartCollection_subSelection,
				0, Integer.MAX_VALUE, null, null);

		for (SmartCollection _element : _value) {
			if (_element != null)
				_element.setSmartCollection_subSelection_parent(this);
		}
	}

	/**
	 * Set single element of list subSelection
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setSubSelection(SmartCollection _element, int _i) {
		if (_element != null)
			_element.setSmartCollection_subSelection_parent(this);
		subSelection.set(_i, _element);
	}

	/**
	 * Add an element to list subSelection
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSubSelection(SmartCollection _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "SubSelection._element"));
		_element.setSmartCollection_subSelection_parent(this);

		return subSelection.add(_element);
	}

	/**
	 * Remove an element from list subSelection
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSubSelection(SmartCollection _element) {
		return subSelection.remove(_element);
	}

	/**
	 * Make subSelection empty 
	 */
	public void clearSubSelection() {
		subSelection.clear();
	}

	/**
	 * Get value of property subSelection
	 *
	 * @return - value of field subSelection
	 */
	public AomList<SmartCollection> getSubSelection() {
		return subSelection;
	}

	/**
	 * Get single element of list subSelection
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list subSelection
	 */
	public SmartCollection getSubSelection(int _i) {
		return subSelection.get(_i);
	}

	/* *** Arc criterion *** */

	private AomList<Criterion> criterion = new FastArrayList<Criterion>(
			"criterion", PackageInterface.SmartCollection_criterion, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property criterion
	 *
	 * @param _value - new element value
	 */
	public void setCriterion(AomList<Criterion> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "criterion"));
		criterion = _value;
		for (Criterion _element : _value) {
			if (_element != null)
				_element.setSmartCollection_parent(this);

		}
	}

	/**
	 * Set value of property criterion
	 *
	 * @param _value - new element value
	 */
	public void setCriterion(Collection<Criterion> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "criterion"));
		criterion = new FastArrayList<Criterion>(_value, "criterion",
				PackageInterface.SmartCollection_criterion, 0,
				Integer.MAX_VALUE, null, null);

		for (Criterion _element : _value) {
			if (_element != null)
				_element.setSmartCollection_parent(this);
		}
	}

	/**
	 * Set single element of list criterion
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setCriterion(Criterion _element, int _i) {
		if (_element != null)
			_element.setSmartCollection_parent(this);
		criterion.set(_i, _element);
	}

	/**
	 * Add an element to list criterion
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addCriterion(Criterion _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Criterion._element"));
		_element.setSmartCollection_parent(this);

		return criterion.add(_element);
	}

	/**
	 * Remove an element from list criterion
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeCriterion(Criterion _element) {
		return criterion.remove(_element);
	}

	/**
	 * Make criterion empty 
	 */
	public void clearCriterion() {
		criterion.clear();
	}

	/**
	 * Get value of property criterion
	 *
	 * @return - value of field criterion
	 */
	public AomList<Criterion> getCriterion() {
		return criterion;
	}

	/**
	 * Get single element of list criterion
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list criterion
	 */
	public Criterion getCriterion(int _i) {
		return criterion.get(_i);
	}

	/* *** Arc sortCriterion *** */

	private AomList<SortCriterion> sortCriterion = new FastArrayList<SortCriterion>(
			"sortCriterion", PackageInterface.SmartCollection_sortCriterion, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property sortCriterion
	 *
	 * @param _value - new element value
	 */
	public void setSortCriterion(AomList<SortCriterion> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "sortCriterion"));
		sortCriterion = _value;
		for (SortCriterion _element : _value) {
			if (_element != null)
				_element.setSmartCollection_parent(this);

		}
	}

	/**
	 * Set value of property sortCriterion
	 *
	 * @param _value - new element value
	 */
	public void setSortCriterion(Collection<SortCriterion> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "sortCriterion"));
		sortCriterion = new FastArrayList<SortCriterion>(_value,
				"sortCriterion",
				PackageInterface.SmartCollection_sortCriterion, 0,
				Integer.MAX_VALUE, null, null);

		for (SortCriterion _element : _value) {
			if (_element != null)
				_element.setSmartCollection_parent(this);
		}
	}

	/**
	 * Set single element of list sortCriterion
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setSortCriterion(SortCriterion _element, int _i) {
		if (_element != null)
			_element.setSmartCollection_parent(this);
		sortCriterion.set(_i, _element);
	}

	/**
	 * Add an element to list sortCriterion
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSortCriterion(SortCriterion _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "SortCriterion._element"));
		_element.setSmartCollection_parent(this);

		return sortCriterion.add(_element);
	}

	/**
	 * Remove an element from list sortCriterion
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSortCriterion(SortCriterion _element) {
		return sortCriterion.remove(_element);
	}

	/**
	 * Make sortCriterion empty 
	 */
	public void clearSortCriterion() {
		sortCriterion.clear();
	}

	/**
	 * Get value of property sortCriterion
	 *
	 * @return - value of field sortCriterion
	 */
	public AomList<SortCriterion> getSortCriterion() {
		return sortCriterion;
	}

	/**
	 * Get single element of list sortCriterion
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list sortCriterion
	 */
	public SortCriterion getSortCriterion(int _i) {
		return sortCriterion.get(_i);
	}

	/* *** Arc postProcessor *** */

	private PostProcessor postProcessor;

	/**
	 * Set value of property postProcessor
	 *
	 * @param _value - new field value
	 */
	public void setPostProcessor(PostProcessor _value) {
		if (_value != null)
			_value.setSmartCollection_parent(this);
		postProcessor = _value;
	}

	/**
	 * Get value of property postProcessor
	 *
	 * @return - value of field postProcessor
	 */
	public PostProcessor getPostProcessor() {
		return postProcessor;
	}

	/* *** Arc asset *** */

	private AomList<String> asset = new FastArrayList<String>("asset",
			PackageInterface.SmartCollection_asset, 0, Integer.MAX_VALUE, null,
			null);

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = _value;
	}

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = new FastArrayList<String>(_value, "asset",
				PackageInterface.SmartCollection_asset, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list asset
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setAsset(String _element, int _i) {
		asset.set(_i, _element);
	}

	/**
	 * Add an element to list asset
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addAsset(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Asset._element"));

		return asset.add(_element);
	}

	/**
	 * Remove an element from list asset
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeAsset(String _element) {
		return asset.remove(_element);
	}

	/**
	 * Make asset empty 
	 */
	public void clearAsset() {
		asset.clear();
	}

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public AomList<String> getAsset() {
		return asset;
	}

	/**
	 * Get single element of list asset
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list asset
	 */
	public String getAsset(int _i) {
		return asset.get(_i);
	}

	/* ----- Equality and Identity ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof SmartCollection) || !super.equals(o))
			return false;
		SmartCollection other = (SmartCollection) o;
		return ((getSubSelection() == null && other.getSubSelection() == null) || (getSubSelection() != null && getSubSelection()
				.equals(other.getSubSelection())))

				&& ((getCriterion() == null && other.getCriterion() == null) || (getCriterion() != null && getCriterion()
						.equals(other.getCriterion())))

				&& ((getSortCriterion() == null && other.getSortCriterion() == null) || (getSortCriterion() != null && getSortCriterion()
						.equals(other.getSortCriterion())))

				&& ((getPostProcessor() == null && other.getPostProcessor() == null) || (getPostProcessor() != null && getPostProcessor()
						.equals(other.getPostProcessor())))

		;
	}

	/**
	 * Returns the hash code for this object.
	 * @return the hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.Object#equals(Object)
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {

		int hashCode = super.hashCode()
				* 31
				+ ((getSubSelection() == null) ? 0 : getSubSelection()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getCriterion() == null) ? 0 : getCriterion().hashCode());

		hashCode = 31
				* hashCode
				+ ((getSortCriterion() == null) ? 0 : getSortCriterion()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getPostProcessor() == null) ? 0 : getPostProcessor()
						.hashCode());

		return hashCode;
	}

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	public boolean isKeyIdentical(Object o) {
		return this == o;
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return hashCode();
	}

	/**
	 * Creates a clone of this object.

	 *   Not supported in this class
	 * @throws CloneNotSupportedException;
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException {

		super.validateCompleteness();
	}

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		validateCompleteness();
	}

	@Override
	public String toString() {
		return getStringId();
	}

}
