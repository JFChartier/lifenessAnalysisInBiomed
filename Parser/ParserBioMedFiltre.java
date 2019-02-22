/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parser;

import Article.Article;
import Fichier.FileUtils;
import Segmentation.Segmenteur.ExtracteurUtils;
import Tokenisation.Tokeniseur;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author JF Chartier
 */
public class ParserBioMedFiltre 
{
        
    // recuperer les articles filtres
    public static Collection<Article> recupererContenuArticle(File files, TypeContenu typeContenu) throws IOException
    {
        System.out.println("RECUPERER ARTICLE");
        List<File> listeFile = FileUtils.getAllFileInPath(files, ".txt", new ArrayList<File>());
        Collection<Article> articles = new ArrayList<>(listeFile.size());
                
        int id = 0;
        int n = listeFile.size();
        for (File file: listeFile)
        {
            System.out.println("\t" + id+1+"/"+n);
            
            if (typeContenu.toString().equals(TypeContenu.ARTICLE.toString()))
            {
                List<String> tokens = new ArrayList<>();
                Collection<String> segments = recupererPhrases(file);
                for (String segment: segments)
                {
                    tokens.addAll(Tokeniseur.extraireListeToken(segment));
                }
                articles.add(new Article(id, tokens));
                id++;
            }
            else if (typeContenu.toString().equals(TypeContenu.PARAGRAPHE.toString()))
            {
                
                Collection<String> segments = recupererParagraphes(file);
                for (String segment: segments)
                {
                    List<String> tokens = new ArrayList<>();
                    tokens.addAll(Tokeniseur.extraireListeToken(segment));
                    articles.add(new Article(id, tokens));
                    id++;
                }
            }
            else if (typeContenu.toString().equals(TypeContenu.PHRASE.toString()))
            {
                Collection<String> segments = recupererPhrases(file);
                for (String segment: segments)
                {
                    List<String> tokens = new ArrayList<>();
                    tokens.addAll(Tokeniseur.extraireListeToken(segment));
                    articles.add(new Article(id, tokens));
                    id++;
                }
            }
        }
        return articles;
    }
    
    public static void recupererContenuArticle (File file, TypeContenu typeContenu, Collection<Article> articles) throws IOException
    {
        int id = articles.size();
        
        if (typeContenu.toString().equals(TypeContenu.ARTICLE.toString()))
            {
                List<String> tokens = new ArrayList<>();
                Collection<String> segments = recupererPhrases(file);
                for (String segment: segments)
                {
                    tokens.addAll(Tokeniseur.extraireListeToken(segment));
                }
                articles.add(new Article(id, tokens));
                id++;
            }
            else if (typeContenu.toString().equals(TypeContenu.PARAGRAPHE.toString()))
            {
                
                Collection<String> segments = recupererParagraphes(file);
                for (String segment: segments)
                {
                    List<String> tokens = new ArrayList<>();
                    tokens.addAll(Tokeniseur.extraireListeToken(segment));
                    articles.add(new Article(id, tokens));
                    id++;
                }
            }
            else if (typeContenu.toString().equals(TypeContenu.PHRASE.toString()))
            {
                Collection<String> segments = recupererPhrases(file);
                for (String segment: segments)
                {
                    List<String> tokens = new ArrayList<>();
                    tokens.addAll(Tokeniseur.extraireListeToken(segment));
                    articles.add(new Article(id, tokens));
                    id++;
                }
            }
    }
    
    
    public static Collection<String> recupererPhrases (File file) throws IOException
    {
//        String document = FileUtils.read(file.getPath());
        Pattern pattern = Pattern.compile("<s>"+"(.*?)"+"</s>", Pattern.DOTALL);
        return ExtracteurUtils.extraireParBalises(FileUtils.read(file.getPath()), pattern);
    }
    
    public static  Collection<String> recupererParagraphes (File file) throws IOException
    {
        System.out.println("RECUPERER LES PARAGRAPHES DE L'ARTICLE");
        String document;
        Pattern pattern = Pattern.compile("<p>"+"(.*?)"+"</p>", Pattern.DOTALL);
        document = FileUtils.read(file.getPath());
        return ExtracteurUtils.extraireParBalises(document, pattern);
                
    }
}
