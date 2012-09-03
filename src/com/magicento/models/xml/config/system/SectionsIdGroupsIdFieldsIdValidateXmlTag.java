package com.magicento.models.xml.config.system;

import com.magicento.models.xml.config.MagentoConfigXmlTag;

import java.util.ArrayList;

/**
 * @author Enrique Piatti
 */
public class SectionsIdGroupsIdFieldsIdValidateXmlTag extends MagentoConfigXmlTag
{

    public SectionsIdGroupsIdFieldsIdValidateXmlTag()
    {
        super();
        possibleValues = new ArrayList<String>();
        possibleValues.add("IsEmpty");
        possibleValues.add("validate-email");
        possibleValues.add("validate-number");
        possibleValues.add("required-entry");
        possibleValues.add("alidate-date");
        possibleValues.add("validate-select");
        possibleValues.add("validate-digits");
        possibleValues.add("validate-alpha");
        possibleValues.add("validate-alphanum");
        possibleValues.add("validate-zip");
        possibleValues.add("validate-password");
        possibleValues.add("validate-admin-password");
        possibleValues.add("validate-url");
        possibleValues.add("validate-currency-dollar");
        possibleValues.add("validate-phoneLax");
        possibleValues.add("validate-phoneStrict");
        possibleValues.add("validate-street");
        possibleValues.add("validate-code");
        possibleValues.add("validate-fax");
        possibleValues.add("validate-not-negative-number");
        possibleValues.add("validate-greater-than-zero");
        possibleValues.add("validate-zero-or-greater");
        possibleValues.add("validate-css-length");

    }

}
