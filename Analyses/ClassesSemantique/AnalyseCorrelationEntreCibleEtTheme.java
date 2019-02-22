/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.ClassesSemantique;

import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import Article.Article;
import Corpus.CorpusUtils;
import Fichier.Arff.ArffUtils;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.VectoriseurUtils;
import Parser.TypeContenu;
import Ponderation.CorrelationMatthews;
import Ponderation.Independance1;
import Ponderation.NormalisePMI;
import Ponderation.ProbConditionnelle1;
import UtilsJFC.TrieUtils;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author JF Chartier
 */
public class AnalyseCorrelationEntreCibleEtTheme 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            File fileClasse = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\classification.csv");
            File fileCible = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\motCible.csv");
            File fileCorpus = CorpusUtils.getRepertoireArticleFiltreRevueSelectionne();
            new AnalyseCorrelationEntreCibleEtTheme().calculerAssociation(fileCorpus, TypeContenu.PARAGRAPHE, fileCible, fileClasse, -1);
            
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
        public void calculerAssociation(File filesArticles, TypeContenu typeContenu, File fileMotCible, File fileClassification, int nombreArticleSelectionne) throws IOException
    {
        Map<String, Integer> map_token_idClasse = getClasse(fileClassification);
        Map<String, String> map_token_catCible = getCible(fileMotCible);
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        
        TreeSet<String> dictionnaire = new TreeSet<>(map_token_idClasse.keySet());
        dictionnaire.addAll(map_token_catCible.keySet());
        dictionnaire.addAll(CorpusUtils.getTokens(articles));
//        articles = Pretraitement.Pretraitement.selectionnerTokenDuDictionnaire(articles, dictionnaire);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        Map<String, Integer> map_token_freqDoc = CorpusUtils.getMap_token_nombreArticle(articles, map_token_catCible.keySet());
        AfficheurTableau.afficher2Colonnes("token et frequence", "token", "nombre document", map_token_freqDoc);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        
        Map<Integer, Set<Integer>> map_idCategorie_idsArts = getMap_idCategorieCible_idsArticle(map_token_catCible, map_idToken_idsArticle, map_token_id);
        Map<Integer, Set<Integer>> map_idNewClasses_idsArts = getMap_idCategorieCible_idsArticle(map_token_idClasse, map_idToken_idsArticle, map_token_id);
        getAssociationEntreMotCibleEtClasse(map_idCategorie_idsArts, map_idNewClasses_idsArts, CorpusUtils.getEnsembleIdArticle(articles));
    }
        
    public void calculerVecteurParagsEtCentroides(File fileMotCible, final File fileArffVecteurMots, final File filesParagFiltre) throws FileNotFoundException, IOException
    {
        System.out.println("recupererVecteursEtCentroides");
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_idV_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
        Map<String, Integer> map_token_id = TrieUtils.inverserMap(map_idV_token);
        data.deleteAttributeAt(data.attribute("@@name@@").index());       
//        Map<Integer, VecteurCreux> map_idToken_vecteur = ArffUtils.getDenseVector(data, 0.0);
        
        // centroide
        Map<Integer, double[]> map_idClasse_centroide = new HashMap<>();
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        for (String c: map_classe_idVecteurs.keySet())
        {
//            System.out.println(c);
            int idC = Integer.parseInt(c);
            map_idClasse_centroide.put(idC, calculerCentroide(data, map_classe_idVecteurs.get(c)));
        }
        
        
        
        
    }
        
    public Map<String, double[]> getVecteurCatCible (File fileMotCible, Map<String, Integer> map_token_id, File filesParagFiltre, TypeContenu typeContenu) throws IOException
    {
        Map<String, String> map_tokenCible_catCible = getCible(fileMotCible);
//        Map<String, Integer> tokenCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_tokenCible_catCible.keySet());
        Map<String, double[]> map_CatCible_centroide = new HashMap<>();
        Map<String, Set<String>> map_CatCible_tokensCible = TrieUtils.recupererMap_valeur_ensembleCle2(map_tokenCible_catCible);
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesParagFiltre, typeContenu);
        TreeSet<String> dictionnaire = new TreeSet<>(map_token_id.keySet());
        dictionnaire.addAll(map_tokenCible_catCible.keySet());
        dictionnaire.addAll(CorpusUtils.getTokens(articles));
        
        for (String c: map_CatCible_tokensCible.keySet())
        {
//            System.out.println(c);
            Set<Integer> idInstances = new HashSet<>(map_CatCible_tokensCible.get(c).size());
            for (String tokenCible: map_CatCible_tokensCible.get(c))
            {
                if (map_token_id.containsKey(tokenCible))
                    idInstances.add(map_token_id.get(tokenCible));
            }
            map_CatCible_centroide.put(c, calculerCentroide(data, idInstances));
        }
        
    }
    
    private double[] calculerCentroide(Instances data, Set<Integer> idInstances)
    {
        double[] v = new double[data.numAttributes()];
        Arrays.fill(v, 0.0);
        for (int i: idInstances)
            v = VecteurCreux.additionnerVecteurs(v, data.get(i).toDoubleArray());
        return VecteurCreux.normer(v);
    }
    
    public static <V> Map<Integer, Set<Integer>> getMap_idCategorieCible_idsArticle (Map<String, V> map_token_categorieCible, Map<Integer, Set<Integer>> map_idToken_idsArticle, Map<String, Integer> map_token_id)
    {
        Map<Integer, Set<Integer>> map_idCat_idsArticle = new TreeMap<>();
        Map<V, Set<String>> cat_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_categorieCible);
        Map<V, Integer> map_cat_id = new TreeMap<>();
        
        int idCible = 0;
        for (V c: cat_tokens.keySet())
        {
            map_cat_id.put(c, idCible);
            Set<Integer> idsArticle = new TreeSet<>();
            for (String t: cat_tokens.get(c))
            {
                if (map_token_id.get(t)!=null)
                {
                    int id = map_token_id.get(t);
                    idsArticle.addAll(map_idToken_idsArticle.get(id));
                }
            }
            map_idCat_idsArticle.put(idCible, idsArticle);
            idCible++;
        }
        AfficheurTableau.afficher2Colonnes("categorie et nouveau id", "categorie", "id", map_cat_id);
        return map_idCat_idsArticle;
    }
    
    // cette methode a sa version static dans CorpusUtils
    public Map<String, Integer> getClasse (File fileClassification) throws FileNotFoundException, IOException
    {
        // on remplace le Reader par un InputStreameReader qui est sa super classe, afin de choisir l'encodage
        // Le Reader de OpenCSV ne permet pas de choisir l'encodage
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileClassification), "UTF-8"), ';' , '"' , 1);
        Map<String, Integer> map = new HashMap<>();
        
        //Read CSV line by line and use the string array as you want
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) 
        {
           if (nextLine != null) 
           {
               map.put(nextLine[0], Integer.parseInt(nextLine[1]));
           }
        }
        AfficheurTableau.afficher2Colonnes("token et classe", "token", "classe", map);
        return map;
    }
    
    // cette methode a sa version static dans CorpusUtils
    public Map<String, String> getCible (File fileMotCible) throws FileNotFoundException, IOException
    {
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileMotCible), "UTF-8"), ';' , '"' , 1);
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
    
    
    protected static void getAssociationEntreMotCibleEtClasse (Map<Integer, Set<Integer>> map_idToken_idsArt, Map<Integer, Set<Integer>> map_idClasse_idsArts, Set<Integer> idsArts) throws IOException
    {
        Map<Integer, VecteurIndicie> vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, idsArts, new ProbConditionnelle1(), true);
        AfficheurVecteur.afficherVecteurs("Pr(theme|categorieCible)", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, idsArts, new Independance1(), true);
        AfficheurVecteur.afficherVecteurs("probabilite a priori du theme independant de la presence de la categorie cible", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, idsArts, new CorrelationMatthews(), true);
        AfficheurVecteur.afficherVecteurs("corelation entre le theme et la categorie cible", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, idsArts, new NormalisePMI(), true);
        AfficheurVecteur.afficherVecteurs("PMI normalise entre le theme et la categorie cible", map_idClasse_idsArts.keySet(), vecteurs.values()); 
    }
}
