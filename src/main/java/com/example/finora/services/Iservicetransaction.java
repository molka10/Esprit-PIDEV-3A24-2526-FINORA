package com.example.finora.services;

import com.example.finora.entities.transaction;

import java.util.List;

public interface Iservicetransaction {

    void ajouter(transaction t);
    void modifier(transaction t);
    void supprimer(int id);
    List<transaction> afficher();

}
