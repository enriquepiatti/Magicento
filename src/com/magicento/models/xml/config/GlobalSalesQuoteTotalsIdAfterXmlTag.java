package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalSalesQuoteTotalsIdAfterXmlTag extends MagentoConfigXmlTag {

    public GlobalSalesQuoteTotalsIdAfterXmlTag(){
        super();
        help = "Executes this total collector after the total collector defined here";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = getAllNodeNamesFromMergedXml("//global/sales/quote/totals/*");
        return super.getPossibleValues();
    }

}
