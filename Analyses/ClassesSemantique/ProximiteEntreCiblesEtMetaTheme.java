/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.ClassesSemantique;

import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import AnalyseSimilitude.AnalyseSimilitude;
import Article.Article;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Fichier.Arff.ArffUtils;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Similitude.ProduitScalaire;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.InformationMutuelle;
import Pretraitement.Pretraitement;
import UtilsJFC.TrieUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
public class ProximiteEntreCiblesEtMetaTheme 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            File fileCible = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\motCible.csv");
            new ProximiteEntreCiblesEtMetaTheme().calculerProximites(fileCible, CorpusUtils.getFileArffVecteurMots(), CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, new InformationMutuelle());
            
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void calculerProximites(File fileMotCible, final File fileArffVecteurMots, final File filesArticle, TypeContenu typeContenu, CoefficientAssociation coefficient) throws FileNotFoundException, IOException
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
        
        // centroide theme
        Map<Integer, double[]> map_idClasse_centroide = new HashMap<>();
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        for (String c: map_classe_idVecteurs.keySet())
        {
//            System.out.println(c);
            int idC = Integer.parseInt(c);
            map_idClasse_centroide.put(idC, calculerCentroide(data, map_classe_idVecteurs.get(c)));
        }
        
        // centroide des meta-themes
        Map<String, String> map_idClasse_metaTheme = Dictionnaire.getMap_token_cat(Dictionnaire.getFileThemeEtMetaThemeV2());
        Map<String, double[]> map_meta_centroide = calculerCentroideMetaTheme(map_idClasse_centroide, map_idClasse_metaTheme);
        
//        System.out.println("nbre occurrences dans corpus : "+ CorpusUtils.getNombreOccurrence(Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticle, typeContenu)));
        
        Map<String, double[]> map_CatCible_centroide = getVecteurCatCible(fileMotCible, map_token_id, filesArticle, typeContenu, coefficient);
        Map<String, Map<String,Double>> vecteursSim = AnalyseSimilitude.calculerMetriqueEntreVecteursEtCoVecteurs(map_CatCible_centroide, map_meta_centroide, new ProduitScalaire(), false);
        
        AfficheurVecteur.afficherVecteurs("Cosinus entre cibles et classes", map_meta_centroide.keySet(), vecteursSim);
        
        
    }
    
    public Map<String, double[]> getVecteurCatCible (File fileMotCible, Map<String, Integer> map_token_id, File filesArticle, TypeContenu typeContenu, CoefficientAssociation coefficient) throws IOException
    {
        Map<String, String> map_tokenCible_catCible = CorpusUtils.getMap_token_catCible(fileMotCible);
        Map<String, Integer> map_cible_id = new TreeMap<>();
        int idCible = 0;
        for (String c: map_tokenCible_catCible.keySet())
        {
            map_cible_id.put(c, idCible);
            idCible++;
        }
        AfficheurTableau.afficher2Colonnes("tokenCible et id", "tokenCible", "id", map_cible_id);
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticle, typeContenu);

        TreeSet<String> dictionnaire = new TreeSet<>(map_token_id.keySet());
        dictionnaire.addAll(map_tokenCible_catCible.keySet());
//        dictionnaire.addAll(CorpusUtils.getTokens(articles));
        articles = Pretraitement.selectionnerTokenDuDictionnaire(articles, dictionnaire);
        System.out.println("nbre occurrences dans corpus apres pretraitement : "+ CorpusUtils.getNombreOccurrence(articles));
        
        Map<String, Integer> cible_nbredoc = CorpusUtils.getMap_token_nombreArticle(articles, map_cible_id.keySet());
        AfficheurTableau.afficher2Colonnes("nbre parag par cible", "cible", "n", cible_nbredoc);
        
        Map<Integer, double[]> vecteurCible = VectoriseurUtils.vectoriserAvecCoefficientAssociation5(CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_cible_id), CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id), CorpusUtils.getEnsembleIdArticle(articles), coefficient, 0.0);
        Map<String, double[]> map_CatCible_centroide = new HashMap<>();
        Map<String, Set<String>> map_CatCible_tokensCible = TrieUtils.recupererMap_valeur_ensembleCle2(map_tokenCible_catCible);
        
        for (String c: map_CatCible_tokensCible.keySet())
        {
            System.out.println(c);
            double[] centroide = new double[map_token_id.size()];
            Arrays.fill(centroide, 0.0);
            for (String tokenCible: map_CatCible_tokensCible.get(c))
            {
                System.out.println(tokenCible);
                int id = map_cible_id.get(tokenCible);
//                if (centroide.length!=vecteurCible.get(id).length)
//                {
//                    System.out.println("vecteurs pas de meme longueur");
//                    System.out.println("centoide: " + centroide.length);
//                    System.out.println("cible : " + vecteurCible.get(map_cible_id.get(tokenCible)).length);
//                    System.exit(1);
//                }
                if (vecteurCible.containsKey(id))
                    centroide = VecteurCreux.additionnerVecteurs(centroide, vecteurCible.get(map_cible_id.get(tokenCible)));
            }
            map_CatCible_centroide.put(c, VecteurCreux.normer(centroide));
        }
        return map_CatCible_centroide;        
    }
    
    private Map<String, double[]> calculerCentroideMetaTheme (Map<Integer, double[]> map_idClasse_centroide, Map<String, String> map_idClasse_metaTheme)
    {
        Map<String, Set<String>> map_meta_themes = TrieUtils.recupererMap_valeur_ensembleCle2(map_idClasse_metaTheme);
        int dim = new TreeMap<>(map_idClasse_centroide).firstEntry().getValue().length;
        Map<String, double[]> map_meta_centroide = new TreeMap<>();
        
        for (String meta: map_meta_themes.keySet())
        {
            System.out.println("meta: " +meta);
            double[] metaCentroide = new double[dim];
            Arrays.fill(metaCentroide, 0.0);
            for (String theme: map_meta_themes.get(meta))
            {
                int idClasse = Integer.parseInt(theme);
                metaCentroide = VecteurCreux.additionnerVecteurs(map_idClasse_centroide.get(idClasse), metaCentroide);
            }
            metaCentroide = VecteurCreux.normer(metaCentroide);
            map_meta_centroide.put(meta, metaCentroide);
        }
        return map_meta_centroide;
    }
    
    private double[] calculerCentroide(Instances data, Set<Integer> idInstances)
    {
        double[] v = new double[data.numAttributes()];
        Arrays.fill(v, 0.0);
        for (int i: idInstances)
            v = VecteurCreux.additionnerVecteurs(v, data.get(i).toDoubleArray());
        return VecteurCreux.normer(v);
    }
    
}
