/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Biomed;

import Affichage.Tableau.AfficherTableau;
import AnalyseSimilitude.CalculateurParralleleMatriceCosinusEntreMots;
import Classification.Classe;
import Classification.Partition;
import ClassificationAutomatique.NonSupervisee.WrapperCommonMath.Dbscan;
import ClassificationAutomatique.NonSupervisee.KMeans.ClassifieurKmeansMaximum;
import ClassificationAutomatique.NonSupervisee.KMeans.ClassifieurKmeansRepete;
import Corpus.BioMed.ParserBioMed;
import Corpus.Corpus;
import Corpus.DocumentJFC;
import Corpus.ParseurCorpusFormatJFC;
import Evaluateur.ApproximateurNombreClasse.ApproximateurNombreClasse;
import Evaluateur.AvecCentroide.Silhouette.EvaluateurSilhouette;
import Evaluateur.AvecCentroide.VarianceRatio.EvaluateurRatioVariance;
import Evaluateur.AvecCentroide.VarianceRatio.EvaluateurRatioVarianceMultiClassifieur;
import Fichier.CSV.FileCSV;
import Fichier.FichierTxt;
import Fichier.FileUtils;
import FiltreurDomif.FiltreurDomif;
import FiltreurDomif.FiltreurDomifAleatoireSansRemise;
import FiltreurDomif.FiltreurDomifLongueurMin;
import FiltreurDomif.FiltreurDomifSelonDictionnaire;
import FiltreurToken.FiltreurAntidictionnaire;
import FiltreurToken.FiltreurTokenSelonDictionnaire;
import FiltreurToken.FiltreurFrequenceDocumentaire;
import FiltreurToken.FiltreurToken;
//import FiltreurUnif.NonSupervise.FiltreurUnifParEntropieDocumentaireNmax;

import Indexation.Indexation;
import Indexation.IndexationBase;
import Indexation.Indexeur.IndexeurDomifParUnif.IndexeurDomifParUnif;
import Indexation.Indexeur.Unif;
import Indexation.Indexeur.UnifBase;
import Indexation.Indexeur.UnifContenu;
import Indexation.Indexeur.UnifParDomif.IndexeurUnifBaseParDomif;
import Indexation.Indexeur.UnifParDomif.IndexeurUnifParDomif;
import Indexation.Indexeur.UnifParDomif.IndexeurUnifParDomif_JBlas;
import Indexation.Indexeur.UnifParDomif.IndexeurUnifParDomif_MTJ;
import Indexation.Indexeur.UnifParUnif.IndexeurUnifCoocParUnifCibleCoefficient;
import Indexation.Indexeur.UnifParUnif.IndexeurUnifParUnif;
import MDS.MDS;
import Matrice.MtjMatrice;
import Matrice.Vectorisateur.Domif.VectorisateurDomif;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.Vectoriseur;
import Metrique.Distance.DistanceEuclidienne;
import Metrique.Distance.ErreurQuadratique;
import Metrique.Similitude.Cosinus;
import Metrique.Similitude.MetriqueSim;
import Ponderation.CoefficientAssociation;
import Ponderation.InformationMutuelle;
import Ponderation.ProbConditionnelle1;
import Ponderation.CoefficientProbConditionnelle2;
import Ponderation.CorrelationMatthews;
import Ponderation.PonderateurParClasse;
import Ponderation.Ponderation;
import static Projets.ProjetMagritte2.ponderationParClasse;
import static Projets.ProjetMagritte2.statistiqueIndexation;
import ReductionDimensionnelle.SVD.CalculateurSVD_JBlas;
import ReductionDimensionnelle.SVD.ReducteurSVD_MTJ;
import SQLite.BdSimParadigmatiqueEntreMot;
import Segmentation.Domif;
import Segmentation.DomifSegment;
import Segmentation.Segmentation;
import Segmentation.Segmenteur.Contexte.SegmenteurParDocument;
import Segmentation.Segmenteur.ExtracteurUtils;
import Tokenisation.Tokeniseur;
import UtilsJFC.Intersection;
import UtilsJFC.StatistiqueEchantillon;
import UtilsJFC.TirageAleatoire;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import org.jdom2.JDOMException;

/**
 *
 * @author JF Chartier
 */
public class ProjetBioMed 
{
    public static void main(String[] args)
    {
                
        try
        {
            File fileMotCibleEtDescripteur = new File ("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\listeMotsCiblesEtDescripteursV2.txt");
            File fileMotCible = new File ("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\listeMotsCiblesV2.txt");
            File fileDescripteur = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\listeMotsDescripteursV2.txt");
            File fileCorpusLanci = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\articleFiltre\\");
//            File fileListeMotPlusFrequent = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste des mots(nom-adj-verbe) 25k plus frequents.txt");
            File fileListeMotPlusFrequent = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste des mots(nom-adj-verbe) ]25-50]k plus frequents.txt");
            File fileListeCooccurrents = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste 10000 des cooc des 20k mots plus frequents.txt");
            String cheminBD = "C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\BDsql\\CosinusEntreMotsCibleMoinsFrequents.db";
            File fileListeV4MotCible = new File ("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\listeMotsCiblesV4.txt");
            File fileListeV4MotDescripteur = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\listeMotsDescripteursV4.txt");
            File fileCorpusLanci2 = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\articleFiltre2\\");
            File fileCsvCible = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\MatriceCorrelationEntreCibleEtDescripteur50000.csv");
            File fileCsvDescripteur = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\MatriceCorrelationDescripteur500000.csv");
            File fileCsvCorrelationentreCategories = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\MatriceProbCond2EntreCatCibleEtCatDescripteurTousParagV4.csv");
            File fileCategorieMotDescripteurV4 = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste v4 cat�gories des descripteurs.txt");
            File fileCategorieMotCibleV4 = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\liste v4 cat�gories des mots cibles.txt");
//            new ProjetBioMed().recupererMotDescripteurMaxEntropie(fileCorpusLanci, fileListeMotCible, 10000);
//            System.exit(1);
//            new ProjetBioMed().calculerMatriceCosinusEntreMotsCibles(fileDescripteur, fileCorpusLanci, fileListeMotCible, fileListeCooccurrents, cheminBD);
//            System.exit(1);
//            new ProjetBioMed().calculerMatriceMtjCosinusEntreMotsCibles(fileMotCibleEtDescripteur, fileCorpusLanci, fileListeMotPlusFrequent, cheminBD);
//            System.exit(1);
//            new ProjetBioMed().recupererNPlusSimilaire(cheminBD, fileDescripteur, 10);
//            System.exit(1);
//            new ProjetBioMed().analyseThematique(fileCorpusLanci2, fileListeV3MotCible);
//            System.exit(1);
//            new ProjetBioMed().analyseCorrelationEntreMotCibleEtDescripteur(fileCorpusLanci2, fileListeV3MotCible, fileListeV3MotDescripteur, fileCsvCategorieDescripteur, new CorrelationMatthews(), 50000);
            new ProjetBioMed().analyseCorrelationEntreCategoriesMotCibleEtDescripteur(fileCorpusLanci2, fileListeV4MotCible, fileCategorieMotCibleV4, fileListeV4MotDescripteur, fileCategorieMotDescripteurV4, fileCsvCorrelationentreCategories, new CoefficientProbConditionnelle2(), -1);
            
//            new ProjetBioMed().analyseClassificationMot(fileCorpusLanci2, fileListeV3MotDescripteur, fileListeV3MotCible, new CorrelationMatthews(), 100000);
            
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void analyseClassificationMot (File filePathCorpus, File fileMotCible, File fileMotDescripteur, CoefficientAssociation coefficient, int nombreParagraphe) throws IOException
    {
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(fileMotCible.getAbsolutePath());
        Set<String> ensembleMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(fileMotDescripteur.getAbsolutePath());
        Set<String> dictionnaire = new HashSet<String>(ensembleMotCible);
        dictionnaire.addAll(ensembleMotDescripteur);
        
        Map<Integer, Domif> map_id_domif = recupererDomifParagraphe(filePathCorpus, dictionnaire);
        map_id_domif = new FiltreurTokenSelonDictionnaire(dictionnaire).filtrer(map_id_domif);
        map_id_domif = new FiltreurDomifLongueurMin(2).filtrerDomif(map_id_domif);
        map_id_domif = new FiltreurDomifAleatoireSansRemise(nombreParagraphe, map_id_domif.keySet()).filtrerDomif(map_id_domif);
            
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(map_id_domif.values());
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnif(map_id_domif, map_unifString_id);
        map_id_domif.clear();
        System.gc();
        
        Map<Integer, String> map_id_motDescripteur = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, ensembleMotDescripteur);
        Map<Integer, String> map_id_motCible = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, ensembleMotCible);
        new AfficherTableau("Les mots cibles", "id", "mots cibles", new TreeMap(map_id_motCible)).setVisible(true);
        new AfficherTableau("Les mots Descripteurs", "id", "mots", new TreeMap(map_id_motDescripteur)).setVisible(true);
        
        Map<Integer, ? extends UnifBase> map_id_unif = new IndexeurUnifCoocParUnifCibleCoefficient(coefficient).indexerUnifCoocParUnifCible(Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_id_motCible.keySet()), Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_id_motDescripteur.keySet()), map_idDomif_listeIdUnif.keySet());
        Map<Integer, VecteurCreux> map_id_vecteur = new VectorisateurDomif(IndexationBase.recupererMap_IdDomif_ensembleIdUnif(map_id_unif)).vectorisationCreuse(map_id_unif);
        
        ProjetMagritte2.approximerK(new EvaluateurRatioVariance(map_id_vecteur, new ErreurQuadratique()), new Cosinus(), 2, 40, 1, 10);
        
//        Partition partition = classerSelonGAP(map_id_vecteur, 1, 30);
        
//        Partition partition = new Partition(0, new ClassifieurKmeansMaximum(new TreeMap(map_id_vecteur), new Cosinus(), 40, 500, 0.0, true).partitionnerMax(10));
//        TreeMap<Integer, Integer> mapIdClasseNbreDomif = partition.recupererMapIdClasseNbreDomif();
//        new AfficherTableau("Nombre de domif par classe", "idClasse", "nbre domif", mapIdClasseNbreDomif).setVisible(true);
//        ponderationParClasse(map_id_domif.keySet(), partition.recupererMap_IdClasse_ensembleIdDomif(), Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_unifString_id));

        
    }
    
    public static Partition classerSelonGAP (Map<Integer, VecteurCreux> map_id_vecteur, int minK, int maxK)
    {
        Partition partitionBest = Projet.trouverMeilleurPartitionKmeansMaximumSelonGap(new TreeMap(map_id_vecteur), new Cosinus(), 500, 0.00000, minK, maxK, 1, true, 10);
        TreeMap<Integer, Integer> map_IdDomif_IdClasse = partitionBest.recupererMap_IdDomif_IdClasse();
        new AfficherTableau("Le idClasse pour chaque idDomif d'une partition", "ID DOMIF", "ID CLASSE", map_IdDomif_IdClasse).setVisible(true);
        TreeMap<Integer, Integer> mapIdClasseNbreDomif = partitionBest.recupererMapIdClasseNbreDomif();
        new AfficherTableau("Nombre de domif par classe", "idClasse", "nbre domif", mapIdClasseNbreDomif).setVisible(true);
        TreeMap<Integer, Double> map_idDomif_proximiteCentroide = partitionBest.calculerEcartAuCentroide(new TreeMap(map_id_vecteur), new Cosinus());
        new AfficherTableau("Proximite de chaque oeuvre a son centroide", "idOeuvre", "proximite avec le centroide", map_idDomif_proximiteCentroide).setVisible(true);
        
        return partitionBest;
    }
    
    public void analyseCorrelationEntreCategoriesMotCibleEtDescripteur (File filePathCorpus, File fileMotCible, File fileCategorieMotCible, File fileMotDescripteur, File fileCategorieMotDescripteur, File fileCSV, CoefficientAssociation coefficient, int nombreParagraphe) throws IOException, SQLException
    {
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(fileMotCible.getAbsolutePath());
        Set<String> ensembleMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(fileMotDescripteur.getAbsolutePath());
        Set<String> dictionnaire = new HashSet<String>(ensembleMotCible);
        dictionnaire.addAll(ensembleMotDescripteur);
        
        Map<Integer, Domif> map_id_domif = recupererDomifParagraphe(filePathCorpus, dictionnaire);
        map_id_domif = new FiltreurTokenSelonDictionnaire(dictionnaire).filtrer(map_id_domif);
        map_id_domif = new FiltreurDomifLongueurMin(2).filtrerDomif(map_id_domif);
        if (nombreParagraphe>0)
            map_id_domif = new FiltreurDomifAleatoireSansRemise(nombreParagraphe, map_id_domif.keySet()).filtrerDomif(map_id_domif);
        
        new AfficherTableau("mots cibles", "mot", "frequence", new TreeMap(Segmentation.recupererMap_unifString_frequenceTotale(ensembleMotCible, Segmentation.recupererMap_idDomif_listeTokens(map_id_domif).values()))).setVisible(true);
        new AfficherTableau("mots descripteurs", "mot", "frequence", new TreeMap(Segmentation.recupererMap_unifString_frequenceTotale(ensembleMotDescripteur, Segmentation.recupererMap_idDomif_listeTokens(map_id_domif).values()))).setVisible(true);
        
        Map<String, String> map_motCible_categorie = recupererCategorieMot(fileCategorieMotCible);
        Map<String, String> map_motDescripteur_categorie = recupererCategorieMot(fileCategorieMotDescripteur);
        remplacerMotParCategorie(map_id_domif, map_motCible_categorie);
        remplacerMotParCategorie(map_id_domif, map_motDescripteur_categorie);
        // une fois remplace les token par leur categorie, il faut supprimer les tokens  
        map_id_domif = new FiltreurAntidictionnaire(dictionnaire).filtrer(map_id_domif);
        new AfficherTableau("categories cibles", "categorie", "frequence", new TreeMap(Segmentation.recupererMap_unifString_frequenceTotale(new TreeSet<>(map_motCible_categorie.values()), Segmentation.recupererMap_idDomif_listeTokens(map_id_domif).values()))).setVisible(true);
        new AfficherTableau("categories descripteurs", "categorie", "frequence", new TreeMap(Segmentation.recupererMap_unifString_frequenceTotale(new TreeSet<>(map_motDescripteur_categorie.values()), Segmentation.recupererMap_idDomif_listeTokens(map_id_domif).values()))).setVisible(true);
                        
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(map_id_domif.values());
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnif(map_id_domif, map_unifString_id);
        map_id_domif.clear();
                
        Map<Integer, String> map_id_catDescripteur = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, new TreeSet<>(map_motDescripteur_categorie.values()));
        Map<Integer, String> map_id_catCible = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, new TreeSet<>(map_motCible_categorie.values()));
        new AfficherTableau("Les categorie cibles", "id", "categorie cibles", new TreeMap(map_id_catCible)).setVisible(true);
        new AfficherTableau("Les categorie descripteurs", "id", "categorie descripteurs", new TreeMap(map_id_catDescripteur)).setVisible(true);
        
        Map<Integer, ? extends UnifBase> map_id_unif = new IndexeurUnifCoocParUnifCibleCoefficient(coefficient).indexerUnifCoocParUnifCible(Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_id_catCible.keySet()), Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_id_catDescripteur.keySet()), map_idDomif_listeIdUnif.keySet());
        Map<Integer, VecteurCreux> map_id_vecteur = new VectorisateurDomif(IndexationBase.recupererMap_IdDomif_ensembleIdUnif(map_id_unif)).vectorisationCreuse(map_id_unif);
        MDS.calculerReductionDimensionelle(map_id_vecteur, new DistanceEuclidienne());
        FichierTxt.enregistrerFichierTxt(fileCSV.getAbsolutePath(), ProjetBioMed.creerMatriceCsv(map_id_catCible, map_id_vecteur, new TreeMap(map_id_catDescripteur)));
      
        Map<Integer, Classe> map_idClasse = new ClassifieurKmeansMaximum(new TreeMap(map_id_vecteur), new Cosinus(), 8, 500, 0, true).partitionnerMax(20);
        Map<Integer, Integer> map_IdDomif_IdClasse = Partition.recupererMap_IdDomif_IdClasse(map_idClasse);
        new AfficherTableau("Le idClasse pour chaque idDomif d'une partition", "ID DOMIF", "ID CLASSE", new TreeMap(map_IdDomif_IdClasse)).setVisible(true);
        
//        ProjetMagritte2.approximerK(new EvaluateurRatioVariance(map_id_vecteur, new ErreurQuadratique()), new Cosinus(), 2, 15, 1, 50);
//        ProjetMagritte2.approximerK(new EvaluateurSilhouette(map_id_vecteur, new ErreurQuadratique()), new Cosinus(), 2, 15, 1, 50);
//        classerSelonGAP(map_id_vecteur, 2, 15);
        
    }
    
    public void analyseCorrelationEntreMotCibleEtDescripteur (File filePathCorpus, File fileMotCible, File fileMotDescripteur, File fileCSV, CoefficientAssociation coefficient, int nombreParagraphe) throws IOException
    {
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(fileMotCible.getAbsolutePath());
        Set<String> ensembleMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(fileMotDescripteur.getAbsolutePath());
        Set<String> dictionnaire = new HashSet<String>(ensembleMotCible);
        dictionnaire.addAll(ensembleMotDescripteur);
        
        Map<Integer, Domif> map_id_domif = recupererDomifParagraphe(filePathCorpus, dictionnaire);
        map_id_domif = new FiltreurTokenSelonDictionnaire(dictionnaire).filtrer(map_id_domif);
        map_id_domif = new FiltreurDomifLongueurMin(2).filtrerDomif(map_id_domif);
        map_id_domif = new FiltreurDomifAleatoireSansRemise(nombreParagraphe, map_id_domif.keySet()).filtrerDomif(map_id_domif);
        
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(map_id_domif.values());
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnif(map_id_domif, map_unifString_id);
        map_id_domif.clear();
        System.gc();
        
        Map<Integer, String> map_id_motDescripteur = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, ensembleMotDescripteur);
        Map<Integer, String> map_id_motCible = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, ensembleMotCible);
        new AfficherTableau("Les mots cibles", "id", "mots cibles", new TreeMap(map_id_motCible)).setVisible(true);
        new AfficherTableau("Les mots Descripteurs", "id", "mots", new TreeMap(map_id_motDescripteur)).setVisible(true);
        
        Map<Integer, ? extends UnifBase> map_id_unif = new IndexeurUnifCoocParUnifCibleCoefficient(coefficient).indexerUnifCoocParUnifCible(Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_id_motCible.keySet()), Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_id_motDescripteur.keySet()), map_idDomif_listeIdUnif.keySet());
        Map<Integer, VecteurCreux> map_id_vecteur = new VectorisateurDomif(IndexationBase.recupererMap_IdDomif_ensembleIdUnif(map_id_unif)).vectorisationCreuse(map_id_unif);
        MDS.calculerReductionDimensionelle(map_id_vecteur, new DistanceEuclidienne());
        FichierTxt.enregistrerFichierTxt(fileCSV.getAbsolutePath(), ProjetBioMed.creerMatriceCsv(map_id_motCible, map_id_vecteur, new TreeMap(map_id_motDescripteur)));
      
    }
    
    private void remplacerMotParCategorie (Map<Integer, Domif> map_id_domif, Map<String, String> map_token_categorie) throws IOException
    {
//        Map<String, String> map_token_categorie = recupererCategorieMot(fileAssociationMotCategorie);
//        Map<Integer, Domif> map_id_domifFiltre = new HashMap<>(map_id_domif.size()); 

        for (Map.Entry<String, String> e: map_token_categorie.entrySet())
        {
            e.setValue("cat_".concat(e.getValue()));
        }
        
        for (Domif domif: map_id_domif.values())
        {
//            for (int i = 0; i < domif.getListeToken().size(); i++)
            for (String token: new TreeSet<>(domif.getListeToken()))
            {
//                String token = domif.getListeToken().get(i);
                if (map_token_categorie.containsKey(token))
                {
                    domif.getListeToken().add(map_token_categorie.get(token));
//                    domif.getListeToken().set(i, map_token_categorie.get(token));
                }
                    
            }
        }
    }
    
    public void analyseThematique (File filePathCorpus, File fileListeMotCible) throws IOException, NotConvergedException
    {
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(fileListeMotCible.getAbsolutePath());
        Map<Integer, Domif> map_id_domif = recupererDomifParagraphe(filePathCorpus, ensembleMotCible);
        map_id_domif = new FiltreurDomifAleatoireSansRemise(50000, map_id_domif.keySet()).filtrerDomif(map_id_domif);
                
        // filtrer hapax
        map_id_domif  = new FiltreurFrequenceDocumentaire(Segmentation.recupererMap_unifString_frequenceDocumentaire(Segmentation.recupererMap_idDomif_listeTokens(map_id_domif)), 5, (map_id_domif.size()/2)).filtrer(map_id_domif);
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(map_id_domif.values());
                
        new AfficherTableau("unif", "unif", "id", new TreeMap(map_unifString_id)).setVisible(true);
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnifUnique(map_id_domif, map_unifString_id);
        Map<Integer, Integer> map_idUnif_idLigne = associerId(new TreeSet<Integer>(map_unifString_id.values()));
        Map<Integer, Integer> map_idDomif_id = associerId(map_idDomif_listeIdUnif.keySet());
//        DenseMatrix matrice = IndexeurUnifParDomif_MTJ.indexerUnifParDomif(map_idDomif_listeIdUnif, map_idDomif_id, map_idUnif_idLigne);
//        DenseMatrix matrice = IndexeurUnifParDomif_MTJ.indexerTFIDFUnifParDomif(map_idDomif_listeIdUnif,Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_unifString_id), map_idDomif_id, map_idUnif_idLigne);
        
//        matrice = CalculateurSVD_MTJ.reduireEtPondererMatriceV(new SVD(matrice.numRows(), matrice.numColumns()).factor(matrice), 300);
//        Map<Integer, VecteurCreux> map_id_vecteur = MtjMatrice.convertir(matrice, map_idDomif_id);
//        Map<Integer, VecteurCreux> map_id_vecteur = CalculateurSVD_JBlas.reduireEtPondererMatriceV(IndexeurUnifParDomif_JBlas.indexerUnifParDomif_double(map_idDomif_listeIdUnif, map_idDomif_id, map_idUnif_idLigne), 200, map_idDomif_id);
        Map<Integer, VecteurCreux> map_id_vecteur = IndexeurDomifParUnif.indexerDomifParUnif_frequence(map_idDomif_listeIdUnif, map_idUnif_idLigne);
        map_id_vecteur = Vectoriseur.normerVecteurCreux(map_id_vecteur.values());
        
        
        
//        ProjetMagritte2.approximerK(new EvaluateurSilhouette(map_id_vecteur, new ErreurQuadratique()), new Cosinus(), 2, 30, 1, 5);
//        ProjetMagritte2.approximerK(new EvaluateurRatioVariance(map_id_vecteur, new ErreurQuadratique()), new Cosinus(), 2, 30, 1, 5);
//        System.exit(1);
        
        Partition partition  = new Partition(0, new Dbscan().partitionner(map_id_vecteur, 2.00, 3));
//        Partition partition = new Partition(0, new ClassifieurKmeansMaximum(new TreeMap(map_id_vecteur), new Cosinus(), 40, 500, 0.0, true).partitionnerMax(5));
        
//        Partition partition = classerOeuvre(map_id_vecteur, 21, 21);
        TreeMap<Integer, Integer> mapIdClasseNbreDomif = partition.recupererMapIdClasseNbreDomif();
        new AfficherTableau("Nombre de domif par classe", "idClasse", "nbre domif", mapIdClasseNbreDomif).setVisible(true);
        
        
//        ponderationUnifParClasse(map_id_vecteur, map_id_unif, partition);
        ponderationParClasse(map_id_domif.keySet(), partition.recupererMap_IdClasse_ensembleIdDomif(), Segmentation.recupererMap_idUnif_ensembleIdDomif(map_idDomif_listeIdUnif, map_unifString_id));
//      
        
    }
    
    public void recupererNPlusSimilaire (String fileBDSimEntreUnif, File filePathDescripteur, int k) throws SQLException
    {
        Set<String> ensembleMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(filePathDescripteur.getAbsolutePath());
        BdSimParadigmatiqueEntreMot bd = new BdSimParadigmatiqueEntreMot(fileBDSimEntreUnif);
        bd.connect();   
        Set<String> set= ensembleMotDescripteur;
        for (String s: set)
        {
            bd.imprimerNmotsPlusSimilaires(s, k);
        }
    }
    
    public void calculerMatriceMtjCosinusEntreMotsCibles (File filePathDescripteur, File filePathCorpus, File fileListeMotCible, String fileBDSimEntreUnif) throws IOException, SQLException, NotConvergedException
    {
        Set<String> ensembleMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(filePathDescripteur.getAbsolutePath());
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(fileListeMotCible.getAbsolutePath());
        ensembleMotCible.addAll(ensembleMotDescripteur);
        
        Map<Integer, Domif> map_id_domif = recupererDocument(filePathCorpus);
        map_id_domif = new FiltreurDomifSelonDictionnaire(ensembleMotCible).filtrerDomif(map_id_domif);
        Set<Integer> set = TirageAleatoire.tirageAleatoirSansDoublonSousEnsemble(20000, map_id_domif.keySet());
        //Set<Integer> s = 
        for (int id: new TreeSet<Integer>(map_id_domif.keySet()))
        {
            if (!set.contains(id))
                    map_id_domif.remove(id);
        }
        map_id_domif = new FiltreurTokenSelonDictionnaire(ensembleMotCible).filtrer(map_id_domif);
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(ensembleMotCible);
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnifUnique(map_id_domif, map_unifString_id);
        
        Map<Integer, Integer> map_idUnif_idLigne = associerId(new TreeSet<Integer>(map_unifString_id.values()));
        DenseMatrix matrice = IndexeurUnifParDomif_MTJ.indexerUnifParDomif(map_idDomif_listeIdUnif, associerId(map_idDomif_listeIdUnif.keySet()), map_idUnif_idLigne);
        matrice = ReducteurSVD_MTJ.reduireEtPondererMatriceU(new SVD(matrice.numRows(), matrice.numColumns()).factor(matrice), 300);
        new CalculateurParralleleMatriceCosinusEntreMots().calculerMatriceCosinus(fileBDSimEntreUnif, matrice, map_unifString_id, map_idUnif_idLigne);
       
    }
    
    public void calculerMatriceCosinusEntreMotsCibles (File filePathDescripteur, File filePathCorpus, File fileListeMotCible, File fileListeCooccurrents, String fileBDSimEntreUnif) throws IOException, SQLException
    {
        Set<String> ensembleMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(filePathDescripteur.getAbsolutePath());
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(fileListeMotCible.getAbsolutePath());
        ensembleMotCible.addAll(ensembleMotDescripteur);
        Set<String> ensembleCooc = Tokeniseur.extraireEnsembleTokenDunFichier(fileListeCooccurrents.getAbsolutePath());
        Set<String> dictionnaire  = new TreeSet<String>(ensembleMotCible);
        dictionnaire.addAll(ensembleCooc);
               
        
        Map<Integer, Domif> map_id_domif = recupererDocument(filePathCorpus);
        map_id_domif = new FiltreurTokenSelonDictionnaire(dictionnaire).filtrer(map_id_domif);
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(dictionnaire);
        Map<String, Integer> map_unifCibleString_id = Intersection.getMap_string_int_intersection(map_unifString_id, ensembleMotCible);
        Map<String, Integer> map_unifCoocString_id = Intersection.getMap_string_int_intersection(map_unifString_id, ensembleCooc);
                
//////        Map<Integer, String> map_id_unifString = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, map_unifString_id.keySet());
//////        Map<Integer, String> map_id_motDescripteur = IndexationBase.recupererSousMap_IdUnif_UnifString(map_unifString_id, ensembleMotDescripteur);
//////        new AfficherTableau("Les mots Descripteurs", "id", "mots", new TreeMap(map_id_motDescripteur)).setVisible(true);
                
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnifUnique(map_id_domif, map_unifString_id);
        
        
        DenseMatrix matrice = IndexeurUnifParDomif_MTJ.indexerUnifParDomif(map_idDomif_listeIdUnif, associerId(map_idDomif_listeIdUnif.keySet()), associerId());
        new CalculateurParralleleMatriceCosinusEntreMots().calculerMatriceCosinus(fileBDSimEntreUnif, map_unifCibleString_id, map_unifCoocString_id, map_idDomif_listeIdUnif);
        
        new CalculateurParralleleMatriceCosinusEntreMots().calculerMatriceCosinus(fileBDSimEntreUnif, map_unifCibleString_id, map_unifCoocString_id, map_idDomif_listeIdUnif);
        
//////        Map<Integer, ? extends UnifBase> map_id_unif = ProjetBioMed.indexationBioMedCooc(map_id_domif, map_unifString_id, map_id_unifString.keySet(), map_id_unifString.keySet(), new CoefficientInformationMutuelle());
//////        Map<Integer, VecteurCreux> map_id_vecteur = new VectorisateurDomif(IndexationBase.recupererMap_IdDomif_ensembleIdUnif(map_id_unif)).vectorisationCreuse(map_id_unif);
//////        map_id_vecteur = Vectorisateur.normerVecteurCreux(map_id_vecteur.values());
//////        System.exit(1);
        
    }
    
    private Map<Integer, Integer> associerId (Set<Integer> ensembleId)
    {
        Map<Integer, Integer> map =new HashMap<>(ensembleId.size());
        int id = 0;
        for (int e: ensembleId)
        {
            map.put(e, id);
            id++;
        }
        return map;        
    }
    
    public void recupererMotDescripteurMaxEntropie (File pathCorpus, File pathMotCible, int nCoocurrent) throws IOException
    {
        
        Map<Integer, Domif> map_id_domif = recupererDocument(pathCorpus);
        Set<String> ensembleMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(pathMotCible.getAbsolutePath());
        map_id_domif = new FiltreurTokenSelonDictionnaire(ensembleMotCible).filtrer(map_id_domif);
        map_id_domif = new FiltreurDomifLongueurMin(3).filtrerDomif(map_id_domif);
        
        Map<String, Integer> map_unifString_id = Segmentation.recupererMap_unifString_idUnif(map_id_domif.values());
        Map<Integer, List<Integer>> map_idDomif_listeIdUnif = Segmentation.recupererMap_idDomif_listeIdUnifUnique(map_id_domif, map_unifString_id);
//        Map<Integer, Set<Integer>> map_idDomif_ensembleIdUnif  = Segmentation.recupererMap_idDomif_ensembleIdUnif(map_id_domif, map_unifString_id);
        
        map_id_domif.clear();
        System.gc();
        Set<Integer> ensembleIdUnif = new TreeSet<Integer>(map_unifString_id.values());
//        Map<Integer, UnifBase> map_id_unif = new IndexeurUnifBaseParDomif().indexer(map_idDomif_listeIdUnif, ensembleIdUnif);
        Map<Integer, UnifBase> map_id_unif = new IndexeurUnifParUnif().indexerUnifBaseParUnifBase2(map_idDomif_listeIdUnif, ensembleIdUnif, ensembleIdUnif);
        Set<Integer> ensembleIdUnifCooc = new FiltreurUnifParEntropieDocumentaireNmax(IndexationBase.recupererMap_idUnif_entropieDocumentaireBinaire(map_id_unif), nCoocurrent).filtrerUnifBase(map_id_unif).keySet(); 
        for (Map.Entry<String, Integer> e: map_unifString_id.entrySet())
        {
            if (ensembleIdUnifCooc.contains(e.getValue()))
            {
                System.out.println(e.getKey());
            }
        }
    }
    
    public void recupererListeMotCible (Collection<Domif> collectionDomif)
    {
        Set<String> set = Segmentation.recupererEnsembleUnifString(collectionDomif);
        Collection coll = new ArrayList();
        for (Domif d: collectionDomif)
            coll.add(d.getListeToken());
        Map<String, Integer> map_token_freqTot = Segmentation.recupererMap_unifString_frequenceTotale(set, coll);
        for (Map.Entry<String,Integer> e: map_token_freqTot.entrySet())
            System.out.println(e.getKey() + "\t" + e.getValue());
        System.exit(1);
    }
    
    public Map<String, String> recupererCategorieMot(File fileCategorieMot) throws IOException
    {
        Map<String, String> map_descripteur_categorie = new TreeMap<>();
        String document = FileUtils.read(fileCategorieMot.getPath());
        List<String> liste = ExtracteurUtils.extraireParBalises(document, "<", ">");
        for (String association: liste)
        {
            List<String> mots = Tokeniseur.extraireListeToken(association);
            if (mots.size() != 2)
            {
                System.err.println("*** mauvaise tekenisation des mots");
            }
            else
            {
                System.out.println(mots);
                map_descripteur_categorie.put(mots.get(0), mots.get(1));
            }
        }
        return map_descripteur_categorie;
    }
    
    public Map<Integer, Domif> recupererDomifPhrase (File filePathCorpus, Set<String> dictionnaire) throws IOException
    {
        System.out.println("RECUPERER LES DOCUMENTS");
        Map<Integer, Domif> map = new TreeMap<Integer, Domif>();
        int id = 0;
        List<File> listeFile = FileUtils.getAllFileInPath(filePathCorpus, ".txt", new ArrayList<File>());
        String document;
        Pattern patternParagraphe = Pattern.compile("<p>"+"(.*?)"+"</p>", Pattern.DOTALL);
        Pattern patternPhrase = Pattern.compile("<s>"+"(.*?)"+"</s>", Pattern.DOTALL);
        
        for (File f: listeFile)
        {
            document = FileUtils.read(f.getPath());
            List<String> listeParag = ExtracteurUtils.extraireParBalises(document, patternParagraphe);
            for (String paragraphe: listeParag)
            {
                List<String> listePhrases = ExtracteurUtils.extraireParBalises(paragraphe, patternPhrase);
                for (String phrase: listePhrases)
                {
                    ArrayList<String> listeToken = Tokeniseur.extraireListeToken(phrase);
                    if (Intersection.verifierSiIntersectionString(dictionnaire, new HashSet<String>(listeToken))==true)
                    {
                        map.put(id, new Domif(null, -1, id, listeToken, -1));
                        id++;
                    }
                }
            }
        }
        System.out.println("nombre de domif construit: " + map.size());
        return map;        
    }
    
    public Map<Integer, Domif> recupererDomifParagraphe (File filePathCorpus, Set<String> dictionnaire) throws IOException
    {
        System.out.println("RECUPERER LES DOCUMENTS");
        Map<Integer, Domif> map = new TreeMap<Integer, Domif>();
        int id = 0;
        List<File> listeFile = FileUtils.getAllFileInPath(filePathCorpus, ".txt", new ArrayList<File>());
        String document;
        Pattern pattern = Pattern.compile("<p>"+"(.*?)"+"</p>", Pattern.DOTALL);
        
        for (File f: listeFile)
        {
            document = FileUtils.read(f.getPath());
            List<String> listeParag = ExtracteurUtils.extraireParBalises(document, pattern);
            for (String paragraphe: listeParag)
            {
                ArrayList<String> listeToken = Tokeniseur.extraireListeToken(paragraphe);
                if (Intersection.verifierSiIntersectionString(dictionnaire, new HashSet<String>(listeToken))==true)
                {
                    map.put(id, new Domif(null, -1, id, listeToken, -1));
                    id++;
                }
            }
        }
        System.out.println("nombre de domif construit: " + map.size());
        return map;        
    }
    
    public Map<Integer, Domif> recupererDocument (File filePathCorpus, Set<String> dictionnaire) throws IOException
    {
        
        List<File> listeFile = FileUtils.getAllFileInPath(filePathCorpus, ".txt", new ArrayList<File>());
        TreeMap<Integer, Domif> map = new TreeMap<Integer, Domif>();
        String doc;
        int id = 0;
        for (File f: listeFile)
        {
            doc = FileUtils.read(f);
            ArrayList<String> listeToken = Tokeniseur.extraireListeToken(doc);
            if (Intersection.verifierSiIntersectionString(dictionnaire, new HashSet<String>(listeToken))==true)
            {
                map.put(id, new Domif(null, -1, id, listeToken, -1));
                id++;
            }
        }
        return map;
    }
    
    public Map<Integer, Domif> recupererDocument (File filePathCorpus) throws IOException
    {
        
        List<File> listeFile = FichierTxt.readManyFile(filePathCorpus, ".txt", new ArrayList<File>());
        TreeMap<Integer, DocumentJFC> map_idDoc_docJFC = new TreeMap<Integer, DocumentJFC>();
        ParseurCorpusFormatJFC parser = new ParseurCorpusFormatJFC();
        String doc;
        int idDoc = 0;
        for (File f: listeFile)
        {
            doc = FichierTxt.readFileAsString(f.getPath());
            map_idDoc_docJFC.put(idDoc, new DocumentJFC(parser.extraireTexte(doc), new TreeSet<String>(), -1, idDoc, -1));
            idDoc++;
        }
        Map<Integer, DomifSegment> map_id_domifSegment = new SegmenteurParDocument().segmenter(new Corpus(map_idDoc_docJFC)).getMap_Id_Domif();
        return Tokenisation.Tokeniseur.tokeniser(map_id_domifSegment); 
        
    }
    
     
    
    public static void statistiqueDomif (Map<Integer, Domif> map_id_domif)
    {
        System.out.println("nbre de doc : " + map_id_domif.size());
        double moy = Segmentation.calculerNbreMoyenTokenParDomif(map_id_domif, Segmentation.compterNbreToken(map_id_domif));
        List<Double> l = new ArrayList<Double>();
        int s;
        for (Domif d: map_id_domif.values())
        {
//            s = d.getListeToken().size()
            System.out.println(d.getListeToken().size());
            l.add((double)d.getListeToken().size());
        }
            
               
        double e = StatistiqueEchantillon.retournerEcartType(l);
        System.out.println("ecart-type : " + e);
        System.exit(1);
    }
    
    public static void statistiqueIndexation (final Map<Integer, ? extends UnifBase> map_id_unif, Map<Integer, String> map_id_unifString)
    {
        TreeMap<String, Double> map_unif_freqTot = IndexationBase.recupererMap_unifString_freqTotale(map_id_unif, map_id_unifString);
        new AfficherTableau("La frequence totale de chaque unif", "UNIF", "Frequence Totale", map_unif_freqTot).setVisible(true);
        TreeMap<String, Integer> map_unif_freqDoc = IndexationBase.recupererMap_unifString_freqDoc(map_id_unif, map_id_unifString);
        new AfficherTableau("La frequence documentaire de chaque unif", "UNIF", "Frequence Documentaire", map_unif_freqDoc).setVisible(true);
        TreeMap<Integer, Double> map_freqDoc_prob = IndexationBase.recupererMap_freqDocumentaire_proportion(map_id_unif, map_id_unifString);
        new AfficherTableau("Loi de Zipf sur la frequence documentaire", "Frequence documentaire", "Probabilite", map_freqDoc_prob).setVisible(true);
    }
        
    public static Partition  analyserThemeOeuvre (Map<Integer, VecteurCreux> map_id_vecteur) 
    {
        
        
//        Map<Integer, Set<Integer>> map_idDomif_ensembleIdUnif = IndexationBase.recupererMap_IdDomif_ensembleIdUnif(map_id_unif);
//        Map<Integer, VecteurCreux> map_id_vecteur = new VectorisateurDomif(map_idDomif_ensembleIdUnif).vectorisationCreuse(map_id_unif);
////        MDS.calculerReductionDimensionelle(map_id_vecteur, new DistanceEuclidienne());
//        System.exit(1);      
//        Projet.approximerParametreK(map_id_vecteur, new Cosinus(), 500, 0.0000001, 2, 50, 1, true, 40);
        
//        ProjetBioMed.approximerK(map_id_vecteur, new Cosinus(), 500, 0.0000001, 2, 15, 1, 20);
        
        Partition partitionBest = Projet.trouverMeilleurPartitionKmeansMaximumSelonRatioVariance(new TreeMap(map_id_vecteur), new Cosinus(), 500, 0.0000001, 2, 20, 1, true, 10);
        TreeMap<Integer, Integer> map_IdDomif_IdClasse = partitionBest.recupererMap_IdDomif_IdClasse();
        new AfficherTableau("Le idClasse pour chaque idDomif d'une partition", "ID DOMIF", "ID CLASSE", map_IdDomif_IdClasse).setVisible(true);
        TreeMap<Integer, Integer> mapIdClasseNbreDomif = partitionBest.recupererMapIdClasseNbreDomif();
        new AfficherTableau("Nombre de domif par classe", "idClasse", "nbre domif", mapIdClasseNbreDomif).setVisible(true);
        TreeMap<Integer, Double> map_idDomif_proximiteCentroide = partitionBest.calculerEcartAuCentroide(new TreeMap(map_id_vecteur), new Cosinus());
        new AfficherTableau("Proximite de chaque oeuvre a son centroide", "idOeuvre", "proximite avec le centroide", map_idDomif_proximiteCentroide).setVisible(true);
                
        return partitionBest;
    }
    
    public static void approximerK (Map<Integer, VecteurCreux> map_id_vecteurCreux, MetriqueSim metriqueSim, int nbreMaxIteration, double minErreur, int minNombreClasse, int maxNombreClasse, int intervalle, int iterationKM)
    {
        ClassifieurKmeansRepete kmR = new ClassifieurKmeansRepete(new TreeMap(map_id_vecteurCreux), metriqueSim, nbreMaxIteration, minErreur, true, iterationKM);
        EvaluateurRatioVarianceMultiClassifieur eval = new EvaluateurRatioVarianceMultiClassifieur(new TreeMap(map_id_vecteurCreux), new ErreurQuadratique());
        ApproximateurNombreClasse aaa = new ApproximateurNombreClasse();
        aaa.approximerNombreClasseKmeans(kmR, eval, map_id_vecteurCreux, minNombreClasse, maxNombreClasse, intervalle);
    }
    
    public static void ponderationUnifParClasse (Map<Integer, VecteurCreux> map_id_vecteur, Map<Integer, ? extends UnifBase> map_id_ToutLesUnif, Partition partition, Map<Integer, String> map_idUnif_unifString)
    {
//        // cette appel de methode est simplement pour permettre de calculer la ponderation sur tous les unif et pas seulement celles utilisees pour la classification
//        TreeMap<Integer, Unif> map_id_UnifReclasses = Indexation.reconstruireMap_IdUnif_Unif(new TreeMap(map_id_ToutLesUnif), partition.getMap_id_Classe());            
        TreeMap<Integer, VecteurIndicie> map_IdUnif_VecteurPonderationParClasse;
        Ponderation ponderationParClasse;
//            
////            // information mutuelle
        map_IdUnif_VecteurPonderationParClasse = new PonderateurParClasse().pondererUnifParClasse(map_id_ToutLesUnif, partition.recupererMap_IdDomif_IdClasse(), new InformationMutuelle());
        ponderationParClasse = new Ponderation(map_IdUnif_VecteurPonderationParClasse);
        ponderationParClasse.afficherTableauDesPonderationUnifParClasse(new TreeMap(map_idUnif_unifString), "information mutuelle");
               
        ////            // probabilite conditionnelle
        map_IdUnif_VecteurPonderationParClasse = new PonderateurParClasse().pondererUnifParClasse(map_id_ToutLesUnif, partition.recupererMap_IdDomif_IdClasse(), new ProbConditionnelle1());
        ponderationParClasse = new Ponderation(map_IdUnif_VecteurPonderationParClasse);
        ponderationParClasse.afficherTableauDesPonderationUnifParClasse(new TreeMap(map_idUnif_unifString), "probabilite conditionnelle");
 
        
//        TreeMap<Integer, VecteurIndicie> map_vecteurSim = AnalyseSimilitude.construireMatriceSimilitudeCosinusOptimise(map_id_vecteur);
//        AnalyseSimilitude.afficherSimilitudeEntreMots(map_vecteurSim, IndexationBase.recupererMap_IdUnif_UnifString(map_id_ToutLesUnif), partition.recupererMap_IdDomif_IdClasse());
    }
    
    public static String creerMatriceCsv (Map<Integer, VecteurCreux> map_id_vecteur, TreeMap<Integer, String> map_id_motDescripteur)
    {
        ArrayList<String> liste = new ArrayList<String>(map_id_motDescripteur.size());
        for (String motDescripteur: map_id_motDescripteur.values())
        {
            liste.add(motDescripteur);
        }
        
        return new FileCSV().construireFichierCSV(liste, map_id_vecteur);
    }
    
    public static String creerMatriceCsv (Map<Integer, String> map_idMotCible_motCible, Map<Integer, VecteurCreux> map_id_vecteur, TreeMap<Integer, String> map_id_motDescripteur)
    {
        ArrayList<String> listeDescripteur = new ArrayList<String>(map_id_motDescripteur.size());
        for (String motDescripteur: map_id_motDescripteur.values())
        {
            listeDescripteur.add(motDescripteur);
        }
        
        return new FileCSV().construireFichierCSV(map_idMotCible_motCible, listeDescripteur, map_id_vecteur);
    }
    
    public static Map<Integer, Domif> segmentationBioMed (Corpus corpus, Set<String> listeToken) throws IOException
    {
        System.out.println("PRETRAITEMENT ");
        Map<Integer, DomifSegment> map_id_domifSegment = new SegmenteurParDocument().segmenter(corpus).getMap_Id_Domif();
        Map<Integer, Domif> map_id_domif = Tokenisation.Tokeniseur.tokeniser(map_id_domifSegment);       
        statistiqueDomif (map_id_domif);
        map_id_domif = new FiltreurDomifSelonDictionnaire(listeToken).filtrerDomif(map_id_domif);
        map_id_domif = new FiltreurDomif.FiltreurDomifLongueurMin(1).filtrerDomif(map_id_domif);
        
//        TreeMap<Integer, String> map_id_segment = new TreeMap(Segmentation.recupererMap_idDomif_tokensEnString(map_id_domif));
//        new AfficherTableau("Les domifs de Pierce", "id domif", "segment avec nom et adjectif seulement", map_id_segment).setVisible(true);
        
        return map_id_domif;
    }
    
    public static Map<Integer, ? extends UnifContenu> indexationBioMed (Map<Integer, Domif> map_id_domif, Set<String> ensembleToken)
    {
        int i = 0;
        Map<String, Integer> map_unifString_id = new HashMap<String, Integer>(ensembleToken.size());
        for (String token: ensembleToken)
        {
            map_unifString_id.put(token, i);
            i++;
        }
        
        Map<Integer, ? extends UnifContenu> map_id_unif = IndexeurUnifParDomif.indexerUnif(map_id_domif, map_unifString_id);
        
        TreeMap<String, Double> map_unif_freqTot = IndexationBase.recupererMap_unifString_freqTotale_SurUnifContenu(map_id_unif);
        new AfficherTableau("La frequence totale de chaque unif", "UNIF", "Frequence Totale", map_unif_freqTot).setVisible(true);
        TreeMap<String, Integer> map_unif_freqDoc = IndexationBase.recupererMap_unifString_freqDoc(map_id_unif);
        new AfficherTableau("La frequence documentaire de chaque unif", "UNIF", "Frequence Documentaire", map_unif_freqDoc).setVisible(true);
        
        return map_id_unif;
    }
    
    public static Map<Integer, UnifBase> indexationBioMedCooc (Map<Integer, Domif> map_id_domif, Map<String, Integer> map_unifString_id, Set<Integer> ensembleIdUnifCible, Set<Integer> ensembleIdUnifDescripteur)
    {
//        Set<String> ensembleToken = new TreeSet<String>();
//        ensembleToken.addAll(ensembleIdUnifCible);
//        ensembleToken.addAll(ensembleIdUnifDescripteur);
        Map<Integer, Set<Integer>> map_idDomif_ensembleIdUnif = Segmentation.recupererMap_idDomif_ensembleIdUnif(map_id_domif, map_unifString_id);
//        Set<Integer> ensembleIdUnifCible = new TreeSet<Integer>();
//        Set<Integer> ensembleIdUnifDescripteur = new TreeSet<Integer>();
//        for (String i: map_unifString_id.keySet())
//        {
//            if (ensembleIdUnifCible.contains(i))
//                ensembleIdUnifCible.add(map_unifString_id.get(i));
//            if (ensembleIdUnifDescripteur.contains(i))
//                ensembleIdUnifDescripteur.add(map_unifString_id.get(i));
//        }
        return IndexeurUnifParUnif.indexerUnifBaseParUnifBase(map_idDomif_ensembleIdUnif, ensembleIdUnifCible, ensembleIdUnifDescripteur);
    }
    
    public static Map<Integer, UnifContenu> indexationBioMedCooc (Map<Integer, Domif> map_id_domif, Map<String, Integer> map_unifString_id, Set<Integer> ensembleIdUnifCible, Set<Integer> ensembleIdUnifDescripteur, Map<Integer, String> map_idUnif_unifString)
    {
        Map<Integer, Set<Integer>> map_idDomif_ensembleIdUnif = Segmentation.recupererMap_idDomif_ensembleIdUnif(map_id_domif, map_unifString_id);
        return IndexeurUnifParUnif.indexerUnifContenuParUnifContenu(map_idDomif_ensembleIdUnif, ensembleIdUnifCible, ensembleIdUnifDescripteur, map_idUnif_unifString);
    }
    
    public static Map<Integer, UnifBase> indexationBioMedCooc (Map<Integer, Domif> map_id_domif, Map<String, Integer> map_unifString_id, Set<Integer> ensembleIdUnifCible, Set<Integer> ensembleIdUnifDescripteur, CoefficientAssociation coefficientAssociation)
    {
        Map<Integer, List<Integer>> map_idDomif_ensembleIdUnif = Segmentation.recupererMap_idDomif_listeIdUnif(map_id_domif, map_unifString_id);
        Set<Integer> ensembleIdUnif = new TreeSet(ensembleIdUnifCible);
        ensembleIdUnif.addAll(ensembleIdUnifDescripteur);
        Map<Integer, UnifBase> indexationUnifParDomif = new IndexeurUnifBaseParDomif().indexer(map_idDomif_ensembleIdUnif, ensembleIdUnif);
        
        Map<Integer, Set<Integer>> map_idCooc_ensembleIdDomif = IndexationBase.recupererMap_idUnif_ensembleIdDomif(indexationUnifParDomif, ensembleIdUnifDescripteur);
        Map<Integer, Set<Integer>> map_idCible_ensembleIdDomif = IndexationBase.recupererMap_idUnif_ensembleIdDomif(indexationUnifParDomif, ensembleIdUnifCible);
        Set<Integer> ensembleIdDomif = map_id_domif.keySet();
        return new IndexeurUnifCoocParUnifCibleCoefficient(coefficientAssociation).indexerUnifCoocParUnifCible(map_idCible_ensembleIdDomif, map_idCooc_ensembleIdDomif, ensembleIdDomif);
    }
    
    public static void calculerProbabiliteConditionnelleCooccurrence (Set<String> ensembleMotCible, Set<String> ensembleDescripteur, Map<Integer, ? extends UnifContenu> map_id_unif, int nbreDomif)
    {
        System.out.println("CALCULER PROBABILITE CONDITIONNELLE DE COOCCURRENCE");
        
        Map<String, Integer> map_unifString_id = IndexationBase.recupererMap_unifString_idUnif(map_id_unif);
        UnifBase u1,u2;
        double probCondCooc;
        int id;
        for (String motCible: ensembleMotCible)
        {
            u1 = map_id_unif.get(map_unifString_id.get(motCible));
            for (String descripteur: ensembleDescripteur)
            {
                // test
                if (map_unifString_id.get(descripteur) == null)
                {
                    System.err.println("descripteur non indexe : " + descripteur);
                    System.exit(1);
                }
                
                id = map_unifString_id.get(descripteur);
                u2 = map_id_unif.get(id);
                probCondCooc = IndexationBase.calculerProbabiliteConditionnelleCooccurrence(u1, u2, nbreDomif);
                System.out.println(motCible + "\t"+descripteur+"\t"+probCondCooc);
            }
        }
    }
    
}
