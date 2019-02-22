/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.SegmentsPlusProchesDesCentroides;

import Corpus.CorpusUtils;
import Fichier.Arff.ArffUtils;
import Fichier.FileUtils;
import Matrice.VecteurCreux.MatriceCreuse;
import Matrice.Vectorisateur.VecteurCreux;
import Metrique.Similitude.MetriqueSim;
import Metrique.Similitude.ProduitScalaire;
import Parser.TypeContenu;
import Tokenisation.Tokeniseur;
import UtilsJFC.TrieUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author JF Chartier
 */
public class GetSegmentTopicParal 
{
    
    private Map<Integer, double[]> map_idClasse_centroide;
    private Instances data;
    private Map<String, Integer> map_token_id;
    private final String repertoireParagSelect = CorpusUtils.getRepertoireParagrapheRevueSelectionnee().toString();
    
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            
            ForkJoinPool commonPool = ForkJoinPool.commonPool();
            System.out.println(commonPool.getParallelism());
            
            File fileArffVecteurMots = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMotsAvecClasseEtNom.arff");
            File fileResultat = new File ("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\Resultats.csv");
            File fileParagFiltre = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\paragrapheFiltreRevueSelect\\");
           
            new GetSegmentTopicParal().retrouverSegment(fileParagFiltre, fileArffVecteurMots, 1000, new ProduitScalaire(), fileResultat);
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void retrouverSegment (final File repertoireParagsFiltres, final File fileArffVecteurMots, int k, MetriqueSim metrique, File fileResultats) throws IOException 
    {
        calculerVecteurParagsEtCentroides(fileArffVecteurMots, repertoireParagsFiltres);
        // crer un fichier vide dans lequel ajouter les resultats
        if (!Files.exists(fileResultats.toPath()))
            Files.createFile(fileResultats.toPath());
       
//        map_idClasse_centroide.keySet().parallelStream().map(c->getKResultats(c, repertoireParagsFiltres, metrique, k)).forEach(pq->ecrireResultat(pq, fileResultats));
        
//        for (int idC: map_idClasse_centroide.keySet())
//        {
//            List<Resultat> rs = getKResultats(idC, repertoireParagsFiltres, metrique, k);
//            ecrireResultat(idC, rs, fileResultats);
//        }
        
        List<Resultat> rs = getKResultats(17, repertoireParagsFiltres, 100, metrique, k);
        ecrireResultat(17, rs, fileResultats);
    }
    
    class Resultat
    {
        private double sim;
        private File file;
//        private int idC;
        public Resultat(double sim, File file) 
        {
            this.file=file;
            this.sim=sim;
//            this.idC =idC;
        }

        public File getFile() {
            return file;
        }

        public double getSim() {
            return sim;
        }
    }
    
    public List<Resultat> getKResultats(int idC, final File reperoireParagsFiltres, int nParag, MetriqueSim metrique, int k) throws IOException 
    {
        System.out.println("getKResultats du theme :"+ idC);
//        PriorityQueue<Resultat> pq = new PriorityQueue<>();
//        DirectoryStream<Path> stream = Files.newDirectoryStream(fileParagFiltre.toPath());
        
        List<File> files = FileUtils.getAllFileInPath(reperoireParagsFiltres, ".txt", new ArrayList());
        Collections.shuffle(files, new Random(1));
        files.subList(0, nParag);
        System.out.println("commence le calcul parralelle");
        List<Resultat> e = files.parallelStream().map(f->calculerSim(map_idClasse_centroide.get(idC), f, metrique)).sorted((s1, s2) -> Double.compare(s2.getSim(), s1.getSim())).limit(k).collect(Collectors.toList());
        return e;        
    }
    
    public Resultat calculerSim(double[] centroide, final File fileParag, MetriqueSim metrique) 
    {
        String s="";
        try {
            s = FileUtils.read(fileParag);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // cette condiction est pour eviter un throw exception. Si on ne peut pas lire le fichier alors le segment est vide et on creer un Resultat avec un similarite de 0.0
        if (s.isEmpty())
            return new Resultat(0.0, fileParag);
        Set<String> tokens = Tokeniseur.extraireEnsembleToken(s);
        double[] v = getSegmentVecto(tokens);
        double sim = metrique.calculerMetrique(v, centroide);
        File file = new File(repertoireParagSelect.concat("\\").concat(fileParag.getName()));
        return new Resultat(sim, file);
    }
    
    public void ecrireResultat(int idC, List<Resultat> resultats, File fileResultats) throws IOException 
    {
        System.out.println("ecrireResultat du theme : " + idC);
        StringBuilder resultat;
        List<String> outputCsv = new ArrayList<>();
        for (Resultat rs: resultats)
        {
            resultat=new StringBuilder(100);
            
            String segment = FileUtils.read(rs.getFile());
            resultat.append(idC).append(";").append(rs.getSim()).append(";").append('"').append(segment).append('"').append(";");
            outputCsv.add(resultat.toString());

        }
        Files.write(fileResultats.toPath(), outputCsv, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        System.out.println(outputCsv.toString());
        
    }
        
//    public PriorityQueue<Resultat> getKResultats(int idC, final File fileParagFiltre, MetriqueSim metrique, int k) 
//    {
//        PriorityQueue<Resultat> pq = new PriorityQueue<>();
//        int n = 0;
//        
//        try {
//            DirectoryStream<Path> stream = Files.newDirectoryStream(fileParagFiltre.toPath());
//            
//            for (Path p: stream)
//            {
//                if (n>1000)
//                    break;
//                n++;
//                
//                Set<String> tokens = Tokeniseur.extraireEnsembleToken(FileUtils.read(p.toFile()));
//                double[] v = getSegmentVecto(tokens);
//                double sim = metrique.calculerMetrique(v, map_idClasse_centroide.get(idC));
//                
//                if (pq.size() < k)
//                {
//                    Resultat rs = new Resultat(sim, fileParagFiltre, idC);
//                    pq.add(rs);
//                }
//                else if (pq.peek().getSim()< sim)
//                {
//                    Resultat rs = new Resultat(sim, fileParagFiltre, idC);
//                    pq.poll();
//                    pq.add(rs);
//                }
//            }
//            stream.close();
//        } 
//        catch (IOException ex) 
//        {
//            ex.printStackTrace();
//        }
//        
//        return pq;
//    }
//    
//    public void ecrireResultat(Stream<Resultat> resultats, File fileResultats) 
//    {
//        StringBuilder out;
//        for (Resultat rs: resultats.iterator())
//        {
//            out=new StringBuilder(100);
//            String segment="";
//            try {
//                segment = FileUtils.read(rs.getFile());
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            out.append(rs.getIdC()).append(";").append(rs.getSim()).append(";").append('"').append(segment).append('"').append(";");
//            
//        }
//        System.out.println(out.toString());
//        try {
//            Files.write(fileResultats.toPath(), out, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//    
    
    
    public void calculerVecteurParagsEtCentroides(final File fileArffVecteurMots, final File filesParagFiltre) throws FileNotFoundException, IOException
    {
        System.out.println("recupererVecteursEtCentroides");
        BufferedReader reader = new BufferedReader(new FileReader(fileArffVecteurMots));
        data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_idV_token =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
        map_token_id = TrieUtils.inverserMap(map_idV_token);
        data.deleteAttributeAt(data.attribute("@@name@@").index());       
//        Map<Integer, VecteurCreux> map_idToken_vecteur = ArffUtils.getDenseVector(data, 0.0);
        
        // centroide
        map_idClasse_centroide = new HashMap<>();
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        for (String c: map_classe_idVecteurs.keySet())
        {
            System.out.println(c);
            int idC = Integer.parseInt(c);
            map_idClasse_centroide.put(idC, calculerCentroide(data, map_classe_idVecteurs.get(c)));
        }
        
    }
    
    private double[] calculerCentroide(Instances data, Set<Integer> idInstances)
    {
        double[] v = new double[data.numAttributes()];
        Arrays.fill(v, 0.0);
        for (int i: idInstances)
            v = VecteurCreux.additionnerVecteurs(v, data.get(i).toDoubleArray());
        return VecteurCreux.normer(v);
    }
    public double[] getSegmentVecto (Set<String> tokensParag)
    {
        double[] v = new double[map_token_id.size()];
        Arrays.fill(v, 0.0);
        for (String token: tokensParag)
        {
            v = VecteurCreux.additionnerVecteurs(v, data.get(map_token_id.get(token)).toDoubleArray());
        }
        return VecteurCreux.normer(v);
    }
    
}
