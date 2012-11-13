package com.magicento.models.xml.layout;

import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.attribute.UpdateHandleXmlAttribute;

/**
 * @author Enrique Piatti
 */
public class UpdateXmlTag extends MagentoLayoutXmlTag {


    @Override
    protected void initChildren() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void initAttributes() {
        MagentoXmlAttribute handle = new UpdateHandleXmlAttribute();
        addAttribute(handle);
    }

    @Override
    protected void initName() {
        name = "update";
    }

    @Override
    protected void initHelp() {
        help = "Includes the content of another handle inside this handle.\n" +
                "This element loads an existing layout handle into the current layout handle.\n" +
                "It provides a kind of inheritance of layout handles.\n" +
                "It must have the handle attribute, which defines the handle of the block to be included";
    }
}
