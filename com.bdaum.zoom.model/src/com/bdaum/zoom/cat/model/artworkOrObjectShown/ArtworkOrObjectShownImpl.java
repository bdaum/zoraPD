package com.bdaum.zoom.cat.model.artworkOrObjectShown;

import java.util.*;
import com.bdaum.zoom.cat.model.ArtworkOrObjectShown_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset artworkOrObjectShown
 */

/* !! This class is not intended to be modified manually !! */

//Modified manually: no backpointers and other overhead

@SuppressWarnings({ "unused" })
public class ArtworkOrObjectShownImpl extends ArtworkOrObjectShown_typeImpl
		implements ArtworkOrObjectShown {

	static final long serialVersionUID = -1895024506L;

	/* ----- Constructors ----- */

	public ArtworkOrObjectShownImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param artworkOrObject - Arc
	 * @param asset - Arc
	 */
	public ArtworkOrObjectShownImpl(String artworkOrObject, String asset) {
		super();
		this.artworkOrObject = artworkOrObject;
		this.asset = asset;

	}

	/* ----- Fields ----- */

	/* *** Arc artworkOrObject *** */

	private String artworkOrObject = AomConstants.INIT_String;

	/**
	 * Set value of property artworkOrObject
	 *
	 * @param _value - new field value
	 */
	public void setArtworkOrObject(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "artworkOrObject"));
		artworkOrObject = _value;
	}

	/**
	 * Get value of property artworkOrObject
	 *
	 * @return - value of field artworkOrObject
	 */
	public String getArtworkOrObject() {
		return artworkOrObject;
	}

	/* *** Arc asset *** */

	private String asset = AomConstants.INIT_String;

	/**
	 * Set value of property asset
	 *
	 * @param _value - new field value
	 */
	public void setAsset(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = _value;
	}

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public String getAsset() {
		return asset;
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

		if (!(o instanceof ArtworkOrObjectShown) || !super.equals(o))
			return false;
		ArtworkOrObjectShown other = (ArtworkOrObjectShown) o;
		return ((getArtworkOrObject() == null && other.getArtworkOrObject() == null) || (getArtworkOrObject() != null && getArtworkOrObject()
				.equals(other.getArtworkOrObject())))

				&& ((getAsset() == null && other.getAsset() == null) || (getAsset() != null && getAsset()
						.equals(other.getAsset())))

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
				+ ((getArtworkOrObject() == null) ? 0 : getArtworkOrObject()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getAsset() == null) ? 0 : getAsset().hashCode());

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

	@Override
	public void validate() throws ConstraintException {
		// do nothing
	}


}
