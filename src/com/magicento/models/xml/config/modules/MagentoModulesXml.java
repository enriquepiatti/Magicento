package com.magicento.models.xml.config.modules;

import com.intellij.openapi.project.Project;
import com.magicento.helpers.Magento;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import com.magicento.models.xml.config.ConfigXmlTag;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Enrique Piatti
 */
public class MagentoModulesXml extends MagentoXml {

    public static MagentoXmlType TYPE = MagentoXmlType.MODULES;

    public MagentoModulesXml(Project project) {
        super(project);
    }

    protected void _init()
    {
        skeletonName = "ModulesSkeleton";
        classNamePrefix = fallbackClassNamePrefix+"config.modules.";
        mergedXmlFilename = "modules.xml";
        //fallbackClassName = "MagentoSystemXmlTag";
        super._init();

        getMergedXmlFile();
    }

    protected MagentoXmlTag _createRootTag()
    {
        // system.xml is merged inside <config> node too, it was separated just for maintenance reasons
        return new ConfigXmlTag();
    }

    @Override
    protected String getMergedXml()
    {

        if(project == null){
            return null;
        }

        Document modulesXml = new Document(new Element("config"));
        modulesXml = Magento.getInstance(project).loadModulesConfiguration(mergedXmlFilename, modulesXml);
        String modulesXmlString = XmlHelper.getXmlStringFromDocument(modulesXml);

        return modulesXmlString;

    }

}
