package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalCatalogProductTypeIdAllow_product_typesIdXmlTag extends IdXmlTag {

    public GlobalCatalogProductTypeIdAllow_product_typesIdXmlTag(){
        super();
        help = "Unique identifier for the product type";
    }

    @Override
    public List<String> getPossibleNames() {

        names.add("simple");
        names.add("virtual");

        return new ArrayList<String>(names);
    }

}
