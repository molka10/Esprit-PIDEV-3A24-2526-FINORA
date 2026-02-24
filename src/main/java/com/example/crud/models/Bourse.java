        package com.example.crud.models;

        import java.sql.Timestamp;
        import java.util.ArrayList;
        import java.util.List;

        /**
         * Classe Bourse - Représente une bourse (marché boursier)
         *
         * RELATION BIDIRECTIONNELLE :
         * - Une bourse peut avoir PLUSIEURS actions
         * - On garde une liste des actions associées
         */
        public class Bourse {

            // ========================================
            // ATTRIBUTS
            // ========================================

            private int idBourse;
            private String nomBourse;
            private String pays;
            private String devise;
            private String statut;
            private Timestamp dateCreation;

            // RELATION BIDIRECTIONNELLE
            // Liste des actions appartenant à cette bourse
            private List<Action> actions;

            // ========================================
            // CONSTRUCTEURS
            // ========================================

            public Bourse() {
                this.actions = new ArrayList<>();
            }

            public Bourse(int idBourse, String nomBourse, String pays, String devise,
                          String statut, Timestamp dateCreation) {
                this.idBourse = idBourse;
                this.nomBourse = nomBourse;
                this.pays = pays;
                this.devise = devise;
                this.statut = statut;
                this.dateCreation = dateCreation;
                this.actions = new ArrayList<>();
            }

            public Bourse(String nomBourse, String pays, String devise, String statut) {
                this.nomBourse = nomBourse;
                this.pays = pays;
                this.devise = devise;
                this.statut = statut;
                this.actions = new ArrayList<>();
            }
            public Bourse(int idBourse, String nomBourse, String pays, String devise) {
                this.idBourse = idBourse;
                this.nomBourse = nomBourse;
                this.pays = pays;
                this.devise = devise;
                this.statut = "ACTIVE"; // valeur par défaut
                this.actions = new ArrayList<>();
            }


            // ========================================
            // GETTERS ET SETTERS
            // ========================================

            public int getIdBourse() {
                return idBourse;
            }

            public void setIdBourse(int idBourse) {
                this.idBourse = idBourse;
            }

            public String getNomBourse() {
                return nomBourse;
            }

            public void setNomBourse(String nomBourse) {
                this.nomBourse = nomBourse;
            }

            public String getPays() {
                return pays;
            }

            public void setPays(String pays) {
                this.pays = pays;
            }

            public String getDevise() {
                return devise;
            }

            public void setDevise(String devise) {
                this.devise = devise;
            }

            public String getStatut() {
                return statut;
            }

            public void setStatut(String statut) {
                this.statut = statut;
            }

            public Timestamp getDateCreation() {
                return dateCreation;
            }

            public void setDateCreation(Timestamp dateCreation) {
                this.dateCreation = dateCreation;
            }

            // Getter/Setter pour la liste des actions
            public List<Action> getActions() {
                return actions;
            }

            public void setActions(List<Action> actions) {
                this.actions = actions;
            }

            // ========================================
            // MÉTHODES UTILES
            // ========================================

            /**
             * Ajouter une action à la bourse
             */
            public void addAction(Action action) {
                if (!this.actions.contains(action)) {
                    this.actions.add(action);
                    action.setBourse(this);  // Synchronisation bidirectionnelle
                }
            }

            /**
             * Retirer une action de la bourse
             */
            public void removeAction(Action action) {
                this.actions.remove(action);
                action.setBourse(null);
            }

            /**
             * Obtenir le nombre d'actions dans cette bourse
             */
            public int getNombreActions() {
                return this.actions.size();
            }

            @Override
            public String toString() {
                return "Bourse{" +
                        "idBourse=" + idBourse +
                        ", nomBourse='" + nomBourse + '\'' +
                        ", pays='" + pays + '\'' +
                        ", devise='" + devise + '\'' +
                        ", statut='" + statut + '\'' +
                        ", nombreActions=" + actions.size() +
                        '}';
            }

            public boolean isActive() {
                return "ACTIVE".equalsIgnoreCase(this.statut);
            }

            public String toShortString() {
                return nomBourse + " (" + devise + ")";
            }
        }