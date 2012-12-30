package com.magicento.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.FileHelper;
import com.magicento.helpers.XmlHelper;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class ChooseModuleDialog extends DialogWrapper
{

    protected final String LOCAL = "local";
    protected final String COMMUNITY = "community";

    protected JComboBox codePool;
    protected JComboBox modules;
    protected Project project;

    protected Map<String, String> communityModules;
    protected Map<String, String> localModules;

    protected String selectedModulePath;
    protected String selectedPool;

    public ChooseModuleDialog(@org.jetbrains.annotations.Nullable Project project)
    {
        super(project);
        this.project = project;
        codePool = new JComboBox();
        modules = new JComboBox();
        communityModules = new LinkedHashMap<String, String>();
        localModules = new LinkedHashMap<String, String>();
        init();
        setTitle("Choose target Module");
    }

    @Override
    protected JComponent createCenterPanel()
    {
        JPanel panel = new JPanel(new GridLayout(0,1,5,10));
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        panel.add(codePool);
        panel.add(modules);

        codePool.setSelectedItem(LOCAL);

        updateModulesComboItems(localModules);
        String moduleName = (String)modules.getSelectedItem();
        selectedPool = LOCAL;
        selectedModulePath = localModules.get(moduleName);

        codePool.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String codePool = (String)((JComboBox)e.getSource()).getSelectedItem();
                if(codePool.equals(LOCAL)){
                    updateModulesComboItems(localModules);
                }
                else {
                    updateModulesComboItems(communityModules);
                }
            }
        });

        modules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String moduleName = (String)((JComboBox)e.getSource()).getSelectedItem();
                String selectedPool = (String)codePool.getSelectedItem();
                if(selectedPool.equals(LOCAL)){
                    selectedModulePath = localModules.get(moduleName);
                    selectedPool = LOCAL;
                }
                else {
                    selectedModulePath = communityModules.get(moduleName);
                    selectedPool = COMMUNITY;
                }
            }
        });

        return panel;
    }

    @Override
    protected void init()
    {
        codePool.addItem(LOCAL);
        codePool.addItem(COMMUNITY);

        fillModules();

        super.init();
    }

    private void fillModules()
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null)
        {
            String basePath = settings.getPathToMagento()+"/app/code/";
            fillModulePaths(basePath+LOCAL, localModules);
            fillModulePaths(basePath+COMMUNITY, communityModules);
        }

    }

    protected void fillModulePaths(String base, Map<String, String> map)
    {
        String packageName;
        String moduleName;
        File etcConfig;
        File pool = new File(base);
        for(File packageFile : FileHelper.getSubdirectoriesFiles(pool)){
            packageName = packageFile.getName();
            for(File moduleFile : FileHelper.getSubdirectoriesFiles(packageFile)){
                etcConfig = new File(moduleFile.getAbsolutePath()+"/etc/config.xml");
                if(etcConfig.exists()){
                    moduleName = moduleFile.getName();
                    map.put(packageName+"_"+moduleName, moduleFile.getAbsolutePath());
                }
            }
        }
    }

    protected void updateModulesComboItems(Map<String, String> map)
    {
        modules.removeAllItems();
        for(Map.Entry<String, String> entry : map.entrySet())
        {
            modules.addItem(entry.getKey());
        }
    }


    public String getSelectedModulePath() {
        return selectedModulePath.replace("\\", "/");
    }

    public String getSelectedPool() {
        return selectedPool;
    }

    public String getSelectedModule()
    {
        return (String)modules.getSelectedItem();
    }

}
