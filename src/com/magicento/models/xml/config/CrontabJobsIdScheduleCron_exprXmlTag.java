package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class CrontabJobsIdScheduleCron_exprXmlTag extends MagentoConfigXmlTag {

    public CrontabJobsIdScheduleCron_exprXmlTag(){
        super();
        help = "Crontab value";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("0 0 * * *");

        return super.getPossibleValues();
    }
}
