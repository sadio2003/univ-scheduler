package com.univ.scheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de connexion à la base de données MySQL
 * Pattern Singleton pour une seule connexion
 */
public class DBConnection {

    // Paramètres de connexion - À MODIFIER selon votre configuration
    private static final String URL = "jdbc:mysql://localhost:3306/univ_scheduler?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String UTILISATEUR = "root"; // Votre utilisateur MySQL
    private static final String MOT_DE_PASSE = "sadio";    // ✅ Votre mot de passe MySQL

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
                // Charger le driver MySQL (optionnel depuis JDBC 4.0)
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir la connexion
                connection = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);
                System.out.println("✅ Connexion à MySQL établie avec succès");

            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé !");
                System.err.println("Vérifiez que mysql-connector-j est dans le pom.xml");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à MySQL !");
                System.err.println("Vérifiez que :");
                System.err.println("1. MySQL est bien lancé (XAMPP/WAMP/MySQL Server)");
                System.err.println("2. L'URL, l'utilisateur et le mot de passe sont corrects");
                System.err.println("3. La base de données 'univ_scheduler' existe");
                System.err.println("4. Le port 3306 n'est pas bloqué");
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