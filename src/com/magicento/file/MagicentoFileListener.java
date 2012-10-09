package com.magicento.file;

import com.magicento.helpers.Magento;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.config.MagentoConfigXml;
import com.magicento.models.xml.config.adminhtml.MagentoAdminhtmlXml;
import com.magicento.models.xml.config.system.MagentoSystemXml;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.magicento.models.xml.layout.MagentoLayoutXml;

/**
 * @author Enrique Piatti
 */
public class MagicentoFileListener extends VirtualFileAdapter {

    protected Project _project;

    public MagicentoFileListener(Project project){
       _project = project;
    }

    @Override
    public void contentsChanged(VirtualFileEvent event) {
        // the project is disposed sometimes here, I don't know why
        if(_project != null && ! _project.isDisposed())
        {
            if(isConfigXml(event)){
                // if any config.xml changes clear the merged and cached config.xml
                MagentoXmlFactory.getInstance(MagentoConfigXml.TYPE, _project).invalidateCache();
            }
            else if(isSystemXml(event)){
                MagentoXmlFactory.getInstance(MagentoSystemXml.TYPE, _project).invalidateCache();
            }
            else if(isAdminhtmlXml(event)){
                MagentoXmlFactory.getInstance(MagentoAdminhtmlXml.TYPE, _project).invalidateCache();
            }
            else if(isLayoutXml(event)){
                MagentoXmlFactory.getInstance(MagentoLayoutXml.TYPE, _project).invalidateCache();
            }
            else if(isModuleXml(event)){
                Magento.getInstance(_project).invalidateDeclaredModulesCache();
                MagentoXmlFactory.getInstance(MagentoConfigXml.TYPE, _project).invalidateCache();
                MagentoXmlFactory.getInstance(MagentoSystemXml.TYPE, _project).invalidateCache();
                MagentoXmlFactory.getInstance(MagentoAdminhtmlXml.TYPE, _project).invalidateCache();
                MagentoXmlFactory.getInstance(MagentoLayoutXml.TYPE, _project).invalidateCache();
            }
        }
    }


    public void fileCreated(VirtualFileEvent event){

    }

    public void fileDeleted(VirtualFileEvent event){
    }

    protected boolean isConfigXml(VirtualFileEvent event)
    {
        if(event.getFileName().equals("config.xml")){
            //if(event.getParent() instanceof VirtualDirectoryImpl)
            if(event.getParent() != null && event.getParent().getName().equals("etc")){
                return true;
            }
        }
        return false;
    }

    protected boolean isAdminhtmlXml(VirtualFileEvent event)
    {
        if(event.getFileName().equals("adminhtml.xml")){
            //if(event.getParent() instanceof VirtualDirectoryImpl)
            if(event.getParent() != null && event.getParent().getName().equals("etc")){
                return true;
            }
        }
        return false;
    }

    protected boolean isSystemXml(VirtualFileEvent event)
    {
        if(event.getFileName().equals("system.xml")){
            //if(event.getParent() instanceof VirtualDirectoryImpl)
            if(event.getParent() != null && event.getParent().getName().equals("etc")){
                return true;
            }
        }
        return false;
    }

    protected boolean isModuleXml(VirtualFileEvent event)
    {
        if(event.getFileName().endsWith(".xml") && event.getFile().getPath().contains("/app/etc/modules/")){
            return true;
        }
        return false;
    }

    protected boolean isLayoutXml(VirtualFileEvent event)
    {
        if(event.getFileName().endsWith(".xml") && event.getFile().getPath().contains("/app/design/")){
            return true;
        }
        return false;
    }
}
