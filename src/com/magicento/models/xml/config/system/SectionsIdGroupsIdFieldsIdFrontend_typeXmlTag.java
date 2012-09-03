package com.magicento.models.xml.config.system;

import com.magicento.models.xml.config.MagentoConfigXmlTag;

import java.util.ArrayList;

/**
 * @author Enrique Piatti
 */
public class SectionsIdGroupsIdFieldsIdFrontend_typeXmlTag extends MagentoConfigXmlTag
{

    public SectionsIdGroupsIdFieldsIdFrontend_typeXmlTag()
    {
        super();
        possibleValues = new ArrayList<String>();
        possibleValues.add("text");
        possibleValues.add("textarea");
        possibleValues.add("select");
        possibleValues.add("multiselect");
        possibleValues.add("password");
        possibleValues.add("time");
        possibleValues.add("image");
        possibleValues.add("allowspecific");
        possibleValues.add("import");
        possibleValues.add("export");
    }

}
