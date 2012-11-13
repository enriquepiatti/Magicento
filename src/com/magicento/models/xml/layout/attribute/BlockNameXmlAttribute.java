package com.magicento.models.xml.layout.attribute;

import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.BlockXmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockNameXmlAttribute extends MagentoLayoutXmlAttribute {

    public BlockNameXmlAttribute()
    {
        super();
        name = "name";
        help = "This is a global unique name which identifies this block (you can use this name to reference, remove or override this block)";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();

        String xpath = "//block";
        List<Element> blocks = getAllNodesFromMergedXml(xpath);

        // TODO: filter by handle: show only blocks from the current handle or default handle??

        if(blocks != null){
            for(Element block : blocks){
                String blockName = block.getAttributeValue("name");
                if(blockName != null){
                    possibleValues.add(blockName);
                }
            }
        }

        return super.getPossibleValues();
    }
}
