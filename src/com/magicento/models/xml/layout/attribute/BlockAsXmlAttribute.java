package com.magicento.models.xml.layout.attribute;

import com.magicento.models.xml.MagentoXmlAttribute;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockAsXmlAttribute extends MagentoLayoutXmlAttribute {

    public BlockAsXmlAttribute()
    {
        super();
        name = "as";
        help = "This is a local alias (valid only inside the parent of this block) for the name of the block.\n" +
                "This must be unique inside the parent block.\n" +
                "You can use this for referencing to this block inside its parent (for example with getChildHtml([ALIAS_HERE]).";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();
        return super.getPossibleValues();
    }
}
