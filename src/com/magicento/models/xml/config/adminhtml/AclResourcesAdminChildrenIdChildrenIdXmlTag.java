package com.magicento.models.xml.config.adminhtml;

import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class AclResourcesAdminChildrenIdChildrenIdXmlTag extends IdXmlTag {

    @Override
    public String getHelp() {
        return "Unique identifier for this admin resource, normally it is some submenu identifier";
    }

    @Override
    public List<String> getPossibleNames()
    {
        XmlTag menuId = getNodeFromContextHierarchy("config/acl/resources/admin/children/*");
        if(menuId != null){
            addAllNamesFrom("config/menu/"+menuId.getName()+"/children/*");
        }

        return new ArrayList<String>(names);
    }

}
