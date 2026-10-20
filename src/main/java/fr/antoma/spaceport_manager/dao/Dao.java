package fr.antoma.spaceport_manager.dao;

import fr.antoma.spaceport_manager.model.Identifiable;

import java.util.List;

/**
 * Opérations d'accès aux données communes à toutes les entités (lecture, enregistrement, suppression).
 *
 * @param <T> type de l'entité gérée
 */
public interface Dao<T extends Identifiable> {

    /** Renvoie toutes les lignes de la table. */
    List<T> findAll();

    /** Crée l'objet s'il n'a pas d'identifiant, le met à jour sinon. */
    void save(T item);

    /** Supprime la ligne portant cet identifiant. */
    void delete(int id);
}
