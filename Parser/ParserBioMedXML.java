/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parser;


import Corpus.Corpus;
import Corpus.CorpusUtils;
import Corpus.Dictionnaire;
import Corpus.DocumentJFC;
import Fichier.FichierTxt;
import Fichier.FileUtils;
import FiltreurToken.FiltreurAntidictionnaire;
import FiltreurToken.FiltreurNombre;
import FiltreurToken.FiltreurSingleton;
import MorphAdornerUtils.Filtreur.FiltreurTousSaufNomAdjectifVerbe;
import MorphAdornerUtils.MorphAdornerSurSegment;
import MorphAdornerUtils.SegmenteurMorphAdorner;
import Pretraitement.PretraitementCorpusXML;
import Tokenisation.Tokeniseur;
import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.AdornedWord;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencemelder.SentenceMelder;
import java.io.File;
import org.jdom2.*;
import org.jdom2.input.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 *
 * @author JF Chartier
 */
public class ParserBioMedXML 
{
    
    private static List<Element> listeSection;
    
    public static void main(String[] args)
    {
                
        try
        {
            
//            String pathCorpus = "C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articles";
//            File file = new File(pathCorpus);
//            String corpus = new ParserBioMed().pr�traitementCorpusBioMed(file);
//            String chemin = "C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\corpusBioMedLemmatiseAndictionnaireFiltreNombreSingleton.txt";
//            FichierTxt.enregistrerFichierTxt(chemin, corpus);
//            System.exit(1);
            
            //1471-2210-9-S1-P77
            //1471-2105-11-160
            //1471-2466-10-12
            //1471-2342-7-7
            // 1748-7161-4-S2-O24
            //1475-2875-11-S1-P63
            
//            File pathArtHome = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articles\\0778-7367-67-1-30.xml");
//            String pathArt = "C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articles\\1756-0500-2-237.xml";          
//            String art = new ParserBioMed().recupererArticleBodyBioMed(pathArtHome);
//            System.out.println(art);
            
            
//            File pathArtLanci = new File ("C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\articles");
//            new ParserBioMed().pr�traitementCorpusBioMed(pathArtLanci, "C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\articleFiltre\\");
//            System.exit(1);
//            new ParserBioMed().recupererParagrapheLemmatiserEtAntidictionnaire(pathArtLanci, "C:\\Users\\chartier\\Documents\\corpus\\Corpus BioMed\\articleFiltre2\\");
            
//            File bioMedCorpusOrdi = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articles\\");
//            ParserBioMedXML.recupererNomsRevues(bioMedCorpusOrdi);
            
//            Collection<File> filesBioMed = recupererFilesSelonDictionnaireRevues(CorpusUtils.getRepertoireArticle(), Dictionnaire.getFileRevueSelectionnees());
//            new ParserBioMedXML().recupererEtFiltreArticles(filesBioMed, CorpusUtils.getRepertoireArticleFiltreRevueSelectionne().getPath());
            
            //20-02-2017 recuperer article des revues selectionner mais ne faire aucun filtrage
//            Collection<File> filesBioMed = recupererFilesSelonDictionnaireRevues(CorpusUtils.getRepertoireArticle(), Dictionnaire.getFileRevueSelectionnees());
//            new ParserBioMedXML().recupererParagraphe(filesBioMed, CorpusUtils.getRepertoireParagrapheRevueSelectionnee().getPath());
        
            // 28-04-2017 test pour recuperer auteurs
            File pathArtHome = new File("C:\\Users\\JF Chartier\\Documents\\Collections standard\\Corpus BioMed\\articles\\0778-7367-67-1-30.xml");
            recupererAuteursDansArticle(pathArtHome, new SAXBuilder());
        } 
        
        catch (JDOMException e) 
        {
            e.printStackTrace();
        }
              
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public static Collection<File> recupererFilesSelonDictionnaireRevues(File files, File dictionnaireRevue) throws IOException, JDOMException
    {
        System.out.println("recupererFileSelonDictionnaireRevues");
        List<File> listeFile = FileUtils.getAllFileInPath(files, ".xml", new ArrayList<File>());
        Set<String> dictionnaireRevues = Dictionnaire.recupererDictionnaireRevues(dictionnaireRevue);
        Collection<File> filesSelect = new TreeSet<>();
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        for (File file: listeFile)
        {
            String revue = recupererNomRevue(file, builder).toLowerCase().trim();
            for (String revueCible: dictionnaireRevues)
            {
                if (revueCible.equalsIgnoreCase(revue))
                    filesSelect.add(file);
            }
        }
        System.out.println("\t nombre de file selectionne : " + filesSelect.size());
        return filesSelect;
    }
    
    public static Collection<String> recupererAnneesPublications(File files) throws JDOMException, IOException
    {
        System.out.println("Recuperer Annees de Publications");
        List<File> listeFile = Fichier.FichierTxt.readManyFile(files, ".xml", new ArrayList<File>());
        return recupererAnneesPublications(listeFile);
    }
    
    public static Collection<String> recupererAnneesPublications(Collection<File> files) throws JDOMException, IOException
    {
        System.out.println("Recuperer Annees de Publications");
        int n = files.size();
        Collection<String> annees = new ArrayList<String>(n);
        
        int i = 0;
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        for (File file: files)
        {
            System.out.println(i++ +"/"+n);
            annees.add(recupererAnneePublication(file, builder));
        }
        System.out.println("\t" + "nombre de fichiers: " + annees.size());
        return annees;     
    }
    
    private static String recupererAnneePublication (File file, SAXBuilder builder) throws JDOMException, IOException
    {
        
        Document document = builder.build(file);
        Element racine = document.getRootElement();
        Element fm = racine.getChild("fm");
        Element bibl = fm.getChild("bibl");
        String dateAnnee = bibl.getChildTextNormalize("pubdate")==null? " ": bibl.getChildTextNormalize("pubdate");
        System.out.println(dateAnnee);
        return dateAnnee;
        
    } 
    
    public static Collection<String> recupererAuteurs(Collection<File> files) throws JDOMException, IOException
    {
        System.out.println("Recuperer Annees de Publications");
        int n = files.size();
        Collection<String> auteurs = new ArrayList<String>(n);
        
        int i = 0;
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        for (File file: files)
        {
            System.out.println(i++ +"/"+n);
            auteurs.addAll(recupererAuteursDansArticle(file, builder));
        }
        System.out.println("\t" + "nombre d'auteurs: " + auteurs.size());
        return auteurs;     
    }
    
    public static Set<String> recupererAuteursDansArticle (File file, SAXBuilder builder) throws JDOMException, IOException
    {
        Document document = builder.build(file);        Element racine = document.getRootElement();

        Element fm = racine.getChild("fm");
        Element bibl = fm.getChild("bibl");
        Element aug = bibl.getChild("aug");
        // verifier si il y a des auteurs
        if (aug==null) return new TreeSet<String>();
        List<Element> au=aug.getChildren("au");
        
        if (au==null)return new TreeSet<String>();
//        
//        
//        
//        if (aug.getChildren("au")==null)
//            System.out.println(aug.toString());
//        else
//            au=aug.getChildren("au");
//        System.out.println(au.size());
        Set<String> names = new HashSet<>(au.size());
        for (Element e: au)
        {
            String name = e.getChildTextNormalize("snm")==null? " ": e.getChildTextNormalize("snm");
            name = name.concat(" ");
            String fn = e.getChildTextNormalize("fnm")==null? " ": e.getChildTextNormalize("fnm");
            name = name.concat(fn);
            names.add(name);
//            Element name = e.getChild("snm");
//            System.out.println(e.toString());
//            System.out.println(e.getChildTextNormalize("snm"));
//            System.out.println(e.getChildTextNormalize("fnm"));
        }
        return names;
    }
    
    public static Collection<String> recupererNomsRevues(File files) throws IOException, JDOMException // repertoire des fichiers xml des articles
    {
        System.out.println("Recuperer Noms Revues");
        List<File> listeFile = Fichier.FichierTxt.readManyFile(files, ".xml", new ArrayList<File>());
        return recupererNomsRevues(listeFile);
    }
    
    public static Collection<String> recupererNomsRevues(Collection<File> files) throws IOException, JDOMException // repertoire des fichiers xml des articles
    {
        System.out.println("Recuperer Noms Revues");
        int n = files.size();
        Collection<String> nomsRevues = new ArrayList<String>(n);
        
        int i = 0;
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        for (File file: files)
        {
            System.out.println(i++ +"/"+n);
            nomsRevues.add(recupererNomRevue(file, builder));
        }
        System.out.println("\t" + "nombre de fichiers: " + nomsRevues.size());
        return nomsRevues;        
    }
    
    private static String recupererNomRevue (File file, SAXBuilder builder) throws JDOMException, IOException
    {
        Document document = builder.build(file);
        Element racine = document.getRootElement();
        Element fm = racine.getChild("fm");
        Element bibl = fm.getChild("bibl");
        String nom = bibl.getChildTextNormalize("source")==null? " ": bibl.getChildTextNormalize("source");
        nom = nom.toLowerCase();
//        System.out.println(nom);
        
        return nom;
        
    }
    
    public void recupererEtFiltreArticles(Collection<File> files, String pathTxtFilesNewDocument) throws Exception
    {
        System.out.println("RECUPERER, PRETRAITER ET ENREGISTRER LES ARTICLES BIOMED");
        System.out.println("\t" + "nombre de file : "+files.size());
        Pretraitement.PretraitementCorpusXML pretraitement = new PretraitementCorpusXML();
        
        // tous les articles BioMed
        int i = 0;
        int n = files.size();
        for (File file : files)
        {
            System.out.println("doc: " + i++ +"/"+n);
            List<Element> listeParagrapheUnArticle = recupererParagraphesDesArticlesBioMed(file);
            StringBuilder article = new StringBuilder(200);
            // tous les paragraphes d'un article
            for (Element paragraphe: listeParagrapheUnArticle)
            {
                article.append(pretraitement.filtrer(paragraphe.getTextNormalize()));
            }
            
            if (article.toString().length() >45) // prendre les documents qui ont au moins 45 caracteres. Ceci permet egalement d'eviter les faux documents qui ne contiennent que "To acces the document, follow "
            {
                FileUtils.write(article.toString(), pathTxtFilesNewDocument+"doc"+i+".txt");
            }
        }
    }
    
    public void recupererArticles(Collection<File> files, String pathTxtFilesNewDocument) throws Exception
    {
        System.out.println("recupererArticles");
        System.out.println("\t" + "nombre de file : "+files.size());
        SegmenteurMorphAdorner segmenteur = new SegmenteurMorphAdorner();
        SentenceMelder melder = new SentenceMelder();
        
        // tous les articles BioMed
        int i = 0;
        int n = files.size();
        for (File file : files)
        {
            System.out.println("doc: " + i++ +"/"+n);
            List<Element> listeParagrapheUnArticle = recupererParagraphesDesArticlesBioMed(file);
            StringBuilder article = new StringBuilder(200);
            // tous les paragraphes d'un article
            List<List<String>> phrases;
            String phrase;
            for (Element paragraphe: listeParagrapheUnArticle)
            {
                article.append("<p>");
                phrases = segmenteur.segmentateurPhraseAnglais(paragraphe.getTextNormalize());
                for (List<String> phraseTokenise: phrases)
                {
                    article.append("<s>");
                    phrase = melder.reconstituteSentence(phraseTokenise);
                    article.append(phrase);
                    article.append("</s>");
                }
                article.append("</p>");
                
            }
            
            if (article.toString().length() >60) // prendre les documents qui ont au moins 45 caracteres. Ceci permet egalement d'eviter les faux documents qui ne contiennent que "To acces the document, follow ", ou "(To access the full article, please see PDF)"
            {
                FileUtils.write(article.toString(), pathTxtFilesNewDocument+"\\doc"+i+".txt");
            }
        }
    }
    
    public void recupererParagraphe(Collection<File> files, String pathTxtFilesNewDocument) throws Exception
    {
        System.out.println("recupererParagraphe");
        System.out.println("\t" + "nombre de file : "+files.size());
        SegmenteurMorphAdorner segmenteur = new SegmenteurMorphAdorner();
        SentenceMelder melder = new SentenceMelder();
        
        // tous les articles BioMed
        int i = 0;
        int n = files.size();
        int j = 555800;
        for (File file : files)
        {
                        
            List<Element> listeParagrapheUnArticle = recupererParagraphesDesArticlesBioMed(file);

            // tous les paragraphes d'un article
                        
            for (Element paragraphe: listeParagrapheUnArticle)
            {
                StringBuilder parag = new StringBuilder();
                List<List<String>> phrases = segmenteur.segmentateurPhraseAnglais(paragraphe.getTextNormalize());
                for (List<String> phraseTokenise: phrases)
                {
                    parag.append("<s>");
                    String phrase = melder.reconstituteSentence(phraseTokenise);
                    parag.append(phrase);
                    parag.append("</s>");
                }
                if (parag.toString().length() >60) // prendre les documents qui ont au moins 45 caracteres. Ceci permet egalement d'eviter les faux documents qui ne contiennent que "To acces the document, follow ", ou "(To access the full article, please see PDF)"
                {
                    if (i>j)
                        FileUtils.write(parag.toString(), pathTxtFilesNewDocument+"\\parag_"+i+".txt");
                    i++;
//                    System.out.println("parag: " + ++i +"/"+n);
                }
            }
        }
    }
    
    public void recupererParagrapheLemmatiserEtAntidictionnaire(File pathXmlFile, String pathTxtFilesNewDocument) throws Exception
    {
        System.out.println("RECUPERER ET PRETRAITER LES ARTICLES BIOMED");
        
        MorphAdornerSurSegment morphAdorner = new MorphAdornerSurSegment();
        FiltreurTousSaufNomAdjectifVerbe filtreurMorph = new FiltreurTousSaufNomAdjectifVerbe();
        FiltreurSingleton fs = new FiltreurToken.FiltreurSingleton();
        FiltreurNombre fn = new FiltreurNombre();
        Set<String> antidictionnaire = new Antidictionnaire.AntidictionnaireChoi().getAntidictionnaire();
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnairePorter().getAntidictionnaire());
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnaireSalton().getAntidictionnaire());
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnaireDuplicat().getAntidictionnaire());
        FiltreurAntidictionnaire fa = new FiltreurToken.FiltreurAntidictionnaire(antidictionnaire);
        
        List<File> listeFile = Fichier.FichierTxt.readManyFile(pathXmlFile, ".xml", new ArrayList<File>());
        StringBuilder article;
        
        
        // tous les articles BioMed
        for (int i = 0; i < listeFile.size(); i++)
        {
            System.out.println("doc: " + i);
            List<Element> listeParagrapheUnArticle = recupererParagraphesDesArticlesBioMed(listeFile.get(i));
            article = new StringBuilder(200);
            // tous les paragraphes d'un article
            for (Element paragraphe: listeParagrapheUnArticle)
            {
                article.append("<p>");
                List<List<AdornedWord>> listePhrasesAdornedWord = morphAdorner.lemmatiser2(paragraphe.getTextNormalize());
                listePhrasesAdornedWord = filtreurMorph.filtrerPlusieursPhrases(listePhrasesAdornedWord);
                // toutes les phrases d'un paragraphe
                for (List<AdornedWord> phrase: listePhrasesAdornedWord)
                {
                    article.append("<s>");
                    List<String> listeTokenPhrase = new ArrayList<String>();
                    for (AdornedWord a: phrase)
                    {
                        // autres filtres similaires a ceux de MorphAdorner
                        if (fs.applicationFiltre(a.getLemmata())==false)
                            if (fn.applicationFiltre(a.getLemmata())==false)
                                if(fa.applicationFiltre(a.getLemmata())==false)
                                    listeTokenPhrase.add(a.getLemmata());
                    }
                    article.append(String.join(" ", listeTokenPhrase));
                    article.append("</s>");
                }
                article.append("</p>");
            }
            
            if (article.toString().length() >45) // prendre les documents qui ont au moins 45 caracteres. Ceci permet egalement d'eviter les faux documents qui ne contiennent que "To acces the document, follow "
            {
                FileUtils.write(article.toString(), pathTxtFilesNewDocument+"doc"+i+".txt");
            }
        }
    }
    
    public void pretraitementCorpusBioMed (File pathXmlFile, String pathTxtFiles) throws IOException, JDOMException, Exception
    {
        List<File> listeFile = Fichier.FichierTxt.readManyFile(pathXmlFile, ".xml", new ArrayList<File>());
//        TreeMap<Integer, DocumentJFC> map_idDocJFC = new TreeMap<Integer, DocumentJFC>();
//        DocumentJFC doc;
        String article;
        List<String> listeToken;
        List<List<AdornedWord>> listeAdornedWord;
        MorphAdornerSurSegment morphAdorner = new MorphAdornerSurSegment();
        FiltreurTousSaufNomAdjectifVerbe filtreurMorph = new FiltreurTousSaufNomAdjectifVerbe();
        FiltreurSingleton fs = new FiltreurToken.FiltreurSingleton();
        FiltreurNombre fn = new FiltreurNombre();
        Set<String> antidictionnaire = new Antidictionnaire.AntidictionnaireChoi().getAntidictionnaire();
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnairePorter().getAntidictionnaire());
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnaireSalton().getAntidictionnaire());
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnaireDuplicat().getAntidictionnaire());
        FiltreurAntidictionnaire fa = new FiltreurToken.FiltreurAntidictionnaire(antidictionnaire);
        
        for (int i = 0; i < listeFile.size(); i++)
        {
            System.out.println("doc: " + i);
            article = recupererBodyBioMed(listeFile.get(i));
            listeAdornedWord = morphAdorner.lemmatiser2(article);
            listeAdornedWord = filtreurMorph.filtrerPlusieursPhrases(listeAdornedWord);
            listeToken = new ArrayList<String>(1000); 
            for (List<AdornedWord> phrase: listeAdornedWord)
            {
                for (AdornedWord a: phrase)
                    listeToken.add(a.getLemmata());
            }
            
            listeToken = fs.filtrerListe(listeToken);
            listeToken = fn.filtrerListe(listeToken);
            listeToken = fa.filtrerListe(listeToken);
            if (listeToken.size() >4) // prendre les documents qui ont au moins 5 mots. Ceci permet egalement d'eviter les faux documents qui ne contiennent que "To acces the document, follow "
            {
                article  = String.join(" ", listeToken);
                article = Corpus.construireDocumentFormatJFC(article);
//                System.out.println(article);
                FichierTxt.enregistrerFichierTxt(pathTxtFiles+"doc"+i+".txt", article);
    //            doc = new DocumentJFC(article, new TreeSet<String>(), -1, i, -1);
    //            map_idDocJFC.put(i, doc);
            }
        }
//        return new Corpus(map_idDocJFC).construireCorpusFormatJFC();
    }
    
       
        
    public Corpus recupererToutLesArticleBioMed (File path) throws IOException, JDOMException
    {
        String pathMotCible = "C:\\Users\\JF Chartier\\Documents\\Lanci\\Projet sur BioMed\\listeMotsCibles.txt";
        String pathMotDescripteur = "C:\\Users\\JF Chartier\\Documents\\Lanci\\Projet sur BioMed\\listeMotsDescripteurs.txt";
        Set<String> listeMotCible = Tokeniseur.extraireEnsembleTokenDunFichier(pathMotCible);
        Set<String> listeMotDescripteur = Tokeniseur.extraireEnsembleTokenDunFichier(pathMotDescripteur);
        Set<String> listeToken = new TreeSet<String>(listeMotCible);
        listeToken.addAll(listeMotDescripteur);    
        
        List<File> listeFile = Fichier.FichierTxt.readManyFile(path, ".xml", new ArrayList<File>());
        TreeMap<Integer, DocumentJFC> map_idDocJFC = new TreeMap<Integer, DocumentJFC>();
        DocumentJFC doc;
        String article, articleFiltre;
        for (int i = 0; i < listeFile.size(); i++)
        {
//            System.out.println("nbre de document: " + i);
            article = recupererBodyBioMed(listeFile.get(i));
            articleFiltre = filterArticleSelonDictionnaire(article, listeToken);
            doc = new DocumentJFC(articleFiltre, new TreeSet<String>(), -1, i, -1);
            map_idDocJFC.put(i, doc);
            
        }
        System.out.println("Nbre articles selectionnes : " + map_idDocJFC.size());
        
        return new Corpus(map_idDocJFC);
    }
    
    public String filterArticleSelonDictionnaire (String article, Set<String> ensembleToken)
    {
        List<String> articleTokenise = Tokeniseur.extraireListeToken(article);
        System.out.println(articleTokenise.size());
        List<String> articleTokeniseFiltre = new ArrayList<String>();
        for (String t: articleTokenise)
        {
            if (ensembleToken.contains(t))
                articleTokeniseFiltre.add(t);
        }
        StringBuilder s = new StringBuilder();
        for (String t: articleTokeniseFiltre)
        {
            s.append(t).append(" ");
        }
        return s.toString();
    }
    
    public String recupererBodyBioMed (File file) throws JDOMException, IOException
    {
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document document = builder.build(file);
//            XMLOutputter fmt = new XMLOutputter();
//            fmt.output(document, System.out);
//        System.out.println(document.hasRootElement());
        Element racine = document.getRootElement();

        List<Element> listeBody = racine.getChildren("bdy");
//            System.out.println("nbre d'element : " + liste.size());
        listeSection = new ArrayList<Element>();
        StringBuilder textBody = new StringBuilder();
        for (Element article: listeBody)
        {
            recupererSectionsEtParagrapheBioMed(article);
//                System.out.println("nbre de section: " + listeSection.size());
            for (Element e2: listeSection)
            {
                textBody.append(e2.getTextNormalize()).append("\r\n");
            }
        }
        return textBody.toString();
    }
    
    public void recupererSectionsEtParagrapheBioMed (Element article)
    {
        listeSection.addAll(article.getChildren("p"));
        for (Element e: article.getChildren("sec"))
        {
//            System.out.println(listeSection.size());
            recupererSectionsEtParagrapheBioMed(e);
        }
//        System.out.println("nb section: " + listeSection.size());       
    }
    
    public List<Element> recupererParagraphesDesArticlesBioMed (File file) throws JDOMException, IOException
    {
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document document = builder.build(file);
        Element racine = document.getRootElement();

        List<Element> listeBody = racine.getChildren("bdy");
//            System.out.println("nbre d'element : " + liste.size());
        listeSection = new ArrayList<Element>();
        StringBuilder textBody = new StringBuilder();
        for (Element article: listeBody)
        {
            recupererSectionsEtParagrapheBioMed(article);
//                System.out.println("nbre de section: " + listeSection.size());
        }
        return new ArrayList<Element>(listeSection);
    }
    
    
////////    public static void recupererSections (Element racine)
////////    {
////////        if (racine.getChildren("sec").isEmpty())
////////        {
////////            listeSection.addAll(racine.getChildren("p"));
////////        }
////////        else
////////        {
////////            for (Element e: racine.getChildren("sec"))
////////            {
////////                listeSection.addAll(racine.getChildren("p"));
////////                System.out.println(listeSection.size());
//////////                listeSection.addAll(e.getChildren("sec"));
//////////                listeSection.addAll(recupererSections(e));
////////                recupererSections(e);
////////            }
////////            System.out.println("nb section: " + listeSection.size());
//////////            return listeSection;
////////        }
////////        
////////    }
    
       
    
    
    
}
