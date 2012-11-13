package com.magicento.models.xml.layout;

import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.attribute.BlockNameXmlAttribute;

/**
 * @author Enrique Piatti
 */
public class ReferenceXmlTag extends BlockChildXmlTag {


    public ReferenceXmlTag(boolean initChildren) {
        super(initChildren);
    }

    public ReferenceXmlTag() {
        super();
    }

    @Override
    protected void initChildren() {

        MagentoLayoutXmlTag block = new BlockXmlTag(false);
        MagentoLayoutXmlTag action = new ActionXmlTag();
        addChild(block);
        addChild(action);

    }

    @Override
    protected void initAttributes() {

        MagentoXmlAttribute name = new BlockNameXmlAttribute();
        addAttribute(name);
    }

    @Override
    protected void initName() {
        name = "reference";
    }

    @Override
    protected void initHelp() {
        help = "Use this node for referencing some block defined in another place.\n" +
                "This element is used to link an already defined block in any layout XML.\n" +
                "To add any child block to an existing block, to modify attributes of an existing block or to perform any action on an existing block, the reference element is used to link to the existing block.\n" +
                "The reference element must have a name attribute which refers to the existing block’s name.";
    }
}
