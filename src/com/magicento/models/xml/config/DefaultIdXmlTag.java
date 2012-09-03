package com.magicento.models.xml.config;

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
public class DefaultIdXmlTag extends IdXmlTag {

    public DefaultIdXmlTag(){

        super();
        help = "You can read the value of this element using Mage::getStoreConfig('NAME_OF_THIS_ELEMENT')";
    }

    @Override
    public List<String> getPossibleNames() {

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

        return new ArrayList<String>(names);
    }

}
