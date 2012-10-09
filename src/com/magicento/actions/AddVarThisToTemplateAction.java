package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.IdeHelper;
import com.magicento.models.layout.Template;

import java.util.List;

/**
 * @author Enrique Piatti
 */
public class AddVarThisToTemplateAction extends MagicentoActionAbstract {


    @Override
    public void executeAction()
    {
        final VirtualFile file = getVirtualFile();
        Template template = new Template(file);
        MagicentoProjectComponent magicento = getMagicentoComponent();
        if(magicento != null)
        {
            List<String> blocks = template.getBlocksClasses(getProject());
            if(blocks != null)
            {
                if(blocks.size() > 0)
                {
                    String code = "";
                    for(String blockName : blocks)
                    {
                        code += "<?php /* @var $this " + blockName + " */ ?>\n";
                    }

                    final String finalCode = code;
                    CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
                        public void run() {
                            writeStringAtPosition(finalCode, 0);
                        }
                    }, null, null);

                }
                else {
                    IdeHelper.showDialog(getProject(), "Cannot find corresponding block from merged layout xml\n" +
                            "Probably this template is assigned directly from PHP using ->setTemplate instead of using the layout",
                            "Block not found");
                }
            }
        }
        else {
            IdeHelper.showDialog(getProject(), "Cannot get Magicento Project Component", "Magicento Go to Block Error");
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        VirtualFile file = getVirtualFile();
        Template template = new Template(file);
        return template.isTemplate();
    }


}
