/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses;

import Affichage.Tableau.AfficherTableau;
import Article.Article;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import MDS.MDS;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Distance.DistanceEuclidienne;
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
public class AnalyseReductionMDSDesCategoriesCibles 
{
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
        map_idCategorie_vecteur = VectoriseurUtils.dichotomiserSeuilMax(0.1, map_idCategorie_vecteur.values());
        MDS.calculerReductionDimensionelle(VectoriseurUtils.convertir(map_idCategorie_vecteur.values()), new DistanceEuclidienne());
        
    }
}
