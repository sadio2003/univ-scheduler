package com.univ.scheduler.db;

import com.univ.scheduler.model.Batiment;
import com.univ.scheduler.model.Equipement;
import com.univ.scheduler.model.Salle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des salles
 */
public class SalleDAO implements DAO<Salle> {

    private Connection connection;
    private BatimentDAO batimentDAO;
    private EquipementDAO equipementDAO;

    public SalleDAO() {
        this.connection = DBConnection.getConnection();
        this.batimentDAO = new BatimentDAO();
        this.equipementDAO = new EquipementDAO();
    }

    @Override
    public Salle getById(int id) {
        String sql = "SELECT s.*, b.nom as batiment_nom, b.localisation as batiment_localisation " +
                "FROM Salle s " +
                "LEFT JOIN Batiment b ON s.id_batiment = b.id " +
                "WHERE s.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractSalleWithRelations(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Salle> getAll() {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT s.*, b.nom as batiment_nom, b.localisation as batiment_localisation " +
                "FROM Salle s " +
                "LEFT JOIN Batiment b ON s.id_batiment = b.id " +
                "ORDER BY s.numero";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                salles.add(extractSalleWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salles;
    }

    @Override
    public boolean insert(Salle salle) {
        String sql = "INSERT INTO Salle (numero, capacite, type, id_batiment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, salle.getNumero());
            stmt.setInt(2, salle.getCapacite());
            stmt.setString(3, salle.getType());
            stmt.setInt(4, salle.getIdBatiment());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    salle.setId(rs.getInt(1));
                }

                // Ajouter les équipements
                insertEquipementsSalle(salle);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Salle salle) {
        String sql = "UPDATE Salle SET numero = ?, capacite = ?, type = ?, id_batiment = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, salle.getNumero());
            stmt.setInt(2, salle.getCapacite());
            stmt.setString(3, salle.getType());
            stmt.setInt(4, salle.getIdBatiment());
            stmt.setInt(5, salle.getId());

            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                // Mettre à jour les équipements
                deleteEquipementsSalle(salle.getId());
                insertEquipementsSalle(salle);
            }
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        try {
            // Supprimer d'abord les liaisons avec les équipements
            deleteEquipementsSalle(id);

            // Puis supprimer la salle
            String sql = "DELETE FROM Salle WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recherche avancée de salles
     */
    public List<Salle> rechercherSalles(Integer capaciteMin, String type, List<Integer> equipementsIds) {
        List<Salle> salles = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT s.*, b.nom as batiment_nom, b.localisation as batiment_localisation " +
                        "FROM Salle s " +
                        "LEFT JOIN Batiment b ON s.id_batiment = b.id "
        );

        List<Object> params = new ArrayList<>();

        if (equipementsIds != null && !equipementsIds.isEmpty()) {
            sql.append("JOIN Salle_Equipement se ON s.id = se.id_salle ");
        }

        sql.append("WHERE 1=1 ");

        if (capaciteMin != null && capaciteMin > 0) {
            sql.append("AND s.capacite >= ? ");
            params.add(capaciteMin);
        }

        if (type != null && !type.isEmpty() && !"Tous".equals(type)) {
            sql.append("AND s.type = ? ");
            params.add(type);
        }

        if (equipementsIds != null && !equipementsIds.isEmpty()) {
            sql.append("AND se.id_equipement IN (");
            for (int i = 0; i < equipementsIds.size(); i++) {
                sql.append("?");
                if (i < equipementsIds.size() - 1) sql.append(",");
            }
            sql.append(") ");

            sql.append("GROUP BY s.id HAVING COUNT(DISTINCT se.id_equipement) = ?");
            params.addAll(equipementsIds);
            params.add(equipementsIds.size());
        }

        sql.append(" ORDER BY s.numero");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) params.get(i));
                } else if (params.get(i) instanceof String) {
                    stmt.setString(i + 1, (String) params.get(i));
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                salles.add(extractSalleWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salles;
    }

    /**
     * Récupère les salles par bâtiment
     */
    public List<Salle> getByBatiment(int batimentId) {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT s.*, b.nom as batiment_nom, b.localisation as batiment_localisation " +
                "FROM Salle s " +
                "LEFT JOIN Batiment b ON s.id_batiment = b.id " +
                "WHERE s.id_batiment = ? " +
                "ORDER BY s.numero";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, batimentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                salles.add(extractSalleWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salles;
    }

    /**
     * Récupère les salles par type
     */
    public List<Salle> getByType(String type) {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT s.*, b.nom as batiment_nom, b.localisation as batiment_localisation " +
                "FROM Salle s " +
                "LEFT JOIN Batiment b ON s.id_batiment = b.id " +
                "WHERE s.type = ? " +
                "ORDER BY s.numero";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                salles.add(extractSalleWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salles;
    }

    /**
     * Insère les équipements d'une salle
     */
    private void insertEquipementsSalle(Salle salle) throws SQLException {
        if (salle.getEquipements() == null || salle.getEquipements().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO Salle_Equipement (id_salle, id_equipement) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Equipement e : salle.getEquipements()) {
                stmt.setInt(1, salle.getId());
                stmt.setInt(2, e.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Supprime les équipements d'une salle
     */
    private void deleteEquipementsSalle(int salleId) throws SQLException {
        String sql = "DELETE FROM Salle_Equipement WHERE id_salle = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            stmt.executeUpdate();
        }
    }

    /**
     * Extrait une salle avec ses relations (bâtiment, équipements)
     */
    private Salle extractSalleWithRelations(ResultSet rs) throws SQLException {
        Salle salle = new Salle();
        salle.setId(rs.getInt("id"));
        salle.setNumero(rs.getString("numero"));
        salle.setCapacite(rs.getInt("capacite"));
        salle.setType(rs.getString("type"));
        salle.setIdBatiment(rs.getInt("id_batiment"));

        // Récupérer le bâtiment
        Batiment batiment = new Batiment();
        batiment.setId(rs.getInt("id_batiment"));
        batiment.setNom(rs.getString("batiment_nom"));
        batiment.setLocalisation(rs.getString("batiment_localisation"));
        salle.setBatiment(batiment);

        // Récupérer les équipements
        List<Equipement> equipements = equipementDAO.getEquipementsBySalle(salle.getId());
        salle.setEquipements(equipements);

        return salle;
    }
}