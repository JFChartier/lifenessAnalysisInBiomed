/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Corpus;

import Affichage.Tableau.AfficheurTableau;
import Article.Article;
import Indexation.Indexeur.UnifBase;
import Indexation.Indexeur.UnifParUnif.IndexeurUnifCoocParUnifCibleCoefficient;
import Matrice.MtjMatrice;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.VectoriseurUtils;
import Ponderation.CoefficientAssociation;
import ReductionDimensionnelle.SVD.ReducteurSVD_MTJ;
import UtilsJFC.Intersection;
import UtilsJFC.TrieUtils;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
public class CorpusUtils 
{
    private static final File repertoireArticle = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articles\\");
    private static final File repertoireArticleFiltreLanci = new File("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\articleFiltre2\\");
    private static final File repertoireArticleFiltreRevueSelectionne = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articleFiltreRevueSelect\\");
    private static final File repertoireArticleRevueSelectionne = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articleRevueSelect\\");
    private static final File repertoireParagrapheRevueSelectionnee = new File ("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\paragrapheRevueSelect\\");
    private static final File fileArffVecteurMots = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMotsAvecClasseEtNom.arff");
    
    public static Collection<Article> recupererArticleSelonDictionnaire (final Collection<Article> articles, Set<String> dictionnaire)
    {
        Collection<Article> articlesFiltre = new ArrayList<>();
        for (Article art: articles)
        {
            if (Intersection.intersection(dictionnaire, art.getListeToken())==true)
                articlesFiltre.add(new Article(art.getId(), new ArrayList<>(art.getListeToken())));
        }
        System.out.println("nombre article recuperes : " + articlesFiltre.size());
        return articlesFiltre;
    }
    
    public static Collection<Article> recupererArticleSelonCible (final Collection<Article> articles, String token)
    {
        System.out.println("recupererArticleSelonCible: " + token);
        Set<String> s = new HashSet<>(1);
        s.add(token);
        return recupererArticleSelonDictionnaire(articles, s);
        
    } 
    
    public static Set<Integer> getEnsembleIdArticle(Collection<Article> articles)
    {
        System.out.println("getEnsembleIdArticle");
        Set<Integer> set = new HashSet<>(articles.size());
        for (Article art: articles)
            set.add(art.getId());
        System.out.println("\t"+set.size());
        return set;
    }
    
    public static Map<String, Integer> getMap_token_id(Collection<Article> articles)
    {
        System.out.println("getMap_token_id");
        Set<String> tokens = getTokens(articles);
        Map<String, Integer> map = new HashMap<>(tokens.size());
        int id = 0;
        for (String token: tokens)
        {
            map.put(token, id);
            id++;
        }
        return map;
    }
    
    public static Set<Integer> getIdTokens (Map<String, Integer> map_token_id, Set<String> tokens)
    {
        Set<Integer> s = new HashSet<>();
        for (String t: tokens)
        {
            if (map_token_id.containsKey(t))
                s.add(map_token_id.get(t));
        }
        return s;
    }
    
    public static Set<Integer> getIdTokens2 (Map<Integer, String> map_id_token, Set<String> tokens)
    {
        Set<Integer> s = new HashSet<>();
        for (int id: map_id_token.keySet())
        {
            if (tokens.contains(map_id_token.get(id)))
                s.add(id);
        }
        
        return s;
    }
    
    public static Map<String, Integer> recupererSousEnsembleAleatoireMap_token_id(Map<String, Integer> map_token_id, int n)
    {
        System.out.println("recupererSousEnsembleAleatoireMap_token_id");
        List<Map.Entry<String, Integer>> liste = new ArrayList<>(map_token_id.entrySet());
        Collections.shuffle(liste, new Random(1));
        Map<String, Integer> map = new HashMap<>(n);
        for (int i = 0; i < n; i++)
            map.put(liste.get(i).getKey(), liste.get(i).getValue());
        return map;
    }
    
    public static Map<String, Integer> recupererSousEnsembleMap_token_id (Map<String, Integer> map_token_id, Set<String> tokens)
    {
        System.out.println("recupererSousEnsembleMap_token_id");
        Map<String, Integer> map = new TreeMap<>();
        for (String s: tokens)
        {
            if (map_token_id.containsKey(s))
                map.put(s, map_token_id.get(s));
        }
        return map;
    }
    
//    public static Map<String, Integer> recupererSousEnsembleMap_token_id(Collection<Article> articles, Map<String, Integer> map_token_id)
//    {
//        System.out.println("");
//        List<Map.Entry<String, Integer>> liste = new ArrayList<>(map_token_id.size());
//        Collections.shuffle(liste);
//        Map<String, Integer> map = new HashMap<>(n);
//        for (int i = 0; i < n; i++)
//            map.put(liste.get(i).getKey(), liste.get(i).getValue());
//        return map;
//    }
    
    public static Set<String> getTokens (Collection<Article> articles)
    {
        System.out.println("getTokens");
        Set<String> tokens = new TreeSet<>();
        for (Article art: articles)
            tokens.addAll(art.getListeToken());
        System.out.println("\t" + "nombre de tokens: " + tokens.size());
        return tokens;
    }
    
    public static Set<Integer> getIdTokens (Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        Set<String> tokens = getTokens(articles);
        return new TreeSet<Integer>(getSousMap_token_id(map_token_id, tokens).values());
    }
    
    public static Map<String, Integer> getSousMap_token_id (Map<String, Integer> map_token_id, Collection<String> dictionnaire)
    {
        System.out.println("getSousMap_token_id");
        return Intersection.getMapFiltre(map_token_id, dictionnaire);
    }
    
    // seulement les token dans le map_token_id sont ajoutes
    public static Map<Integer, Set<Integer>> getMap_idArticle_ensembleIdToken (Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idArticle_ensembleIdToken");
        Map<Integer, Set<Integer>> map_idArt_idTokens = new HashMap<>(articles.size());
        Set<Integer> s;
        for (Article art: articles)
        {
            s = new HashSet<>();
            for (String token: art.getListeToken())
            {
                if (map_token_id.containsKey(token))
                    s.add(map_token_id.get(token));
            }
            map_idArt_idTokens.put(art.getId(), s);
        }
        return map_idArt_idTokens;
    }
    
    // seulement les token dans le map_token_id sont ajoutes
    public static Map<Integer, List<Integer>> getMap_idArticle_listeIdToken (Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idArticle_listeToken");
        Map<Integer, List<Integer>> map_idArt_listeIdToken = new HashMap<>(articles.size());
        for (Article art: articles)
        {
            List<Integer> listeId = new ArrayList<>(art.getListeToken().size());
            for (String token: art.getListeToken())
            {
                if (map_token_id.containsKey(token))
                    listeId.add(map_token_id.get(token));
            }
            map_idArt_listeIdToken.put(art.getId(), listeId);
        }
        return map_idArt_listeIdToken;
    }
    
    public static Map<Integer, List<String>> getMap_idArticle_listeToken (Collection<Article> articles)
    {
        System.out.println("getMap_idArticle_listeToken");
        Map<Integer, List<String>> map_idArt_listeToken = new HashMap<>(articles.size());
        for (Article art: articles)
        {
            map_idArt_listeToken.put(art.getId(), new ArrayList(art.getListeToken()));
        }
        return map_idArt_listeToken;
    }
    
    // seulement les tokens du dictionnaire sont ajoutes
    public static Map<Integer, List<String>> getMap_idArticle_listeToken (Collection<Article> articles, Set<String> dictionnaire)
    {
        System.out.println("getMap_idArticle_listeToken");
        Map<Integer, List<String>> map_idArt_listeToken = new HashMap<>(articles.size());
        for (Article art: articles)
        {
            List<String> intersection = new ArrayList<>();
            for (String token: art.getListeToken())
            {
                if (dictionnaire.contains(token))
                    intersection.add(token);
            }
            map_idArt_listeToken.put(art.getId(), intersection);
        }
        return map_idArt_listeToken;
    }
    
    public static Map<String, Set<Integer>> getMap_token_ensembleIdArticle(Collection<Article> articles)
    {
        System.out.println("getMap_token_ensembleIdArticle");
        return TrieUtils.recupererMap_valeur_ensembleCle(getMap_idArticle_listeToken(articles));
    }
    
    // seulement les token du dictionnaire sont ajoutes
    public static Map<String, Set<Integer>> getMap_token_ensembleIdArticle(Collection<Article> articles, Set<String> dictionnaire)
    {
        System.out.println("getMap_token_ensembleIdArticle");
        Map<String, Set<Integer>> map = TrieUtils.recupererMap_valeur_ensembleCle(getMap_idArticle_listeToken(articles, dictionnaire));
        return map;
    }
    
    // seulement les idToken en parametre sont ajoutes
    public static Map<Integer, Set<Integer>> getMap_idToken_ensembleIdArticle(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idToken_ensembleIdArticle");
        Map<Integer, List<Integer>> map_idArt_listeIdToken = getMap_idArticle_listeIdToken(articles, map_token_id);
        return getMap_idToken_ensembleIdArticle(map_idArt_listeIdToken);
    }
    
    
    public static Map<Integer, Set<Integer>> getMap_idToken_ensembleIdArticle(Map<Integer, List<Integer>> map_idArt_listeIdToken)
    {
        System.out.println("getMap_idToken_ensembleIdArticle");
        return TrieUtils.recupererMap_valeur_ensembleCle(map_idArt_listeIdToken);
    }
    
    public static Map<Integer, VecteurIndicie> getMap_idToken_vecteurAssociationIdCategorie (Map<Integer, List<Integer>> map_idArticle_listeIdToken, Map<Integer, List<Integer>> map_idArticle_listeIdCategorie, Set<Integer> ensembleIdArticle, CoefficientAssociation coefficientAssociation)
    {
        System.out.println("recupererMap_idToken_vecteurAssociationIdCategorie");
        return VectoriseurUtils.vectoriserAvecCoefficientAssociation1(map_idArticle_listeIdToken, map_idArticle_listeIdCategorie, ensembleIdArticle, coefficientAssociation);
    }
    
    public static Map<Integer, VecteurCreux> getMap_idToken_vecteurCreuxBM25(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idToken_vecteurCreuxBM25");
        Collection<VecteurIndicie> vecteurs = VectoriseurUtils.vectoriser_BM25(getMap_idArticle_listeIdToken(articles, map_token_id)).values();
        return VectoriseurUtils.convertir(VectoriseurUtils.transposer(vecteurs, new TreeSet<Integer>(map_token_id.values())).values(), 0);
    }
    
    public static Map<Integer, VecteurIndicie> getMap_idToken_vecteurIndicieBM25(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idToken_vecteurIndicieBM25");
        Collection<VecteurIndicie> vecteurs = VectoriseurUtils.vectoriser_BM25(getMap_idArticle_listeIdToken(articles, map_token_id)).values();
        return VectoriseurUtils.transposer(vecteurs, new TreeSet<Integer>(map_token_id.values()));
    }
    
    public static Map<Integer, VecteurIndicie> getMap_idToken_vecteurIndicieBinaire(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idToken_vecteurIndicieBM25");
        Collection<VecteurIndicie> vecteurs = VectoriseurUtils.vectoriser_BINAIRE(getMap_idArticle_listeIdToken(articles, map_token_id)).values();
        return VectoriseurUtils.transposer(vecteurs, new TreeSet<Integer>(map_token_id.values()));
    }
    
    public static Map<Integer, VecteurIndicie> getMap_idVariable_vecteurAssociationIdCoVariable (Map<Integer, Set<Integer>> map_idVariable_idContextes, Map<Integer, Set<Integer>> map_idCoVariable_idContextes, Set<Integer> ensembleIdContextes, CoefficientAssociation coefficientAssociation)
    {
        System.out.println("recupererMap_idToken_vecteurAssociationIdCategorie");
        return VectoriseurUtils.vectoriserAvecCoefficientAssociation2(map_idVariable_idContextes, map_idCoVariable_idContextes, ensembleIdContextes, coefficientAssociation);
    }
    
    public static Map<Integer, VecteurIndicie> getMap_idArticle_vecteurTokenBM25(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idArticle_vecteurTokenBM25");
        return VectoriseurUtils.vectoriser_BM25(getMap_idArticle_listeIdToken(articles, map_token_id));
    }
    
    public static Map<Integer, VecteurCreux> getMap_idArticle_vecteurCreuxTokenBM25(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idArticle_vecteurTokenBM25");
        Collection<VecteurIndicie> vecteurs = VectoriseurUtils.vectoriser_BM25(getMap_idArticle_listeIdToken(articles, map_token_id)).values();
        return VectoriseurUtils.convertir(vecteurs, 0);
    }
    
    public static Map<Integer, VecteurCreux> getMap_idArticle_vecteurCreuxFreq(Collection<Article> articles, Map<String, Integer> map_token_id)
    {
        System.out.println("getMap_idArticle_vecteurTokenBM25");
        Collection<VecteurIndicie> vecteurs = VectoriseurUtils.vectoriser_FREQ(getMap_idArticle_listeIdToken(articles, map_token_id)).values();
        return VectoriseurUtils.convertir(vecteurs, 0);
    }
    
        
    public static Map<String, Integer> getMap_token_nombreArticle (Collection<Article> articles)
    {
        System.out.println("getMap_token_nombreArticle");
        Map<String, Set<Integer>> map_token_ensembleIdArticle = getMap_token_ensembleIdArticle(articles);
        Map<String, Integer> map_descripteur_freq = new HashMap<>(map_token_ensembleIdArticle.size());
        for (String token: map_token_ensembleIdArticle.keySet())
        {
            map_descripteur_freq.put(token, map_token_ensembleIdArticle.get(token).size());
        }
        return map_descripteur_freq;
    }
    
    // seulement les tokens du dictionnaire sont ajoutes
    public static Map<String, Integer> getMap_token_nombreArticle (Collection<Article> articles, Set<String> tokens)
    {
        System.out.println("getMap_token_nombreArticle");
        Map<String, Set<Integer>> map_token_ensembleIdArticle = getMap_token_ensembleIdArticle(articles, tokens);
        for (String t: tokens)
            map_token_ensembleIdArticle.putIfAbsent(t, new TreeSet());
                
        Map<String, Integer> map_descripteur_freq = new HashMap<>(map_token_ensembleIdArticle.size());
        for (String token: map_token_ensembleIdArticle.keySet())
        {
            map_descripteur_freq.put(token, map_token_ensembleIdArticle.get(token).size());
        }
        return map_descripteur_freq;
    }
    
    public static Map<String, Integer> getToken_frequence (Collection<Article> articles, Set<String> tokens)
    {
        System.out.println("getToken_frequence");
        Map<String, Integer> map_token_freq = new HashMap<>(tokens.size());
        //ini
        for (String token: tokens)
            map_token_freq.put(token, 0);
        
        for (Article art: articles)
        {
            for (String token: art.getListeToken())
            {
                if (tokens.contains(token))
                {
                    map_token_freq.put(token, map_token_freq.get(token)+1);
                }
            }
        }
        return map_token_freq;
    }
    
    public static int getNombreOccurrence (Collection<Article> articles)
    {
        int n = 0;
        for (Article a: articles)
        {
            n+=a.getListeToken().size();
        }
        return n;
    }
    
    public static Map<Integer, VecteurIndicie> additionnerEtNormerVecteurToken(Map<Integer, Set<Integer>> map_idVariable_ensembleIdToken, Map<Integer, VecteurIndicie> vecteursToken)
    {
        System.out.println("additionnerVecteurToken");
        Map<Integer, VecteurIndicie> vecteursCummule = new HashMap<>(map_idVariable_ensembleIdToken.size());
        for (int idVariable: map_idVariable_ensembleIdToken.keySet())
        {
            VecteurIndicie vecteurCumul = new VecteurIndicie(idVariable, new TreeMap<>());
            for (int idDesc: map_idVariable_ensembleIdToken.get(idVariable))
            {
                vecteurCumul.additionnerVecteur(vecteursToken.get(idDesc).getMap_Id_Ponderation());
            }
            vecteurCumul.normer();
            vecteursCummule.put(idVariable, vecteurCumul);
        }
        return vecteursCummule;
    }
    
    // les deux matrices reduite et ponderee sont ecuperees
    public static Map<String, Collection<VecteurCreux>> reductionSVD (final Collection<VecteurIndicie> vecteursTermeParDoc, int nbreTermes, int nbreDocs, int kFacteur) throws NotConvergedException
    {
//        Map<Integer,Integer> map_idArticle_idLigne = new HashMap<>(map_idArticle_vecteur.size());
//        int idLigne = 0;
//        for (int idOeuvre: map_idArticle_vecteur.keySet())
//        {
//            map_idArticle_idLigne.put(idOeuvre, idLigne);
//            idLigne++;
//        }
        // create a document * term matrix
        DenseMatrix matrice = MtjMatrice.convertirVecteur(vecteursTermeParDoc, nbreTermes, nbreDocs);
//        // create a term * document matrix
//        matrice = new DenseMatrix(matrice.transpose(new DenseMatrix(nbreDimensions, nbreVecteurs)));
//        if (matrice.numRows()!=nbreDimensions && matrice.numColumns() != nbreVecteurs)
//        {
//            System.out.println("mauvais nombre de ligne");
//            System.exit(1);
//        }
        SVD svd = new SVD(matrice.numRows(), matrice.numColumns()).factor(matrice);
        AfficheurTableau.afficher1Colonnes("valeur singuliere", "valeur", VectoriseurUtils.convertir(svd.getS()));
//        matrice = ReducteurSVD_MTJ.reduireEtPondererMatriceV(svd, kFacteur);
        Map<String, Collection<VecteurCreux>> matrices = new HashMap<>(2);
        matrices.put("document", MtjMatrice.convertirLigneDeLaMatrice(ReducteurSVD_MTJ.reduireEtPondererMatriceV(svd, kFacteur)).values());
        // les lignes de la matrices representent les termes
//        matrice = ReducteurSVD_MTJ.reduireEtPondererMatriceU(svd, kFacteur);
        matrices.put("terme", MtjMatrice.convertirLigneDeLaMatrice(ReducteurSVD_MTJ.reduireEtPondererMatriceU(svd, kFacteur)).values());        
        return matrices;
    }
    
    public static Map<Integer, Set<Integer>> getMap_idClasse_idArticles (Map<Integer, Set<Integer>> map_idArt_idTokens, Map<Integer, Integer> map_idToken_idClasse)
    {
        System.out.println("getMap_idClasse_idArticles");
        Map<Integer, Set<Integer>> map_idClasse_idsArts = new TreeMap<>();
        for (int idClasse: map_idToken_idClasse.values())
            map_idClasse_idsArts.put(idClasse, new TreeSet<Integer>());
        for (int idArt: map_idArt_idTokens.keySet())
        {
            for (int idToken: map_idArt_idTokens.get(idArt))
            {
                if (map_idToken_idClasse.containsKey(idToken))
                    map_idClasse_idsArts.get(map_idToken_idClasse.get(idToken)).add(idArt);
            }
        }
        return map_idClasse_idsArts;        
    }
    
    public static Map<String, Integer> getMap_token_idClasse (File fileClassification) throws FileNotFoundException, IOException
    {
        // on remplace le Reader par un InputStreameReader qui est sa super classe, afin de choisir l'encodage
        // Le Reader de OpenCSV ne permet pas de choisir l'encodage
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileClassification), "UTF-8"), ';' , '"' , 1);
        Map<String, Integer> map = new HashMap<>();
        
        //Read CSV line by line and use the string array as you want
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) 
        {
           if (nextLine != null) 
           {
               map.put(nextLine[0], Integer.parseInt(nextLine[1]));
           }
        }
//        AfficheurTableau.afficher2Colonnes("token et classe", "token", "classe", map);
        return map;
    }
    
    public static Map<String, String> getMap_token_catCible (File fileCsvMotCible) throws FileNotFoundException, IOException
    {
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileCsvMotCible), "UTF-8"), ';' , '"' , 1);
        Map<String, String> map = new HashMap<>();
        
        //Read CSV line by line and use the string array as you want
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) 
        {
           if (nextLine != null) 
           {
               map.put(nextLine[0], nextLine[1]);
           }
        }
        AfficheurTableau.afficher2Colonnes("token et categorie", "tokenCible", "categorieCible", map);
        return map;
    }
    
        
    public static File getRepertoireArticle() {
        return repertoireArticle;
    }

    public static File getRepertoireArticleFiltreLanci() {
        return repertoireArticleFiltreLanci;
    }

    public static File getRepertoireArticleFiltreRevueSelectionne() {
        return repertoireArticleFiltreRevueSelectionne;
    }

    public static File getRepertoireArticleRevueSelectionne() {
        return repertoireArticleRevueSelectionne;
    }

    public static File getRepertoireParagrapheRevueSelectionnee() {
        return repertoireParagrapheRevueSelectionnee;
    }

    public static File getFileArffVecteurMots() {
        return fileArffVecteurMots;
    }
    
    
    
    
}
