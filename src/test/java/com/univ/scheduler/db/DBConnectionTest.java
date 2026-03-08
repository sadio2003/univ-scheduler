package com.univ.scheduler.db;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la connexion à la base de données
 * Vérifie que la connexion MySQL fonctionne correctement
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBConnectionTest {

    private static Connection connection;

    @BeforeAll
    static void setUpBeforeAll() {
        System.out.println("🚀 Démarrage des tests de connexion à la base de données...");
    }

    @BeforeEach
    void setUp() {
        connection = DBConnection.getConnection();
    }

    @AfterEach
    void tearDown() {
        // Ne pas fermer la connexion ici pour éviter de fermer le singleton
    }

    @AfterAll
    static void tearDownAfterAll() {
        DBConnection.closeConnection();
        System.out.println("✅ Tests de connexion terminés");
    }

    // ==================== TESTS DE CONNEXION ====================

    @Test
    @Order(1)
    @DisplayName("Test de connexion à la base de données")
    void testConnexion() {
        assertNotNull(connection, "La connexion ne devrait pas être null");
        try {
            assertFalse(connection.isClosed(), "La connexion devrait être ouverte");
            System.out.println("✅ Connexion établie avec succès");
        } catch (SQLException e) {
            fail("Erreur lors de la vérification de la connexion: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test de la méthode testConnection()")
    void testTestConnection() {
        assertTrue(DBConnection.testConnection(), "testConnection() devrait retourner true");
    }

    @Test
    @Order(3)
    @DisplayName("Test du singleton - même instance de connexion")
    void testSingleton() {
        Connection conn1 = DBConnection.getConnection();
        Connection conn2 = DBConnection.getConnection();
        assertSame(conn1, conn2, "Les deux appels devraient retourner la même instance");
    }

    // ==================== TESTS DE REQUÊTES SIMPLES ====================

    @Test
    @Order(4)
    @DisplayName("Test d'exécution d'une requête simple")
    void testRequeteSimple() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT 1 as test");
            assertTrue(rs.next(), "La requête devrait retourner un résultat");
            int result = rs.getInt("test");
            assertEquals(1, result, "Le résultat devrait être 1");
            System.out.println("✅ Requête simple exécutée avec succès");
        } catch (SQLException e) {
            fail("Erreur lors de l'exécution de la requête: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Test de récupération de la version MySQL")
    void testMySQLVersion() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT VERSION() as version");
            assertTrue(rs.next(), "Devrait récupérer la version");
            String version = rs.getString("version");
            assertNotNull(version, "La version ne devrait pas être null");
            System.out.println("✅ Version MySQL: " + version);
        } catch (SQLException e) {
            fail("Erreur lors de la récupération de la version: " + e.getMessage());
        }
    }

    // ==================== TESTS DE LA BASE DE DONNÉES ====================

    @Test
    @Order(6)
    @DisplayName("Test que la base de données univ_scheduler existe")
    void testBaseDonneesExiste() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA " +
                            "WHERE SCHEMA_NAME = 'univ_scheduler'"
            );
            boolean exists = rs.next();
            assertTrue(exists, "La base de données 'univ_scheduler' devrait exister");
            if (exists) {
                System.out.println("✅ Base de données 'univ_scheduler' trouvée");
            }
        } catch (SQLException e) {
            fail("Erreur lors de la vérification de la base: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    @DisplayName("Test que toutes les tables existent")
    void testTablesExistent() {
        String[] tables = {
                "Utilisateur", "Batiment", "Equipement",
                "Salle", "Salle_Equipement", "Cours", "Seance"
        };
        try (Statement stmt = connection.createStatement()) {
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) as cnt FROM information_schema.tables " +
                                "WHERE table_schema = 'univ_scheduler' AND table_name = '" + table + "'"
                );
                rs.next();
                int count = rs.getInt("cnt");
                assertEquals(1, count, "La table '" + table + "' devrait exister");
                System.out.println("✅ Table '" + table + "' trouvée");
            }
        } catch (SQLException e) {
            fail("Erreur lors de la vérification des tables: " + e.getMessage());
        }
    }

    // ==================== TESTS DES CONTRAINTES ====================

    @Test
    @Order(8)
    @DisplayName("Test des contraintes d'intégrité référentielle")
    void testContraintes() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as cnt FROM information_schema.table_constraints " +
                            "WHERE constraint_schema = 'univ_scheduler' AND constraint_type = 'FOREIGN KEY'"
            );
            rs.next();
            int nbForeignKeys = rs.getInt("cnt");
            assertTrue(nbForeignKeys > 0, "Il devrait y avoir des clés étrangères");
            System.out.println("✅ " + nbForeignKeys + " contraintes de clé étrangère trouvées");
        } catch (SQLException e) {
            fail("Erreur lors de la vérification des contraintes: " + e.getMessage());
        }
    }

    // ==================== TESTS DES DONNÉES DE TEST ====================

    @Test
    @Order(9)
    @DisplayName("Test que l'utilisateur admin existe")
    void testAdminExiste() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as cnt FROM Utilisateur WHERE email = 'admin@uidt.sn'"
            );
            rs.next();
            int count = rs.getInt("cnt");
            assertTrue(count > 0, "L'utilisateur admin@uidt.sn devrait exister");
            System.out.println("✅ Utilisateur admin trouvé");
        } catch (SQLException e) {
            fail("Erreur lors de la recherche de l'admin: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @DisplayName("Test du nombre d'utilisateurs")
    void testNombreUtilisateurs() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM Utilisateur");
            rs.next();
            int count = rs.getInt("cnt");
            assertTrue(count >= 1, "Il devrait y avoir au moins 1 utilisateur");
            System.out.println("✅ " + count + " utilisateur(s) dans la base");
        } catch (SQLException e) {
            fail("Erreur lors du comptage des utilisateurs: " + e.getMessage());
        }
    }

    // ==================== TESTS DE PERFORMANCE ====================

    @Test
    @Order(11)
    @DisplayName("Test de performance - requête simple")
    void testPerformance() {
        long startTime = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement()) {
            for (int i = 0; i < 10; i++) {
                stmt.executeQuery("SELECT 1");
            }
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            assertTrue(duration < 5000, "Les 10 requêtes devraient prendre moins de 5 secondes");
            System.out.println("✅ Performance: " + duration + "ms pour 10 requêtes");
        } catch (SQLException e) {
            fail("Erreur lors du test de performance: " + e.getMessage());
        }
    }

    // ==================== TESTS DE TRANSACTION ====================

    @Test
    @Order(12)
    @DisplayName("Test de transaction - commit/rollback")
    void testTransaction() {
        try {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(
                        "INSERT INTO Utilisateur (nom, email, mot_de_passe, role) " +
                                "VALUES ('Test User', 'test@test.com', 'password', 'Etudiant')"
                );
                ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) as cnt FROM Utilisateur WHERE email = 'test@test.com'"
                );
                rs.next();
                assertEquals(1, rs.getInt("cnt"), "L'utilisateur devrait être inséré");
                connection.rollback();
                rs = stmt.executeQuery(
                        "SELECT COUNT(*) as cnt FROM Utilisateur WHERE email = 'test@test.com'"
                );
                rs.next();
                assertEquals(0, rs.getInt("cnt"), "L'utilisateur devrait avoir été rollback");
                System.out.println("✅ Transaction rollback réussie");
            }
        } catch (SQLException e) {
            fail("Erreur lors du test de transaction: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ==================== TESTS DE FERMETURE ====================

    @Test
    @Order(13)
    @DisplayName("Test de fermeture et réouverture de connexion")
    void testFermeture() {
        // Fermer la connexion
        DBConnection.closeConnection();

        // Vérifier qu'on peut obtenir une nouvelle connexion
        Connection newConn = DBConnection.getConnection();
        assertNotNull(newConn, "Une nouvelle connexion devrait être créée");

        try {
            assertFalse(newConn.isClosed(), "La nouvelle connexion devrait être ouverte");
            System.out.println("✅ Fermeture et réouverture de connexion réussies");
        } catch (SQLException e) {
            fail("Erreur: " + e.getMessage());
        }
    }
}