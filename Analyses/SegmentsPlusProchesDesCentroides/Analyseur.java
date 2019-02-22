/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.SegmentsPlusProchesDesCentroides;

import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import AnalyseSimilitude.AnalyseSimilitude;
import Article.Article;
import Corpus.CorpusUtils;
import Fichier.Arff.ArffUtils;
import Fichier.CSV.FileCSV;
import Fichier.FileUtils;
import FiltreurToken.FiltreurTokenSelonDictionnaire;
import Matrice.VecteurCreux.MatriceCreuse;
import Matrice.Vectorisateur.VecteurCreux;
import Metrique.Similitude.MetriqueSim;
import Metrique.Similitude.ProduitScalaire;
import MorphAdornerUtils.LemmatiseurUtils;
import MorphAdornerUtils.MorphAdornerSurSegment;
import Parser.ParserBioMedFiltre;
import Parser.TypeContenu;
import Pretraitement.Pretraitement;
import Pretraitement.PretraitementCorpusXML;
import Tokenisation.Tokeniseur;
import UtilsJFC.Intersection;
import UtilsJFC.TirageAleatoire;
import UtilsJFC.TrieUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ArffLoader;


/**
 *
 * @author JF Chartier
 */
public class Analyseur 
{
    private Map<Integer, String> map_id_token;
    private Map<String, Integer> map_token_id;
    private Map<Integer, VecteurCreux> map_idClasse_centroide;
    private Map<Integer, VecteurCreux> map_idToken_vecteur;
    private MorphAdornerSurSegment lemma;
    private File pathParagLemme = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\paragrapheFiltreRevueSelect\\");
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            
            ForkJoinPool commonPool = ForkJoinPool.commonPool();
            System.out.println(commonPool.getParallelism());
            
            File fileCorpus = CorpusUtils.getRepertoireParagrapheRevueSelectionnee();
            File fileVecteurs = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMotsAvecClasseEtNom.arff");
            File fileResultat = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\Resultats.csv");
            File fileClasse = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\classification.csv");
            
            
            new Analyseur().recupererParagEtLemmatiser(fileClasse, fileCorpus);
//            new Analyseur().retrouverSegment(fileCorpus, TypeContenu.PARAGRAPHE, 1000, fileVecteurs, 10, new ProduitScalaire(), fileResultat);
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    
    
    public Analyseur() throws Exception {
        this.lemma = new MorphAdornerSurSegment();
        
    }
    
    public void retrouverSegmentFast (final File fileParagFiltre, final File fileArffVecteurMots, int k, MetriqueSim metrique, File fileResultats) throws IOException, Exception
    {
//        List<File> listeFile = FileUtils.getAllFileInPath(filesArticles, ".txt", new ArrayList<File>());
//        if (nombreArticleSelectionne!=-1)
//        {
//            listeFile = new ArrayList<>(TirageAleatoire.getSousEnsembleSansDoublon(nombreArticleSelectionne, listeFile, 1));
//        }
        recupererVecteursEtCentroides(fileArffVecteurMots);
        // crer un fichier vide dans lequel ajouter les resultats
        if (!Files.exists(fileResultats.toPath()))
            Files.createFile(fileResultats.toPath());
        map_token_id = TrieUtils.inverserMap(map_id_token);
        
        map_idClasse_centroide.values().parallelStream().forEach(c->());
        
        
        for (Path p: stream)
        {
            Set<String> tokens = Tokeniseur.extraireEnsembleToken(FileUtils.read(p.toFile()));
            Set<Integer> ids = CorpusUtils.getIdTokens2(map_id_token, tokens);
            double[] v = getVecteurSegment(ids);
            map_idClasse_centroide.values().parallelStream().forEach(p->metrique.calculerMetrique(p.getTabPonderation(), v));
        }
        
        for (int idC: map_idClasse_centroide.keySet())
        {
            DirectoryStream<Path> stream = Files.newDirectoryStream(fileParagFiltre.toPath());
            StringBuilder resultat;
            List<String> resultats = new ArrayList<>();
            List<Map.Entry<File, Double>> filesParag = getParagFilePlusProche(stream, nombreArticleSelectionne, idC, k, metrique);
            for (Map.Entry<File, Double> e: filesParag)
            {
                resultat=new StringBuilder(100);
                resultat.append(idC).append(";").append(e.getValue()).append(";").append('"').append(FileUtils.read(e.getKey())).append('"').append(";");
                resultats.add(resultat.toString());
                
            }
            System.out.println(resultats.toString());
            Files.write(fileResultats.toPath(), resultats, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
        
    }
    
    public void get(double[] c, final File fileParagFiltre, MetriqueSim metrique, int k) throws IOException
    {
        DirectoryStream<Path> stream = Files.newDirectoryStream(fileParagFiltre.toPath());
        File[] filesK = new File[k];
        double[] simsK = new double[k];
        Arrays.fill(filesK, 0.0);
        double min=0.0;
        int id = 0;
        PriorityQueue<Double> pq = new PriorityQueue<Double>();
        for (Path p: stream)
        {
            Set<String> tokens = Tokeniseur.extraireEnsembleToken(FileUtils.read(p.toFile()));
            double[] v = getSegmentVecto(tokens);
            double sim = metrique.calculerMetrique(v, c);
            for (int i=0;i<k;i++)
            {
                if (pq.size() < k) pq.add(x);
            }
            
            
            for (int x : arr) { 
                if (pq.size() < k) pq.add(x);
                else if (pq.peek() < x) {
                    pq.poll();
                    pq.add(x);
                }
            
            
            
            if (sim>min)
            {
                min=sim;
                for (int i=0;i<k;i++)
                {
                    if (simsK[i]==min)
                    {
                        simsK[i]=sim;
                        filesK[i]=p.toFile();
                        break;
                    }
                }
            }
            else
            {
                
            }
            Collections.m
            for (int i=0;i<k;i++)
            {
                Arrays.sort(simsK);
                min=simsK[i];
            }
        }
        
        
    }
    
    
    public void retrouverSegment (final File filesArticles, TypeContenu typeContenu, int nombreArticleSelectionne, final File fileArffVecteurMots, int k, MetriqueSim metrique, File fileResultats) throws IOException, Exception
    {
//        List<File> listeFile = FileUtils.getAllFileInPath(filesArticles, ".txt", new ArrayList<File>());
//        if (nombreArticleSelectionne!=-1)
//        {
//            listeFile = new ArrayList<>(TirageAleatoire.getSousEnsembleSansDoublon(nombreArticleSelectionne, listeFile, 1));
//        }
        recupererVecteursEtCentroides(fileArffVecteurMots);
        // crer un fichier vide dans lequel ajouter les resultats
        if (!Files.exists(fileResultats.toPath()))
            Files.createFile(fileResultats.toPath());
        

        for (int idC: map_idClasse_centroide.keySet())
        {
//        Path dir = FileSystems.getDefault().getPath(filesArticles.getAbsolutePath());
            DirectoryStream<Path> stream = Files.newDirectoryStream(filesArticles.toPath());
        
            StringBuilder resultat;
            List<String> resultats = new ArrayList<>();
            List<Map.Entry<File, Double>> filesParag = getParagFilePlusProche(stream, nombreArticleSelectionne, idC, k, metrique);
            for (Map.Entry<File, Double> e: filesParag)
            {
                resultat=new StringBuilder(100);
                resultat.append(idC).append(";").append(e.getValue()).append(";").append('"').append(FileUtils.read(e.getKey())).append('"').append(";");
                resultats.add(resultat.toString());
                
            }
            System.out.println(resultats.toString());
            Files.write(fileResultats.toPath(), resultats, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
        
        
        
    }
    
    // cette version de la methode est peut etre plus rapide car il prend en argument un stream plutot qu'une liste de File
    public List<Map.Entry<File, Double>> getParagFilePlusProche (DirectoryStream<Path> stream, int nombreArticleSelectionne, int idCentroide, int k, MetriqueSim metrique) throws IOException, Exception
    {
        System.out.println("getParagFilePlusProche : centroide " + idCentroide);
        
        Map<File, Double> map_fileName_sim = new HashMap<>();
        int i=0;
        for (Path f: stream)
        {
            Set<String> tokens = new TreeSet<>();
            for (final String phrase: ParserBioMedFiltre.recupererPhrases(f.toFile()))
            {
                
                if (phrase.length()>10)
                    tokens.addAll(lemma.lemmatiserPhrase(phrase));
            }
            Set<Integer> ids = CorpusUtils.getIdTokens2(map_id_token, Intersection.getIntersection(map_id_token.values(), tokens));
            double sim = calculerSimilariteAvecCentroide(idCentroide, ids, metrique);
            map_fileName_sim.put(f.toFile(), sim);
            
            if (++i == nombreArticleSelectionne) break;
        }
        return TrieUtils.trieMapDecroissantSelonValeur(map_fileName_sim).subList(0, k);
    }
    
    public List<Map.Entry<File, Double>> getParagFilePlusProche (final List<File> listeFile, int idCentroide, int k, MetriqueSim metrique) throws IOException, Exception
    {
        System.out.println("getParagFilePlusProche : centroide " + idCentroide);
        
        Map<File, Double> map_fileName_sim = new HashMap<>(listeFile.size());
        for (File f: listeFile)
        {
            Set<String> tokens = new TreeSet<>();
            for (final String phrase: ParserBioMedFiltre.recupererPhrases(f))
            {
                if (phrase.length()>10)
                    tokens.addAll(lemma.lemmatiserPhrase(phrase));
            }
            Set<Integer> ids = CorpusUtils.getIdTokens2(map_id_token, Intersection.getIntersection(map_id_token.values(), tokens));
            double sim = calculerSimilariteAvecCentroide(idCentroide, ids, metrique);
            map_fileName_sim.put(f, sim);
        }
        return TrieUtils.trieMapDecroissantSelonValeur(map_fileName_sim).subList(0, k);
    }
    
    public double[] getVecteurSegment (Set<Integer> ensembleIdToken)
    {
        double[] v = new double[map_id_token.size()];
        Arrays.fill(v, 0.0);
        for (int idToken: ensembleIdToken)
        {
            v = VecteurCreux.additionnerVecteurs(v, map_idToken_vecteur.get(idToken).getTabPonderation());
        }
        return VecteurCreux.normer(v);
    }
    
    public double[] getSegmentVecto (Set<String> tokensParag)
    {
        double[] v = new double[map_id_token.size()];
        Arrays.fill(v, 0.0);
        for (String token: tokensParag)
        {
            v = VecteurCreux.additionnerVecteurs(v, map_idToken_vecteur.get(map_token_id.get(token)).getTabPonderation());
        }
        return VecteurCreux.normer(v);
    }
    
    public double calculerSimilariteAvecCentroide(int idCentroide, Set<Integer> ensembleIdToken, MetriqueSim metrique)
    {
        double[] v = new double[map_id_token.size()];
        Arrays.fill(v, 0.0);
        for (int idToken: ensembleIdToken)
        {
            v = VecteurCreux.additionnerVecteurs(v, map_idToken_vecteur.get(idToken).getTabPonderation());
        }
        v = VecteurCreux.normer(v);
        return metrique.calculerMetrique(map_idClasse_centroide.get(idCentroide).getTabPonderation(), v);
    }
    
    public void calculerVecteurParagsEtCentroides(final File fileArffVecteurMots, final File filesParagFiltre) throws FileNotFoundException, IOException
    {
        System.out.println("recupererVecteursEtCentroides");
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_id_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
        data.deleteAttributeAt(data.attribute("@@name@@").index());       
        map_idToken_vecteur = ArffUtils.getDenseVector(data, 0.0);
                
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        Map<Integer, Set<Integer>> map_idCl_idVecs = new HashMap<>(map_classe_idVecteurs.size());
        for (String c: map_classe_idVecteurs.keySet())
            map_idCl_idVecs.put(Integer.parseInt(c), map_classe_idVecteurs.get(c));
        
        map_idClasse_centroide = MatriceCreuse.additionnerVecteurs(map_idToken_vecteur, map_idCl_idVecs, true);

    }
    
    public void recupererVecteursEtCentroides(final File fileArffVecteurMots) throws FileNotFoundException, IOException
    {
        System.out.println("recupererVecteursEtCentroides");
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        map_id_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
        data.deleteAttributeAt(data.attribute("@@name@@").index());       
        map_idToken_vecteur = ArffUtils.getDenseVector(data, 0.0);
                
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        Map<Integer, Set<Integer>> map_idCl_idVecs = new HashMap<>(map_classe_idVecteurs.size());
        for (String c: map_classe_idVecteurs.keySet())
            map_idCl_idVecs.put(Integer.parseInt(c), map_classe_idVecteurs.get(c));
        
        map_idClasse_centroide = MatriceCreuse.additionnerVecteurs(map_idToken_vecteur, map_idCl_idVecs, true);

    }
    
    public void recupererParagEtLemmatiser(final File fileClasse, File dossierParag) throws IOException, Exception
    {
        System.out.println("recupererParagEtLemmatiser");
        final Set<String> tokens = CorpusUtils.getMap_token_catCible(fileClasse).keySet();
//        MorphAdornerSurSegment lemma = new MorphAdornerSurSegment();
//        List<File> listeFile = FileUtils.getAllFileInPath(dossierParag, ".txt", new ArrayList<File>());
        LemmatiseurUtils lemma = new LemmatiseurUtils();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dossierParag.toPath());
        for (Path p: stream)
        {
            lemmatiserEtenregistrer(p.toFile(), tokens, lemma);
        }
        stream.close();
        
//        for (File p: listeFile)
//            lemmatiserEtenregistrer(p, tokens, lemma);
//        listeFile.stream().forEach(p->{
//            try {
//                lemmatiserEtenregistrer(p, tokens, lemma);
//            } catch (Exception ex) {
//                Logger.getLogger(Analyseur.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        });
//        
    }
    
    private void lemmatiserEtenregistrer(File f, Set<String> tokens, LemmatiseurUtils lemma) throws IOException, Exception 
    {
//        MorphAdornerSurSegment lemma = new MorphAdornerSurSegment();
        StringBuilder s = new StringBuilder();
        Set<String> paragToken = new HashSet<>();
        
        for (final String phrase: ParserBioMedFiltre.recupererPhrases(f))
        {
            
            if (phrase.length()>10)
                try {
                    paragToken.addAll(lemma.lemmatiserPhrase(phrase));
            } catch (Exception ex) 
            {
                System.out.println("phrase problematique: " +phrase);
                Logger.getLogger(Analyseur.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        paragToken = Intersection.getIntersectionParal(tokens, paragToken);
        for (String t: paragToken)
        {
            s.append(t).append("; ");
        }
        String p = pathParagLemme.getPath().concat("\\").concat(f.getName());
        FileUtils.write(s.toString(), p);
            
    }

    
}
