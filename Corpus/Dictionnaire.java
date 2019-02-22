package Corpus;

import Affichage.Tableau.AfficheurTableau;
import Fichier.FileUtils;
import Segmentation.Segmenteur.ExtracteurUtils;
import Tokenisation.Tokeniseur;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JF Chartier
 */
public class Dictionnaire 
{
//    public static File fileDictionnaireMotCible = new File("");
//    public static File fileDictionnaireMotDescripteur = new File ("");
    private static final File fileLanciCategorieMotDescripteurV4 = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste v4 catégories des descripteurs.txt");
    private static final File fileLanciCategorieMotCibleV4 = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste v4 catégories des mots cibles.txt");
    private static final File fileCategorieMotCibleV5 = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Lanci\\Projet sur BioMed\\liste v5 catégories des mots cibles.txt");
    private static final File fileCategorieMotDescripteurV5 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Lanci\\Projet sur BioMed\\liste v5 catégories des mots descripteurs.txt");
    private static final File fileCategorieMotCibleV7 = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Lanci\\Projet sur BioMed\\liste v7 catégories des mots cibles.txt");
    private static final File fileCategorieMotDescripteurV7 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Lanci\\Projet sur BioMed\\liste v7 catégories des mots descripteurs.txt");
    
    private static final File fileCategorieMotDescripteurV8 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v8 catégories des mots descripteurs.txt");
    private static final File fileCategorieMotCibleV8 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v8 catégories des mots cibles.txt");
    
    private static final File fileCategorieMotCibleV9 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v9 catégories des mots cibles.csv");
    private static final File fileCategorieMotDescripteurV9 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v9 catégories des mots descripteurs.txt");
    
    private static final File fileCategorieMotDescripteurV10 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v10 catégories des mots descripteurs.txt");
    
    private static final File fileRevueSelectionnees = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\Revues selectionnees V1.txt");
    
    private static final File fileThemeEtMetaThemeV1 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v1 des themes et meta-themes.csv");
    
    private static final File fileThemeEtMetaThemeV2 = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\liste v2 des themes et meta-themes.csv");
    
    
    public static Set<String> recupererDictionnaireRevues(File file) throws IOException
    {
        System.out.println("recupererDictionnaireRevues");
        String document = FileUtils.read(file.getPath());
        List<String> liste = ExtracteurUtils.extraireParBalises(document, "<", ">");
        System.out.println("\t nombre de revue dans le dictionnaire: " + liste.size());
        System.out.println("\t" + liste);
        return new TreeSet<String>(liste);
    }
    
    public static Map<String, String> getMap_token_cat (File fileCsvMotCible) throws FileNotFoundException, IOException
    {
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileCsvMotCible), "UTF-8"), ';' , '"' , 1);
        Map<String, String> map = new HashMap<>();
        
        //Read CSV line by line and use the string array as you want
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) 
        {
           if (nextLine != null) 
           {
               map.put(nextLine[0], nextLine[1]);
           }
        }
        AfficheurTableau.afficher2Colonnes("token et categorie", "tokenCible", "categorieCible", map);
        return map;
    }
    
    public static Map<String, String> recupererCategorieMot(File file) throws IOException
    {
        System.out.println("recupererCategorieMot");
        Map<String, String> map_descripteur_categorie = new TreeMap<>();
        String document = FileUtils.read(file.getPath());
        List<String> liste = ExtracteurUtils.extraireParBalises(document, "<", ">");
        for (String association: liste)
        {
            List<String> mots = Tokeniseur.extraireListeToken(association);
            if (mots.size() != 2)
            {
                System.err.println("*** mauvaise tekenisation des mots");
            }
            else
            {
                System.out.println(mots);
                map_descripteur_categorie.put(mots.get(0), mots.get(1));
            }
        }
        return map_descripteur_categorie;
    }

    public static File getFileLanciCategorieMotCibleV4() {
        return fileLanciCategorieMotCibleV4;
    }

    public static File getFileLanciCategorieMotDescripteurV4() {
        return fileLanciCategorieMotDescripteurV4;
    }

    public static File getFileRevueSelectionnees() {
        return fileRevueSelectionnees;
    }

    public static File getFileCategorieMotCibleV5() {
        return fileCategorieMotCibleV5;
    }

    public static File getFileCategorieMotDescripteurV5() {
        return fileCategorieMotDescripteurV5;
    }

    public static File getFileCategorieMotCibleV7() {
        return fileCategorieMotCibleV7;
    }

    public static File getFileCategorieMotDescripteurV7() {
        return fileCategorieMotDescripteurV7;
    }

    public static File getFileCategorieMotDescripteurV8() {
        return fileCategorieMotDescripteurV8;
    }

    public static File getFileCategorieMotCibleV8() {
        return fileCategorieMotCibleV8;
    }
    
    public static File getFileCategorieMotCibleV9() {
        return fileCategorieMotCibleV9;
    }

    public static File getFileCategorieMotDescripteurV9() {
        return fileCategorieMotDescripteurV9;
    }

    public static File getFileCategorieMotDescripteurV10() {
        return fileCategorieMotDescripteurV10;
    }

    public static File getFileThemeEtMetaThemeV1() {
        return fileThemeEtMetaThemeV1;
    }

    public static File getFileThemeEtMetaThemeV2() {
        return fileThemeEtMetaThemeV2;
    }
    
    
    
    
    
    
    
}
