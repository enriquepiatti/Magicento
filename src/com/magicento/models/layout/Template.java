package com.magicento.models.layout;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.JavaHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.MagentoClassInfo;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author Enrique Piatti
 */
public class Template extends LayoutFile {



    public Template(VirtualFile file) {
        super(file);
    }

    public Template(File file) {
        super(file);
    }


    public boolean isTemplate()
    {
        return file != null && file.exists() && file.isFile() && FileUtil.getExtension(file.getAbsolutePath()).equals("phtml");
    }



    public String getRelativePath()
    {
        String area = getArea();
        String packageName = getPackage();
        String theme = getTheme();
        if(area != null && packageName != null && theme != null){
            String filePath = file.getAbsolutePath().replace("\\", "/");
            String regex = "^.*/app/design/"+area+"/"+packageName+"/"+theme+"/template/(.*)$";
            return JavaHelper.extractFirstCaptureRegex(regex, filePath);
        }
        return null;
    }


    public List<Element> getBlockElements(@NotNull Project project)
    {

        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
        if(magicento != null)
        {
            File layoutFile = magicento.getCachedLayoutXml(this.getArea(), this.getPackage(), this.getTheme());
            if(layoutFile != null && layoutFile.exists())
            {
                //Set<String> blocks = new LinkedHashSet<String>();
                //Set<String> factoriesAdded = new HashSet<String>();
                String templatePath = this.getRelativePath();

                String xpath = "//block[@template='"+templatePath+"']";
                List<Element> blocksElements = XmlHelper.findXpath(layoutFile, xpath);

                if(blocksElements == null){
                    blocksElements = new ArrayList<Element>();
                }

                //regex = "//action/*[.='"+templatePath+"']";
                xpath = "//action[*[.='"+templatePath+"']]";
                //regex = "//*[action/*[.='"+templatePath+"']]";
                List<Element> blocksElementsFromActions = new ArrayList<Element>(); // XmlHelper.findXpath(layoutFile, regex);
                List<Element> actionsWithTemplate = XmlHelper.findXpath(layoutFile, xpath);
                if(actionsWithTemplate != null)
                {
                    int n = actionsWithTemplate.size();
                    for(int i=n-1; i>=0; i--)
                    {
                        Element actionElement = actionsWithTemplate.get(i);
                        String actionMethod = actionElement.getAttributeValue("method");
                        if(actionMethod != null && actionMethod.equals("addItemRender")){
                            for(Object actionParam : actionElement.getChildren()){
                                String paramValue = ((Element)actionParam).getValue();
                                if(MagentoParser.isUri(paramValue)){
                                    blocksElementsFromActions.add((Element)actionParam);
                                }
                            }
                        }
                        else {
                            Element blockElement = actionElement.getParentElement();
                            if(blockElement.getName().equals("reference"))
                            {
                                String blockName = blockElement.getAttributeValue("name");
                                if(blockName != null){
                                    xpath = "//block[@name='"+blockName+"']";
                                    List<Element> referencedBlocks = XmlHelper.findXpath(layoutFile, xpath);
                                    // blocksElementsFromActions.remove(i);
                                    if(referencedBlocks != null){
                                        blocksElementsFromActions.addAll(referencedBlocks);
                                    }
                                }
                            }
                        }

                    }
                }

                blocksElements.addAll(blocksElementsFromActions);
                return blocksElements;
            }
        }
        return null;
    }


    /**
     * get blocks using this template
     * @return
     */
    public List<String> getBlocksClasses(@NotNull Project project)
    {

        List<Element> blockElements = getBlockElements(project);
        if(blockElements != null)
        {
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
            Set<String> blocks = new LinkedHashSet<String>();
            Set<String> factoriesAdded = new HashSet<String>();
            for(Element blockElement : blockElements)
            {
                String type = blockElement.getAttributeValue("type");
                if(type == null || type.isEmpty())
                {
                    type = blockElement.getValue(); // could be a para for the addItemRenderer action
                }
                if(type != null && MagentoParser.isUri(type)){
                    if( ! factoriesAdded.contains(type)){
                        // String className = magicento.getClassNameFromFactory(type);
                        List<MagentoClassInfo> classes = magicento.findBlocksOfFactoryUri(type);
                        if(classes != null){
                            for(MagentoClassInfo classInfo : classes){
                                blocks.add(classInfo.name);
                            }
                        }
                        factoriesAdded.add(type);
                    }

                }
            }

            return new ArrayList<String>(blocks);
        }

        return null;
    }

}
