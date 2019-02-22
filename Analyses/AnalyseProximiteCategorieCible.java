/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses;

import Affichage.Tableau.AfficherTableau;
import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import AnalyseSimilitude.AnalyseSimilitude;
import Article.Article;
import Classification.Partition;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.Vectoriseur;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Metrique;
import Metrique.Similitude.CorrelationPearsonIndicie;
import Metrique.Similitude.CosinusIndicie;
import Metrique.Similitude.ProduitScalaireIndicie;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.InformationMutuelle;
import Pretraitement.Pretraitement;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author JF Chartier
 */
public class AnalyseProximiteCategorieCible 
{
    public static void main(String[] args)
    {
                
        try
        {
            

            AnalyseProximiteCategorieCible.analyseCorrelationEntreCategorieCible(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV8(), Dictionnaire.getFileCategorieMotDescripteurV9(), new InformationMutuelle(), TypeContenu.PARAGRAPHE, 20000);
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    // vecteur d'information mutuelle des mots cibles et mots descripteurs
    // addition des vecturs mots selon leur catégorie
    // Correlation entre entre la proximite des vecteurs categorie cible et des vecteurs categorie descripteur
    // dans les paragraphe
    // est-ce que la presence d'une categorie cibles est correlee a la presence d'une categorie descripteur? 
    public static void analyseCorrelationEntreCategorieCible (File filesArticles, File fileCategorieMotCible, File fileCategorieMotDescripteur, CoefficientAssociation coefficient, TypeContenu typeContenu, int nombreContexte) throws IOException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
       
        if (nombreContexte!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreContexte);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        Pretraitement.initialiserIdArticle(articles);
        articles = new Pretraitement(true, true, true, 10, articles.size()/3).filtrerArticle(articles);
        articles = Pretraitement.raciner(articles);
        
        Map<String, String> map_motCible_categorie = Dictionnaire.recupererCategorieMot(fileCategorieMotCible);
        map_motCible_categorie = Pretraitement.raciner(map_motCible_categorie);
        Map<String, Integer> map_catCible_id =  getCategorie_id(new TreeSet<>(map_motCible_categorie.values()));
        Map<String, String> map_motDescripteur_categorie = Dictionnaire.recupererCategorieMot(fileCategorieMotDescripteur);
        map_motDescripteur_categorie = Pretraitement.raciner(map_motDescripteur_categorie);
        Map<String, Integer> map_catDesc_id =  getCategorie_id(new TreeSet<>(map_motDescripteur_categorie.values()));
        
        AfficheurTableau.afficher2Colonnes("map categorie Cible et id", "categorie", "id", map_catCible_id);
        AfficheurTableau.afficher2Colonnes("categorie cibles", "categorie", "frequence doc", CorpusUtils.getMap_token_nombreArticle(articles, map_motCible_categorie.keySet()));
        AfficheurTableau.afficher2Colonnes("map categorie Descripteur et id","categorie", "id", map_catDesc_id);
        AfficheurTableau.afficher2Colonnes("categorie descripteur", "categorie", "frequence doc", CorpusUtils.getMap_token_nombreArticle(articles, map_motDescripteur_categorie.keySet()));
        
        
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        Map<String, Integer> map_motCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_motCible_categorie.keySet());
//        AfficheurTableau.afficher2Colonnes("categorie cible et id", "categorie", "id", map_motCible_id);
        
        Map<String, Integer> map_motDescripteur_id = CorpusUtils.getSousMap_token_id(map_token_id, map_motDescripteur_categorie.keySet());
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
//        Map<Integer, Set<Integer>> map_idMotCible_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_motCible_id);
//        Map<Integer, Set<Integer>> map_idMotDesc_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_motDescripteur_id);
        
        Map<Integer, VecteurIndicie> vecteursToken = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArticle, map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
        Vectoriseur.normerVecteurIndicie2(vecteursToken.values());
        
//        Map<Integer, VecteurIndicie> vecteursMotCible = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idMotCible_idsArticle, map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient);
//        Vectoriseur.normerVecteurIndicie2(vecteursMotCible.values());
//        Map<Integer, VecteurIndicie> vecteursMotDescr = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idMotDesc_idsArticle, map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient);
//        Vectoriseur.normerVecteurIndicie2(vecteursMotDescr.values());
        
        Set<Integer> idTokenCible = new TreeSet<Integer>(map_motCible_id.values());
        Set<Integer> idTokenDesc = new TreeSet<Integer>(map_motDescripteur_id.values());
        
//        Collection<VecteurIndicie> vecteurCatCible = CorpusUtils.additionnerEtNormerVecteurToken(getMap_idCategorie_idTokens(map_motCible_id, map_motCible_categorie, map_catCible_id), TrieUtils.getSubMap(vecteursToken, idTokenCible)).values();
//        Collection<VecteurIndicie> vecteurCatDesc = CorpusUtils.additionnerEtNormerVecteurToken(getMap_idCategorie_idTokens(map_motDescripteur_id, map_motDescripteur_categorie, map_catDesc_id), TrieUtils.getSubMap(vecteursToken, idTokenDesc)).values();
        articles = Pretraitement.filtreAleatoireArticle(articles, 10000);
        Pretraitement.initialiserIdArticle(articles);
        Collection<VecteurIndicie> vecteursDoc = VectoriseurUtils.additionnerVecteurs(CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), vecteursToken).values();
//        Collection<VecteurIndicie> vecteursDoc = CorpusUtils.additionnerEtNormerVecteurToken(CorpusUtils.getMap_idArticle_ensembleIdToken(Pretraitement.filtreAleatoireArticle(articles, 10000), map_token_id), vecteursToken).values();
//        analyserCorrelationEntreTheme(vecteursDoc.values(), vecteurCatCible, vecteurCatDesc);
        
        analyserCorrelationEntreTheme(
                vecteursDoc,//CorpusUtils.additionnerEtNormerVecteurToken(CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), vecteursToken).values(), 
                VectoriseurUtils.additionnerVecteurs(getMap_idCategorie_idTokens(map_motCible_id, map_motCible_categorie, map_catCible_id), TrieUtils.getSubMap(vecteursToken, idTokenCible)).values(), 
                VectoriseurUtils.additionnerVecteurs(getMap_idCategorie_idTokens(map_motDescripteur_id, map_motDescripteur_categorie, map_catDesc_id), TrieUtils.getSubMap(vecteursToken, idTokenDesc)).values());
        
    }
    
    public static void analyserCorrelationEntreTheme (final Collection<VecteurIndicie> map_idArt_vecteurIdToken, final Collection<VecteurIndicie> map_idTokenCible_vecteurIdToken, final Collection<VecteurIndicie> map_idTokenDescrip_vecteurIdToken)
    {
        System.out.println("analyserCorrelationEntreTheme");
        // normer afin d'utiliser ensuite le produit scalaire a la place du cosinus
        VectoriseurUtils.normerVecteurIndicieB(map_idArt_vecteurIdToken);
        VectoriseurUtils.normerVecteurIndicieB(map_idTokenCible_vecteurIdToken);
        VectoriseurUtils.normerVecteurIndicieB(map_idTokenDescrip_vecteurIdToken);
        
        Map<Integer, VecteurIndicie> map_idCategorieCible_vecteurSimAvecArt = AnalyseSimilitude.calculerMetriqueEntreVecteurs(map_idTokenCible_vecteurIdToken, map_idArt_vecteurIdToken, new ProduitScalaireIndicie());
        Map<Integer, VecteurIndicie> map_idCategorieDesc_vecteurSimAvecArt = AnalyseSimilitude.calculerMetriqueEntreVecteurs(map_idTokenDescrip_vecteurIdToken, map_idArt_vecteurIdToken, new ProduitScalaireIndicie());
        Map<Integer, VecteurIndicie> map_idCategorieCible_correlation = AnalyseSimilitude.calculerMetriqueEntreVecteurs(map_idCategorieCible_vecteurSimAvecArt.values(), map_idCategorieDesc_vecteurSimAvecArt.values(), new CorrelationPearsonIndicie(map_idArt_vecteurIdToken.size()));
        AfficheurVecteur.afficherVecteurs("correlation de pearson enre categorie cible et categorie descripteur", map_idCategorieDesc_vecteurSimAvecArt.keySet(), map_idCategorieCible_correlation.values());
    }
    
    private static Map<Integer, Set<Integer>> getMap_idCategorie_idTokens (Map<String, Integer> map_token_id, Map<String, String> map_token_categorie, Map<String, Integer> map_categorie_id)
    {
        Map<String, Set<String>> map_cat_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_categorie);
        Map<Integer, Set<Integer>> map = new HashMap<>(map_cat_tokens.size());
        
        for (String cat: map_cat_tokens.keySet())
        {
            Set<Integer> set = new TreeSet<>();
            for (String t: map_cat_tokens.get(cat))
            {
                if (map_token_id.containsKey(t))
                    set.add(map_token_id.get(t));
            }
            
            map.put(map_categorie_id.get(cat), set);
            System.out.println("taille map_idCat_idsTokens : " +map.size());
            System.out.println(map);
        }
        return map;
    }
    
////    private static Map<Integer, Set<Integer>> getMap_idCategorie_idToken (Map<String, Integer> map_token_id, Map<String, String> map_token_categorie)
////    {
////        Map<String, Set<String>> map_cat_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_categorie);
////        Map<Integer, Set<Integer>> map = new HashMap<>(map_cat_tokens.size());
////        int idCat = 0;
////        for (String s: map_cat_tokens.keySet())
////        {
////            Set<Integer> set = new HashSet<>(map_cat_tokens.get(s).size());
////            for (String t: map_cat_tokens.get(s))
////                set.add(map_token_id.get(t));
////            map.put(idCat, set);
////            idCat++;
////        }
////        return map;
////    }
    
    private static Map<String, Integer> getCategorie_id (Set<String> categorie)
    {
        Map<String, Integer> m = new HashMap<>(categorie.size());
        int id = 0;
        for (String s: categorie)
        {
            m.put(s, id);
            id++;
        }
        return m;
    }
}
