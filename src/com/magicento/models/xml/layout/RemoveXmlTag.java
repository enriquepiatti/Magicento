package com.magicento.models.xml.layout;

import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.attribute.BlockNameXmlAttribute;

/**
 * @author Enrique Piatti
 */
public class RemoveXmlTag extends MagentoLayoutXmlTag {
    @Override
    protected void initChildren() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void initAttributes() {
        MagentoXmlAttribute name = new BlockNameXmlAttribute();
        addAttribute(name);
    }

    @Override
    protected void initName() {
        name = "remove";
    }

    @Override
    protected void initHelp() {
        help = "Remove the block from the layout (you won't be able to reference this block name anymore).\n" +
                "The block to be removed is specified with the name attribute";
    }
}
