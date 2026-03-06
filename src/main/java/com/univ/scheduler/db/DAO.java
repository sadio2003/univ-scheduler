package com.univ.scheduler.db;

import java.util.List;

/**
 * Interface générique pour les DAO (Data Access Object)
 * Définit les opérations CRUD de base
 * @param <T> Le type d'objet géré par le DAO
 */
public interface DAO<T> {

    /**
     * Récupère un objet par son ID
     * @param id L'identifiant de l'objet
     * @return L'objet trouvé ou null
     */
    T getById(int id);

    /**
     * Récupère tous les objets
     * @return Liste de tous les objets
     */
    List<T> getAll();

    /**
     * Insère un nouvel objet
     * @param obj L'objet à insérer
     * @return true si l'insertion a réussi
     */
    boolean insert(T obj);

    /**
     * Met à jour un objet existant
     * @param obj L'objet à mettre à jour
     * @return true si la mise à jour a réussi
     */
    boolean update(T obj);

    /**
     * Supprime un objet par son ID
     * @param id L'identifiant de l'objet à supprimer
     * @return true si la suppression a réussi
     */
    boolean delete(int id);

    /**
     * Compte le nombre total d'objets
     * @return Le nombre d'objets
     */
    default int count() {
        return getAll().size();
    }

    /**
     * Vérifie si un objet existe
     * @param id L'identifiant à vérifier
     * @return true si l'objet existe
     */
    default boolean exists(int id) {
        return getById(id) != null;
    }
}