/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pretraitement;

import FiltreurToken.FiltreurAntidictionnaire;
import FiltreurToken.FiltreurNombre;
import FiltreurToken.FiltreurSingleton;
import MorphAdornerUtils.Filtreur.FiltreurTousSaufNomAdjectifVerbe;
import MorphAdornerUtils.MorphAdornerSurSegment;
import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.AdornedWord;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JF Chartier
 */
public class PretraitementCorpusXML 
{
    private MorphAdornerSurSegment morphAdorner;
    private FiltreurTousSaufNomAdjectifVerbe filtreurMorph;
    private FiltreurSingleton fs;
    private FiltreurNombre fn;
    private FiltreurAntidictionnaire fa;

    public PretraitementCorpusXML() throws Exception 
    {
        morphAdorner = new MorphAdornerSurSegment();
        filtreurMorph = new FiltreurTousSaufNomAdjectifVerbe();
        fs = new FiltreurToken.FiltreurSingleton();
        fn = new FiltreurNombre();
        Set<String> antidictionnaire = new Antidictionnaire.AntidictionnaireChoi().getAntidictionnaire();
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnairePorter().getAntidictionnaire());
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnaireSalton().getAntidictionnaire());
        antidictionnaire.addAll(new Antidictionnaire.AntidictionnaireDuplicat().getAntidictionnaire());
        fa = new FiltreurToken.FiltreurAntidictionnaire(antidictionnaire);
    }
    
    public String filtrer (String paragraphe) throws Exception
    {
        StringBuilder paragrapheFiltre = new StringBuilder(200);
        paragrapheFiltre.append("<p>");
        List<List<AdornedWord>> listePhrasesAdornedWord = morphAdorner.lemmatiser2(paragraphe);
        listePhrasesAdornedWord = filtreurMorph.filtrerPlusieursPhrases(listePhrasesAdornedWord);
        // toutes les phrases d'un paragraphe
        for (List<AdornedWord> phrase: listePhrasesAdornedWord)
        {
            paragrapheFiltre.append("<s>");
            List<String> listeTokenPhrase = new ArrayList<String>();
            for (AdornedWord a: phrase)
            {
                // autres filtres similaires a ceux de MorphAdorner
                if (fs.applicationFiltre(a.getLemmata())==false)
                    if (fn.applicationFiltre(a.getLemmata())==false)
                        if(fa.applicationFiltre(a.getLemmata())==false)
                            listeTokenPhrase.add(a.getLemmata());
            }
            paragrapheFiltre.append(String.join(" ", listeTokenPhrase));
            paragrapheFiltre.append("</s>");
        }
        paragrapheFiltre.append("</p>");
        return paragrapheFiltre.toString();
    }
    
    
    
    
    
}
