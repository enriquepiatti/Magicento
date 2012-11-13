package com.magicento.models.xml.layout;

import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.attribute.*;

/**
 * @author Enrique Piatti
 */
public class ActionXmlTag extends BlockChildXmlTag {
    @Override
    protected void initChildren()
    {
        MagentoLayoutXmlTag parameters = new ActionParameterXmlTag();
        addChild(parameters);
    }

    @Override
    protected void initAttributes() {
        MagentoXmlAttribute method = new ActionMethodXmlAttribute();
        addAttribute(method);

        MagentoXmlAttribute ifconfig = new ActionIfconfigXmlAttribute();
        addAttribute(ifconfig);

        MagentoXmlAttribute translate = new ActionTranslateXmlAttribute();
        addAttribute(translate);

        MagentoXmlAttribute module = new ActionModuleXmlAttribute();
        addAttribute(module);

        MagentoXmlAttribute json = new ActionJsonXmlAttribute();
        addAttribute(json);

    }

    @Override
    protected void initName() {
        name = "action";
    }

    @Override
    protected void initHelp() {
        help = "Public method from the parent Block of this node to be executed after the creation of the block";
    }
}
