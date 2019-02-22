/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses;

import Affichage.Tableau.AfficherTableau;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author JF Chartier
 */
public class AnalyseDesAssociationEntreCategoriesCiblesEtDescripteurs 
{
    public static void analyser (File filesArticles, File fileCategorieMotCible, File fileCategorieMotDescripteur, CoefficientAssociation coefficient, TypeContenu typeContenu, int nombreContexte) throws IOException
    {
        System.out.println("AnalyseDesAssociationEntreCategoriesCiblesEtDescripteurs");
        
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
       
        if (nombreContexte!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreContexte);
        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
//        articles = new Pretraitement(true, true, true, 1, articles.size()).filtrerOeuvres(articles);
                        
        Map<String, String> map_motCible_categorie = Dictionnaire.recupererCategorieMot(fileCategorieMotCible);
        Map<String, String> map_motDescripteur_categorie = Dictionnaire.recupererCategorieMot(fileCategorieMotDescripteur);
        AfficheurTableau.afficher2Colonnes("map token et categorie Cible", "token", "categorie cible", map_motCible_categorie);
        AfficheurTableau.afficher2Colonnes("map token et categorie Descripteur","token", "categorie descipteur", map_motDescripteur_categorie);
        
        
        articles = Pretraitement.remplacerMotParCategorie(articles, map_motCible_categorie);
        articles = Pretraitement.remplacerMotParCategorie(articles, map_motDescripteur_categorie);
        Set<String> categories = new TreeSet<>(map_motCible_categorie.values());
        categories.addAll(map_motDescripteur_categorie.values());
        articles = Pretraitement.selectionnerTokenDuDictionnaire(articles, categories);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        
        Map<String, Integer> map_categorieCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_motCible_categorie.values());
        new AfficherTableau("categorie cibles", "categorie", "frequence doc", CorpusUtils.getMap_token_nombreArticle(articles, map_categorieCible_id.keySet())).setVisible(true);
        new AfficherTableau("categorie cibles", "categorie", "frequence tot", CorpusUtils.getToken_frequence(articles, map_categorieCible_id.keySet())).setVisible(true);
        
        Map<String, Integer> map_categorieDescripteur_id = CorpusUtils.getSousMap_token_id(map_token_id, map_motDescripteur_categorie.values());
        new AfficherTableau("mots descripteurs", "categorie", "frequence doc", CorpusUtils.getMap_token_nombreArticle(articles, map_categorieDescripteur_id.keySet())).setVisible(true);
        new AfficherTableau("mots descripteurs", "categorie", "frequence tot", CorpusUtils.getToken_frequence(articles, map_categorieDescripteur_id.keySet())).setVisible(true);
        
        Map<Integer, Set<Integer>> map_idCatCible_ensembleIdArt = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieCible_id);
        Map<Integer, Set<Integer>> map_idCatDescripteur_ensembleIdArt = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieDescripteur_id);
                
        Map<Integer, VecteurIndicie> map_idCategorieCible_vecteurAssociationIdCategorieDescripteur = CorpusUtils.getMap_idVariable_vecteurAssociationIdCoVariable(map_idCatCible_ensembleIdArt, map_idCatDescripteur_ensembleIdArt, ensembleIdArticle, coefficient);
        AfficheurVecteur.afficherVecteurs("coefficient association entre categorie cible et categorie descripteur", TrieUtils.inverserMap(map_categorieCible_id), TrieUtils.inverserMap(map_categorieDescripteur_id), map_idCategorieCible_vecteurAssociationIdCategorieDescripteur.values());
        
        
    }
    
    public static void analyserLesMots (File filesArticles, File fileCategorieMotCible, File fileCategorieMotDescripteur, CoefficientAssociation coefficient, TypeContenu typeContenu, int nombreContexte) throws IOException
    {
        System.out.println("AnalyseDesAssociationEntreMotsCiblesEtMotsDescripteurs");
        
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
       
        if (nombreContexte!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreContexte);
        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
//        articles = new Pretraitement(true, true, true, 1, articles.size()).filtrerOeuvres(articles);
                        
        Map<String, String> map_motCible_categorie = Dictionnaire.recupererCategorieMot(fileCategorieMotCible);
        Map<String, String> map_motDescripteur_categorie = Dictionnaire.recupererCategorieMot(fileCategorieMotDescripteur);
        AfficheurTableau.afficher2Colonnes("map token et categorie Cible", "token", "categorie cible", map_motCible_categorie);
        AfficheurTableau.afficher2Colonnes("map token et categorie Descripteur","token", "categorie descipteur", map_motDescripteur_categorie);
        
        
//        articles = Pretraitement.remplacerMotParCategorie(articles, map_motCible_categorie);
//        articles = Pretraitement.remplacerMotParCategorie(articles, map_motDescripteur_categorie);
        Set<String> categories = new TreeSet<>(map_motCible_categorie.keySet());
        categories.addAll(map_motDescripteur_categorie.keySet());
        articles = Pretraitement.selectionnerTokenDuDictionnaire(articles, categories);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        
        Map<String, Integer> map_categorieCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_motCible_categorie.keySet());
        new AfficherTableau("categorie cibles", "categorie", "frequence doc", CorpusUtils.getMap_token_nombreArticle(articles, map_categorieCible_id.keySet())).setVisible(true);
        new AfficherTableau("categorie cibles", "categorie", "frequence tot", CorpusUtils.getToken_frequence(articles, map_categorieCible_id.keySet())).setVisible(true);
        
        Map<String, Integer> map_categorieDescripteur_id = CorpusUtils.getSousMap_token_id(map_token_id, map_motDescripteur_categorie.keySet());
        new AfficherTableau("mots descripteurs", "categorie", "frequence doc", CorpusUtils.getMap_token_nombreArticle(articles, map_categorieDescripteur_id.keySet())).setVisible(true);
        new AfficherTableau("mots descripteurs", "categorie", "frequence tot", CorpusUtils.getToken_frequence(articles, map_categorieDescripteur_id.keySet())).setVisible(true);
        
        Map<Integer, Set<Integer>> map_idCatCible_ensembleIdArt = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieCible_id);
        Map<Integer, Set<Integer>> map_idCatDescripteur_ensembleIdArt = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_categorieDescripteur_id);
                
        Map<Integer, VecteurIndicie> map_idCategorieCible_vecteurAssociationIdCategorieDescripteur = CorpusUtils.getMap_idVariable_vecteurAssociationIdCoVariable(map_idCatCible_ensembleIdArt, map_idCatDescripteur_ensembleIdArt, ensembleIdArticle, coefficient);
        AfficheurVecteur.afficherVecteurs("coefficient association entre mots cibles et mots descripteurs", TrieUtils.inverserMap(map_categorieCible_id), TrieUtils.inverserMap(map_categorieDescripteur_id), map_idCategorieCible_vecteurAssociationIdCategorieDescripteur.values());
        
        
    }
}
