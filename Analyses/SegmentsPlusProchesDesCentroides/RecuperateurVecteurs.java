/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.SegmentsPlusProchesDesCentroides;

import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import AnalyseSimilitude.AnalyseSimilitude;
import Analyses.ClassesSemantique.ConstruireVecteurMot;
import Classification.Partition;
import Corpus.CorpusUtils;
import Fichier.Arff.ArffUtils;
import Matrice.VecteurCreux.MatriceCreuse;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.VectoriseurUtils;
import Metrique.Similitude.Cosinus;
import Metrique.Similitude.ProduitScalaire;
import Metrique.Similitude.ProduitScalaireIndicie;
import Parser.TypeContenu;
import Ponderation.InformationMutuelle;
import UtilsJFC.TrieUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author JF Chartier
 */
public class RecuperateurVecteurs 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            File fileClasse = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\classification.csv");
            File fileCible = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\motCible.csv");
            File fileCorpus = CorpusUtils.getRepertoireArticleFiltreRevueSelectionne();
            File fileVecteurs = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMotsAvecClasseEtNom.arff");
//          
//            new RecuperateurVecteurs().analyseurCentroide(fileVecteurs, fileClasse);
            new RecuperateurVecteurs().analyseCentroides(fileVecteurs);
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private Map<Integer, String> map_idVecteur_nom;
    private Map<Integer, String> map_id_coToken;

    public RecuperateurVecteurs()
    {
        
    }
    
    
    public Map<Integer, String> getMap_id_token() {
        return map_idVecteur_nom;
    }
    
    
    
    public void analyseurCentroide(File fileArff, File fileClasse) throws FileNotFoundException, IOException
    {
        Map<Integer, VecteurCreux> vecteurs = getVecteursCreux(fileArff);
//        Map<String, Integer> map_token_idClasse = CorpusUtils.getMap_token_idClasse(fileClasse);
        Map<Integer, VecteurCreux> centroides = getCentroidesCreux(vecteurs, fileClasse);
        Set<Integer> idClasses = centroides.keySet();
        
//        centroides = AnalyseSimilitude.calculerMetriqueEntreVecteurs(vecteurs.values(), centroides.values(), new ProduitScalaireIndicie(), true);
        
        centroides = AnalyseSimilitude.calculerMetriqueEntreVecteursCreux(vecteurs.values(), centroides.values(), new ProduitScalaire(), false);
        if (!map_idVecteur_nom.keySet().equals(centroides.keySet()))
        {
            System.err.println("les id vecteurs ne sont pas les memes");
            System.exit(1);
        }
        AfficheurTableau.afficher2Colonnes("id et token", "id", "token", map_idVecteur_nom);
        AfficheurVecteur.afficherVecteursCreux("proximite du token avec centroide", map_idVecteur_nom, idClasses, centroides.values());
        
    }
    
    public void analyseCentroides(File fileArff) throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileArff));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idV_classe = ArffUtils.getMap_idInstance_attribut(data, "@@class@@"); 
        data.deleteAttributeAt(data.attribute("@@class@@").index());
        Map<Integer, String> map_idVecteur_nom =  ArffUtils.getMap_idInstance_attribut(data, "@@name@@");
        data.deleteAttributeAt(data.attribute("@@name@@").index());
        Map<Integer, String> map_id_coToken = ArffUtils.getMap_id_attribut(data);
        
        Map<Integer, VecteurCreux> vecteurs = ArffUtils.getDenseVector(data, 0.0);;
        Map<String, Set<Integer>> map_classe_idVecteurs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        Map<Integer, Set<Integer>> map_idCl_idVecs = new HashMap<>(map_classe_idVecteurs.size());
        for (String c: map_classe_idVecteurs.keySet())
            map_idCl_idVecs.put(Integer.parseInt(c), map_classe_idVecteurs.get(c));
        
        Map<Integer, VecteurCreux> centroides = MatriceCreuse.additionnerVecteurs(vecteurs, map_idCl_idVecs, true);
//        Map<Integer, VecteurCreux> centroides = getCentroidesCreux(vecteurs, fileClasse);
        Set<Integer> idClasses = centroides.keySet();
        centroides = AnalyseSimilitude.calculerMetriqueEntreVecteursCreux(vecteurs.values(), centroides.values(), new ProduitScalaire(), false);
        if (!map_idVecteur_nom.keySet().equals(centroides.keySet()))
        {
            System.err.println("les id vecteurs ne sont pas les memes");
            System.exit(1);
        }
        AfficheurTableau.afficher2Colonnes("id et token", "id", "token", map_idVecteur_nom);
        AfficheurVecteur.afficherVecteursCreux("proximite du token avec centroide", map_idVecteur_nom, idClasses, centroides.values());
        
    }
    
    private Map<Integer, VecteurCreux> getVecteursCreux (File fileArff) throws FileNotFoundException, IOException
    {
        System.out.println("getVecteurs");
        BufferedReader reader = new BufferedReader(new FileReader(fileArff));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
//        data.setClassIndex(data.numAttributes()-1);
        
        map_idVecteur_nom = ArffUtils.getMap_idInstance_attribut(data, "@@Class@@"); 
        data.deleteAttributeAt(data.numAttributes()-1);
        map_id_coToken = ArffUtils.getMap_id_attribut(data);
        return ArffUtils.getDenseVector(data, 0.0);
    }
    
    // a valider
//    public Map<Integer, VecteurIndicie> getVecteursIndicie (File fileArff) throws FileNotFoundException, IOException
//    {
//        System.out.println("getVecteurs");
//        BufferedReader reader = new BufferedReader(new FileReader(fileArff));
//        Instances data = new ArffLoader.ArffReader(reader).getData();
//        reader.close();
//        data.setClassIndex(data.numAttributes()-1);
//        map_id_token = ArffUtils.getMap_idInstance_class(data, data.classIndex());
//        return ArffUtils.getSparseVectorWithoutClass(data, false);
//    }
    
    public Map<Integer, VecteurCreux> getCentroidesCreux(Map<Integer, VecteurCreux> vecteurs, File fileClasse) throws IOException
    {
        System.out.println("getCentroides");
        Map<String, Integer> map_token_idClasse = CorpusUtils.getMap_token_idClasse(fileClasse);
        AfficheurTableau.afficher2Colonnes("token et classe", "token", "classe", map_token_idClasse);
        Map<Integer, Set<String>> map_idClasse_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_idClasse);
        System.out.println("nbre de classe: " + map_idClasse_tokens.size());
        
        Map<String, Integer> map_token_id = TrieUtils.inverserMap(map_idVecteur_nom);
        Map<Integer, VecteurCreux> centroides = new TreeMap<>();
        double[] w;
        for (Integer idC: map_idClasse_tokens.keySet())
        {
            w = new double[map_id_coToken.size()];
            Arrays.fill(w, 0.0);
            VecteurCreux v = new VecteurCreux(idC, w);
            for (String token: map_idClasse_tokens.get(idC))
            {
                v.additionnerVecteur(vecteurs.get(map_token_id.get(token)).getTabPonderation());
            }
            v.normer();
            centroides.put(idC, v);
        }
        return centroides;
        
    }
    
    public Map<Integer, VecteurCreux> getCentroidesCreux(Map<Integer, VecteurCreux> vecteurs, int dimensions, Map<Integer, String> map_idV_classe) throws IOException
    {
        System.out.println("getCentroides");
        Map<String, Set<Integer>> map_idClasse_idVecs = TrieUtils.recupererMap_valeur_ensembleCle2(map_idV_classe);
        System.out.println("nbre de classe: " + map_idClasse_idVecs.size());
        
        Map<Integer, VecteurCreux> centroides = new TreeMap<>();
        double[] w;
        for (String idC: map_idClasse_idVecs.keySet())
        {
            w = new double[dimensions];
            Arrays.fill(w, 0.0);
            VecteurCreux v = new VecteurCreux(Integer.parseInt(idC), w);
            for (int idV: map_idClasse_idVecs.get(idC))
            {
                v.additionnerVecteur(vecteurs.get(idV).getTabPonderation());
            }
            v.normer();
            centroides.put(Integer.parseInt(idC), v);
        }
        return centroides;
        
    }
    
    // a valider
//    public Map<Integer, VecteurIndicie> getCentroidesIndicie(Map<Integer, VecteurIndicie> vecteurs, Map<String, Integer> map_token_id, File fileClasse) throws IOException
//    {
//        System.out.println("getCentroides");
//        Map<String, Integer> map_token_idClasse = CorpusUtils.getMap_token_idClasse(fileClasse);
//        AfficheurTableau.afficher2Colonnes("token et classe", "token", "classe", map_token_idClasse);
//        Map<Integer, Set<String>> map_idClasse_tokens = TrieUtils.recupererMap_valeur_ensembleCle2(map_token_idClasse);
//        System.out.println("nbre de classe: " + map_idClasse_tokens.size());
//        
//        Map<Integer, VecteurIndicie> centroides = new TreeMap<>();
//        for (Integer idC: map_idClasse_tokens.keySet())
//        {
//            VecteurIndicie v = new VecteurIndicie(idC, new TreeMap<>());
//            for (String token: map_idClasse_tokens.get(idC))
//            {
//                v.additionnerVecteur(vecteurs.get(map_token_id.get(token)).getMap_Id_Ponderation());
//            }
//            v.normer();
//            centroides.put(idC, v);
//        }
//        return centroides;
//    }
    
}
