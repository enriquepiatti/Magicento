package com.magicento.models.xml.layout.attribute;

import com.magicento.models.xml.MagentoXmlAttribute;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockBeforeXmlAttribute extends BlockAfterXmlAttribute {

    public BlockBeforeXmlAttribute()
    {
        super();
        name = "before";
        help = "Define the position of this block inside the parent. This is normally used when the parent block is core/text_list\n" +
                "The value of this attribute must be a valid block 'name' (attribute).\n"+
                "before=\"-\" is a special command used to position the block at the very top of a structural block";
    }

}
