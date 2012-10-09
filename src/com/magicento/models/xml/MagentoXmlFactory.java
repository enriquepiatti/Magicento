package com.magicento.models.xml;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.xml.config.MagentoConfigXml;
import com.magicento.models.xml.config.adminhtml.MagentoAdminhtmlXml;
import com.magicento.models.xml.config.system.MagentoSystemXml;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magicento.models.xml.layout.MagentoLayoutXml;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class MagentoXmlFactory {

    protected static Map<Project, Map<MagentoXmlType, MagentoXml>> instance = new HashMap<Project, Map<MagentoXmlType, MagentoXml>>();

    public static MagentoXml getInstance(MagentoXmlType type){
        return getInstance(type, null);
    }

    /**
     *
     * @param type
     * @param project
     * @return MagentoXml or null if the project is null and cannot be guessed
     */
    public static MagentoXml getInstance(MagentoXmlType type, Project project){

        if(project == null){
            project = ProjectUtil.guessCurrentProject(null);
        }
        if(project == null){
            return null;
        }

        Map<MagentoXmlType, MagentoXml> projectInstances = instance.get(project);

        if(projectInstances == null)
        {
            projectInstances = new HashMap<MagentoXmlType, MagentoXml>();
            instance.put(project, projectInstances);
        }

        if(projectInstances.get(type) == null)
        {
            switch(type){
                case CONFIG:
                    projectInstances.put(type, new MagentoConfigXml(project));
                    break;
                case SYSTEM:
                    projectInstances.put(type, new MagentoSystemXml(project));
                    break;
                case ADMINHTML:
                    projectInstances.put(type, new MagentoAdminhtmlXml(project));
                    break;
                case LAYOUT:
                    projectInstances.put(type, new MagentoLayoutXml(project));
                    break;
            }
        }
        return projectInstances.get(type);
    }

    public static MagentoXml getInstance(PsiElement psiElement)
    {
        final PsiFile file = psiElement.getContainingFile().getOriginalFile();
        String fileName = file./*getVirtualFile().*/getName();
        Project project = psiElement.getProject();
        if( fileName.equals("config.xml") ){
            return MagentoXmlFactory.getInstance(MagentoXmlType.CONFIG, project);
        }
        else if( fileName.equals("system.xml")){
            return MagentoXmlFactory.getInstance(MagentoXmlType.SYSTEM, project);
        }
        else if( fileName.equals("adminhtml.xml")){
            return MagentoXmlFactory.getInstance(MagentoXmlType.ADMINHTML, project);
        }
        else if( fileName.endsWith(".xml") && file.getVirtualFile().getPath().contains("app/design/") ){
            MagentoLayoutXml layoutXml = (MagentoLayoutXml)MagentoXmlFactory.getInstance(MagentoXmlType.LAYOUT, project);
            LayoutFile layoutFile = new LayoutFile(file.getVirtualFile());
            layoutXml.setArea(layoutFile.getArea());
            layoutXml.setPackageName(layoutFile.getPackage());
            layoutXml.setTheme(layoutFile.getTheme());
            return layoutXml;
        }
        return null;
    }


}
