package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalSalesQuoteItemProduct_attributesIdXmlTag extends IdXmlTag {

    public GlobalSalesQuoteItemProduct_attributesIdXmlTag(){
        super();
        help = "Attribute names of the product entity when the products are loaded inside the items collection";
    }

    @Override
    public List<String> getPossibleNames() {

        names.add("SOME_PRODUCT_ATTRIBUTE_NAME_HERE");
        // addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
