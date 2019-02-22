/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pretraitement;

import Antidictionnaire.Antidictionnaire;
import Article.Article;
import Corpus.CorpusUtils;
import Fichier.FileUtils;
import FiltreurToken.FiltreurAntidictionnaire;
import FiltreurToken.FiltreurFrequenceDocumentaire;
import FiltreurToken.FiltreurNombre;
import FiltreurToken.FiltreurSingleton;
import FiltreurToken.FiltreurToken;
import FiltreurToken.FiltreurTokenSelonDictionnaire;
import Matrice.MtjMatrice;
import Matrice.Vectorisateur.VecteurCreux;
import N_Grams.N_Grams;
import Racinisation.RacineurAnglais;
import Racinisation.RacineurFrancais;
import Racinisation.RacinisationAnglaise;
import ReductionDimensionnelle.SVD.ReducteurSVD_MTJ;
import Segmentation.Segmenteur.ExtracteurUtils;
import UtilsJFC.TirageAleatoire;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;

/**
 *
 * @author JF Chartier
 */
public class Pretraitement 
{
    private boolean antidictionnaire; 
    private boolean nombre;
    private boolean singleton;
    private int freqOeuvreMin;
    private int freqOeuvreMax;

    public Pretraitement(boolean antidictionnaire, boolean nombre, boolean singleton, int freqOeuvreMin, int freqOeuvreMax) {
        this.antidictionnaire = antidictionnaire;
        this.nombre = nombre;
        this.singleton = singleton;
        this.freqOeuvreMin = freqOeuvreMin;
        this.freqOeuvreMax = freqOeuvreMax;
    }
    
    public static Collection<Article> filtrerArticle (final Collection<Article> articles,
            FiltreurAntidictionnaire filtreurAntidictionnaire, 
            FiltreurNombre filtreurNombre, 
            FiltreurSingleton filtreurSingleton, 
            FiltreurFrequenceDocumentaire filtreurFreq, 
            Set<String> tokensProteges)
    {
        Collection<Article> articlesfiltres = new ArrayList<>(articles.size());
        
        for (Article art: articles)
        {
            List<String> tokens = new ArrayList<>();
            for (String token: art.getListeToken())
            {
                if (tokensProteges.contains(token))
                {
                    tokens.add(token);
                    continue;
                }
                if (filtreurAntidictionnaire.applicationFiltre(token)==true)
                    continue;
                if (filtreurNombre.applicationFiltre(token)==true)
                    continue;
                if (filtreurSingleton.applicationFiltre(token)==true)
                    continue;
                if (filtreurFreq.applicationFiltre(token)==true)
                    continue;
                tokens.add(token);
            }
 
            articlesfiltres.add(new Article(art.getId(), tokens));
        }
        return articlesfiltres;
    }
    
    
    public Collection<Article> filtrerArticle (final Collection<Article> articles)
    {
        System.out.println("filtrerOeuvres");
        Collection<Article> articlesfiltres = new ArrayList<>(articles.size());
        FiltreurAntidictionnaire filtreurAntidictionnaire = new FiltreurAntidictionnaire("anglais", false);
        FiltreurNombre filtreurNombre = new FiltreurNombre();
        FiltreurSingleton filtreurSingleton = new FiltreurSingleton();
        Map<String, Integer> map_descrip_freq = CorpusUtils.getMap_token_nombreArticle(articles);
        FiltreurFrequenceDocumentaire filtreurFreq = new FiltreurFrequenceDocumentaire(map_descrip_freq, freqOeuvreMin, freqOeuvreMax);
        for (Article art: articles)
        {
            List<String> tokensFiltre = filtrerToken(art.getListeToken(), filtreurAntidictionnaire, filtreurNombre, filtreurSingleton, filtreurFreq);
//            List<String> tokensFiltre = filtrerToken(oeuvre.getListeDescripteur());
            articlesfiltres.add(new Article(art.getId(), tokensFiltre));
        }
        System.out.println("\t nombre token apres filtrage : " + CorpusUtils.getTokens(articlesfiltres).size());
        return articlesfiltres;
    }
    
    public static Collection<Article> racinerEtRegrouper (Collection<Article> articles)
    {
        Set<String> racines = RacineurAnglais.raciner(CorpusUtils.getTokens(articles));
        Map<String, String> racine_feuilles = new HashMap<>(racines.size());
        for (String i: racines)
            racine_feuilles.put(i, i);
        
        
    }
    
    public static Collection<Article> raciner (Collection<Article> articles)
    {
        Collection<Article> articlesfiltres = new ArrayList<>(articles.size());
        for (Article art: articles)
        {
            List<String> tokensFiltre = RacineurAnglais.raciner(art.getListeToken());
//            List<String> tokensFiltre = filtrerToken(oeuvre.getListeDescripteur());
            articlesfiltres.add(new Article(art.getId(), tokensFiltre));
        }
        return articlesfiltres;
    }
    
    public static Map<String, String> raciner (Map<String, String> map_token_categorie)
    {
        Map<String, String> m = new TreeMap<>();
        for (String token: map_token_categorie.keySet())
        {
            m.put(RacineurAnglais.raciner(token), map_token_categorie.get(token));
        }
        return m;
    }
    
    
    
    
   
    private List<String> filtrerToken (final List<String> listeToken, FiltreurAntidictionnaire filtreurAntidictionnaire, FiltreurNombre filtreurNombre, 
            FiltreurSingleton filtreurSingleton, FiltreurFrequenceDocumentaire filtreurFreq)
    {
        List<String> listeTokenFiltre = new ArrayList<>();
        if (antidictionnaire==true)
            listeTokenFiltre = filtreurAntidictionnaire.filtrerListe(listeToken);
        if (nombre==true)
            listeTokenFiltre= filtreurNombre.filtrerListe(listeTokenFiltre);
        if (singleton==true)
            listeTokenFiltre = filtreurSingleton.filtrerListe(listeTokenFiltre);
        listeTokenFiltre = filtreurFreq.filtrerListe(listeTokenFiltre);
        
        return listeTokenFiltre;        
    }
    
    public static Collection<Article> remplacerMotParCategorie (Collection<Article> articles, Map<String, String> map_token_categorie) throws IOException
    {
        System.out.println("remplacer les token par leur categorie");
        Collection<Article> articlesFiltre = new ArrayList<>(articles.size());
        for (Article art: articles)
        {
            List<String> tokensFiltre = new ArrayList<>(art.getListeToken().size());
            for (String token: art.getListeToken())
            {
                String tokenFiltre = map_token_categorie.get(token);
                if (tokenFiltre!=null)
                    tokensFiltre.add(tokenFiltre);
                else
                    tokensFiltre.add(token);
                
//////                String tokenFiltre = map_token_categorie.containsKey(token)? map_token_categorie.get(token): token;
//////                tokensFiltre.add(tokenFiltre);
            }
            articlesFiltre.add(new Article(art.getId(), tokensFiltre));
        }
        return articlesFiltre;        
    }
    
    public static Collection<Article> filtrerTokenSelonAntidictionnaire (Collection<Article> articles, Set<String> antidictionnaire)
    {
        System.out.println("filtrerToken");
        Collection<Article> articlesFiltre = new ArrayList<>(articles.size());
        FiltreurAntidictionnaire filtreur = new FiltreurAntidictionnaire(antidictionnaire);
        for (Article art: articles)
        {
            List<String> tokensFiltre = filtreur.filtrerListe(art.getListeToken());
            articlesFiltre.add(new Article(art.getId(), tokensFiltre));
        }
        return articlesFiltre;
    }    
    // filtrer tous les token qui ne sont pas dans le dictionnaire
    public static Collection<Article> selectionnerTokenDuDictionnaire (Collection<Article> articles, Set<String> dictionnaire)
    {
        System.out.println("filtrerToken");
        Collection<Article> articlesFiltre = new ArrayList<>(articles.size());
        FiltreurTokenSelonDictionnaire filtreur = new FiltreurTokenSelonDictionnaire(dictionnaire);
        for (Article art: articles)
        {
            List<String> tokensFiltre = filtreur.filtrerListe(art.getListeToken());
            articlesFiltre.add(new Article(art.getId(), tokensFiltre));
        }
        return articlesFiltre;
    }
    
    public static Collection<Article> filtreAleatoireArticle(Collection<Article> articles, int nombreArticleSelectionne)
    {
        System.out.println("filtreAleatoireArticle");
        Set<Integer> ensembleIdArt = CorpusUtils.getEnsembleIdArticle(articles);
        ensembleIdArt = TirageAleatoire.recupererSousEnsembleSansDoublon(nombreArticleSelectionne, ensembleIdArt, 1);
        Collection<Article> articlesFiltre = new ArrayList<>(nombreArticleSelectionne);
        for (Article art: articles)
        {
            if (ensembleIdArt.contains(art.getId()))
                articlesFiltre.add(new Article(art.getId(), art.getListeToken()));
        }
        System.out.println("\t"+ensembleIdArt.size());
        return articlesFiltre;
    }
    
    public static void initialiserIdArticle(Collection<Article> articles)
    {
        System.out.println("initialiserIdArticle");
        int i = 0;
        for (Article art: articles)
        {
            art.setId(i);
            i++;
        }
                    
    }
    
    public static Map<Integer, VecteurCreux> reduireViaSvd (Map<Integer, VecteurCreux> map_id_vecteur, int nbreDimensions, int nbreVecteurs, int kFacteur) throws NotConvergedException
    {
        Map<Integer,Integer> map_idOeuvre_idLigne = new HashMap<>(map_id_vecteur.size());
        int idLigne = 0;
        for (int idOeuvre: map_id_vecteur.keySet())
        {
            map_idOeuvre_idLigne.put(idOeuvre, idLigne);
            idLigne++;
        }
        DenseMatrix matrice = MtjMatrice.convertirVecteurVersLigneMatrice(map_id_vecteur.values(), map_idOeuvre_idLigne, nbreVecteurs, nbreDimensions);
        matrice = new DenseMatrix(matrice.transpose(new DenseMatrix(nbreDimensions, nbreVecteurs)));
        if (matrice.numRows()!=nbreDimensions && matrice.numColumns() != nbreVecteurs)
        {
            System.out.println("mauvais nombre de ligne");
            System.exit(1);
        }

        matrice = ReducteurSVD_MTJ.reduireEtPondererMatriceV(new SVD(matrice.numRows(), matrice.numColumns()).factor(matrice), kFacteur);
        return MtjMatrice.convertirLigneDeLaMatrice(matrice, map_idOeuvre_idLigne);
    }
    
    
    
    
    
}
