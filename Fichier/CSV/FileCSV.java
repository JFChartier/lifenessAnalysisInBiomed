/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Fichier.CSV;

import Fichier.FichierTxt;
import Matrice.Vectorisateur.VecteurCreux;
import Matrice.Vectorisateur.VecteurIndicie;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author JF Chartier
 */
public class FileCSV extends FichierTxt
{
    public String construireFichierCSV (double[] vecteur)
    {
        StringBuilder sb = new StringBuilder(vecteur.length);
        
        for (int i =0; i < vecteur.length-1; i++)
        {
            sb.append(vecteur[i]).append(", ");
        }
        sb.append(vecteur[vecteur.length-1]); // necessaire pour eviter que la derniere valeur soit suivie d'une ","
        return sb.toString();
    }
    
    // a valider
    public String construireFichierCSV (double[][] matrice)
    {
        StringBuilder sb = new StringBuilder();
        for (int i =0; i < matrice.length; i++)
        {
            sb.append(construireFichierCSV(matrice[i])).append("\r\n");
        }
        return sb.toString();
    }
    
    public String construireFichierCSV (List<String> entetes, Map<Integer, VecteurCreux> map_id_vecteur, Map<Integer, Integer> map_idVecteur_idClasse)
    {
        System.out.println("CONSTRUIRE FICHIER CSV");
        
        StringBuilder sb = new StringBuilder();
        sb.append("id, ");
        for (int i = 0; i < entetes.size(); i++)
        {
            sb.append(entetes.get(i)).append(", ");
        }
        sb.append("\r\n");
        
        for (int idVecteur: map_id_vecteur.keySet())
        {
            sb.append(idVecteur).append(", ").append(construireFichierCSV(map_id_vecteur.get(idVecteur).getTabPonderation())).append(map_idVecteur_idClasse.get(idVecteur)).append(", ").append("\r\n");
        }
        return sb.toString();
    }
    
    // il faut que la liste des attribut de l'entete respecte l,ordre croissant 
    public String construireFichierCSV (Map<Integer, VecteurCreux> map_id_vecteur)
    {
        System.out.println("CONSTRUIRE FICHIER CSV");
        
        
        StringBuilder sb = new StringBuilder();
        
        
        for (int idVecteur: map_id_vecteur.keySet())
        {
            sb.append(idVecteur).append(", ").append(construireFichierCSV(map_id_vecteur.get(idVecteur).getTabPonderation())).append("\r\n");
        }
        return sb.toString();
    }
    
    // il faut que la liste des attribut de l'entete respecte l,ordre croissant 
    public String construireFichierCSV (List<String> entetes, Map<Integer, VecteurCreux> map_id_vecteur)
    {
        System.out.println("CONSTRUIRE FICHIER CSV");
        // test
        if (entetes.size() != map_id_vecteur.get(0).getTabPonderation().length)
        {
            System.err.println("la liste des attributs != le nombre de dimension");
            System.exit(1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("id, ");
        for (int i = 0; i < entetes.size()-1; i++)
        {
            sb.append(entetes.get(i)).append(", ");
        }
        sb.append(entetes.get(entetes.size()-1));// necessaire pour eviter que la derniere valeur soit suivie d'une ","        
        sb.append("\r\n");
        
        for (int idVecteur: map_id_vecteur.keySet())
        {
            sb.append(idVecteur).append(", ").append(construireFichierCSV(map_id_vecteur.get(idVecteur).getTabPonderation())).append("\r\n");
        }
        return sb.toString();
    }
    
    // il faut que la liste des attribut de l'entete respecte l,ordre croissant 
    public String construireFichierCSV (Map<Integer, String> map_idVecteur_variable, List<String> entetes, Map<Integer, VecteurCreux> map_id_vecteur)
    {
        System.out.println("CONSTRUIRE FICHIER CSV");
        // test
        if (entetes.size() != new TreeMap<Integer, VecteurCreux>(map_id_vecteur).firstEntry().getValue().getTabPonderation().length)
        {
            System.err.println("la liste des attributs != le nombre de dimension");
            System.exit(1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("id, ");
        for (int i = 0; i < entetes.size()-1; i++)
        {
            sb.append(entetes.get(i)).append(", ");
        }
        sb.append(entetes.get(entetes.size()-1));// necessaire pour eviter que la derniere valeur soit suivie d'une ","        
        sb.append("\r\n");
        
        for (int idVecteur: map_id_vecteur.keySet())
        {
            sb.append(map_idVecteur_variable.get(idVecteur)).append(", ").append(construireFichierCSV(map_id_vecteur.get(idVecteur).getTabPonderation())).append("\r\n");
        }
        return sb.toString();
    }
    
    // il faut que la liste des attribut de l'entete respecte l,ordre croissant 
    public String construireFichierCSV (Map<Integer, String> entetes, Map<Integer, VecteurIndicie> map_id_vecteur)
    {
        System.out.println("CONSTRUIRE FICHIER CSV");
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("id, ");
        for (int i = 0; i < entetes.size()-1; i++)
        {
            sb.append(entetes.get(i)).append(", ");
        }
        sb.append(entetes.get(entetes.size()-1));// necessaire pour eviter que la derniere valeur soit suivie d'une ","        
        sb.append("\r\n");
        
        for (int idVecteur: map_id_vecteur.keySet())
        {
            sb.append(map_idVecteur_variable.get(idVecteur)).append(", ").append(construireFichierCSV(map_id_vecteur.get(idVecteur).getTabPonderation())).append("\r\n");
        }
        return sb.toString();
    }
    
    
    public static List<String[]> getCsv (File fileCsv, char sep) throws FileNotFoundException, IOException
    {
        // on remplace le Reader par un InputStreameReader qui est sa super classe, afin de choisir l'encodage
        // Le Reader de OpenCSV ne permet pas de choisir l'encodage
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(fileCsv), "UTF-8"), sep);
        List<String[]> data = new ArrayList<>();
        
        //Read CSV line by line and use the string array as you want
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) 
        {
           if (nextLine != null) 
           {
               data.add(nextLine);
           }
        }
        return data;
    }
    
    
    
    
}
