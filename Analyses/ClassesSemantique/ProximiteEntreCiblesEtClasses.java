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
public class ProximiteEntreCiblesEtClasses 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            File fileClasse = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\classification.csv");
            File fileCible = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\motCible.csv");
            File fileCorpus = CorpusUtils.getRepertoireArticleFiltreRevueSelectionne();
            new ProximiteEntreCiblesEtClasses().calculerProximites(fileCible, CorpusUtils.getFileArffVecteurMots(), CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, new InformationMutuelle());
            
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void calculerProximites(File fileMotCible, final File fileArffVecteurMots, final File filesParagFiltre, TypeContenu typeContenu, CoefficientAssociation coefficient) throws FileNotFoundException, IOException
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
        Map<String, double[]> map_CatCible_centroide = getVecteurCatCible(fileMotCible, map_token_id, filesParagFiltre, typeContenu, coefficient);
        Map<String, Map<Integer,Double>> vecteursSim = AnalyseSimilitude.calculerMetriqueEntreVecteursEtCoVecteurs(map_CatCible_centroide, map_idClasse_centroide, new ProduitScalaire(), false);
        
        AfficheurVecteur.afficherVecteurs("Cosinus entre cibles et classes", map_idClasse_centroide.keySet(), vecteursSim);
        
        
    }
    
    public Map<String, double[]> getVecteurCatCible (File fileMotCible, Map<String, Integer> map_token_id, File filesParagFiltre, TypeContenu typeContenu, CoefficientAssociation coefficient) throws IOException
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
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesParagFiltre, typeContenu);
        TreeSet<String> dictionnaire = new TreeSet<>(map_token_id.keySet());
        dictionnaire.addAll(map_tokenCible_catCible.keySet());
//        dictionnaire.addAll(CorpusUtils.getTokens(articles));
        articles = Pretraitement.selectionnerTokenDuDictionnaire(articles, dictionnaire);
        
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
    
    private double[] calculerCentroide(Instances data, Set<Integer> idInstances)
    {
        double[] v = new double[data.numAttributes()];
        Arrays.fill(v, 0.0);
        for (int i: idInstances)
            v = VecteurCreux.additionnerVecteurs(v, data.get(i).toDoubleArray());
        return VecteurCreux.normer(v);
    }
}
