package com.magicento.models.xml.layout.attribute;

import com.magicento.models.xml.MagentoXmlAttribute;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class BlockOutputXmlAttribute extends MagentoLayoutXmlAttribute {

    public BlockOutputXmlAttribute()
    {
        super();
        name = "output";
        help = "If you define an output value for this block it will be rendered automatically when the layout is rendered.\n" +
                "Normally you will have only one output block for every page.\n" +
                "The value of this attribute must be a public method of the Block class (defined in type attribute), a common value is 'toHtml'.\n" +
                "A layout should have at least one output block. Normally, the root block is the only output block in a layout but there can be multiple output blocks for a single page. In that case, the output of each output block is merged and returned in the response";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();
        possibleValues.add("toHtml");
        return super.getPossibleValues();
    }
}
