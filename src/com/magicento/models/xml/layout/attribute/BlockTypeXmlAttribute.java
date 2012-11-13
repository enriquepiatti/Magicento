package com.magicento.models.xml.layout.attribute;

import com.intellij.psi.PsiElement;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.MagentoXmlTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockTypeXmlAttribute  extends MagentoLayoutXmlAttribute {

    public BlockTypeXmlAttribute()
    {
        super();
        name = "type";
        help = "Uri for the Block class to be instantiated. This instance of this Block class will be $this inside the template of this block";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();
        MagentoXmlTag parentTag = getXmlTag();
        if(parentTag != null)
        {
            String prefix = getPrefix();
            if(prefix.isEmpty()){
                possibleValues.add("Type at least one character to get block uri autocomplete");
            }
            else {
                String uri = prefix+"*";
                MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(parentTag.getProject());
                List<MagentoClassInfo> classes = magicento.findBlocksOfFactoryUri(uri);
                if(classes != null){
                    for(MagentoClassInfo info : classes)
                    {
                        possibleValues.add(info.getUri());
                    }
                }
            }
        }
        return super.getPossibleValues();
    }
}
