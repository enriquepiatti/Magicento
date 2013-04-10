package com.magicento.models;


import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.FileContentUtil;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.FileHelper;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PsiPhpHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PhpStormMetaNamespace
{

    protected static Map<Project, PhpStormMetaNamespace> instances;

    protected Project _project;
    protected MagicentoProjectComponent _magicentoProject;
    protected File _metaFile;


    private PhpStormMetaNamespace(Project project)
    {
        _project = project;
        _magicentoProject = MagicentoProjectComponent.getInstance(_project);
        _metaFile = new File(getPhpStormMetaFilePath());
    }

    public static PhpStormMetaNamespace getInstance(Project project)
    {
        if(instances == null){
            instances = new HashMap<Project, PhpStormMetaNamespace>();
        }
        if( ! instances.containsKey(project)){
            instances.put(project, new PhpStormMetaNamespace(project));
        }
        return instances.get(project);
    }

    public void savePhpStormMetaFile(final String content)
    {
        if(content == null || content.isEmpty()){
            IdeHelper.showNotification("Magicento: PHPSTORM_META content is empty", NotificationType.ERROR, _project);
            return;
        }
        String filename = getPhpStormMetaFile().getName();
        String directoryPath = getPhpStormMetaFile().getParent();
        FileHelper.createPsiFile(filename, directoryPath, content, FileHelper.getPhpFileType(_project), _project);
    }

    public String getPhpStormMetaFilePath()
    {
        String metaFileName = "magicento.phpstorm.meta.php";
        String pathToMagento = _magicentoProject.getMagicentoSettings().getPathToMagento();
        String filePath = pathToMagento+"/"+metaFileName;
        return filePath;
    }

    public File getPhpStormMetaFile()
    {
        return _metaFile;
    }


    public void savePhpStormMetaFile(Map<MagentoClassInfo.UriType, Map<String, String>> mappingByType)
    {
        String fileContent = createMetaContent(mappingByType);
        savePhpStormMetaFile(fileContent);
    }

    /**
     * @todo make this run faster
     * @param mappingByType
     * @return
     */
    protected String createMetaContent(Map<MagentoClassInfo.UriType, Map<String, String>> mappingByType)
    {
        String content = "<?php\n" +
                "\tnamespace PHPSTORM_META {\n" +
                "\t/** @noinspection PhpUnusedLocalVariableInspection */\n" +
                "\t/** @noinspection PhpIllegalArrayKeyTypeInspection */\n" +
                "\t$STATIC_METHOD_TYPES = [\n";

        for(Map.Entry<MagentoClassInfo.UriType, Map<String, String>> entry : mappingByType.entrySet())
        {
            List<String> methods = new ArrayList<String>();
            switch (entry.getKey()){
                case HELPER:
                    methods.add("helper");
                    break;
                case MODEL:
                    methods.add("getModel");
                    methods.add("getSingleton");
                    break;
                case RESOURCEMODEL:
                    methods.add("getResourceModel");
                    methods.add("getResourceSingleton");
                    break;
            }


            for(String method : methods){

                content += "\n\t\t\\Mage::"+method+"('') => [\n";
                for(Map.Entry<String, String> mapping : entry.getValue().entrySet())
                {
                    content += "\n\t\t\t'"+mapping.getKey()+"' instanceof \\"+mapping.getValue()+",";
                }
                content += "\n\t\t],";
            }
        }

        content += "\n\t];\n}";
        return content;
    }


    public void refreshMetaFile()
    {
        // refresh file
        VirtualFile virtualFile = FileHelper.getVirtualFileFromFile(_metaFile);
        if(virtualFile != null){
            FileHelper.refreshVirtualFile(virtualFile);
            // FileHelper.refreshVirtualFile(virtualFile, _project);
        }

    }


    public void addFactory(MagentoClassInfo.UriType uriType, String uri, String className)
    {
        Map<MagentoClassInfo.UriType, Map<String, String>> mappingByType = parse();
        mappingByType.get(uriType).put(uri, className);
        savePhpStormMetaFile(createMetaContent(mappingByType));
    }

    public void removeFactory(MagentoClassInfo.UriType uriType, String uri)
    {
        Map<MagentoClassInfo.UriType, Map<String, String>> mappingByType = parse();
        mappingByType.get(uriType).remove(uri);
        savePhpStormMetaFile(createMetaContent(mappingByType));
    }


    public Map<MagentoClassInfo.UriType, Map<String, String>> parse()
    {
        // get first array creation expression
        Map<MagentoClassInfo.UriType, Map<String, String>> mappingByType = new HashMap<MagentoClassInfo.UriType, Map<String, String>>();
        for (MagentoClassInfo.UriType uriType : MagentoClassInfo.UriType.values()) {
            mappingByType.put(uriType, new HashMap<String, String>());
        }

        PsiFile psiFile = getPsiMetaFile();
        PsiElement firstArrayExpression = PsiPhpHelper.findFirstChildOfType(psiFile.getFirstChild(), PsiPhpHelper.ARRAY_CREATION_EXPRESSION, true);
        if(firstArrayExpression != null){
            PsiElement hashElement = PsiPhpHelper.findFirstChildOfType(firstArrayExpression, PsiPhpHelper.HASH_ARRAY_ELEMENT, true);
            while(hashElement != null){
                PsiElement arrayKey = PsiPhpHelper.findFirstChildOfType(hashElement, PsiPhpHelper.ARRAY_KEY, true);
                PsiElement arrayValue = PsiPhpHelper.findFirstChildOfType(hashElement, PsiPhpHelper.ARRAY_VALUE, true);
                if(arrayKey != null && arrayValue != null)
                {
                    PsiElement methodReference = PsiPhpHelper.findFirstChildOfType(arrayKey, PsiPhpHelper.METHOD_REFERENCE, true);
                    String methodName = MagentoParser.getMethodName(methodReference);
                    MagentoClassInfo.UriType uriType = getUriTypeFromMethodName(methodName);

                    Map<String, String> mapping = mappingByType.get(uriType);

                    arrayValue = PsiPhpHelper.findFirstChildOfType(arrayValue, PsiPhpHelper.ARRAY_VALUE, true);
                    while(arrayValue != null){
                        // String factoryDefinition = arrayValue.getText();
                        PsiElement psiUri = PsiPhpHelper.findFirstChildOfType(arrayValue, PsiPhpHelper.STRING, true);
                        PsiElement psiClass = PsiPhpHelper.findFirstChildOfType(arrayValue, PsiPhpHelper.CLASS_REFERENCE, true);
                        mapping.put(psiUri.getText().replaceAll("'", ""), psiClass.getText().substring(1));
                        arrayValue = PsiPhpHelper.findNextSiblingOfType(arrayValue, PsiPhpHelper.ARRAY_VALUE);
                    }
                }
                hashElement = PsiPhpHelper.findNextSiblingOfType(hashElement, PsiPhpHelper.HASH_ARRAY_ELEMENT);
            }
        }
        return mappingByType;
    }

    public MagentoClassInfo.UriType getUriTypeFromMethodName(String methodName)
    {
        MagentoClassInfo.UriType uriType = MagentoClassInfo.UriType.MODEL;
        if(methodName.equals("helper")){
            uriType = MagentoClassInfo.UriType.HELPER;
        }
        else if(methodName.equals("getResourceModel") || methodName.equals("getResourceSingleton")){
            uriType = MagentoClassInfo.UriType.RESOURCEMODEL;
        }
        return uriType;
    }


    public PsiFile getPsiMetaFile()
    {
        return FileHelper.getPsiFileFromFile(_metaFile, _project);
    }
}
