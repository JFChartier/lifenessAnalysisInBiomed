/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses;

import Affichage.Tableau.AfficheurTableau;
import Article.Article;
import ClassificationAutomatique.Supervisee.KPPV.ClassifieurPlusProcheVoisin;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import FiltreurToken.FiltreurAntidictionnaire;
import FiltreurToken.FiltreurFrequenceDocumentaire;
import FiltreurToken.FiltreurNombre;
import FiltreurToken.FiltreurSingleton;
import Matrice.MtjMatrice;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.Vectoriseur;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Similitude.MetriqueSim;
import Metrique.Similitude.ProduitScalaireIndicie;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.InformationMutuelle;
import Pretraitement.Pretraitement;
import Racinisation.RacineurAnglais;
import ReductionDimensionnelle.SVD.ReducteurSVD_MTJ;
import Tokenisation.Tokeniseur;
import UtilsJFC.TrieUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;

/**
 *
 * @author JF Chartier
 */
public class AnalyseDesMotsPlusSimilaires 
{
    public static void main(String[] args)
    {
                
        try
        {
//            Set<String> tokens = new TreeSet<>(Dictionnaire.recupererCategorieMot(Dictionnaire.getFileCategorieMotCibleV8()).values());
//            new AnalyseDesMotsPlusSimilaires().analyserKNN(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), tokens, new InformationMutuelle(), TypeContenu.PARAGRAPHE, 500000, 50);
            new AnalyseDesMotsPlusSimilaires().analyserKNNdesCategoriesCibles(CorpusUtils.getRepertoireArticleFiltreRevueSelectionne(), Dictionnaire.getFileCategorieMotCibleV9(), new InformationMutuelle(), TypeContenu.PARAGRAPHE, -1, 50);
            
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    // tous les mots cibles sont conserves, meme ceux peu frequents
    // pour chaque categorie, cornstruit une centroide de ses tokens
    // rechecher pour chaque centroide, les k mots les plus similaires  
    public void analyserKNNdesCategoriesCibles (File filesArticles, File fileCategoriesCibles, CoefficientAssociation coefficient, TypeContenu typeContenu, int nombreContexte, int knn) throws IOException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
       
        if (nombreContexte!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreContexte);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        Pretraitement.initialiserIdArticle(articles);
        
        Map<String, String> map_token_catCible = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
        
//        articles = new Pretraitement(true, true, true, 100, articles.size()/3).filtrerArticle(articles);
        // cette methode empeche de supprimer les mots cibles
        articles = Pretraitement.filtrerArticle(articles, new FiltreurAntidictionnaire("anglais", false),
                new FiltreurNombre(), new FiltreurSingleton(), new FiltreurFrequenceDocumentaire(CorpusUtils.getMap_token_nombreArticle(articles), 100, articles.size()/3), 
                map_token_catCible.keySet());
        
        
//        articles = Pretraitement.raciner(articles);
               
//        tokens = RacineurAnglais.raciner(tokens);
        
        
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        AfficheurTableau.afficher2Colonnes("token et categorie", "token", "categorie", map_token_catCible);
        
        Map<String, Integer> map_token_freqDoc = CorpusUtils.getMap_token_nombreArticle(articles, map_token_catCible.keySet());
        AfficheurTableau.afficher2Colonnes("token et frequence", "token", "nombre document", map_token_freqDoc);
        
        Map<String, Set<String>> map_cat_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_catCible);
//        Map<String, Integer> map_motCible_id = CorpusUtils.getSousMap_token_id(map_token_id, tokens);
//        AfficheurTableau.afficher2Colonnes("categorie cible et id", "categorie", "id", map_motCible_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
//        Map<Integer, Set<Integer>> map_idMotCible_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_motCible_id);
//        Map<Integer, Set<Integer>> map_idMotDesc_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_motDescripteur_id);
        
        Map<Integer, VecteurIndicie> vecteursToken = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
        Vectoriseur.normerVecteurIndicie2(vecteursToken.values());
        
        Map<Integer, String> map_id_token = TrieUtils.inverserMap(map_token_id);
        for (String cat: map_cat_tokens.keySet())
        {
            VecteurIndicie v = new VecteurIndicie(-1, new TreeMap<>());
            for (String token: map_cat_tokens.get(cat))
            {
                if (map_token_id.containsKey(token))
                {
                    v.additionnerVecteur(vecteursToken.get(map_token_id.get(token)).getMap_Id_Ponderation());
                }
            }
                        
            List<Map.Entry<Integer, Double>> entries = ClassifieurPlusProcheVoisin.kPlusProcheVoisin(v.getMap_Id_PonderationNorme(), vecteursToken.values(), knn, new ProduitScalaireIndicie());
            for (int i = 0; i < entries.size(); i++)
            {
                
                String coToken = map_id_token.get(entries.get(i).getKey());
                System.out.printf("%-30.30s %-30.30s %-30.30s\n", cat, coToken, entries.get(i).getValue());
            }
        }
        
    }
    
    public void analyserKNN (File filesArticles, Set<String> tokens, CoefficientAssociation coefficient, TypeContenu typeContenu, int nombreContexte, int knn) throws IOException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
       
        if (nombreContexte!=-1)
            articles = Pretraitement.filtreAleatoireArticle(articles, nombreContexte);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        Pretraitement.initialiserIdArticle(articles);
        articles = new Pretraitement(true, true, true, 100, articles.size()/3).filtrerArticle(articles);
//        articles = Pretraitement.raciner(articles);
        
        
//        tokens = RacineurAnglais.raciner(tokens);
        
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        Map<String, Integer> map_motCible_id = CorpusUtils.getSousMap_token_id(map_token_id, tokens);
//        AfficheurTableau.afficher2Colonnes("categorie cible et id", "categorie", "id", map_motCible_id);
        
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
//        Map<Integer, Set<Integer>> map_idMotCible_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_motCible_id);
//        Map<Integer, Set<Integer>> map_idMotDesc_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_motDescripteur_id);
        
        Map<Integer, VecteurIndicie> vecteursToken = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
        Vectoriseur.normerVecteurIndicie2(vecteursToken.values());
        
        Map<Integer, String> map_id_token = TrieUtils.inverserMap(map_token_id);
        for (String reference: map_motCible_id.keySet())
        {
            int idReference = map_motCible_id.get(reference);
            List<Map.Entry<Integer, Double>> entries = ClassifieurPlusProcheVoisin.kPlusProcheVoisin(vecteursToken.get(idReference).getMap_Id_Ponderation(), vecteursToken.values(), knn, new ProduitScalaireIndicie());
            for (int i = 0; i < entries.size(); i++)
            {
                
                String coToken = map_id_token.get(entries.get(i).getKey());
                System.out.printf("%-30.30s %-30.30s %-30.30s\n", reference, coToken, entries.get(i).getValue());
            }
        }
        
    }
    
    
    public static String analyser(File filesArticles, File fileCategoriesCibles, TypeContenu typeContenu, int nombreArticles, int kFacteurs, int NPlusSimilaire, MetriqueSim metrique) throws IOException, NotConvergedException
    {
        Collection<Article> articles = Parser.ParserBioMedFiltre.recupererContenuArticle(filesArticles, typeContenu);
        articles = Pretraitement.filtreAleatoireArticle(articles, nombreArticles);
//        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        articles = new Pretraitement(true, true, true, 15, nombreArticles-1000).filtrerArticle(articles);
        
        Map<String, String> map_token_categorie = Dictionnaire.recupererCategorieMot(fileCategoriesCibles);
        articles = Pretraitement.remplacerMotParCategorie(articles, map_token_categorie);
        Map<String, Integer> map_token_id = CorpusUtils.getMap_token_id(articles);
        Map<String, Integer> map_categorieCible_id = CorpusUtils.getSousMap_token_id(map_token_id, map_token_categorie.values());
        
        Map<Integer, VecteurCreux> vecteurs = vectoriser(articles, map_token_id, kFacteurs);
        Map<Integer, String> map_id_token = TrieUtils.inverserMap(map_token_id);
        StringBuilder rapport = new StringBuilder("categorie \t mot \t similarite" + "\r\n");
        for (String categorie: map_categorieCible_id.keySet())
        {
            int idCategorie = map_categorieCible_id.get(categorie);
            Map<Integer, Double> map_idTerme_sim = ClassifieurPlusProcheVoisin.kPlusProcheVoisin(vecteurs.get(idCategorie).getTabPonderation(), vecteurs.values(), NPlusSimilaire, metrique);
            for (int id: map_idTerme_sim.keySet())
            {
                String termeSim = map_id_token.get(id);
                String s = categorie + "\t" + termeSim + "\t" + map_idTerme_sim.get(id);
                rapport.append(s).append("\r\n");
                System.out.println(categorie + "\t" + termeSim + "\t" + map_idTerme_sim.get(id));
            }
        }
        return rapport.toString();
    }
    
    private static Map<Integer, VecteurCreux> vectoriser (Collection<Article> articles, Map<String, Integer> map_token_id, int kFacteurs) throws NotConvergedException
    {
        Map<Integer, VecteurIndicie> vecteurs = CorpusUtils.getMap_idArticle_vecteurTokenBM25(articles, map_token_id);
        Set<Integer> ensembleIdArticle = CorpusUtils.getEnsembleIdArticle(articles);
        int nbreArt = ensembleIdArticle.size();
        int nbreTerme = map_token_id.size();
        Map<Integer, Integer> map_idArt_idColonne = associerId(ensembleIdArticle);
        Map<Integer, Integer> map_idToken_idLigne = associerId(new TreeSet<Integer>(map_token_id.values()));
        DenseMatrix matriceTokenArticle = MtjMatrice.convertirVecteurVersColonneMatrice(vecteurs.values(), map_idArt_idColonne, map_idToken_idLigne, nbreTerme, nbreArt);
//        matriceTokenArticle = new ReducteurSVD_MTJ(matriceTokenArticle).reduireEtPondererMatriceU(kFacteurs);
        return ReducteurSVD_MTJ.recupererVecteurTermeReduit(matriceTokenArticle, kFacteurs, TrieUtils.inverserMap(map_idToken_idLigne));
    }
    
    private static Map<Integer, Integer> associerId (Set<Integer> ensembleId)
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
}
