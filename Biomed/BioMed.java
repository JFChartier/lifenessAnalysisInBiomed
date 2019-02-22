/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Biomed;

import Affichage.Tableau.AfficherTableau;
import Affichage.Tableau.AfficheurVecteur;
import Analyses.AnalyseDesAssociationEntreCategoriesCiblesEtDescripteurs;
import Analyses.AnalyseDesMotsPlusSimilaires;
import Analyses.AnalyseMDS;
import Analyses.AnalyseReductionMDSDesCategoriesCibles;
import Analyses.AnalyseSpecificitesDesCategoriesCibles;
import Article.Article;
import Corpus.BioMedAttributs;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Fichier.FileUtils;
import MDS.MDS;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Distance.DistanceEuclidienne;
import Metrique.Similitude.Cosinus;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.CoefficientChi2;
import Ponderation.ProbConditionnelle1;
import Ponderation.CoefficientProbConditionnelle2;
import Ponderation.CorrelationMatthews;
import Ponderation.CorrelationMatthewsPositive;
import Ponderation.Independance1;
import Ponderation.InformationMutuelle;
import Ponderation.InformationMutuellePositive;
import Ponderation.NormalisePMI;
import Ponderation.PointwiseMutualInformation;
import Ponderation.Ponderation;
import Ponderation.PositiveNPMI;
import Ponderation.PositivePointwiseMutualInformation;
import Pretraitement.Pretraitement;
import Specificites.CalculateurSpecificites;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import no.uib.cipr.matrix.NotConvergedException;
import org.jdom2.JDOMException;

/**
 *
 * @author JF Chartier
 */
public class BioMed 
{
    public static final File rapportSim = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Lanci\\Projet sur BioMed\\rapportSim.txt");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, JDOMException, NotConvergedException 
    {
       //**** probabliteConditoonnelle1 = P(descripteur|cible) 
        
        
//        new BioMedAttributs().recupererMap_momRevue_frequence(CorpusUtils.getRepertoireArticle());
//        new BioMedAttributs().recupererMap_anneePublication_frequence(CorpusUtils.getRepertoireArticle());
//        
//        AnalyseSpecificitesDesCategoriesCibles.analyser(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV7(), TypeContenu.PARAGRAPHE, 300000, new InformationMutuellePositive());
//        AnalyseSpecificitesDesCategoriesCibles.analyser(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV8(), Dictionnaire.getFileCategorieMotDescripteurV8(), TypeContenu.PARAGRAPHE, -1, new CorrelationMatthewsPositive());
//        AnalyseMDS.analyser(CorpusUtils.getRepertoireArticleFiltreLanci(), Dictionnaire.getFileCategorieMotCibleV5(), TypeContenu.PARAGRAPHE, 10000, new  CorrelationMatthews());
        
//        String rapport = AnalyseDesMotsPlusSimilaires.analyser(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV5(), TypeContenu.ARTICLE, 20000, 300, 50, new Cosinus());
//        FileUtils.write(rapport, rapportSim.getPath());
        
        AnalyseDesAssociationEntreCategoriesCiblesEtDescripteurs.analyser(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV8(), Dictionnaire.getFileCategorieMotDescripteurV9(), new ProbConditionnelle1(), TypeContenu.PHRASE, 500000);
//        AnalyseDesAssociationEntreCategoriesCiblesEtDescripteurs.analyser(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV8(), Dictionnaire.getFileCategorieMotDescripteurV8(), new Independance1(), TypeContenu.PARAGRAPHE, -1);
//        AnalyseDesAssociationEntreCategoriesCiblesEtDescripteurs.analyserLesMots(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV8(), Dictionnaire.getFileCategorieMotDescripteurV9(), new PositiveNPMI(), TypeContenu.ARTICLE, -1);
        
    }
    
    public void analyseDesSpecificitesDesCategoriesCibles (File filesArticles, File fileCategoriesCibles, TypeContenu typeContenu, int nombreArticleSelectionne, CoefficientAssociation coefficient) throws IOException
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
    
    public void analyseMSDDesDesCategoriesCibles (File filesArticles, File fileCategoriesCibles, TypeContenu typeContenu, int nombreArticleSelectionne, CoefficientAssociation coefficient) throws IOException
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
        new AfficherTableau(null, "id categorie", "categorie", map_id_categorie).setVisible(true);
        Map<Integer, VecteurIndicie> map_idCategorie_vecteur = CorpusUtils.getMap_idToken_vecteurAssociationIdCategorie(map_idArt_listeIdCategorie, map_idArt_listeIdToken, ensembleIdArticle, coefficient);
        MDS.calculerReductionDimensionelle(VectoriseurUtils.convertir(map_idCategorie_vecteur.values()), new DistanceEuclidienne());
        
    }
    
    
    
}
