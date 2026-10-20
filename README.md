# spaceport_manager
FISA4 - Java Project

Application Swing de gestion d'un spatioport (fictif) : **vols**, **passagers** et **bagages**, stockés dans MySQL.

## Prérequis

- JDK 25
- Maven 3.9+
- Un serveur MySQL 8 (par défaut sur `localhost:3306`)

## Lancement

```bash
mvn clean package
java -jar target/spaceport_manager-1.0-SNAPSHOT.jar
```

Au premier lancement, une fenêtre demande l'URL JDBC, l'utilisateur et le mot de passe MySQL.
Ces paramètres sont enregistrés dans `db.properties` (ignoré par git) et modifiables ensuite via
**Fichier > Connexion à la base...**.

La base `spaceport_manager` et ses tables sont créées automatiquement
(`src/main/resources/sql/schema.sql`), et des données de démonstration sont insérées
si la base est vide (`src/main/resources/sql/seed.sql`).

## Connexion à l'application

Un écran de connexion vérifie l'identifiant et le mot de passe dans la table `app_user`
(mots de passe hachés en SHA-256 avec un sel). Au premier lancement, le compte **admin / admin**
est créé ; d'autres comptes peuvent être ajoutés via **Fichier > Nouvel utilisateur...**.
L'application se ferme après trois tentatives échouées.

## Fonctionnalités

- **Vols** : code, vaisseau, origine, destination, horaires, quai, capacité, statut.
- **Passagers** : identité, planète d'origine, passeport, email et vol affecté.
- **Bagages** : étiquette, type (cabine, soute, cryogénique, matière dangereuse), poids, propriétaire.
- Ajout / modification (double-clic) / suppression (touche Suppr), tri par colonne, recherche.
- Vols : changement rapide du statut et liste des passagers d'un vol.
- Export de l'onglet affiché en CSV (**Fichier > Exporter l'onglet en CSV...**).
- Thème clair (IntelliJ) ou sombre (Darcula), couleur d'accent et barre d'état dans le menu **Affichage**.

### Règles de gestion

- Code de vol, numéro de passeport et étiquette de bagage sont uniques.
- L'arrivée d'un vol doit suivre son départ, et sa capacité ne peut pas descendre sous le nombre de passagers.
- Un passager ne peut être affecté qu'à un vol non complet et encore ouvert (programmé, en embarquement ou retardé).
- Le poids d'un bagage est limité selon son type (cabine 12 kg, soute 32 kg, cryogénique 80 kg, dangereux 25 kg).
- Supprimer un vol désaffecte ses passagers ; supprimer un passager supprime ses bagages.

## Correspondance avec le sujet

| Consigne | Où la trouver |
|---|---|
| Table d'authentification (login, mot de passe) | `app_user`, `UserDao`, `LoginDialog` |
| Table(s) de formulaire d'enregistrement | `flight`, `passenger`, `baggage` |
| Connexion JDBC | `Database`, paquet `dao` |
| Boutons Ajouter / Modifier / Supprimer | `EntityPanel` (commun aux trois onglets) |
| Classes, attributs, constructeurs, objets | paquets `model`, `dao`, `ui` |
| Encapsulation | attributs `private` (DAO, panneaux), records immuables du paquet `model` |
| Héritage et polymorphisme | `FlightPanel` / `PassengerPanel` / `BaggagePanel` héritent de `EntityPanel` ; les DAO sont manipulés via `Dao<T>` |
| Classes abstraites et interfaces | `EntityPanel`, `FormDialog` (abstraites) ; `Dao`, `Identifiable` (interfaces) |
| Collections | `List`, `Map`/`EnumMap`, streams (`FlightPanel.showPassengers`) |
| Exceptions | `DataAccessException`, `ValidationException`, `SQLException`, `IOException` |
| `JButton`, `JLabel`, `JTextField`, `JPanel`, `JScrollPane`, `JComboBox` | tous les formulaires |
| `JTextArea` | description d'un bagage (`BaggageDialog`) |
| `JCheckBox` | 'Fragile' (`BaggageDialog`), 'Afficher le mot de passe' (`LoginDialog`) |
| `JRadioButton` + `ButtonGroup` | type de bagage (`BaggageDialog`) |
| `ImageIcon` | logo de `LoginDialog`, icône des fenêtres, 'À propos' |
| `JFileChooser` | export CSV (`MainFrame.exportCurrentTab`) |
| `JColorChooser` | couleur d'accent (`MainFrame.chooseAccentColor`) |
| `JMenuBar`, `JMenu`, `JMenuItem` | menus de `MainFrame` |
| `JCheckBoxMenuItem` | Affichage > Barre d'état |
| `JRadioButtonMenuItem` | Affichage > Thème clair / sombre |
| `JList` | Vols > Passagers du vol |
| `showMessageDialog` | messages d'information et d'erreur (`Dialogs`) |
| `showConfirmDialog` | confirmation de suppression, paramètres de connexion |
| `showInputDialog` | Vols > Changer le statut, Fichier > Nouvel utilisateur |

## Structure

```
fr.antoma.spaceport_manager
├── Main            point d'entrée, connexion à la base
├── db              configuration et initialisation de la base
├── model           entités (Flight, Passenger, Baggage, User) et énumérations
├── dao             accès aux données en JDBC (interface Dao<T>)
└── ui              interface Swing (connexion, un onglet et un formulaire par entité)
```
