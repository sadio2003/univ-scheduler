package com.univ.scheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de connexion à la base de données MySQL
 * Pattern Singleton pour une seule connexion
 */
public class DBConnection {

    // Paramètres de connexion
    private static final String URL = "jdbc:mysql://localhost:3306/univ_scheduler?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = "sadio";

    private static Connection connection = null;

    /**
     * Constructeur privé pour empêcher l'instanciation
     */
    private DBConnection() {}

    /**
     * Obtient la connexion à la base de données (Singleton)
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);
                System.out.println("✅ Connexion à MySQL établie avec succès");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé !");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à MySQL !");
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Teste la connexion
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si la connexion est fermée
     */
    public static boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * Ferme la connexion
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("🔌 Connexion MySQL fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion");
                e.printStackTrace();
            }
        }
    }
}