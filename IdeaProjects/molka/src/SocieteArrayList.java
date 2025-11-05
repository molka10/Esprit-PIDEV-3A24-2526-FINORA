
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SocieteArrayList implements IGestion<Employe> ,IRechercheAvancee<Employe>  {
    // Une collection dynamique pour stocker les employés
    private ArrayList<Employe> liste = new ArrayList<>();

    @Override
    public void ajouterEmployer(Employe e) {
        liste.add(e);
    }

    @Override
    public boolean rechercherEmployer(String nom) {
        for (Employe e : liste) {
            if (e.getNom().equalsIgnoreCase(nom)) return true;
        }
        return false;
    }

    @Override
    public boolean rechercherEmployer(Employe e) {
        return liste.contains(e);  // utilise equals()
    }

    @Override
    public void supprimerEmploye(Employe e) {
        liste.remove(e);
    }

    @Override
    public void displayEmployer() {
        for (Employe e : liste) System.out.println(e);
    }

    // Tri naturel → par id (Comparable)
    @Override
    public void trierEmployerParId() {
        Collections.sort(liste);  // Comparable doit être implémenté dans Employe
    }


    @Override
    public void trierEmployeParNomDépartementEtGrade() {
        Collections.sort(liste, new Comparator<Employe>() {
            @Override
            public int compare(Employe e1, Employe e2) {
                int res = e1.getNomDépartement().compareToIgnoreCase(e2.getNomDépartement());
                if (res == 0) res = Integer.compare(e1.getGrade(), e2.getGrade());
                if (res == 0) res = e1.getNom().compareToIgnoreCase(e2.getNom());
                return res;
            }
        });
    }


    @Override
    public List<Employe> rechercherParDepartement(String nomDepartement) {
        List<Employe> result = new ArrayList<>();

        for (Employe e : liste) {
            if (e.getNomDépartement().equalsIgnoreCase(e.nomDépartement)) {
                result.add(e);
            }
        }

        return result;
    }
}
