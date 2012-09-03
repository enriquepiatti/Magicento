package com.magicento.models.xml.config.system;

import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class IdXmlTag extends com.magicento.models.xml.config.IdXmlTag {


    @Override
    public List<String> getPossibleNames() {
        //names = new ArrayList<String>();
        names.add("your_custom_identifier_here");
        return new ArrayList<String>(names);
    }

    protected String getFieldName()
    {
        XmlTag id = getNodeFromContextHierarchy("config/sections/*/groups/*/fields/*");
        if(id != null){
            return id.getName();
        }
        return "";
    }

    protected String getGroupsName()
    {
        XmlTag id = getNodeFromContextHierarchy("config/sections/*/groups/*");
        if(id != null){
            return id.getName();
        }
        return "";
    }

    protected String getSectionsName()
    {
        XmlTag id = getNodeFromContextHierarchy("config/sections/*");
        if(id != null){
            return id.getName();
        }
        return "";
    }

}
