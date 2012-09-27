package com.magicento.models.xml.config;

import com.intellij.openapi.project.Project;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.Magento;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // check for possible errors
        checkConfigXml(configXml);

        return XmlHelper.getXmlStringFromDocument(configXml);

    }

    private void checkConfigXml(Document configXml)
    {
        checkRewriteConflicts(configXml);
        checkMultilineNodes(configXml);
    }

    private void checkMultilineNodes(Document configXml) {
        // TODO: we should check here for invalid nodes like:
        // <product>Namespace_Module_Model_Product
        // </product>
    }

    private void checkRewriteConflicts(Document configXml)
    {
        // with XPpath 2: [name() = following-sibling::*/name() and not(name() = preceding-sibling::*/name())]

        String xpath = "/config/global/*/*/rewrite/*";
        List<Element> rewrites = XmlHelper.findXpath(configXml, xpath);
        if(rewrites != null && rewrites.size() > 0){
            List<String> duplicatedBlocks = new ArrayList<String>();
            List<String> duplicatedModels = new ArrayList<String>();
            List<String> duplicatedHelpers = new ArrayList<String>();
            Map<String, Element> fullPathElements = new HashMap<String, Element>();
            for(Element rewrite : rewrites){
                String type = ((Element)rewrite.getParent().getParent().getParent()).getName();
                String group = ((Element)rewrite.getParent().getParent()).getName();
                String name = rewrite.getName();
                String fullPath = type+" "+group+" "+name;
                if( ! fullPathElements.containsKey(fullPath)){
                    fullPathElements.put(fullPath, rewrite);
                }
                else {
                    String factory = group+"/"+name;
                    if(type.equals("models")){
                        duplicatedModels.add(factory);
                    }
                    else if(type.equals("blocks")){
                        duplicatedBlocks.add(factory);
                    }
                    else if(type.equals("helpers")){
                        duplicatedHelpers.add(factory);
                    }
                }
            }
            if(duplicatedBlocks.size() > 0 || duplicatedHelpers.size() > 0 || duplicatedModels.size() > 0){
                String message = "There are duplicated rewrites for:\n";
                message += (duplicatedBlocks.size() > 0 ? "Blocks:\n"+StringUtils.join(duplicatedBlocks, "\n  ") : "");
                message += (duplicatedModels.size() > 0 ? "Models:\n"+StringUtils.join(duplicatedModels, "\n  ") : "");
                message += (duplicatedHelpers.size() > 0 ? "Helpers:\n"+StringUtils.join(duplicatedHelpers, "\n  ") : "");
                // TODO find a more friendly UI for showing and solving these rewrite errors:
                IdeHelper.showDialog(message, "Warning: duplicate rewrites");
                // IdeHelper.logError(message);
            }
        }
    }

}
