/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Corpus;

import Parser.ParserBioMedXML;
import Affichage.Tableau.AfficherTableau;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jdom2.JDOMException;

/**
 *
 * @author JF Chartier
 */
public class BioMedAttributs 
{
    public static void main(String[] args) throws IOException, JDOMException 
    {
//        new BioMedAttributs().recupererMap_momRevue_frequence(CorpusUtils.getRepertoireArticle(), Dictionnaire.getFileRevueSelectionnees());
        new BioMedAttributs().recupererMap_auteurs_frequence(CorpusUtils.getRepertoireArticle(), Dictionnaire.getFileRevueSelectionnees());
       

    }
    
    public static Map<String, Integer> recupererMap_momRevue_frequence (File files) throws IOException, JDOMException
    {
        Collection<String> nomsRevues = ParserBioMedXML.recupererNomsRevues(files);
        Map<String, Integer> map_momRevue_frequence = TrieUtils.recupererMap_valeur_frequence(nomsRevues);
        new AfficherTableau("Frequence de chaque nom de revue du corpus", "nom de revue", "frequence", new TreeMap<String, Integer>(map_momRevue_frequence)).setVisible(true);
        return map_momRevue_frequence;
    }
    
    public static Map<String, Integer> recupererMap_momRevue_frequence (File files, File revuesSelect) throws IOException, JDOMException
    {
        Collection<File> filesSelect = ParserBioMedXML.recupererFilesSelonDictionnaireRevues(files, revuesSelect);
        Collection<String> nomsRevues = ParserBioMedXML.recupererNomsRevues(filesSelect);
        Map<String, Integer> map_momRevue_frequence = TrieUtils.recupererMap_valeur_frequence(nomsRevues);
        new AfficherTableau("Frequence de chaque nom de revue du corpus", "nom de revue", "frequence", new TreeMap<String, Integer>(map_momRevue_frequence)).setVisible(true);
        return map_momRevue_frequence;
    }
    
    public static Map<String, Integer> recupererMap_anneePublication_frequence (File files) throws IOException, JDOMException
    {
        Collection<String> nomsRevues = ParserBioMedXML.recupererAnneesPublications(files);
        Map<String, Integer> map_momRevue_frequence = TrieUtils.recupererMap_valeur_frequence(nomsRevues);
        new AfficherTableau("Frequence de chaque annee de publication des revues du corpus", "annee de publication", "frequence", new TreeMap<String, Integer>(map_momRevue_frequence)).setVisible(true);
        return map_momRevue_frequence;
    }
    
    public static Map<String, Integer> recupererMap_anneePublication_frequence (File files, File revuesSelect) throws IOException, JDOMException
    {
        Collection<File> filesSelect = ParserBioMedXML.recupererFilesSelonDictionnaireRevues(files, revuesSelect);
        
        Collection<String> nomsRevues = ParserBioMedXML.recupererAnneesPublications(filesSelect);
        Map<String, Integer> map_momRevue_frequence = TrieUtils.recupererMap_valeur_frequence(nomsRevues);
        new AfficherTableau("Frequence de chaque annee de publication des revues du corpus", "annee de publication", "frequence", new TreeMap<String, Integer>(map_momRevue_frequence)).setVisible(true);
        return map_momRevue_frequence;
    }
    
    public static Map<String, Integer> recupererMap_auteurs_frequence (File files, File revuesSelect) throws IOException, JDOMException
    {
        Collection<File> filesSelect = ParserBioMedXML.recupererFilesSelonDictionnaireRevues(files, revuesSelect);
        
        Collection<String> auteurs = ParserBioMedXML.recupererAuteurs(filesSelect);
        Map<String, Integer> map_auteurs_frequence = TrieUtils.recupererMap_valeur_frequence(auteurs);
        new AfficherTableau("Frequence de chaque auteurs des revues du corpus", "auteurs", "frequence", new TreeMap<String, Integer>(map_auteurs_frequence)).setVisible(true);
        return map_auteurs_frequence;
    }
    
       
    
}
