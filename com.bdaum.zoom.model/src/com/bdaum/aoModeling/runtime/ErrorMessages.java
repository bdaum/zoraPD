/*
 * Created on 04.05.2003
 * 
 * (c) 2003, Berthold Daum
 *
 */
package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * This class defines the error messages of the AOM runtime system
 */
public class ErrorMessages {
	
	public static final String ARGUMENT_MAXOCCURS = "Argument.maxOccurs";
	public static final String ARGUMENT_MINOCCURS = "Argument.minOccurs";
	public static final String ARGUMENT_NOT_NULL = "Argument.not_null";
	public static final String ARGUMENT_VIOLATES_ENUMERATION = "Argument.violates_enumeration";
	public static final String ARGUMENT_VIOLATES_ENUMERATION_RANGE = "Argument.violates_enumeration_range";
	public static final String ARGUMENT_INVALID_TYPEUNION = "Argument.invalid_type_union";
	public static final String ARGUMENT_TYPE_VIOLATION = "Argument.type_violation";
	public static final String CONSTRAINT_VIOLATED = "Constraint.violated";
	public static final String ARGUMENT_NOT_A_STRING = "Argument.not_a_string";
	public static final String ARGUMENT_NONXML_CHARACTERS = "Argument.nonXML_characters";
	public static final String ARGUMENT_INVALID_TYPE_PATTERN = "Argument.invalid_type_pattern";
	public static final String ARGUMENT_INVALID_CHOICE = "Argument.invalid_choice";
	public static final String OPERATIONASPECT_ILLEGALCALL = "OperationAspect.illegal_call";
	public static final String SECURITYASPECT_ILLEGALCALL = "SecurityAspect.illegal_call";
	public static final String GUIASPECT_ILLEGALCALL = "GuiAspect.illegal_call";
	public static final String CONSTRAINTASPECT_ILLEGALCALL = "ConstraintAspect.illegal_call";

	public static final String ILLEGAL_XMLATTRIBUTE = "Parser.illegal_xmlAttribute";
	public static final String START_TAG_EXPECTED = "Parser.startTag_expected";
	public static final String END_TAG_EXPECTED = "Parser.endTag_expected";
	public static final String TAG_NOT_CLOSED = "Parser.tag_not_closed";
	public static final String ILLEGAL_TAG = "Parser.illegal_tag";
	
	public static final String START_DOCUMENT_EXPECTED = "Parser.start_document_expected";
	public static final String END_DOCUMENT_EXPECTED = "Parser.end_document_expected";
	public static final String MISSING_ATTRIBUTE = "Parser.missing_attribute";
	public static final String MISSING_PROPERTY = "Parser.missing_property";
	public static final String EMPTY_MODELGROUP = "Parser.empty_modelgroup";
	
	public static final String TEXT_EXPECTED = "Parser.text_expected";
	public static final String BAD_INTEGER = "Parser.bad_integer";
	public static final String BAD_BOOLEAN = "Parser.bad_boolean";
	public static final String BAD_FLOAT = "Parser.bad_float";
	public static final String BAD_DECIMAL = "Parser.bad_decimal";
	public static final String BAD_CHARACTER = "Parser.bad_character";
	public static final String BAD_URI = "Parser.bad_uri";
	public static final String BAD_CURRENCY = "Parser.bad_currency";
	public static final String BAD_DATATYPE_SPECIFICATION = "Parser.bad_datatype_specification";
	public static final String BAD_XSDTYPE = "Parser.bad_xsdtype";
	public static final String MINOCC_VIOLATED = "Parser.minocc_violated";
	public static final String BAD_FIXED_VALUE = "Parser.bad_fixed_value";
	public static final String BAD_GENERIC = "Parser.bad_generic";
	public static final String BAD_NAMESPACE = "Parser.bad_namespace";
}
