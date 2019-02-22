/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Article;

import java.util.List;

/**
 *
 * @author JF Chartier
 */
public class Article 
{
    private int id;
    private List<String> listeToken;
    

    public Article(int id, List<String> listeToken) {
        this.id = id;
        this.listeToken = listeToken;
    }

    public int getId() {
        return id;
    }

    public List<String> getListeToken() {
        return listeToken;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    
    
    
}
