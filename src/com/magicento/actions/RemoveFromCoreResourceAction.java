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
                boolean isData = isData(currentFile);
                String method = isData ? "DataVersion" : "DbVersion";
                String previousVersion = getFromVersion(currentFile);
                String phpCode = "Mage::getResourceSingleton('core/resource')->set"+method+"('"+resourceName+"', '"+previousVersion+"');" +
                        "echo Mage::getResourceSingleton('core/resource')->get"+method+"('"+resourceName+"');";

                boolean isUpgrade = previousVersion != null;
                if( ! isUpgrade){

                    phpCode =
                            "$resName = '"+resourceName+"';" +
                                    "$resource = Mage::getResourceSingleton('core/resource');" +
                                    "$mainTable = $resource->getMainTable();" +
                                    "$conn = $resource->getReadConnection();" +
                                    "echo $conn->delete($mainTable, $conn->quoteInto('code=?', $resName));";

                    if( ! IdeHelper.prompt("Because this is not an upgrade the entire row must be removed from core_resource table, this will reset both, data and normal installer", "Warning: Remove installer")){
                        return;
                    }

                }

                String result = magicento.executePhpWithMagento(phpCode);
                if(result != null && ! result.isEmpty()){
                    String message = "Installer for "+resourceName+" was reset to "+ result +" version";
                    if( ! isUpgrade){
                        boolean modified = result.equals("1");
                        message = modified ? "Installer for "+resourceName+" was removed from core_resource" : "Error: Installer was not reset to previous version";
                    }
                    magicento.showMessage(message,"Magicento - Reset Installer", MagicentoIcons.MAGENTO_ICON_16x16);
                }
                else {
                    IdeHelper.showDialog(getProject(), "Null PHP response", "Reset Installer Error");
                }
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
