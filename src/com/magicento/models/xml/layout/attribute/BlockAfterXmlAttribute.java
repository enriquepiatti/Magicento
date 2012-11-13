package com.magicento.models.xml.layout.attribute;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.BlockChildXmlTag;
import com.magicento.models.xml.layout.BlockXmlTag;
import com.magicento.models.xml.layout.MagentoLayoutXmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockAfterXmlAttribute extends MagentoLayoutXmlAttribute {

    public BlockAfterXmlAttribute()
    {
        super();
        name = "after";
        help = "Define the position of this block inside the parent. This is normally used when the parent block is core/text_list\n" +
                "The value of this attribute must be a valid block 'name' (attribute).\n"+
                "after=\"-\" is a special command used to position the block at the very bottom of a structural block";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();
        possibleValues.add("-");
        String currentBlockName = getCurrentPsiXmlTag().getName();
        List<Element> siblings = ((BlockChildXmlTag)getXmlTag()).getSiblings();
        for(Element sibling : siblings){
            String siblingName = sibling.getAttributeValue("name");
            if(siblingName != null && ! siblingName.equals(currentBlockName)){
                possibleValues.add(siblingName);
            }
        }
        return super.getPossibleValues();
    }

}
