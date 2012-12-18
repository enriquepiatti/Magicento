package com.magicento.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.*;
import com.magicento.models.layout.Template;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Enrique Piatti
 */
public class AddTranslationDialog extends DialogWrapper
{

    private JTextField originalText;
    private JTextField translatedText;
    private JCheckBox useThemeTranslator;
    private JComboBox comboForArea;
    private JComboBox comboForPackage;
    private JComboBox comboForTheme;
    private JComboBox locale;
    private JComboBox csvFile;

    protected Project project;
    protected PsiElement context;
    protected String contextFilePath;

    protected Map<String, String> communityModules;
    protected Map<String, String> localModules;

    protected String selectedModulePath;
    protected String selectedPool;

    protected Template template;

    private static final String TRANSLATE_CSV = "translate.csv";

    public AddTranslationDialog(@org.jetbrains.annotations.Nullable Project project, String original, PsiElement context)
    {
        super(project);
        this.project = project;
        this.context = context;
        contextFilePath = context.getContainingFile().getOriginalFile().getVirtualFile().getPath();
        originalText = new JTextField(original);
        template = new Template(context.getContainingFile().getOriginalFile().getVirtualFile());
        init();
        setTitle("Add Translation to selected CSV");
    }

    @Override
    protected JComponent createCenterPanel()
    {
        JPanel panel = new JPanel(new GridLayout(0,2,5,10));
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        panel.add(new JLabel("Original:"));
        panel.add(originalText);

        panel.add(new JLabel("Translated:"));
        panel.add(translatedText);

        panel.add(new JLabel("Use Theme Translator:"));
        panel.add(useThemeTranslator);

        panel.add(new JLabel("Area:"));
        panel.add(comboForArea);

        panel.add(new JLabel("Package:"));
        panel.add(comboForPackage);

        panel.add(new JLabel("Theme:"));
        panel.add(comboForTheme);

        panel.add(new JLabel("Locale:"));
        panel.add(locale);

        panel.add(new JLabel("CSV File:"));
        panel.add(csvFile);

        if( ! useThemeTranslator.isSelected()){
            // comboForArea.setEnabled(false);
            comboForPackage.setEnabled(false);
            comboForTheme.setEnabled(false);
        }

        useThemeTranslator.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = useThemeTranslator.isSelected();
                // comboForArea.setEnabled(isSelected);
                comboForPackage.setEnabled(isSelected);
                comboForTheme.setEnabled(isSelected);
                if(isSelected){
                    csvFile.removeAllItems();
                    csvFile.addItem(TRANSLATE_CSV);
                    csvFile.setEnabled(false);
                }
                else {
                    initCsvFile();
                    csvFile.setEnabled(true);
                }
            }
        });

        comboForArea.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String area = (String)((JComboBox)e.getSource()).getSelectedItem();
                updateComboItems(comboForPackage, getAllPackages(area));
                String packageName = (String) comboForPackage.getSelectedItem();
                updateComboItems(comboForTheme, getAllThemesFromPackage(area, packageName));
                if( ! useThemeTranslator.isSelected()){
                    initCsvFile();
                }
            }
        });

        comboForPackage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String area = (String) comboForArea.getSelectedItem();
                String packageName = (String)((JComboBox)e.getSource()).getSelectedItem();
                updateComboItems(comboForTheme, getAllThemesFromPackage(area, packageName));
            }
        });

        return panel;
    }

    @Override
    protected void init()
    {
        translatedText = new JTextField();
        useThemeTranslator = new JCheckBox(TRANSLATE_CSV);
        initComboThemes();
        initLocale();
        initCsvFile();
        super.init();
    }


    protected void initComboThemes()
    {
        String[] areas = {"frontend", "adminhtml"};
        comboForArea = new JComboBox(areas);
        comboForArea.setSelectedItem(guessArea());
        String selectedArea = (String)comboForArea.getSelectedItem();

        comboForPackage = new JComboBox();
        updateComboItems(comboForPackage, getAllPackages(selectedArea));
        if(template.isTemplate()){
            comboForPackage.setSelectedItem(template.getPackage());
        }
        String selectedPackage = (String) comboForPackage.getSelectedItem();

        comboForTheme = new JComboBox();
        updateComboItems(comboForTheme, getAllThemesFromPackage(selectedArea, selectedPackage));
        if(template.isTemplate()){
            comboForTheme.setSelectedItem(template.getTheme());
        }

    }


    protected void updateComboItems(JComboBox comboBox, String[] items)
    {
        comboBox.removeAllItems();
        for(String item : items){
            comboBox.addItem(item);
        }
    }


    @NotNull
    protected String[] getAllPackages(String area)
    {
        return Magicento.getAllPackages(project, area);
    }

    protected String[] getAllThemesFromPackage(String area, String packageName)
    {
        return Magicento.getAllThemesFromPackage(project, area, packageName);
    }

    protected String getLocalePath()
    {
        return MagicentoSettings.getInstance(project).getPathToMagento()+"/app/locale";
    }

    protected String getLocaleThemePath()
    {
        String area = (String)comboForArea.getSelectedItem(); // template.getArea();
        String packageName = (String)comboForPackage.getSelectedItem(); // template.getPackage();
        String theme = (String)comboForTheme.getSelectedItem(); // template.getTheme();
        return MagicentoSettings.getInstance(project).getPathToMagento()+"/app/design/"+area+"/"+packageName+"/"+theme+"/locale";
    }

    protected void initLocale()
    {
        Set locales = new TreeSet();
        locales.addAll(Arrays.asList(FileHelper.getSubdirectories(new File(getLocalePath()))));
        File localeTheme = new File(getLocaleThemePath());
        locales.addAll(Arrays.asList(FileHelper.getSubdirectories(localeTheme)));
        locale = new JComboBox(locales.toArray());
        locale.setToolTipText("If your locale doesn't appear here, create the folder first");
    }

    protected void initCsvFile()
    {
        String area = (String)comboForArea.getSelectedItem();
        String xpath = "//"+area+"/translate/modules/*/files/*";
        File configXml = MagicentoProjectComponent.getInstance(project).getCachedConfigXml();
        java.util.List<Element> files = XmlHelper.findXpath(configXml, xpath);
        Set<String> fileNames = new TreeSet<String>();
        if(files != null){
            for(Element fileElement : files){
                fileNames.add(fileElement.getValue());
            }
        }
        if(csvFile == null){
            csvFile = new JComboBox();
        }

        csvFile.removeAllItems();
        for(String fileName : fileNames){
            csvFile.addItem(fileName);
        }

        if( ! useThemeTranslator.isSelected()){
            String guessedCsvFile = guessCsvFile();
            if(guessedCsvFile != null){
                csvFile.setSelectedItem(guessedCsvFile);
            }
        }

        csvFile.setToolTipText("<html>The csv file will be created if not exists.<br>If the file is not in the list add it first to your config.xml</html>");
    }


    public String getSelectedCsvFileName()
    {
        return (String)csvFile.getSelectedItem();
    }

    public String getSelectedCsvFilePath()
    {
        String csvFilePath = getSelectedCsvFileDirectory();
//        if(useThemeTranslator.isSelected()){
//            csvFilePath += "/"+TRANSLATE_CSV;
//        }
//        else {
//            csvFilePath += "/"+csvFile.getSelectedItem();
//        }
        csvFilePath += "/"+getSelectedCsvFileName();
        return csvFilePath;
    }

    public String getTranslatedText()
    {
        return translatedText.getText();
    }


    protected String guessArea()
    {
        if(template.isTemplate()){
            return template.getArea();
        }
        String className = PsiPhpHelper.getClassName(context);
        if(MagentoParser.isController(context)){
            // TODO: check class hierarchy, if it's Mage_Core_Controller_Front_Action or Mage_Adminhtml_Controller_Action
        }
        if(className != null && className.contains("_Adminhtml_")){
            return "adminhtml";
        }
        return "frontend";
    }

    protected String guessCsvFile()
    {

        if(template.isTemplate()){
            java.util.List<String> blocks = template.getBlocksClasses(project);
            if(blocks != null){
                for(String block : blocks){
                    String moduleName = MagentoParser.getNamespaceModuleFromClassName(block);
                    return getCsvFileFromModuleName(moduleName);
                }
            }
        }
        else {
            String moduleName = MagentoParser.getModuleNameFromModulePath(contextFilePath);
            if(moduleName != null){
                return getCsvFileFromModuleName(moduleName);
            }
        }

        return null;

    }

    protected String getCsvFileFromModuleName(String moduleName)
    {
        String area = (String)comboForArea.getSelectedItem();
        File configXml = MagicentoProjectComponent.getInstance(project).getCachedConfigXml();
        String xpath = "//"+area+"/translate/modules/"+moduleName+"/files/*";
        java.util.List<Element> files = XmlHelper.findXpath(configXml, xpath);
        if(files != null && files.size() > 0){
            return files.get(0).getValue();
        }
        return null;
    }

    public String getSelectedCsvFileDirectory()
    {
        String csvDirectory = "";
        if(useThemeTranslator.isSelected()){
            csvDirectory = getLocaleThemePath()+"/"+locale.getSelectedItem();
        }
        else {
            csvDirectory = getLocalePath()+"/"+locale.getSelectedItem();
        }
        return csvDirectory;
    }

    public String getOriginalText() {
        return originalText.getText();
    }
}
