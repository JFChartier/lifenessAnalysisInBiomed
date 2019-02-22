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
import Classification.Classe;
import Classification.Partition;
import ClassificationAutomatique.NonSupervisee.KMeansPlusPlus.ClassifieurKmeansPlusPlus;
import ClassificationAutomatique.NonSupervisee.KMeansPlusPlus.KmeansPlusPlusMax;
import ClassificationAutomatique.NonSupervisee.WrapperCommonMath.WrapperClassifieur;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Evaluateur.ApproximateurNombreClasse.ApproximateurNombreClasse;
import Evaluateur.AvecCentroide.EvaluateurAvecCentroide;
import Evaluateur.AvecCentroide.Silhouette.EvaluateurSilhouette;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.Vectoriseur;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Distance.DistanceCosinusCommonMath;
import Metrique.Distance.ProduitScalaireInverse;
import Metrique.Distance.ProduitScalaireInverseCommonMath;
import Metrique.Similitude.Cosinus;
import Metrique.Similitude.MetriqueSim;
import Metrique.Similitude.ProduitScalaire;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.CorrelationMatthews;
import Ponderation.InformationMutuelle;
import Ponderation.InformationMutuellePositive;
import Pretraitement.Pretraitement;
import UtilsJFC.Arrondisseur;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import no.uib.cipr.matrix.NotConvergedException;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.evaluation.SumOfClusterVariances;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.MersenneTwister;

/**
 *
 * @author JF Chartier
 */
public class AnalyseThematique 
{
    
    public static void main(String[] args)
    {
                
        try
        {
            
//            AnalyseThematique.analyser(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, "bacteria", 10000, 40, new CorrelationMatthews());
//            AnalyseThematique.analyserParEspaceDeMots(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, "bacteria", 10000, 40, new InformationMutuelle());
//            AnalyseThematique.analyserParEspaceDeMotsCumule(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, "bacteria", -1, 40, new InformationMutuelle());
            AnalyseThematique.analyserParEspaceDeMotsCumule2(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.ARTICLE, -1, 40, new InformationMutuelle());
//            AnalyseThematique.analyserParEspaceDeMots3(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, "bacteria", 300000, 10000, 40, new InformationMutuelle());
//            AnalyseThematique.analyserLSA2(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, "virus", -1, 40);
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    
    
    
    public static void analyser (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
//        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        
        articles = new Pretraitement(true, true, true, 5, articles.size()/2).filtrerArticle(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        
        Map<Integer, VecteurCreux> vecteurs = CorpusUtils.getMap_idArticle_vecteurCreuxTokenBM25(articles, map_token_id);
//        Map<Integer, VecteurCreux> vecteurs = CorpusUtils.getMap_idArticle_vecteurCreuxFreq(articles, map_token_id);
        
        vecteurs = Vectoriseur.normerVecteurCreux(vecteurs.values());
        vecteurs = Pretraitement.reduireViaSvd(vecteurs, map_token_id.size(), vecteurs.size(), 250);
        
        DistanceMeasure distance = new DistanceCosinusCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 1, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, vecteurs.values());
        
//        ClassifieurKmeansPlusPlus classifieur = new ClassifieurKmeansPlusPlus(vecteurs, 500, new DistanceCosinusCommonMath(), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE , 1);
//        TreeMap<Integer, Classe> map_id_classe = classifieur.partitionner(k);
        AfficheurTableau.afficher2Colonnes("Nombre de domif par classe", "idClasse", "nbre domif", Partition.recupererMap_idClasse_nbreDomif(map_id_classe));
        
        
        Map<Integer, Set<Integer>> map_idClasse_ensembleIdArticle = Partition.recupererMap_IdClasse_ensembleIdDomif(map_id_classe);
        Map<Integer, Set<Integer>> map_idToken_ensembleIdArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        Map<Integer, VecteurIndicie> map_idToken_vecteurIdClasseAssocie = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idToken_ensembleIdArticle, map_idClasse_ensembleIdArticle, map_idToken_ensembleIdArticle.keySet(), coefficient);
        AfficheurVecteur.afficherVecteurs("coefficient association entre token et classe", TrieUtils.inverserMap(map_token_id), map_idClasse_ensembleIdArticle.keySet(), map_idToken_vecteurIdClasseAssocie.values());
        
    }
    
    public static void analyserLSA2 (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int kClasse) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
//        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        
        Pretraitement.initialiserIdArticle(articles);
        articles = new Pretraitement(true, true, true, 5, articles.size()/3).filtrerArticle(articles);
        Set<String> s = new HashSet<String>(1);
        s.add(cible);
        s.add("oth");
        articles = Pretraitement.filtrerTokenSelonAntidictionnaire(articles, s);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        int nArt = articles.size();
        
//        Map<Integer, VecteurCreux> vecteurs = CorpusUtils.getMap_idArticle_vecteurCreuxTokenBM25(articles, map_token_id);
//        Map<Integer, VecteurCreux> vecteurs = CorpusUtils.getMap_idArticle_vecteurCreuxFreq(articles, map_token_id);
        
//        vecteurs = Vectoriseur.normerVecteurCreux(vecteurs.values());
//        Map<Integer, VecteurCreux> vecteursArt = Pretraitement.reduireViaSvd(CorpusUtils.getMap_idArticle_vecteurCreuxTokenBM25(articles, map_token_id), map_token_id.size(), nArt, 250);
//        Map<Integer, VecteurCreux> vecteursArt = Pretraitement.reduireViaSvd(CorpusUtils.getMap_idArticle_vecteurCreuxTokenBM25(articles, map_token_id), map_token_id.size(), nArt, 250);
        
//        Map<Integer, VecteurIndicie> vecteurs= CorpusUtils.getMap_idToken_vecteurIndicieBM25(articles, map_token_id);
//        vecteurs = Vectoriseur.normerVecteurIndicie(vecteurs.values());
        Map<String, Collection<VecteurCreux>> matrices = CorpusUtils.reductionSVD(CorpusUtils.getMap_idToken_vecteurIndicieBM25(articles, map_token_id).values(), map_token_id.size(), nArt, 250);
        
        DistanceMeasure distance = new DistanceCosinusCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(kClasse, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 5, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, matrices.get("document"));
        
//        ClassifieurKmeansPlusPlus classifieur = new ClassifieurKmeansPlusPlus(vecteurs, 500, new DistanceCosinusCommonMath(), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE , 1);
//        TreeMap<Integer, Classe> map_id_classe = classifieur.partitionner(k);
        AfficheurTableau.afficher2Colonnes("Nombre de domif par classe", "idClasse", "nbre domif", Partition.recupererMap_idClasse_nbreDomif(map_id_classe));
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(matrices.get("document"), map_id_classe);
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(matrices.get("terme"), map_idTheme_centroide, new Cosinus()).values());

    }
    
    public static void analyserParEspaceDeMots (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
//        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        
        articles = new Pretraitement(true, true, true, 5, articles.size()/2).filtrerArticle(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        
        Map<Integer, VecteurIndicie> vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idToken_idsArticle, map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient);
  
        vecteurs = Vectoriseur.normerVecteurIndicie(vecteurs.values());
//        vecteurs = Pretraitement.reduireViaSvd(vecteurs, map_token_id.size(), vecteurs.size(), 250);
        
        DistanceMeasure distance = new DistanceCosinusCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 1, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, VectoriseurUtils.convertir(vecteurs.values(), 0.0).values());
        
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        new AfficherTableau("Le idClasse pour chaque idToken d'une partition", "ID token", "ID CLASSE", map_idToken_idClasse).setVisible(true);
        TreeMap<Integer, Integer> map_idClasse_nbreDescripteur = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
        new AfficherTableau("Nombre de token par classe", "idClasse", "nbre de token", map_idClasse_nbreDescripteur).setVisible(true);
        
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(VectoriseurUtils.convertir(vecteurs.values(), 0.0), map_id_classe);
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(VectoriseurUtils.convertir(vecteurs.values(), 0.0), map_idTheme_centroide, new Cosinus()).values());
//      

    }
    
    public static void analyserParEspaceDeMotsCumule (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
//        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        
        articles = new Pretraitement(true, true, true, 5, articles.size()/2).filtrerArticle(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        
        Map<Integer, VecteurIndicie> vecteursMot = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idToken_idsArticle, map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient);
//        vecteursMot = Vectoriseur.normerVecteurIndicie(vecteursMot.values());
        Map<Integer, VecteurIndicie> vecteursDoc = CorpusUtils.additionnerEtNormerVecteurToken(CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), vecteursMot);
//        vecteursDoc = Vectoriseur.normerVecteurIndicie(vecteursDoc.values());
        
//        vecteurs = Pretraitement.reduireViaSvd(vecteurs, map_token_id.size(), vecteurs.size(), 250);
        
        DistanceMeasure distance = new DistanceCosinusCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 1, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, VectoriseurUtils.convertir(vecteursDoc.values(), 0.0).values());
        
        TreeMap<Integer, Integer> map_idArt_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        new AfficherTableau("Le idClasse pour chaque idDoc d'une partition", "ID doc", "ID CLASSE", map_idArt_idClasse).setVisible(true);
        TreeMap<Integer, Integer> map_idClasse_nbreDoc = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
        new AfficherTableau("Nombre de doc par classe", "idClasse", "nbre de doc", map_idClasse_nbreDoc).setVisible(true);
        
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(VectoriseurUtils.convertir(vecteursDoc.values(), 0.0), map_id_classe);
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(VectoriseurUtils.convertir(vecteursMot.values(), 0.0), map_idTheme_centroide, new Cosinus()).values());
//      

    }
    
    // methode ecrite le 19-09-2017
    // trouver les vecteurs de mots les plus proche de cluster de documents
    public static void analyserParEspaceDeMotsCumule2 (File filesArticles, TypeContenu typeContenu, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Pretraitement.initialiserIdArticle(articles);
        Pretraitement pretraitement = new Pretraitement(true, true, true, 100, articles.size()/3);
        articles = pretraitement.filtrerArticle(articles);
//        articles = Pretraitement.raciner(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        Map<String, String> map_token_categorieCible = Dictionnaire.recupererCategorieMot(Dictionnaire.getFileCategorieMotCibleV9());
        Map<String, Integer> map_token_freqDoc = CorpusUtils.getMap_token_nombreArticle(articles, map_token_categorieCible.keySet());
        AfficheurTableau.afficher2Colonnes("token et frequence", "token", "nombre document", map_token_freqDoc);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(pretraitement.filtrerArticle(articles), map_token_id);
        Map<Integer, VecteurCreux> map_idToken_vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation6(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, 0.0, false);
        map_idToken_vecteurs = VectoriseurUtils.normerVecteurCreuxB(map_idToken_vecteurs.values());
        Map<Integer, VecteurCreux> vecteursDoc = VectoriseurUtils.additionnerVecteursCreux(CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_vecteurs, map_token_id.size());
        vecteursDoc=VectoriseurUtils.normerVecteurCreuxB(vecteursDoc.values());
        
        EvaluateurSilhouette evalCluster = new EvaluateurSilhouette(vecteursDoc, new ProduitScalaireInverse());
        approximerK(evalCluster, new ProduitScalaire(), 2, 10, 1, 1);  
        
        
        DistanceMeasure distance = new DistanceCosinusCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 1, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, vecteursDoc.values());
        
        TreeMap<Integer, Integer> map_idArt_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        new AfficherTableau("Le idClasse pour chaque idDoc d'une partition", "ID doc", "ID CLASSE", map_idArt_idClasse).setVisible(true);
        TreeMap<Integer, Integer> map_idClasse_nbreDoc = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
        new AfficherTableau("Nombre de doc par classe", "idClasse", "nbre de doc", map_idClasse_nbreDoc).setVisible(true);
        
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(vecteursDoc.values(), map_id_classe);
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(map_idToken_vecteurs.values(), map_idTheme_centroide, new Cosinus()).values());
//      

    }
    
    public static void approximerK (EvaluateurAvecCentroide evaluateur, MetriqueSim metriqueSim, int minNombreClasse, int maxNombreClasse, int repetitionClassifieur, int trial) throws IOException
    {
        ApproximateurNombreClasse approximateur = new ApproximateurNombreClasse();
        KmeansPlusPlusMax kmPlusPlusMax = new KmeansPlusPlusMax(trial, evaluateur.getMap_Id_VecteurCreux(), 500, new DistanceCosinusCommonMath(), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        approximateur.approximerNombreClasseSelonClassifieurNonSupervise(kmPlusPlusMax, repetitionClassifieur, evaluateur, minNombreClasse, maxNombreClasse, 1);       
      
    }
    
    public static void analyserParEspaceDeMots3 (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int nCoocurrent, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
//        if (nombreArticleSelectionne!=-1)
        articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);        
        Pretraitement pretrai = new Pretraitement(true, true, true, 15, nombreArticleSelectionne/3);
        articles = pretrai.filtrerArticle(articles);
//        Set<String> s = new HashSet<String>(1);
//        s.add(cible);
//        articles = Pretraitement.filtrerTokenSelonAntidictionnaire(articles, s);
        
        
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);

//        Map<Integer, Set<Integer>> map_idTokenVariable_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, CorpusUtils.recupererSousEnsembleMap_token_id(map_token_id, CorpusUtils.getTokens(CorpusUtils.recupererArticleSelonCible(articles, cible))));
//        Map<Integer, Set<Integer>> map_idTokenCoVariable_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, CorpusUtils.recupererSousEnsembleAleatoireMap_token_id(map_token_id, nCoocurrent));
        
//        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        
        Map<Integer, VecteurIndicie> vecteursMot = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(CorpusUtils.getMap_idToken_ensembleIdArticle(articles, CorpusUtils.recupererSousEnsembleMap_token_id(map_token_id, CorpusUtils.getTokens(CorpusUtils.recupererArticleSelonCible(articles, cible)))), CorpusUtils.getMap_idToken_ensembleIdArticle(articles, CorpusUtils.recupererSousEnsembleAleatoireMap_token_id(map_token_id, nCoocurrent)), CorpusUtils.getEnsembleIdArticle(articles), coefficient);
//        Map<Integer, VecteurIndicie> vecteursMot = VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idTokenVariable_idsArticle, map_idTokenCoVariable_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient);
//        vecteursMot = Vectoriseur.normerVecteurIndicie(vecteursMot.values());
        Vectoriseur.normerVecteurIndicie2(vecteursMot.values());
        
//        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
//        articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Map<Integer, VecteurIndicie> vecteursDoc = CorpusUtils.additionnerEtNormerVecteurToken(CorpusUtils.getMap_idArticle_ensembleIdToken(CorpusUtils.recupererArticleSelonCible(articles, cible), map_token_id), vecteursMot);
//        vecteursDoc = Vectoriseur.normerVecteurIndicie(vecteursDoc.values());
        
//        vecteurs = Pretraitement.reduireViaSvd(vecteurs, map_token_id.size(), vecteurs.size(), 250);
        
        DistanceMeasure distance = new DistanceCosinusCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 1, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, VectoriseurUtils.convertir(vecteursDoc.values(), 0.0).values());
        
        TreeMap<Integer, Integer> map_idArt_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        new AfficherTableau("Le idClasse pour chaque idDoc d'une partition", "ID doc", "ID CLASSE", map_idArt_idClasse).setVisible(true);
        TreeMap<Integer, Integer> map_idClasse_nbreDoc = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
        new AfficherTableau("Nombre de doc par classe", "idClasse", "nbre de doc", map_idClasse_nbreDoc).setVisible(true);
        
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(VectoriseurUtils.convertir(vecteursDoc.values(), 0.0), map_id_classe);
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(VectoriseurUtils.convertir(vecteursMot.values(), 0.0), map_idTheme_centroide, new Cosinus()).values());
//      

    }
    
    public static void wordSubSpaceAnalysis (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
//        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
//        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        
        articles = new Pretraitement(true, true, true, 10, articles.size()/3).filtrerArticle(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        
        Map<Integer, VecteurIndicie> vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArticle, map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
  
        Vectoriseur.normerVecteurIndicie2(vecteurs.values());
        
        ProduitScalaireInverseCommonMath distance = new ProduitScalaireInverseCommonMath();
        KMeansPlusPlusClusterer kmPlus = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(kmPlus, 2, new SumOfClusterVariances<>(distance));
        TreeMap<Integer, Classe> map_id_classe = new WrapperClassifieur().partitionner(classifieur, VectoriseurUtils.convertir(vecteurs.values(), 0.0).values());
        
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        new AfficherTableau("Le idClasse pour chaque idToken d'une partition", "ID token", "ID CLASSE", map_idToken_idClasse).setVisible(true);
        TreeMap<Integer, Integer> map_idClasse_nbreDescripteur = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
        new AfficherTableau("Nombre de token par classe", "idClasse", "nbre de token", map_idClasse_nbreDescripteur).setVisible(true);
        
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(VectoriseurUtils.convertir(vecteurs.values(), 0.0), map_id_classe);
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(VectoriseurUtils.convertir(vecteurs.values(), 0.0), map_idTheme_centroide, new Cosinus()).values());
//      

    }
    
    
    
    
}
