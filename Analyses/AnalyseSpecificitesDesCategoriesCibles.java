/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses;

import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import Article.Article;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Matrice.Vectorisateur.VecteurIndicie;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Pretraitement.Pretraitement;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JF Chartier
 */
public class AnalyseSpecificitesDesCategoriesCibles 
{
    public static void analyser (File filesArticles, File fileCategoriesCibles, TypeContenu typeContenu, int nombreArticleSelectionne, CoefficientAssociation coefficient) throws IOException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
        articles = Pretraitement.remplacerMotParCategorie(articles, map_token_categorie);
        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        articles = new Pretraitement(true, true, true, 10, articles.size()-1000).filtrerArticle(articles);
        
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        Map<String, Integer> map_categorieCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_token_categorie.values());
        articles = CorpusUtils.recupererArticleSelonDictionnaire(articles, map_categorieCible_id.keySet());
        
        Map<Integer, List<Integer>> map_idArt_listeIdToken = CorpusUtils.getMap_idArticle_listeIdToken(articles, map_token_id);
        
//        Map<Integer, Set<Integer>> map_idCategorie_ensembleIdArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieCible_id);
        Map<Integer, List<Integer>> map_idArt_listeIdCategorie = getMap_idArt_listeIdCategorie(articles, map_categorieCible_id);
        Map<Integer, String> map_id_token = TrieUtils.inverserMap(map_token_id);
        Map<Integer, String> map_id_categorie = TrieUtils.inverserMap(map_categorieCible_id);
        
        Map<Integer, VecteurIndicie> map_idToken_vecteurAssociationIdCategorie = CorpusUtils.getMap_idToken_vecteurAssociationIdCategorie(map_idArt_listeIdToken, map_idArt_listeIdCategorie, ensembleIdArticle, coefficient);
//        for (VecteurIndicie v: map_idToken_vecteurAssociationIdCategorie.values())
//        {
//            System.out.println(map_id_token.get(v.getId()) + "\t"+v.getMap_Id_Ponderation().toString());
//        }
        
        AfficheurVecteur.afficherVecteurs("coefficient association entre token et categories", map_id_token, map_id_categorie, map_idToken_vecteurAssociationIdCategorie.values());
        
//        new Ponderation(new TreeMap(map_idToken_vecteurAssociationIdCategorie)).afficherTableauDesPonderationUnifParClasse(map_id_token, "coefficient association entre token et categories");
        
    }
    
    // cette methode ne calcul que les specificites des mots descripteurs selectionnes
    public static void analyser (File filesArticles, File fileCategoriesCibles, File fileCategoriesDescripteur, TypeContenu typeContenu, int nombreArticleSelectionne, CoefficientAssociation coefficient) throws IOException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Map<String, String> map_token_categorieCible = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
        articles = Pretraitement.remplacerMotParCategorie(articles, map_token_categorieCible);
        Map<String, String> map_token_categorieDescrip = Dictionnaire.recupererCategorieMot(fileCategoriesDescripteur);
                
        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        articles = new Pretraitement(true, true, true, 10, articles.size()-1000).filtrerArticle(articles);
        
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        Map<String, Integer> map_categorieCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_token_categorieCible.values());
        articles = CorpusUtils.recupererArticleSelonDictionnaire(articles, map_categorieCible_id.keySet());
                
        AfficheurTableau.afficher2Colonnes("map token et categorie Cible", "token", "categorie cible", map_token_categorieCible);
        Map<String, Integer> map_tokenDescript_id = CorpusUtils.getSousMap_token_id(map_token_id, map_token_categorieDescrip.keySet());
        AfficheurTableau.afficher2Colonnes("map token et categorie Descripteur","token", "categorie descipteur", map_token_categorieDescrip);
        
        
//        articles = CorpusUtils.recupererArticleSelonDictionnaire(articles, map_categorieCible_id.keySet());
        
//        Map<Integer, List<Integer>> map_idArt_listeIdToken = CorpusUtils.getMap_idArticle_listeIdToken(articles, map_token_id);
        
//        Map<Integer, Set<Integer>> map_idCategorie_ensembleIdArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieCible_id);
        Map<Integer, List<Integer>> map_idArt_listeIdCategorieCible = getMap_idArt_listeIdCategorie(articles, map_categorieCible_id);
        Map<Integer, List<Integer>> map_idArt_listeIdTokenDescript = getMap_idArt_listeIdCategorie(articles, map_tokenDescript_id);
        
        Map<Integer, String> map_id_tokenDescript = TrieUtils.inverserMap(map_tokenDescript_id);
        Map<Integer, String> map_id_categorieCible = TrieUtils.inverserMap(map_categorieCible_id);
        
        Map<Integer, VecteurIndicie> map_idTokenDescript_vecteurAssociationIdCategorie = CorpusUtils.getMap_idToken_vecteurAssociationIdCategorie(map_idArt_listeIdTokenDescript, map_idArt_listeIdCategorieCible, ensembleIdArticle, coefficient);

        AfficheurVecteur.afficherVecteurs("coefficient association entre token descripteur et categories cibles", map_id_tokenDescript, map_id_categorieCible, map_idTokenDescript_vecteurAssociationIdCategorie.values());
        
//        new Ponderation(new TreeMap(map_idToken_vecteurAssociationIdCategorie)).afficherTableauDesPonderationUnifParClasse(map_id_token, "coefficient association entre token et categories");
        
    }
    
    private static Map<Integer, List<Integer>> getMap_idArt_listeIdCategorie (Collection<Article> articles, Map<String, Integer> map_categorieCible_id)
    {
        articles = Pretraitement.selectionnerTokenDuDictionnaire(articles, map_categorieCible_id.keySet());
        return CorpusUtils.getMap_idArticle_listeIdToken(articles, map_categorieCible_id);
    }
}
