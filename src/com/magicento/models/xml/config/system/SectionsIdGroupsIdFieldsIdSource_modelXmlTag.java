package com.magicento.models.xml.config.system;

import com.magicento.models.xml.config.MagentoConfigXmlTag;

import java.util.ArrayList;

/**
 * @author Enrique Piatti
 */
public class SectionsIdGroupsIdFieldsIdSource_modelXmlTag extends MagentoConfigXmlTag
{

    public SectionsIdGroupsIdFieldsIdSource_modelXmlTag()
    {
        super();
        possibleValues = new ArrayList<String>();
        possibleValues.add("adminhtml/system_config_source_admin_page");
        possibleValues.add("adminhtml/system_config_source_cms_page");
        possibleValues.add("adminhtml/system_config_source_customer_group");
        possibleValues.add("adminhtml/system_config_source_date_short");
        possibleValues.add("adminhtml/system_config_source_email_identity");
        possibleValues.add("adminhtml/system_config_source_email_template");
        possibleValues.add("adminhtml/system_config_source_locale_country");
        possibleValues.add("adminhtml/system_config_source_locale_currency");
        possibleValues.add("adminhtml/system_config_source_locale_timezone");
        possibleValues.add("adminhtml/system_config_source_locale_weekdays");
        possibleValues.add("adminhtml/system_config_source_order_status");
        possibleValues.add("adminhtml/system_config_source_payment_allmethods");
        possibleValues.add("adminhtml/system_config_source_shipping_allmethods");
        possibleValues.add("adminhtml/system_config_source_country");
        possibleValues.add("adminhtml/system_config_source_allregion");
        possibleValues.add("adminhtml/system_config_source_enabledisable");
        possibleValues.add("adminhtml/system_config_source_notoptreq");
        possibleValues.add("adminhtml/system_config_source_yesno");
    }

}
