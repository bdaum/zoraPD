package com.bdaum.zoom.cat.model.group;

import com.bdaum.zoom.cat.model.Group_typeImpl;
import java.util.*;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset group
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class GroupImpl extends Group_typeImpl implements Group {

	static final long serialVersionUID = -808417450L;

	/* ----- Constructors ----- */

	public GroupImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param system - Property
	 * @param showLabel - Property
	 * @param labelTemplate - Property
	 * @param fontSize - Property
	 * @param alignment - Property
	 * @param annotations - Property
	 */
	public GroupImpl(String name, boolean system, int showLabel,
			String labelTemplate, int fontSize, int alignment,
			String annotations) {
		super(name, system, showLabel, labelTemplate, fontSize, alignment,
				annotations);

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
		attachInstrumentation(_instrumentation, GroupImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc group_subgroup_parent *** */

	private Group group_subgroup_parent;

	/**
	 * Set value of property group_subgroup_parent
	 *
	 * @param _value - new field value
	 */
	public void setGroup_subgroup_parent(Group _value) {
		group_subgroup_parent = _value;
	}

	/**
	 * Get value of property group_subgroup_parent
	 *
	 * @return - value of field group_subgroup_parent
	 */
	public Group getGroup_subgroup_parent() {
		return group_subgroup_parent;
	}

	/* *** Arc rootCollection *** */

	private AomList<String> rootCollection = new FastArrayList<String>(
			"rootCollection", PackageInterface.Group_rootCollection, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property rootCollection
	 *
	 * @param _value - new element value
	 */
	public void setRootCollection(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "rootCollection"));
		rootCollection = _value;
	}

	/**
	 * Set value of property rootCollection
	 *
	 * @param _value - new element value
	 */
	public void setRootCollection(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "rootCollection"));
		rootCollection = new FastArrayList<String>(_value, "rootCollection",
				PackageInterface.Group_rootCollection, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list rootCollection
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setRootCollection(String _element, int _i) {
		rootCollection.set(_i, _element);
	}

	/**
	 * Add an element to list rootCollection
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addRootCollection(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "RootCollection._element"));

		return rootCollection.add(_element);
	}

	/**
	 * Remove an element from list rootCollection
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeRootCollection(String _element) {
		return rootCollection.remove(_element);
	}

	/**
	 * Make rootCollection empty 
	 */
	public void clearRootCollection() {
		rootCollection.clear();
	}

	/**
	 * Get value of property rootCollection
	 *
	 * @return - value of field rootCollection
	 */
	public AomList<String> getRootCollection() {
		return rootCollection;
	}

	/**
	 * Get single element of list rootCollection
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list rootCollection
	 */
	public String getRootCollection(int _i) {
		return rootCollection.get(_i);
	}

	/* *** Arc slideshow *** */

	private AomList<String> slideshow = new FastArrayList<String>("slideshow",
			PackageInterface.Group_slideshow, 0, Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property slideshow
	 *
	 * @param _value - new element value
	 */
	public void setSlideshow(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "slideshow"));
		slideshow = _value;
	}

	/**
	 * Set value of property slideshow
	 *
	 * @param _value - new element value
	 */
	public void setSlideshow(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "slideshow"));
		slideshow = new FastArrayList<String>(_value, "slideshow",
				PackageInterface.Group_slideshow, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Set single element of list slideshow
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setSlideshow(String _element, int _i) {
		slideshow.set(_i, _element);
	}

	/**
	 * Add an element to list slideshow
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSlideshow(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Slideshow._element"));

		return slideshow.add(_element);
	}

	/**
	 * Remove an element from list slideshow
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSlideshow(String _element) {
		return slideshow.remove(_element);
	}

	/**
	 * Make slideshow empty 
	 */
	public void clearSlideshow() {
		slideshow.clear();
	}

	/**
	 * Get value of property slideshow
	 *
	 * @return - value of field slideshow
	 */
	public AomList<String> getSlideshow() {
		return slideshow;
	}

	/**
	 * Get single element of list slideshow
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list slideshow
	 */
	public String getSlideshow(int _i) {
		return slideshow.get(_i);
	}

	/* *** Arc exhibition *** */

	private AomList<String> exhibition = new FastArrayList<String>(
			"exhibition", PackageInterface.Group_exhibition, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property exhibition
	 *
	 * @param _value - new element value
	 */
	public void setExhibition(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "exhibition"));
		exhibition = _value;
	}

	/**
	 * Set value of property exhibition
	 *
	 * @param _value - new element value
	 */
	public void setExhibition(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "exhibition"));
		exhibition = new FastArrayList<String>(_value, "exhibition",
				PackageInterface.Group_exhibition, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Set single element of list exhibition
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setExhibition(String _element, int _i) {
		exhibition.set(_i, _element);
	}

	/**
	 * Add an element to list exhibition
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addExhibition(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Exhibition._element"));

		return exhibition.add(_element);
	}

	/**
	 * Remove an element from list exhibition
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeExhibition(String _element) {
		return exhibition.remove(_element);
	}

	/**
	 * Make exhibition empty 
	 */
	public void clearExhibition() {
		exhibition.clear();
	}

	/**
	 * Get value of property exhibition
	 *
	 * @return - value of field exhibition
	 */
	public AomList<String> getExhibition() {
		return exhibition;
	}

	/**
	 * Get single element of list exhibition
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list exhibition
	 */
	public String getExhibition(int _i) {
		return exhibition.get(_i);
	}

	/* *** Arc webGallery *** */

	private AomList<String> webGallery = new FastArrayList<String>(
			"webGallery", PackageInterface.Group_webGallery, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property webGallery
	 *
	 * @param _value - new element value
	 */
	public void setWebGallery(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "webGallery"));
		webGallery = _value;
	}

	/**
	 * Set value of property webGallery
	 *
	 * @param _value - new element value
	 */
	public void setWebGallery(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "webGallery"));
		webGallery = new FastArrayList<String>(_value, "webGallery",
				PackageInterface.Group_webGallery, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Set single element of list webGallery
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setWebGallery(String _element, int _i) {
		webGallery.set(_i, _element);
	}

	/**
	 * Add an element to list webGallery
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addWebGallery(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "WebGallery._element"));

		return webGallery.add(_element);
	}

	/**
	 * Remove an element from list webGallery
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeWebGallery(String _element) {
		return webGallery.remove(_element);
	}

	/**
	 * Make webGallery empty 
	 */
	public void clearWebGallery() {
		webGallery.clear();
	}

	/**
	 * Get value of property webGallery
	 *
	 * @return - value of field webGallery
	 */
	public AomList<String> getWebGallery() {
		return webGallery;
	}

	/**
	 * Get single element of list webGallery
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list webGallery
	 */
	public String getWebGallery(int _i) {
		return webGallery.get(_i);
	}

	/* *** Arc subgroup *** */

	private AomList<Group> subgroup = new FastArrayList<Group>("subgroup",
			PackageInterface.Group_subgroup, 0, Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property subgroup
	 *
	 * @param _value - new element value
	 */
	public void setSubgroup(AomList<Group> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subgroup"));
		subgroup = _value;
		for (Group _element : _value) {
			if (_element != null)
				_element.setGroup_subgroup_parent(this);

		}
	}

	/**
	 * Set value of property subgroup
	 *
	 * @param _value - new element value
	 */
	public void setSubgroup(Collection<Group> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subgroup"));
		subgroup = new FastArrayList<Group>(_value, "subgroup",
				PackageInterface.Group_subgroup, 0, Integer.MAX_VALUE, null,
				null);

		for (Group _element : _value) {
			if (_element != null)
				_element.setGroup_subgroup_parent(this);
		}
	}

	/**
	 * Set single element of list subgroup
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setSubgroup(Group _element, int _i) {
		if (_element != null)
			_element.setGroup_subgroup_parent(this);
		subgroup.set(_i, _element);
	}

	/**
	 * Add an element to list subgroup
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSubgroup(Group _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Subgroup._element"));
		_element.setGroup_subgroup_parent(this);

		return subgroup.add(_element);
	}

	/**
	 * Remove an element from list subgroup
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSubgroup(Group _element) {
		return subgroup.remove(_element);
	}

	/**
	 * Make subgroup empty 
	 */
	public void clearSubgroup() {
		subgroup.clear();
	}

	/**
	 * Get value of property subgroup
	 *
	 * @return - value of field subgroup
	 */
	public AomList<Group> getSubgroup() {
		return subgroup;
	}

	/**
	 * Get single element of list subgroup
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list subgroup
	 */
	public Group getSubgroup(int _i) {
		return subgroup.get(_i);
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

		if (!(o instanceof Group) || !super.equals(o))
			return false;
		Group other = (Group) o;
		return ((getRootCollection() == null && other.getRootCollection() == null) || (getRootCollection() != null && getRootCollection()
				.equals(other.getRootCollection())))

				&& ((getSlideshow() == null && other.getSlideshow() == null) || (getSlideshow() != null && getSlideshow()
						.equals(other.getSlideshow())))

				&& ((getExhibition() == null && other.getExhibition() == null) || (getExhibition() != null && getExhibition()
						.equals(other.getExhibition())))

				&& ((getWebGallery() == null && other.getWebGallery() == null) || (getWebGallery() != null && getWebGallery()
						.equals(other.getWebGallery())))

				&& ((getSubgroup() == null && other.getSubgroup() == null) || (getSubgroup() != null && getSubgroup()
						.equals(other.getSubgroup())))

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
				+ ((getRootCollection() == null) ? 0 : getRootCollection()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getSlideshow() == null) ? 0 : getSlideshow().hashCode());

		hashCode = 31 * hashCode
				+ ((getExhibition() == null) ? 0 : getExhibition().hashCode());

		hashCode = 31 * hashCode
				+ ((getWebGallery() == null) ? 0 : getWebGallery().hashCode());

		hashCode = 31 * hashCode
				+ ((getSubgroup() == null) ? 0 : getSubgroup().hashCode());

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
