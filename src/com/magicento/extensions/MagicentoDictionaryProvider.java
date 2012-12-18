package com.magicento.extensions;

import com.intellij.spellchecker.BundledDictionaryProvider;

import java.io.InputStream;

/**
 * @author Enrique Piatti
 */
public class MagicentoDictionaryProvider implements BundledDictionaryProvider {
    public String[] getBundledDictionaries() {
        return new String[]{"magento.dic"};
    }
}