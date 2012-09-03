package com.magicento.models.xml.config;

import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.Magento;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import com.intellij.openapi.project.Project;
import org.jdom.Document;

import java.io.File;

/**
 * Creates a model for magento config.xml
 * it uses ConfigSkeleton.xml forcreating the schema/hierarchy, every node in the skeleton has a corresponding class
 *
 * @author Enrique Piatti
 */
public class MagentoConfigXml extends MagentoXml {

    public static MagentoXmlType TYPE = MagentoXmlType.CONFIG;

    protected void _init()
    {
        skeletonName = "ConfigSkeleton";
        classNamePrefix = fallbackClassNamePrefix+"config.";
        fallbackClassName = "MagentoConfigXmlTag";
        mergedXmlFilename = "config.xml";
        super._init();
    }

    protected MagentoXmlTag _createRootTag()
    {
        return new ConfigXmlTag();
    }

    @Override
    protected String getMergedXml(Project project)
    {
        // TODO: execute PHP only if user has enabled this feature
        // MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
        // return magicentoProject.executePhpWithMagento("echo Mage::app()->getConfig()->getNode()->asXML();");

        if(project == null){
            return null;
        }

        Document configXml = Magento.getInstance(project).loadModules();
        return XmlHelper.getXmlStringFromDocument(configXml);

    }

}
