/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyses.ClassesSemantique;

import Affichage.Tableau.AfficheurTableau;
import Affichage.Tableau.AfficheurVecteur;
import static Analyses.ClassesSemantique.AnalyseAssociationCiblesEtClasses.getAssociationEntreMotCibleEtClasse;
import static Analyses.ClassesSemantique.AnalyseAssociationCiblesEtClasses.getMap_idCategorieCible_idsArticle;
import Article.Article;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Fichier.Arff.ArffIndicie;
import Fichier.Arff.ArffUtils;
import Fichier.FileUtils;
import Fichier.Weka.FichierArff;
import Matrice.Vectorisateur.VecteurIndicie;
import Matrice.Vectorisateur.Vectoriseur;
import Matrice.Vectorisateur.VectoriseurUtils;
import Parser.TypeContenu;
import Ponderation.CoefficientAssociation;
import Ponderation.InformationMutuelle;
import Pretraitement.Pretraitement;
import UtilsJFC.TrieUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import weka.core.Attribute;
import static weka.core.Attribute.NOMINAL;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

/**
 *
 * @author JF Chartier
 */
public class ConstruireVecteurMot 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
                
        try
        {
            File fileClasse = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\classification.csv");
            File fileCible = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\motCible.csv");
            File fileCorpus = CorpusUtils.getRepertoireArticleFiltreRevueSelectionne();
            File fileVecteurs = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMots.arff");
//            new AnalyseAssociationCiblesEtClasses().calculerAssociation(fileCorpus, TypeContenu.PARAGRAPHE, fileCible, fileClasse, -1);
            
//            new ConstruireVecteurMot().getVecteursMotsArff(fileCorpus, fileVecteurs, TypeContenu.PARAGRAPHE, -1, new InformationMutuelle());
            File newFileVecteurs = new File("C:\\Users\\JF Chartier\\Google Drive\\Ordinateur\\Projet sur BioMed\\V6\\vecteursMotsAvecClasseEtNom.arff");
            String dataArff = new ConstruireVecteurMot().ajouterClasse(fileVecteurs, fileClasse, newFileVecteurs);
            FileUtils.write(dataArff, newFileVecteurs.getAbsolutePath(), "utf8");
        } 
        
                      
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    protected String ajouterClasse (File fileVecteurs, File fileClasse, File newFileVecteurs) throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileVecteurs));
        Instances data = new ArffLoader.ArffReader(reader).getData();
        reader.close();
        Map<Integer, String> map_idVecteur_nom = ArffUtils.getMap_idInstance_attribut(data, "@@Class@@"); 
        Map<String, String> map_token_classe = CorpusUtils.getMap_token_catCible(fileClasse);
        Map<Integer, String> map_idVecteur_classe = new TreeMap<>();
        // test
        if (map_idVecteur_nom.values().size()!=map_token_classe.keySet().size())
        {
            System.err.println("pas les memes mots");
            System.exit(1);
        }
        
        for (int id: map_idVecteur_nom.keySet())
        {
            map_idVecteur_classe.put(id, map_token_classe.get(map_idVecteur_nom.get(id)));
        }
        data.renameAttribute(data.attribute("@@Class@@"), "@@name@@");
       
//        List<String> valeur = new ArrayList<String>(map_idVecteur_classe.values());
//        Attribute atClasse = new Attribute("@@class@@", valeur);
//        Attribute atClasse = new Attribute("@@class@@", (List<String>)null, data.numAttributes());
        
        
        data.insertAttributeAt(new Attribute("@@class@@", (List<String>)null), data.numAttributes());
//        data.insertAttributeAt(atClasse, data.numAttributes());
        
//        Attribute atName = new Attribute("@@Name@@", (List<String>)null);
//                
//        int indexClass = data.attribute("@@Class@@").index();
//        System.out.println("idAtt classe: " + indexClass);
//        int indexName = data.numAttributes();
        System.out.println("nbre attribut: " + data.numAttributes());
//        data.insertAttributeAt(atName, indexName);
//        System.out.println("nbre attribut: " + data.numAttributes());
        
        for (int i=0;i<data.numInstances();i++)
        {
            System.out.println(i+"/"+data.numInstances());
//            data.get(i).setDataset(data);
//            Instance inst = data.get(i);
//            System.out.println("nombre d'attribut du vecteur : "+inst.numAttributes());
//            System.out.println("valeur de l'attribut de classe: "+inst.stringValue(atClasse.index()));
//            data.get(i).insertAttributeAt(atClasse.index());
            
//            System.out.println("id attribut Class du vecteur : "+data.get(i).attribute(data.attribute("@@Class@@").index()).index());
            
            
//            System.out.println("idAtt name: " + data.attribute("@@Name@@").index());
//            data.get(i).attribute(data.attribute("@@class@@").index()).setStringValue(map_idVecteur_classe.get(i));
            
//            inst.setValue(atClasse.index(), map_idVecteur_classe.get(i));
//            inst.setValue(atClasse, map_idVecteur_classe.get(i));
//            System.out.println("valeur de l'attribut de classe: "+inst.stringValue(atClasse.index()));
//            System.out.println("valeur de l'attribut: " +map_idVecteur_classe.get(i));
            data.get(i).setValue(data.attribute("@@class@@"), map_idVecteur_classe.get(i));
//            data.get(i).setValue(data.attribute("@@Name@@"), map_idVecteur_nom.get(i));
           
//            System.out.println("idAtt classe: " + data.attribute("@@Class@@").index());
            
            
        }
        return data.toString();
        
    }
    
    
    
    public String getVecteursMotsArff(File filesArticles, File fileVecteurs, TypeContenu typeContenu, int nombreArticleSelectionne, CoefficientAssociation coefficient) throws IOException
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
        Map<Integer, Set<Integer>> map_idToken_idsArticle = CorpusUtils.getMap_idToken_ensembleIdArticle(articles, map_token_id);
        Map<Integer, VecteurIndicie> map_idToken_vecteurs = VectoriseurUtils.vectoriserAvecCoefficientAssociation4(map_idToken_idsArticle, CorpusUtils.getEnsembleIdArticle(articles), coefficient, false);
        Vectoriseur.normerVecteurIndicie2(map_idToken_vecteurs.values());
        
//        AfficheurVecteur.afficherVecteurs("Vecteurs", TrieUtils.inverserMap(map_token_id).keySet(), map_idToken_vecteurs.values());
        
//        String fichier = ArffUtils.getArffFile(map_idToken_vecteurs.values(), TrieUtils.inverserMap(map_token_id), "vecteur_mot_full_corpus");
        String fichier = ArffUtils.getArffFileWithClass(map_idToken_vecteurs.values(), TrieUtils.inverserMap(map_token_id), TrieUtils.inverserMap(map_token_id), "vecteur_mot_full_corpus");
        
        FileUtils.write(fichier, fileVecteurs.getAbsolutePath(), "utf8");
        return fichier;
    }
    
    
    
    
    
    
}
