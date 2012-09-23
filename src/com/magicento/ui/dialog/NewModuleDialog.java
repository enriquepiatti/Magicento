package com.magicento.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.layout.Template;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class NewModuleDialog extends DialogWrapper
{
    private JComboBox codePool;
    private JTextField namespace;
    private JTextField module;
    private JTextField version;
    private JList depends;
    private JCheckBox helper;
    private JCheckBox model;
    private JCheckBox block;
    private JCheckBox installer;
    private JTextField group;
    private JPanel panel;


    protected Project project;

    public NewModuleDialog(Project project)
    {
        super(project);
        this.project = project;
        codePool = new JComboBox();
        namespace = new JTextField(10);
        module = new JTextField(10);
        version = new JTextField(10);
        depends = new JList();
        helper = new JCheckBox("helper");
        model = new JCheckBox("model");
        block = new JCheckBox("block");
        installer = new JCheckBox("installer");
        group = new JTextField(10);
        init();
        setTitle("New Module");
    }

    @Override
    protected void init()
    {
        codePool.addItem("local");
        codePool.addItem("community");

        fillDepends();

        version.setText("0.1.0");

        model.setSelected(true);
        helper.setSelected(true);
        block.setSelected(true);
        installer.setSelected(true);

        super.init();
    }

    private void fillDepends()
    {
        depends.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
        File configXml = magicento.getCachedConfigXml();
        List<Element> modules = XmlHelper.findXpath(configXml, "config/modules/*");
        if(modules != null && modules.size()>0){
            String[] moduleNames = new String[modules.size()];
            int i = 0;
            for(Element module : modules){
                moduleNames[i] = module.getName();
                i++;
            }
            Arrays.sort(moduleNames);
            depends.setListData(moduleNames);
        }
    }

    @Override
    protected JComponent createCenterPanel()
    {
        //JPanel panel = new JPanel(new GridLayout(0,2,5,10));
        panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        c.gridx = 0; c.gridy = 0;
        panel.add(new Label("Code Pool"), c);
        c.gridx = 1;
        panel.add(codePool, c);

        c.gridx = 0; c.gridy = 1;
        panel.add(new Label("Namespace"), c);
        c.gridx = 1;
        panel.add(namespace, c);

        c.gridx = 0; c.gridy = 2;
        panel.add(new Label("Module"), c);
        c.gridx = 1; c.gridy = 2;
        panel.add(module, c);

        c.gridx = 0; c.gridy = 3;
        panel.add(new Label("Group"), c);
        c.gridx = 1; c.gridy = 3;
        panel.add(group,c);
        group.setToolTipText("Leave empty for using the default (namespace_module)");

        c.gridx = 0; c.gridy = 4;
        panel.add(new Label("Version"), c);
        c.gridx = 1; c.gridy = 4;
        panel.add(version, c);

        c.gridx = 0; c.gridy = 5;
        panel.add(new Label("Depends"), c);
        c.gridx = 1; c.gridy = 5;
        //panel.add(depends, c);
        depends.setVisibleRowCount(6);
        JScrollPane listScroller = new JScrollPane(depends);
        //listScroller.setPreferredSize(new Dimension(250, 80));
        panel.add(listScroller, c);

        c.gridx = 0; c.gridy = 6;
        panel.add(new Label("Include"), c);
        JPanel includePanel = new JPanel();
        includePanel.setLayout(new BoxLayout(includePanel, BoxLayout.Y_AXIS));
        includePanel.add(model);
        includePanel.add(helper);
        includePanel.add(block);
        includePanel.add(installer);
        c.gridx = 1; c.gridy = 6;
        panel.add(includePanel, c);

        return panel;
    }

    public String getCodePool()
    {
        return (String)codePool.getSelectedItem();
    }

    public String getNamespace()
    {
        return namespace.getText();
    }

    public String getModule()
    {
        return module.getText();
    }

    public String getVersion()
    {
        return version.getText();
    }

    public String getGroup()
    {
        return group.getText();
    }

    public String[] getDepends()
    {
        return Arrays.copyOf(depends.getSelectedValues(), depends.getSelectedValues().length, String[].class);
        // Arrays.asList(Object_Array).toArray(new String[Object_Array.length]);
        // return (String[])depends.getSelectedValues();
    }

    public boolean includeHelper()
    {
        return helper.isSelected();
    }

    public boolean includeBlock()
    {
        return block.isSelected();
    }

    public boolean includeModel()
    {
        return model.isSelected();
    }

    public boolean includeInstaller()
    {
        return installer.isSelected();
    }

    @Override
    public JComponent getPreferredFocusedComponent()
    {
        return namespace;
    }
}
