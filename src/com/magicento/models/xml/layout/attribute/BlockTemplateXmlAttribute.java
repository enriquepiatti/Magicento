package com.magicento.models.xml.layout.attribute;

import com.intellij.openapi.project.Project;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.FileHelper;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.layout.Template;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.MagentoLayoutXml;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.util.*;

/**
 * @author Enrique Piatti
 */
public class BlockTemplateXmlAttribute extends MagentoLayoutXmlAttribute {

    public BlockTemplateXmlAttribute()
    {
        super();
        name = "template";
        help = "Relative path to the .phtml file to be used for rendering the block. The path must be relative to the 'template' folder.\n" +
                "This attribute is only valid if the block type extends from core/template";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();

        Set<String> allowedPackages = null;
        Set<String> allowedThemes = null;
        Project project = getProject();
        if(project != null){
            MagicentoSettings settings = MagicentoSettings.getInstance(project);
            if(settings != null){
                allowedPackages = new LinkedHashSet<String>(settings.getPackages());
                allowedThemes = new LinkedHashSet<String>(settings.getThemes());
            }
        }

        MagentoLayoutXml layout = ((MagentoLayoutXml)getManager());
        String area = layout.getArea();
        String basePath = layout.getBasePathForArea(area);
        List<String> packages = layout.getAllPackages();
        for(String packageName : packages)
        {
            if(allowedPackages == null || allowedPackages.isEmpty() || allowedPackages.contains(packageName)){
                List<String> themes = layout.getAllThemes(packageName);
                for(String theme : themes){
                    if(allowedThemes == null || allowedThemes.isEmpty() || allowedThemes.contains(theme)){
                        File themeFolder = new File(basePath+"/"+packageName+"/"+theme);
                        List<File> templates = FileHelper.getAllFiles(themeFolder, true);
                        for(File file : templates){
                            Template template = new Template(file);
                            possibleValues.add(template.getRelativePath());
                        }
                    }
                }
            }
        }
        // List<File> templates = .getFileFromAllPackagesAndThemes();
        return super.getPossibleValues();
    }
}
