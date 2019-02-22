/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.ClassesSemantique;

import Affichage.Tableau.AfficherTableau;
import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import AnalyseSimilitude.AnalyseSimilitude;
import Article.Article;
import Classification.Classe;
import Classification.Partition;
import ClassificationAutomatique.NonSupervisee.WrapperCommonMath.WrapperClassifieur;
import ClassificationAutomatique.Supervisee.KPPV.KPlusProcheVoisin;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Fichier.Arff.ArffUtils;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.Vectoriseur;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Distance.DistanceCosinusCommonMath;
import Metrique.Distance.ProduitScalaireInverseCommonMath;
import Metrique.Similitude.CorrelationPearsonIndicie;
import Metrique.Similitude.Cosinus;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.CorrelationMatthews;
import Ponderation.Independance1;
import Ponderation.InformationMutuelle;
import Ponderation.NormalisePMI;
import Ponderation.ProbConditionnelle1;
import Pretraitement.Pretraitement;
import Racinisation.RacineurAnglais;
import UtilsJFC.Intersection;
import UtilsJFC.TrieUtils;
import com.opencsv.CSVReader;
import edu.northwestern.at.utils.math.Probability;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import no.uib.cipr.matrix.NotConvergedException;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.evaluation.SumOfClusterVariances;
import org.apache.commons.math3.random.MersenneTwister;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author JF Chartier
 */
public class AnalyseClasseSemantique 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            
            AnalyseClasseSemantique.associationEntreClassesV3(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), TypeContenu.PARAGRAPHE, -1, 11, new InformationMutuelle());
            AnalyseClasseSemantique.associationEntreClassesV4(CorpusUtils.getFileArffVecteurMots(), TypeContenu.PARAGRAPHE, 11, new InformationMutuelle());
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
//    private Map<String, Integer> map_token_id;
    
    public static void associationEntreClassesV1 (File filesArticles, TypeContenu typeContenu, String cible, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
//        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Pretraitement.initialiserIdArticle(articles);
        articles = new Pretraitement(true, true, true, 100, articles.size()/3).filtrerArticle(articles);
//        articles = Pretraitement.raciner(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        Set<String> motCible = new TreeSet<>(Dictionnaire.recupererCategorieMot(Dictionnaire.getFileCategorieMotCibleV8()).values());
        Map<String, Integer> map_tokenCible_id = TrieUtils.getSubMap(map_token_id, motCible);
        
        
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        AfficheurTableau.afficher2Colonnes("token cible et id", "token cible", "id", map_tokenCible_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
//        Set<Integer> idTokensLinked = CorpusUtils.getIdTokens(CorpusUtils.recupererArticleSelonCible(articles, cible), map_token_id);
                
        Map<Integer, VecteurIndicie> map_idToken_vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
  
        Vectoriseur.normerVecteurIndicie2(map_idToken_vecteurs.values());
        
        TreeMap<Integer, Classe> map_id_classe=classer(k, map_idToken_vecteurs.values());
//        TreeMap<Integer, Classe> map_id_classe = getClassesLinkedToTarget(classer(k, vecteurs.values()), idTokensLinked);
        
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
//        new AfficherTableau("Le idClasse pour chaque idToken d'une partition", "ID token", "ID CLASSE", map_idToken_idClasse).setVisible(true);
//        TreeMap<Integer, Integer> map_idClasse_nbreDescripteur = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
//        new AfficherTableau("Nombre de token par classe", "idClasse", "nbre de token", map_idClasse_nbreDescripteur).setVisible(true);
//        
//        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(VectoriseurUtils.convertir(vecteurs.values(), 0.0), map_id_classe);
//        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", TrieUtils.inverserMap(map_token_id), map_id_classe.keySet(), Partition.calculerMetriqueEntreVecteurEtCentroides(VectoriseurUtils.convertir(vecteurs.values(), 0.0), map_idTheme_centroide, new Cosinus()).values());
//        TreeMap<Integer, Double> map_idToken_proximiteCentroide = Partition.calculerEcartAuCentroide(VectoriseurUtils.convertir(vecteurs.values(), 0.0), new Cosinus(), map_idToken_idClasse, map_idTheme_centroide);
//        AfficheurTableau.afficher4Colonnes("Proximite du token a son centroide le plus proche", "idToken", "token","proximite avec le centroide","classe", TrieUtils.inverserMap(map_token_id), map_idToken_proximiteCentroide, map_idToken_idClasse);
        
        afficherClasses(map_id_classe, TrieUtils.inverserMap(map_token_id), VectoriseurUtils.convertir(map_idToken_vecteurs.values(), 0.0), 50);
        
        
//        getAssociationEntreClasse(CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_idClasse, new ProbConditionnelle1());
        getAssociationEntreMotCibleEtClasse(TrieUtils.getSubMap(map_idToken_idsArticle, new TreeSet<>(map_tokenCible_id.values())), CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_idClasse, new ProbConditionnelle1());
    }
    
    public static void associationEntreClassesV2 (File filesArticles, TypeContenu typeContenu, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
//        articles = CorpusUtils.recupererArticleSelonCible(articles, cible);
        if (nombreArticleSelectionne!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticleSelectionne);
        Pretraitement.initialiserIdArticle(articles);
        articles = new Pretraitement(true, true, true, 100, articles.size()/3).filtrerArticle(articles);
//        articles = Pretraitement.raciner(articles);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        Map<String, String> map_token_categorieCible = Dictionnaire.recupererCategorieMot(Dictionnaire.getFileCategorieMotCibleV9());
        Map<String, Integer> map_token_freqDoc = CorpusUtils.getMap_token_nombreArticle(articles, map_token_categorieCible.keySet());
        AfficheurTableau.afficher2Colonnes("token et frequence", "token", "nombre document", map_token_freqDoc);
        
//        Set<String> categorieCible = new TreeSet<>(map_token_categorieCible.values());
//        Map<String, Integer> map_tokenCible_id = TrieUtils.getSubMap(map_token_id, categorieCible);
        
        
        
//        AfficheurTableau.afficher2Colonnes("token cible et id", "token cible", "id", map_tokenCible_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
                
        Map<Integer, VecteurIndicie> map_idToken_vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
  
        Vectoriseur.normerVecteurIndicie2(map_idToken_vecteurs.values());
        
        TreeMap<Integer, Classe> map_id_classe=classer(k, map_idToken_vecteurs.values());
        
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        afficherClasses(map_id_classe, TrieUtils.inverserMap(map_token_id), VectoriseurUtils.convertir(map_idToken_vecteurs.values(), 0.0), 50);
                
//        getAssociationEntreClasse(CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_idClasse, new ProbConditionnelle1());
        getAssociationEntreMotCibleEtClasse(getMap_idCategorieCible_idsArticle(map_token_categorieCible, map_idToken_idsArticle, map_token_id), CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_idClasse, new ProbConditionnelle1());
    }
    
    // c'est la version sur laquelle a ete construite les vecteurs de mots de l'etude
    public static void associationEntreClassesV3 (File filesArticles, TypeContenu typeContenu, int nombreArticleSelectionne, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
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
        Map<Integer, VecteurIndicie> map_idToken_vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
        Vectoriseur.normerVecteurIndicie2(map_idToken_vecteurs.values());
        
        
        
        VectoriseurUtils.initialiserDimensionsV2(map_idToken_vecteurs.values());
        TreeMap<Integer, Classe> map_id_classe=classer(k, map_idToken_vecteurs.values());
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        afficherClasses(map_id_classe, TrieUtils.inverserMap(map_token_id), VectoriseurUtils.convertir(map_idToken_vecteurs.values(), 0.0), 50);
        getAssociationEntreMotCibleEtClasse(getMap_idCategorieCible_idsArticle(map_token_categorieCible, map_idToken_idsArticle, map_token_id), CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_idClasse, new ProbConditionnelle1());
    }
    
    public static void associationEntreClassesV4 (final File fileArffVecteurMots, TypeContenu typeContenu, int k, CoefficientAssociation coefficient) throws IOException, NotConvergedException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_idV_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
        Map<String, Integer> map_token_id = TrieUtils.inverserMap(map_idV_token);
        data.deleteAttributeAt(data.attribute("@@name@@").index());       
        AfficheurTableau.afficher2Colonnes("token et id", "token", "id", map_token_id);
        
        Map<Integer, VecteurCreux> map_idToken_vecteurs = new TreeMap<>();
        for (int id = 0; id<data.numInstances(); id++)
        {
            map_idToken_vecteurs.put(id, new VecteurCreux(id, data.get(id).toDoubleArray()));
        }
        
//        Map<String, String> map_token_categorieCible = Dictionnaire.recupererCategorieMot(Dictionnaire.getFileCategorieMotCibleV9());
//        Map<String, Integer> map_token_freqDoc = CorpusUtils.getMap_token_nombreArticle(articles, map_token_categorieCible.keySet());
//        AfficheurTableau.afficher2Colonnes("token et frequence", "token", "nombre document", map_token_freqDoc);
//        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(pretraitement.filtrerArticle(articles), map_token_id);
        
        TreeMap<Integer, Classe> map_id_classe=classerVecteurCreux(k, map_idToken_vecteurs.values());
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        afficherClasses(map_id_classe, TrieUtils.inverserMap(map_token_id), map_idToken_vecteurs, 50);
//        getAssociationEntreMotCibleEtClasse(getMap_idCategorieCible_idsArticle(map_token_categorieCible, map_idToken_idsArticle, map_token_id), CorpusUtils.getMap_idArticle_ensembleIdToken(articles, map_token_id), map_idToken_idClasse, new ProbConditionnelle1());
    }
    
    public static Map<Integer, Set<Integer>> getMap_idCategorieCible_idsArticle (Map<String, String> map_token_categorieCible, Map<Integer, Set<Integer>> map_idToken_idsArticle, Map<String, Integer> map_token_id)
    {
        Map<Integer, Set<Integer>> map_idCat_idsArticle = new TreeMap<>();
        Map<String, Set<String>> cat_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_categorieCible);
        Map<String, Integer> map_cat_id = new TreeMap<>();
        
        int idCible = 0;
        for (String c: cat_tokens.keySet())
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
    
    public static void afficherClasses(TreeMap<Integer, Classe> map_id_classe, Map<Integer, String> map_id_token, Map<Integer, VecteurCreux> map_idToken_vecteurs, int k)
    {
        TreeMap<Integer, Integer> map_idToken_idClasse = Partition.recupererMap_IdDomif_IdClasse(map_id_classe);
        new AfficherTableau("Le idClasse pour chaque idToken d'une partition", "ID token", "ID CLASSE", map_idToken_idClasse).setVisible(true);
        TreeMap<Integer, Integer> map_idClasse_nbreDescripteur = Partition.recupererMap_idClasse_nbreDomif(map_id_classe);
        new AfficherTableau("Nombre de token par classe", "idClasse", "nbre de token", map_idClasse_nbreDescripteur).setVisible(true);
        
        Map<Integer, double[]> map_idTheme_centroide = Partition.calculerCentroideDesClasse(map_idToken_vecteurs, map_id_classe);
        AfficheurVecteur.afficherVecteurs("centroide des classes", map_idTheme_centroide);
        
        Map<Integer, VecteurIndicie> map_idCentroide_vecteurCorrelationAvecCentroide = AnalyseSimilitude.calculerMatriceSimilitudeCarreViaArrays(map_idTheme_centroide, new Cosinus(), true, true);
        AfficheurVecteur.afficherVecteurs("proximite entre centroide des themes", map_idTheme_centroide.keySet(), map_idCentroide_vecteurCorrelationAvecCentroide.values());
        
        Map<Integer, VecteurIndicie> map_idToken_vecteurSimToClasse = Partition.calculerMetriqueEntreVecteurEtCentroides(map_idToken_vecteurs.values(), map_idTheme_centroide, new Cosinus());
        AfficheurVecteur.afficherVecteurs("proximite du token avec centroide", map_id_token, map_id_classe.keySet(), map_idToken_vecteurSimToClasse.values());
//        TreeMap<Integer, Double> map_idToken_proximiteCentroide = Partition.calculerEcartAuCentroide(VectoriseurUtils.convertir(map_idToken_vecteurs.values(), 0.0), new Cosinus(), map_idToken_idClasse, map_idTheme_centroide);
//        AfficheurTableau.afficher4Colonnes("Proximite du token a son centroide le plus proche", "idToken", "token","proximite avec le centroide","classe", map_id_token, map_idToken_proximiteCentroide, map_idToken_idClasse);
        
        Map<Integer, Set<Integer>> map_idClasse_kppv = KPlusProcheVoisin.kPlusProcheVoisinDeVecteursCibles(VectoriseurUtils.transposer(map_idToken_vecteurSimToClasse.values(), map_id_classe.keySet()), k);
        for (int idClasse: map_idClasse_kppv.keySet())
        {
            for (int idToken: map_idClasse_kppv.get(idClasse))
            {
                System.out.printf("%-30.30s %-30.30s %-30.30s %-30.30s\n", idClasse, idToken, map_id_token.get(idToken), map_idToken_vecteurSimToClasse.get(idToken).getMap_Id_Ponderation().get(idClasse));
            }
        }
        
    }
    
    
    
    
    
    protected static TreeMap<Integer, Classe> getClassesLinkedToTarget (Map<Integer, Classe> classes, Set<Integer> idTokensLinkedToTarget)
    {
        System.out.println("filtrer les classes");
        TreeMap<Integer, Classe> classesFiltred = new TreeMap();
        for (Classe c: classes.values())
        {
            if (Intersection.hasIntersection(idTokensLinkedToTarget, c.getIdElements())==true)
            {
                classesFiltred.put(c.getIdClasse(), new Classe(c.getIdClasse(), c.getIdElements()));
            }
        }
        System.out.println("\t" + "nombre de classe conserve: " + classesFiltred.size());
        return classesFiltred;
    }
    
    protected static void getAssociationEntreMotCibleEtClasse (Map<Integer, Set<Integer>> map_idToken_idsArt, Map<Integer, Set<Integer>> map_idArt_idTokens, Map<Integer, Integer> map_idToken_idClasse, CoefficientAssociation coefficient) throws IOException
    {
        Map<Integer, Set<Integer>> map_idClasse_idsArts = CorpusUtils.getMap_idClasse_idArticles(map_idArt_idTokens, map_idToken_idClasse);
        Map<Integer, VecteurIndicie> vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, map_idArt_idTokens.keySet(), coefficient, false);
        AfficheurVecteur.afficherVecteurs("Pr(theme|categorieCible)", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, map_idArt_idTokens.keySet(), new Independance1(), false);
        AfficheurVecteur.afficherVecteurs("probabilite a priori du theme independant de la presence de la categorie cible", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, map_idArt_idTokens.keySet(), new CorrelationMatthews(), false);
        AfficheurVecteur.afficherVecteurs("corelation entre le theme et la categorie cible", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation3(map_idToken_idsArt, map_idClasse_idsArts, map_idArt_idTokens.keySet(), new NormalisePMI(), false);
        AfficheurVecteur.afficherVecteurs("PMI normalise entre le theme et la categorie cible", map_idClasse_idsArts.keySet(), vecteurs.values());
        
        
//        return vecteurs; 
    }
    
    protected static Map<Integer, VecteurIndicie> getAssociationEntreClasse (Map<Integer, Set<Integer>> map_idArt_idTokens, Map<Integer, Integer> map_idToken_idClasse, CoefficientAssociation coefficient)
    {
        Map<Integer, Set<Integer>> map_idClasse_idsArts = CorpusUtils.getMap_idClasse_idArticles(map_idArt_idTokens, map_idToken_idClasse);
        Map<Integer, VecteurIndicie> vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idClasse_idsArts, map_idArt_idTokens.keySet(), coefficient, false);
        AfficheurVecteur.afficherVecteurs("Association entre classes", map_idClasse_idsArts.keySet(), vecteurs.values());
        return vecteurs;  
    }
    
    protected static TreeMap<Integer, Classe> classer (int k, Collection<VecteurIndicie> vecteurs)
    {
        ProduitScalaireInverseCommonMath distance = new ProduitScalaireInverseCommonMath();
//        DistanceCosinusCommonMath distance = new DistanceCosinusCommonMath();
//        DBSCANClusterer classifieur =  new DBSCANClusterer(0.60, 3, distance);
        
        KMeansPlusPlusClusterer km = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(km, 5, new SumOfClusterVariances<>(distance));
        return new WrapperClassifieur().partitionner(classifieur, VectoriseurUtils.convertir(vecteurs, 0.0).values());
        
    }
    protected static TreeMap<Integer, Classe> classerVecteurCreux (int k, Collection<VecteurCreux> vecteurs)
    {
        ProduitScalaireInverseCommonMath distance = new ProduitScalaireInverseCommonMath();
//        DistanceCosinusCommonMath distance = new DistanceCosinusCommonMath();
//        DBSCANClusterer classifieur =  new DBSCANClusterer(0.60, 3, distance);
        
        KMeansPlusPlusClusterer km = new KMeansPlusPlusClusterer(k, 500, distance, new MersenneTwister(3), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        Clusterer classifieur = new MultiKMeansPlusPlusClusterer(km, 5, new SumOfClusterVariances<>(distance));
        return new WrapperClassifieur().partitionner(classifieur, vecteurs);
        
    }
    
}
