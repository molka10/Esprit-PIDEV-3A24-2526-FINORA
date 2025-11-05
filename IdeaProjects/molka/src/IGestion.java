public interface IGestion<T> {
public void ajouterEmployer( T t);
public boolean rechercherEmployer( String nom);
public boolean rechercherEmployer( T t);
public void supprimerEmploye(T t);
public void displayEmployer();
public void trierEmployerParId(); //comparable
    public void trierEmployeParNomDépartementEtGrade();

}

