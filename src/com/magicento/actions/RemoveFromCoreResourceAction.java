package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.JavaHelper;

/**
 * @author Enrique Piatti
 */
public class RemoveFromCoreResourceAction extends MagicentoPhpActionAbstract {

    @Override
    public void executeAction()
    {
        VirtualFile currentFile = getVirtualFile();
        if(currentFile != null)
        {
            MagicentoProjectComponent magicento = getMagicentoComponent();
            if(magicento != null)
            {
                String resourceName = getResourceName(currentFile);
                String method = isData(currentFile) ? "setDataVersion" : "setDbVersion";
                String previousVersion = getFromVersion(currentFile);
                String phpCode =
                        "$resName = '"+resourceName+"';" +
                        "$resource = Mage::getResourceSingleton('core/resource');" +
                        "$mainTable = $resource->getMainTable();" +
                        "$conn = $resource->getReadConnection();" +
                        "echo $conn->delete($mainTable, $conn->quoteInto('code=?', $resName));";

                if(previousVersion != null){
                    phpCode = "echo Mage::getResourceSingleton('core/resource')->"+method+"('"+resourceName+"', '"+previousVersion+"');";
                }

                String result = magicento.executePhpWithMagento(phpCode);
                boolean modified = result.equals("1");
                String message = modified ? "Installer was reseted to previous version" : "Error: Installer was not reseted to previous version";
                magicento.showMessage(message,"Magicento - Reset Installer", MagicentoIcons.MAGENTO_ICON_16x16);
            }
            else {
                IdeHelper.showDialog(getProject(), "Couldn't find Magicento component", "Reset Installer Error");
            }

        }
        else {
            IdeHelper.showDialog(getProject(), "Couldn't find current file", "Reset Installer Error");
        }

    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        //final VirtualFile currentFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        VirtualFile currentFile = getVirtualFile();
        return currentFile != null && isInstaller(currentFile);
    }

    private Boolean isInstaller(VirtualFile file)
    {
        String[] names = {
            "mysql4-install-",
            "mysql4-upgrade-",
            "mysql4-data-install-",
            "mysql4-data-upgrade-",
            "upgrade-",
            "install-",
            "data-",
            "data-upgrade-",
        };
        //String fullPath = file.getPath().replace("\\", "/");
        String fileName = file.getName();
        for(String name : names){
            if(fileName.startsWith(name)){
                return true;
            }
        }
        return false;
    }

    protected boolean isData(VirtualFile file)
    {
        String fileName = file.getName();
        return fileName.contains("data-");
    }

    protected boolean isUpgrade(VirtualFile file)
    {
        String fileName = file.getName();
        return fileName.contains("upgrade-");
    }

    protected String getToVersion(VirtualFile file)
    {
        String fileName = file.getName();
        String regex = ".+-([0-9.]+)\\.php";
        return JavaHelper.extractFirstCaptureRegex(regex, fileName);
    }

    protected String getFromVersion(VirtualFile file)
    {
        if( ! isUpgrade(file)){
            return null;
        }
        String fileName = file.getName();
        String regex = "upgrade-([0-9.]+)-.+";
        return JavaHelper.extractFirstCaptureRegex(regex, fileName);
    }


    protected String getResourceName(VirtualFile file)
    {
        // String fullPath = file.getPath().replace("\\", "/");
        return file.getParent().getName();
    }

}
