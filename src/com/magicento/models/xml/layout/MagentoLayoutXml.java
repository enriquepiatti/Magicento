package com.magicento.models.xml.layout;

import com.intellij.openapi.project.Project;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class MagentoLayoutXml extends MagentoXml {

    public static MagentoXmlType TYPE = MagentoXmlType.LAYOUT;

    protected String area = "frontend";
    protected String packageName = "base";
    protected String theme = "default";

    private enum MagentoDesignType
    {
        LAYOUT, TEMPLATE, SKIN, LOCALE;
    }

    public MagentoLayoutXml(Project project) {
        super(project);
    }


    protected void _init()
    {
        skeletonName = "LayoutSkeleton";
        classNamePrefix = fallbackClassNamePrefix+"layout.";
        fallbackClassName = "MagentoLayoutXmlTag";
        mergedXmlFilename = "layout.xml";
        super._init();
    }

    @Override
    protected MagentoXmlTag _createRootTag()
    {
        return new LayoutXmlTag();
    }

    public String getMergedXmlFilename()
    {
        return "layout_"+area+"_"+packageName+"_"+theme+".xml";
    }


    @Override
    protected String getMergedXml()
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null)
        {
            if(settings.phpEnabled)
            {
                String phpCode = "echo '<layout>' . Mage::app()->getLayout()->getUpdate()" +
                        "->getFileLayoutUpdatesXml('"+area+"', '"+packageName+"', '"+theme+"')" +
                        "->innerXml() . '</layout>';";

                MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
                return magicentoProject.executePhpWithMagento(phpCode);
            }
            else
            {
                IdeHelper.showDialog(project, "This feature is supported only with PHP Enabled at the moment.\n" +
                        "Please enable PHP going to File > Settings > Magicento", "Magicento Layout Error");
            }
        }

        return null;
    }


    protected String getFileLayoutUpdatesXml(String area, String packageName, String theme/*, int storeId*/)
    {
        // $updatesRoot = Mage::app()->getConfig()->getNode($area.'/layout/updates');
        MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
        if(magicentoProject != null){
            File configXmlFile = magicentoProject.getCachedConfigXml();
            if(configXmlFile != null && configXmlFile.exists()){
                String xpath = "//"+area+"/layout/updates/*";
                List<Element> updates = XmlHelper.findXpath(configXmlFile, xpath);
                List<String> updateFiles = new ArrayList<String>();
                for(Element update : updates)
                {
                    Element fileNode = update.getChild("file");
                    if(fileNode != null){
                        updateFiles.add(fileNode.getValue());
                    }
                }
                updateFiles.add("local.xml");
                for(String fileName : updateFiles)
                {
                    File file = getFileWithFallback(fileName, MagentoDesignType.LAYOUT);
                    // TODO: merge layout xml files
                }
            }
        }
        return null;
    }

    protected File getFileWithFallback(String relativeFilePath, MagentoDesignType type/*, String area, String packageName, String theme*/)
    {
        // TODO: deisgn fallback system
        return null;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

}
