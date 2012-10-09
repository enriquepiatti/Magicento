package com.magicento.models.xml.config.system;

import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.Magento;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import com.magicento.models.xml.config.ConfigXmlTag;
import com.intellij.openapi.project.Project;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;

/**
 * @author Enrique Piatti
 */
public class MagentoSystemXml extends MagentoXml {

    public static MagentoXmlType TYPE = MagentoXmlType.SYSTEM;

    public MagentoSystemXml(Project project) {
        super(project);
    }

    protected void _init()
    {
        skeletonName = "SystemSkeleton";
        classNamePrefix = fallbackClassNamePrefix+"config.system.";
        mergedXmlFilename = "system.xml";
        //fallbackClassName = "MagentoSystemXmlTag";
        super._init();
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

        // TODO: execute PHP only if user has enabled this feature
//        MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
//        String systemXmlWithPHP =  magicentoProject.executePhpWithMagento("echo Mage::getConfig()->loadModulesConfiguration('system.xml')->getNode()->asXML();");

        Document systemXml = new Document(new Element("config"));
        systemXml = Magento.getInstance(project).loadModulesConfiguration(mergedXmlFilename, systemXml);
        String systemXmlString = XmlHelper.getXmlStringFromDocument(systemXml);

        return systemXmlString;

    }

}
