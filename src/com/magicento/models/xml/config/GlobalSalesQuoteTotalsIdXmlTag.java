package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalSalesQuoteTotalsIdXmlTag extends IdXmlTag {

    public GlobalSalesQuoteTotalsIdXmlTag(){
        super();
        help = "Unique identifier for the product type";
    }

    @Override
    public List<String> getPossibleNames() {

        names.add("YOUR_NEW_TOTAL");
        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
