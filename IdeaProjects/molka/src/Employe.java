public class Employe implements Comparable<Employe>{
     int id;
     String nom;
     String prenom;
     String nomDépartement;
     int  grade;

     public Employe(){}
     Employe(int id, String nom,
    String prenom,
    String nomDépartement,
    int  grade) {
         this.id = id;
         this.nom = nom;
         this.prenom = prenom;
         this.nomDépartement=nomDépartement;
         this.grade=grade;
     }

    public int getId() {
        return id;
    }
    public void setId(int id) {}
    public String getNom() {
         return nom;
    }
    public void setNom(String nom) {}
    public String getPrenom() {
         return prenom;
    }
    public void setPrenom(String prenom) {}

    public String getNomDépartement() {
        return nomDépartement;
    }
    public void setNomDépartement(String nom) {}
    public int getGrade() {
         return grade;
    }
    public void setGrade(int grade) {}
    @Override
    public String toString() {
         return "Employe [          ," +
                 "nom=" + nom + ", prenom=" + prenom + "NomDepartment"+nomDépartement+"grade=" + grade + "]";
    }




@Override
    public int compareTo(Employe e) {
        return Integer.compare(this.id, e.id);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employe e = (Employe) obj;
        return id == e.id && nom.equalsIgnoreCase(e.nom);
    }
}
