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
public class ArticleMeta extends Article
{
    private int date;
    private String revue;
    
    public ArticleMeta(int date, String revue, int id, List<String> listeToken) {
        super(id, listeToken);
        this.date = date;
        this.revue = revue;
        
    }

    
    
}
