package fr.antoma.spaceport_manager.model;

public record User(Integer id, String login) implements Identifiable {
}
