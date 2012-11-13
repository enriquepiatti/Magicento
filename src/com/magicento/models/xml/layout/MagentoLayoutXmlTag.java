package com.magicento.models.xml.layout;

import com.intellij.psi.xml.XmlTag;
import com.magicento.models.xml.MagentoXmlTag;

import java.util.List;

/**
 * @author Enrique Piatti
 */
abstract public class MagentoLayoutXmlTag extends MagentoXmlTag
{
    public MagentoLayoutXmlTag(){
        this(true);
    }

    public MagentoLayoutXmlTag(boolean withChildren){
        super();
        unique = false;
        initName();
        initHelp();
        if( withChildren){
            initChildren();
        }
        initAttributes();
    }

    abstract protected void initChildren();

    abstract protected void initAttributes();

    abstract protected void initName();

    abstract protected void initHelp();

}
