/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses;

import Affichage.Tableau.AfficherTableau;
import Affichage.Tableau.AfficheurTableau;
import Analyses.SegmentsPlusProchesDesCentroides.GetSegmentTopicSerie;
import Article.Article;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Fichier.Arff.ArffUtils;
import MDS.MDS;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Distance.DistanceEuclidienne;
import Metrique.Distance.ProduitScalaireInverse;
import Metrique.Similitude.ProduitScalaire;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author JF Chartier
 */
public class AnalyseMDS 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            
//            ForkJoinPool commonPool = ForkJoinPool.commonPool();
//            System.out.println(commonPool.getParallelism());
            
            File fileArffVecteurMots = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMotsAvecClasseEtNom.arff");
            
            AnalyseMDS.mdsFromArffWithCentroides(fileArffVecteurMots);
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    
    
    public static void analyser (File filesArticles, File fileCategoriesCibles, TypeContenu typeContenu, int nombreArticleSelectionne, CoefficientAssociation coefficient) throws IOException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        articles = new Pretraitement(true, true, true, 1, articles.size()).filtrerArticle(articles);
        
        Map<String, String> map_descripteur_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
        articles = Pretraitement.remplacerMotParCategorie(articles, map_descripteur_categorie);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
//        Map<Integer, Set<Integer>> map_idToken_ensembleIdArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        Map<Integer, List<Integer>> map_idArt_listeIdToken = CorpusUtils.getMap_idArticle_listeIdToken(articles, map_token_id);
        
        Map<String, Integer> map_categorieCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_descripteur_categorie.values());
        articles = CorpusUtils.recupererArticleSelonDictionnaire(articles, map_categorieCible_id.keySet());
        articles = Pretraitement.selectionnerTokenDuDictionnaire(articles, map_categorieCible_id.keySet());
//        Map<Integer, Set<Integer>> map_idCategorie_ensembleIdArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieCible_id);
        Map<Integer, List<Integer>> map_idArt_listeIdCategorie = CorpusUtils.getMap_idArticle_listeIdToken(articles, map_categorieCible_id);
//        Map<Integer, String> map_id_token = TriageCollection.inverserMap(map_token_id);
        Map<Integer, String> map_id_categorie = TrieUtils.inverserMap(map_categorieCible_id);
        new AfficherTableau("", "id categorie", "categorie", map_id_categorie).setVisible(true);
        Map<Integer, VecteurIndicie> map_idCategorie_vecteur = CorpusUtils.getMap_idToken_vecteurAssociationIdCategorie(map_idArt_listeIdCategorie, map_idArt_listeIdToken, ensembleIdArticle, coefficient);
        map_idCategorie_vecteur = VectoriseurUtils.dichotomiserSeuilMax(0.2, map_idCategorie_vecteur.values());
        MDS.calculerReductionDimensionelle(VectoriseurUtils.convertir(map_idCategorie_vecteur.values(), 0.0), new DistanceEuclidienne());
        
    }
    
    public static void mdsFromArff(final File fileArffVecteurMots) throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
//        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_idV_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
//        Map<String, Integer> map_token_id = TrieUtils.inverserMap(map_idV_token);
        data.deleteAttributeAt(data.attribute("@@name@@").index());    
        
        Map<Integer, VecteurCreux> vecteurs = new HashMap<>(data.size());
        for (int id: map_idV_token.keySet())
        {
            vecteurs.put(id, new VecteurCreux(id, data.get(id).toDoubleArray()));
        }
        AfficheurTableau.afficher2Colonnes("tokens", "id", "token", map_idV_token);
        MDS.calculerReductionDimensionelle(vecteurs, new DistanceEuclidienne());
    }
    
    public static void mdsFromArffWithCentroides(final File fileArffVecteurMots) throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_idV_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
//        Map<String, Integer> map_token_id = TrieUtils.inverserMap(map_idV_token);
        data.deleteAttributeAt(data.attribute("@@name@@").index());   
        
        Map<Integer, VecteurCreux> vecteurs = new HashMap<>(data.size());
//        for (int id: map_idV_token.keySet())
//        {
//            vecteurs.put(id, new VecteurCreux(id, data.get(id).toDoubleArray()));
//        }
        AfficheurTableau.afficher2Colonnes("tokens", "id", "token", map_idV_token);
        
        // centroide
//        Map<Integer, double[]> map_idClasse_centroide = new HashMap<>();
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        for (String c: map_classe_idVecteurs.keySet())
        {
//            System.out.println(c);
            int idC = -Integer.parseInt(c);
            vecteurs.put(idC, new VecteurCreux(idC, calculerCentroide(data, map_classe_idVecteurs.get(c))));
//            map_idClasse_centroide.put(idC, calculerCentroide(data, map_classe_idVecteurs.get(c)));
        }
        vecteurs = VectoriseurUtils.normerVecteurCreuxB(vecteurs.values());
        MDS.calculerReductionDimensionelle(vecteurs, new ProduitScalaireInverse());
    }
    
    private static double[] calculerCentroide(Instances data, Set<Integer> idInstances)
    {
        double[] v = new double[data.numAttributes()];
        Arrays.fill(v, 0.0);
        for (int i: idInstances)
            v = VecteurCreux.additionnerVecteurs(v, data.get(i).toDoubleArray());
        return VecteurCreux.normer(v);
    }
    
}
