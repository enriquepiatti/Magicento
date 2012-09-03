package com.magicento.models.xml.config.adminhtml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class AclResourcesAdminChildrenIdXmlTag extends IdXmlTag {

    @Override
    public String getHelp() {
        return "Unique identifier for this admin resource, normally it is some <menu> identifier";
    }

    @Override
    public List<String> getPossibleNames()
    {
        addAllNamesFrom("config/menu/*");
        return new ArrayList<String>(names);
    }

}
