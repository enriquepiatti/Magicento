package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class CrontabJobsIdXmlTag extends IdXmlTag {

    public CrontabJobsIdXmlTag(){

        super();
        help = "Unique identifier for the cronjob";
    }

    @Override
    public List<String> getPossibleNames() {

        names.add("YOUR_CRON_JOB_IDENTIFIER");

        return new ArrayList<String>(names);
    }

}
