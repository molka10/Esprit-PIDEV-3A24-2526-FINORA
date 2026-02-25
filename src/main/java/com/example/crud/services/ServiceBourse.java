package com.example.crud.services;

import com.example.crud.interfaces.IServices;
import com.example.crud.entities.Action;
import com.example.crud.entities.Bourse;
import com.example.crud.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations CRUD sur les Bourses

 * BONUS IMPORTANT :
 * - getByIdWithActions(id) charge une bourse + ses actions (relation bidirectionnelle)
 */
public class ServiceBourse implements IServices<Bourse> {

    private final Connection connection;

    public ServiceBourse() {
        this.connection = Database.getInstance().getConnection();
    }

    // ==========================================
    // CREATE - Ajouter une bourse
    // ==========================================
    @Override
    public void add(Bourse bourse) {
        String req = "INSERT INTO bourse (nom_bourse, pays, devise, statut) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, bourse.getNomBourse());
            pst.setString(2, bourse.getPays());
            pst.setString(3, bourse.getDevise());
            pst.setString(4, bourse.getStatut());

            pst.executeUpdate();
            System.out.println("✅ Bourse ajoutée : " + bourse.getNomBourse());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // READ ALL - Récupérer toutes les bourses
    // ==========================================
    @Override
    public List<Bourse> getAll() {
        List<Bourse> bourses = new ArrayList<>();
        String req = "SELECT * FROM bourse ORDER BY nom_bourse";

        try (Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                bourses.add(mapBourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération : " + e.getMessage());
            e.printStackTrace();
        }

        return bourses;
    }

    // ==========================================
    // UPDATE - Modifier une bourse
    // ==========================================
    @Override
    public void update(Bourse bourse) {
        String req = "UPDATE bourse SET nom_bourse=?, pays=?, devise=?, statut=? WHERE id_bourse=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, bourse.getNomBourse());
            pst.setString(2, bourse.getPays());
            pst.setString(3, bourse.getDevise());
            pst.setString(4, bourse.getStatut());
            pst.setInt(5, bourse.getIdBourse());

            pst.executeUpdate();
            System.out.println("✅ Bourse modifiée : " + bourse.getNomBourse());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // DELETE - Supprimer une bourse
    // ==========================================
    @Override
    public void delete(Bourse bourse) {
        String req = "DELETE FROM bourse WHERE id_bourse=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, bourse.getIdBourse());
            pst.executeUpdate();

            System.out.println("✅ Bourse supprimée : " + bourse.getNomBourse());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void delete(int id) {
        String req = "DELETE FROM bourse WHERE id_bourse=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("✅ Bourse supprimée (id=" + id + ")");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ==========================================
    // MÉTHODES SUPPLÉMENTAIRES
    // ==========================================

    /**
     * Récupérer une bourse par ID (sans actions)
     */
    public Bourse getById(int id) {
        Bourse bourse = null;
        String req = "SELECT * FROM bourse WHERE id_bourse=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    bourse = mapBourse(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById : " + e.getMessage());
            e.printStackTrace();
        }

        return bourse;
    }

    /**
     * Rechercher des bourses par nom ou pays
     */
    public List<Bourse> searchByName(String keyword) {
        List<Bourse> bourses = new ArrayList<>();
        String req = "SELECT * FROM bourse WHERE nom_bourse LIKE ? OR pays LIKE ? ORDER BY nom_bourse";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            String pattern = "%" + keyword + "%";
            pst.setString(1, pattern);
            pst.setString(2, pattern);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    bourses.add(mapBourse(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur searchByName : " + e.getMessage());
            e.printStackTrace();
        }

        return bourses;
    }

    /**
     * ⭐ Récupérer une bourse par ID + charger toutes ses actions
     * (Remplit bourse.getActions() et synchronise action.setBourse(bourse))
     */
    public Bourse getByIdWithActions(int idBourse) {
        Bourse bourse = null;

        // 1) Charger la bourse
        String reqBourse = "SELECT * FROM bourse WHERE id_bourse=?";

        try (PreparedStatement pst = connection.prepareStatement(reqBourse)) {
            pst.setInt(1, idBourse);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    bourse = mapBourse(rs);
                } else {
                    return null;
                }
            }

            // 2) Charger les actions liées à cette bourse
            String reqActions = "SELECT * FROM action WHERE id_bourse=? ORDER BY symbole";

            try (PreparedStatement pst2 = connection.prepareStatement(reqActions)) {
                pst2.setInt(1, idBourse);

                try (ResultSet rs2 = pst2.executeQuery()) {
                    while (rs2.next()) {
                        Action action = new Action();
                        action.setIdAction(rs2.getInt("id_action"));
                        action.setIdBourse(rs2.getInt("id_bourse"));
                        action.setSymbole(rs2.getString("symbole"));
                        action.setNomEntreprise(rs2.getString("nom_entreprise"));
                        action.setSecteur(rs2.getString("secteur"));
                        action.setPrixUnitaire(rs2.getDouble("prix_unitaire"));
                        action.setQuantiteDisponible(rs2.getInt("quantite_disponible"));
                        action.setDateAjout(rs2.getTimestamp("date_ajout"));
                        action.setStatut(rs2.getString("statut"));

                        // ✅ Synchronisation bidirectionnelle : ajoute l'action à la bourse
                        // et met action.setBourse(bourse) automatiquement via addAction()
                        bourse.addAction(action);
                    }
                }
            }

            System.out.println("✅ Bourse chargée avec " + bourse.getActions().size() + " action(s)");

        } catch (SQLException e) {
            System.err.println("❌ Erreur getByIdWithActions : " + e.getMessage());
            e.printStackTrace();
        }

        return bourse;
    }

    // ==========================================
    // HELPERS (Mapping)
    // ==========================================
    private Bourse mapBourse(ResultSet rs) throws SQLException {
        Bourse b = new Bourse();
        b.setIdBourse(rs.getInt("id_bourse"));
        b.setNomBourse(rs.getString("nom_bourse"));
        b.setPays(rs.getString("pays"));
        b.setDevise(rs.getString("devise"));
        b.setStatut(rs.getString("statut"));
        b.setDateCreation(rs.getTimestamp("date_creation"));
        // b.getActions() est déjà initialisé dans le constructeur
        return b;
    }
}
