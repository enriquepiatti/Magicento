package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;

/**
 * @author Enrique Piatti
 */
public class FlushCacheAction extends MagicentoPhpActionAbstract
{
    @Override
    public void executeAction()
    {
        MagicentoProjectComponent magicento = getMagicentoComponent();
        if(magicento != null)
        {
            String phpCode =
                            "Mage::app()->getCacheInstance()->flush();"+
                            "Mage::app()->cleanCache();"+
                            "Enterprise_PageCache_Model_Cache::getCacheInstance()->clean(Enterprise_PageCache_Model_Processor::CACHE_TAG);" +
                            "echo '1';";
            String result = magicento.executePhpWithMagento(phpCode);
            String text = result.equals("1") ? "Magento and Storage cache flushed" : "Error flushing the cache";
            magicento.showMessage(text, "Flush Cache", MagicentoIcons.MAGENTO_ICON_16x16);
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }
}
