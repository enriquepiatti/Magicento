package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class CrontabJobsIdScheduleConfig_pathXmlTag extends MagentoConfigXmlTag {

    public CrontabJobsIdScheduleConfig_pathXmlTag(){
        super();
        help = "You can define a system config path here, and it will be used for reading the cron schedule expression (ie: 0 0 * * *)";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {

        possibleValues = new ArrayList<String>();

        return super.getPossibleValues();
    }
}
