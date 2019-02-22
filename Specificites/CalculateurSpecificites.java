/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Specificites;

import Matrice.Vectorisateur.VecteurIndicie;
import Ponderation.CoefficientAssociation;
import Ponderation.PonderateurParClasse;
import Ponderation.Ponderation;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author JF Chartier
 */
public class CalculateurSpecificites 
{
    public static void ponderationDescripteurParClasse (Set<Integer> ensembleIdArticle, Map<Integer, Set<Integer>> map_idClasse_ensembleIdArticle, Map<Integer, Set<Integer>> map_idVariable_ensembleIdArticle, Map<Integer, String> map_idVariable_nom, CoefficientAssociation coefficient)
    {
        TreeMap<Integer, VecteurIndicie> map_IdVar_VecteurPonderationParClasse = new PonderateurParClasse().pondererVariableParClasse(ensembleIdArticle, map_idVariable_ensembleIdArticle, map_idVariable_ensembleIdArticle, coefficient);
        Ponderation ponderationParClasse = new Ponderation(map_IdVar_VecteurPonderationParClasse);
        ponderationParClasse.afficherTableauDesPonderationUnifParClasse(map_idVariable_nom, "coefficient association");
    }
    
}
