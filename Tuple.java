/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preference_filterthenverifysw;

public class Tuple {    
    public int id;
    public int measure_values[];
    
    Tuple(int id, int measure_values[])
    {        
        this.id = id;       
        this.measure_values = measure_values.clone();
    } 
}