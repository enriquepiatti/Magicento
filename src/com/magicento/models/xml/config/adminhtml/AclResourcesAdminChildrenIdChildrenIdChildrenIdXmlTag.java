package com.magicento.models.xml.config.adminhtml;

import com.intellij.psi.xml.XmlTag;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class AclResourcesAdminChildrenIdChildrenIdChildrenIdXmlTag extends IdXmlTag {

    @Override
    public String getHelp() {
        return "Unique identifier for this admin resource, if the parents of this element are system > config, then " +
                "the value of this element must be some <section> identifier defined in any system.xml";
    }

    @Override
    public List<String> getPossibleNames()
    {
        XmlTag menuId = getNodeFromContextHierarchy("config/acl/resources/admin/children/*");
        if(menuId != null){
            XmlTag submenuId = getNodeFromContextHierarchy("config/acl/resources/admin/children/*/children/*");
            if(submenuId != null){
                String menu = menuId.getName();
                String submenu = submenuId.getName();
                if(menu.equals("system") && submenu.equals("config")){
                    MagentoXml magentoXml = MagentoXmlFactory.getInstance(MagentoXmlType.SYSTEM, getProject());
                    if(magentoXml != null){
                        List<String> path = new ArrayList<String>();
                        path.add("config");
                        path.add("sections");
                        path.add("*");
                        MagentoXmlTag matchedTag = magentoXml.getMatchedTag(path);
                        if(matchedTag != null){
                           matchedTag.setContext(getContext());
                           names.addAll(matchedTag.getPossibleNames());
                        }
                    }
                }
                else{
                    addAllNamesFrom("config/menu/"+menu+"/children/"+submenu+"/children/*");
                }
            }
        }

        return new ArrayList<String>(names);
    }

}
