package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalCatalogProductTypeIdXmlTag extends IdXmlTag {

    public GlobalCatalogProductTypeIdXmlTag(){
        super();
        help = "Unique identifier for the product type";
    }

    @Override
    public List<String> getPossibleNames() {

        names.add("YOUR_NEW_PRODUCT_TYPE");
        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
