package com.magicento.models.xml.config.system;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class SectionsIdGroupsIdFieldsIdDependsIdXmlTag extends IdXmlTag
{

    @Override
    public List<String> getPossibleNames() {

        String xpath = "/config/sections/"+getSectionsName()+"/groups/"+getGroupsName()+"/fields/*";
        List<String> nodeNames = getAllNodeNamesFromMergedXml(xpath);
        if(nodeNames != null){
            String fieldName = getFieldName();
            //nodeNames.remove(fieldName);
            for(int i=nodeNames.size()-1; i>=0; i--){
                if(nodeNames.get(i).equals(fieldName)){
                    nodeNames.remove(i);
                    break;
                }
            }
            names.addAll(nodeNames);
        }

        return new ArrayList<String>(names);
    }


}
