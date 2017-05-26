package com.bdaum.zoom.cat.model.meta;

import com.bdaum.zoom.cat.model.Category_typeImpl;
import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset category
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class CategoryImpl extends Category_typeImpl implements Category {

	static final long serialVersionUID = 1523345851L;

	/* ----- Constructors ----- */

	public CategoryImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param label - Property
	 */
	public CategoryImpl(String label) {
		super(label);

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
		attachInstrumentation(_instrumentation, CategoryImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc meta_parent *** */

	private Meta meta_parent;

	/**
	 * Set value of property meta_parent
	 *
	 * @param _value - new field value
	 */
	public void setMeta_parent(Meta _value) {
		meta_parent = _value;
	}

	/**
	 * Get value of property meta_parent
	 *
	 * @return - value of field meta_parent
	 */
	public Meta getMeta_parent() {
		return meta_parent;
	}

	/* *** Incoming Arc category_subCategory_parent *** */

	private Category category_subCategory_parent;

	/**
	 * Set value of property category_subCategory_parent
	 *
	 * @param _value - new field value
	 */
	public void setCategory_subCategory_parent(Category _value) {
		category_subCategory_parent = _value;
	}

	/**
	 * Get value of property category_subCategory_parent
	 *
	 * @return - value of field category_subCategory_parent
	 */
	public Category getCategory_subCategory_parent() {
		return category_subCategory_parent;
	}

	/* *** Arc subCategory *** */

	private AomMap<String, Category> subCategory = new FastHashMap<String, Category>(
			"subCategory", PackageInterface.Category_subCategory, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property subCategory
	 *
	 * @param _value - new element value
	 */
	public void setSubCategory(AomMap<String, Category> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subCategory"));
		subCategory = _value;
		for (Category _element : _value.values()) {
			if (_element != null)
				_element.setCategory_subCategory_parent(this);

		}
	}

	/**
	 * Set value of property subCategory
	 *
	 * @param _value - new element value
	 */
	public void setSubCategory(Map<String, Category> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subCategory"));
		subCategory = new FastHashMap<String, Category>(_value, "subCategory",
				PackageInterface.Category_subCategory, 0, Integer.MAX_VALUE,
				null, null);

		for (Category _element : _value.values()) {
			if (_element != null)
				_element.setCategory_subCategory_parent(this);
		}
	}

	/**
	 * Add an element to map subCategory under key _element.getLabel()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public Category putSubCategory(Category _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "SubCategory._element"));
		_element.setCategory_subCategory_parent(this);

		return (Category) subCategory.put(_element.getLabel(), _element);
	}

	/**
	 * Remove an element from map subCategory
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public Category removeSubCategory(String _key) {
		return (Category) subCategory.remove(_key);
	}

	/**
	 * Make subCategory empty 
	 */
	public void clearSubCategory() {
		subCategory.clear();
	}

	/**
	 * Get value of property subCategory
	 *
	 * @return - value of field subCategory
	 */
	public AomMap<String, Category> getSubCategory() {
		return subCategory;
	}

	/**
	 * Get single element of map subCategory
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key
	 */
	public Category getSubCategory(String _key) {
		return subCategory.get(_key);
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

		return (o instanceof CategoryImpl)
				&& getStringId().equals(((CategoryImpl) o).getStringId());
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

		return getStringId().hashCode();
	}

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	public boolean isKeyIdentical(Object o) {
		if (!(o instanceof Category))
			return false;
		Category other = (Category) o;
		return ((getLabel() == null && other.getLabel() == null) || (getLabel() != null && getLabel()
				.equals(other.getLabel())));
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return ((getLabel() == null) ? 0 : getLabel().hashCode());
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

}
