package com.example.finora.interfaces.services;

import com.example.finora.entities.categorie;
import java.util.List;

public interface Iservicecategorie {

    void ajouter(categorie c);
    void modifier(categorie c);
    void supprimer(int id);
    List<categorie> afficher();

}
