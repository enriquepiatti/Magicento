package com.magicento.models.xml.config.adminhtml;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
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
public class MagentoAdminhtmlXml extends MagentoXml {

    public static MagentoXmlType TYPE = MagentoXmlType.ADMINHTML;

    public MagentoAdminhtmlXml(Project project) {
        super(project);
    }

    protected void _init()
    {
        skeletonName = "AdminhtmlSkeleton";
        classNamePrefix = fallbackClassNamePrefix+"config.adminhtml.";
        mergedXmlFilename = "adminhtml.xml";
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

        // TODO: execute PHP only if user has enabled this feature
//        MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
//        String systemXmlWithPHP =  magicentoProject.executePhpWithMagento("echo Mage::getConfig()->loadModulesConfiguration('adminhtml.xml')->getNode()->asXML();");

        Document adminhtmlXml = new Document(new Element("config"));
        adminhtmlXml = Magento.getInstance(project).loadModulesConfiguration(mergedXmlFilename, adminhtmlXml);
        String adminhtmlXmlString = XmlHelper.getXmlStringFromDocument(adminhtmlXml);

        return adminhtmlXmlString;

    }

}
