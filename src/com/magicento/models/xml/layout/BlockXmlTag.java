package com.magicento.models.xml.layout;

import com.intellij.psi.xml.XmlTag;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.layout.attribute.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockXmlTag extends ReferenceXmlTag
{

    public BlockXmlTag() {
        super();
    }

    public BlockXmlTag(boolean initChildren) {
        super(initChildren);
    }


    @Override
    protected void initAttributes()
    {
        super.initAttributes();

        MagentoXmlAttribute type = new BlockTypeXmlAttribute();

        MagentoXmlAttribute template = new BlockTemplateXmlAttribute();
        MagentoXmlAttribute as = new BlockAsXmlAttribute();
        MagentoXmlAttribute before = new BlockBeforeXmlAttribute();
        MagentoXmlAttribute after = new BlockAfterXmlAttribute();
        MagentoXmlAttribute output = new BlockOutputXmlAttribute();

        addAttribute(type);
        addAttribute(template);
        addAttribute(as);
        addAttribute(before);
        addAttribute(after);
        addAttribute(output);

        // label
        // module
        // parent

    }

    @Override
    protected void initName() {
        name = "block";
    }

    @Override
    protected void initHelp() {
        help = "Define a new Block";
    }

}
