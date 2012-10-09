package com.magicento.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.FileHelper;
import com.magicento.helpers.JavaHelper;
import com.magicento.helpers.Magicento;
import com.magicento.models.layout.Template;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * @author Enrique Piatti
 */
public class CopyTemplateDialog extends DialogWrapper {

    // private JPanel myPanel;
    private JLabel labelForPackage;
    private JLabel labelForTheme;
    private JComboBox comboForPackage;
    private JComboBox comboForTheme;

    private Template template;
    protected Project project;


    public CopyTemplateDialog(Project project, Template template) {
        super(project);
        this.project = project;
        labelForPackage = new JLabel("Package:");
        labelForTheme = new JLabel("Theme:");
        comboForPackage = new ComboBox();
        comboForTheme = new ComboBox();
        this.template = template;
        init();
        setTitle("Choose package and theme destination");
    }

    @Override
    protected JComponent createCenterPanel()
    {
        //JPanel panel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(0,2,5,10));
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        panel.add(labelForPackage);
        panel.add(comboForPackage);
        panel.add(labelForTheme);
        panel.add(comboForTheme);
        //panel.setPreferredSize(new Dimension(400, 500));

        updateComboItems(comboForPackage, getAllPackages());
        comboForPackage.setSelectedItem(template.getPackage());
        updateComboItems(comboForTheme, getAllThemesFromPackage((String) comboForPackage.getSelectedItem()));
        comboForTheme.setSelectedItem(template.getTheme());

        comboForPackage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String packageName = (String)((JComboBox)e.getSource()).getSelectedItem();
                updateComboItems(comboForTheme, getAllThemesFromPackage(packageName));
            }
        });

        return panel;
    }

    protected void updateComboItems(JComboBox comboBox, String[] items)
    {
        comboBox.removeAllItems();
        for(String item : items){
            comboBox.addItem(item);
        }
    }

    @NotNull
    protected String[] getAllPackages()
    {
        return Magicento.getAllPackages(project, template.getArea());
    }

    protected String[] getAllThemesFromPackage(String packageName)
    {
        return Magicento.getAllThemesFromPackage(project, template.getArea(), packageName);
    }

    public String getPackage()
    {
        return (String) comboForPackage.getSelectedItem();
    }

    public String getTheme()
    {
        return (String) comboForTheme.getSelectedItem();
    }

}
