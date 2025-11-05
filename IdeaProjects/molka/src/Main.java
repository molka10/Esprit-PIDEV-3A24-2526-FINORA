//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        SocieteArrayList societe = new SocieteArrayList();

        Employe e1 = new Employe(1, "Ali", "Ahmed", "IT", 2);
        Employe e2 = new Employe(3, "Mouna", "Khaled", "RH", 1);
        Employe e3 = new Employe(2, "Sami", "Fares", "IT", 3);

        societe.ajouterEmployer(e1);
        societe.ajouterEmployer(e2);
        societe.ajouterEmployer(e3);

        System.out.println("\n--- Liste originale ---");
        societe.displayEmployer();

        System.out.println("\n--- Tri naturel (par id) ---");
        societe.trierEmployerParId();
        societe.displayEmployer();

        System.out.println("\n--- Tri personnalisé (département, grade, nom) ---");
        societe.trierEmployeParNomDépartementEtGrade();
        societe.displayEmployer();
    }
}
    
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
