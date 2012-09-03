package com.magicento.models.xml;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Enrique Piatti
 */
abstract public class MagentoXmlElement {

    protected boolean isRequired = false;
    // TODO: this is weird, the type is a child class !
    protected MagentoXmlTag parent;

    protected Callable<Map<String, String>> possibleValuesCallback;

    protected List<String> possibleValues;

    protected String name;
    protected String help;

    protected MagentoXml manager;

//    public MagentoXmlElement() {
//        possibleValues = new ArrayList<String>();
//    }

    public boolean isRequired() {
        return isRequired;
    }
    public void isRequired(boolean required) {
        isRequired = required;
    }

    public String getName() {
        return name;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPossibleValuesCallback(Callable<Map<String, String>> possibleValuesCallback) {
        this.possibleValuesCallback = possibleValuesCallback;
    }

    public MagentoXmlTag getParent(){
        return parent;
    }

    public void setParent(MagentoXmlTag parent){
        this.parent = parent;
    }


    public List<String> getPossibleNames(){
        //Set<String> names = new LinkedHashSet<String>();
        List<String> names = new ArrayList<String>();
        names.add(getName());
        return names;
    }

    /**
     * when the node can have Multiple Names, name is null and this returns the possible names
     * @return
     */
    abstract public Map<String, String> getPossibleDefinitions();

    /**
     * mapping nameToBeShownOnPopup => codeInsetrtedInCompletion
     * by default it uses possibleValuesCallback or possibleValues if they are not null
     * this is for leaf nodes
     * @return
     */
    public Map<String, String> getPossibleValues(){
        if(possibleValuesCallback != null){
            try {
                return possibleValuesCallback.call();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else if(possibleValues != null){
            Map<String, String> map = new LinkedHashMap<String, String>();
            for(String value : possibleValues){
                map.put(value, value);
            }
            return map;
        }
        return null;
    }


    /**
     * documentation for this node
     * @return
     */
    public String getHelp(){
        return help;
    }


    public MagentoXml getManager() {
        return manager;
    }

    public void setManager(MagentoXml manager) {
        this.manager = manager;
    }


}
