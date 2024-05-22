### AssaultPlugin

AssaultPlugin is a versatile plugin designed for factions servers, providing a comprehensive set of features to enhance faction-based combat experiences. 

#### Main Features

- ⚔️ Faction Assault System: Facilitates faction-based assaults using SaberFactions integration.
- ⏳ Cooldown System: Implements a cooldown mechanism preventing consecutive assaults by the same faction.
- ➕ Join Assault System : You can join an assault of other faction if you are ally with this faction.
- ⛔ Item/ Blocks Restrict : You can restrict item/ block interact, block place and block break in assault or excluding assault
- 🏆 Faction Ranking: Ranks factions based on elo, wins, and losses.
- 📊 Scoreboard Integration: Dynamic scoreboard updates during assaults.
- ⚙️ Fully Configurable: Highly customizable plugin settings to tailor the experience to your server's needs.
- 🔧 Admin Commands: Admin commands for resetting cooldowns and stopping ongoing assaults.

#### Todo List

- 🛡️ Custom Player Titles: Introduce the ability to customize player titles for attackers/defenders.
- 🪦 Anti Deconnexion System : Introduce new system for count kill in score if player who already tagged in the assault disconnecting.
- 🕰️ Cooldown before pvp : Introduce new system for add period without pvp on assault start for let players prepare.

#### Commands

- `/assault help`: Show this help message.
  - Displays detailed help on using AssaultPlugin commands.
- `/assault list`: List ongoing assaults.
  - Displays a list of ongoing assaults on the server.
- `/assault <faction>`: Initiate an assault on a faction.
  - Initiates an assault on the specified faction.
- `/assault admin`: Administration commands.
  - Displays administration commands for managing assaults.
- `/assault ranking`: Faction ranking based on assault victories.
  - Displays faction ranking based on the number of assault victories.
- `/assault join <faction>`: Join assault of other faction.
  - Allow you to join assault in progress with other faction.
- `/assault accept <faction>`: Accept join request of other faction.
  - Allow you to accept join request of other faction for help you in your assault.

#### Requirements

- **Faction Plugin:** [SaberFactions](https://github.com/SaberLLC/Saber-Factions)
- **Plugin Version:** 1.8 to 1.12.2

#### Permissions

- `assault.admin`: Permission to access admin commands.
- `assault.updates`: Permission to receive update notifications.
- `assault.bypass-restricted.items_interact` : Permission to bypass item interact restriction
- `assault.bypass-restricted.block_interact` : Permission to bypass block interact restriction
- `assault.bypass-restricted.block-place` : Permission to bypass block place restriction
- `assault.bypass-restricted.block-break` : Permission to bypass block break restriction

#### Contribution and Support

I am open to any proposals or bug reports. Please feel free to create an issue on GitHub or join our Discord server: [AssaultPlugin Discord](https://discord.gg/GR5xdzvkXc).

---

### AssaultPlugin (French Version)

AssaultPlugin est un plugin polyvalent conçu pour les serveurs de factions, offrant un ensemble complet de fonctionnalités pour améliorer les expériences de combat entre factions.

#### Principales fonctionnalités

- ⚔️ Système d'assaut entre factions : Facilite les assauts entre factions en utilisant l'intégration de SaberFactions.
- ⏳ Système de cooldown : Implémente un mécanisme de cooldown empêchant les assauts consécutifs par la même faction.
- ➕ Système de join assaut : Vous pouvez rejoindre les assauts des autres factions si vous êtes allié avec elle.
- ⛔ Restriction de blocs/ items : Vous pouvez restreindre l'utilisation de blocs/ items, le cassage de bloc et d'items en assaut/ hors assaut.
- 🏆 Classement des factions : Classe les factions en fonction de l'ELO, des victoires et des défaites.
- 📊 Intégration du tableau de bord : Mises à jour dynamiques du tableau de bord pendant les assauts.
- ⚙️ Entièrement configurable : Paramètres du plugin hautement personnalisables pour adapter l'expérience aux besoins de votre serveur.
- 🔧 Commandes d'administration : Commandes d'administration pour réinitialiser les cooldowns et arrêter les assauts en cours.

#### Liste des tâches à accomplir

- 🛡️ Titres personnalisés des joueurs : Introduire la possibilité de personnaliser les titres des joueurs pour les attaquants/défenseurs.
- 🪦 Système anti deconnexion : Introduire un système permettant de compter un kill dans le score si un joueur ayant déjà été tag dans l'assaut se deconnecte
- 🕰️ Cooldown avant pvp : Introduire un système permettant d'ajouter une periode sans pvp lors du début d'un assaut pour laisser le temps aux joueurs de se préparer.

#### Commandes

- `/assault help` : Affiche cette aide.
  - Affiche une aide détaillée sur l'utilisation des commandes d'AssaultPlugin.
- `/assault list` : Liste les assauts en cours.
  - Affiche une liste des assauts en cours sur le serveur.
- `/assault <faction>` : Lance un assaut sur une faction.
  - Lance un assaut sur la faction spécifiée.
- `/assault admin` : Commandes d'administration.
  - Affiche les commandes d'administration pour gérer les assauts.
- `/assault ranking` : Classement des factions ayant gagné le plus d'assaut.
  - Affiche le classement des factions basé sur le nombre d'assauts gagnés.
- `/assault join <faction>`: Rejoindre l'assaut d'une autre faction.
  - Vous permet de rejoindre un assaut en cours avec une autre faction.
- `/assault accept <faction>`: Accepter une demande de join d'une autre faction.
  - Vous permet d'accepter la requête de join d'une autre faction pour qu'elle vienne vous aider.

#### Prérequis

- **Plugin Faction :** [SaberFactions](https://github.com/SaberLLC/Saber-Factions)
- **Version du Plugin :** 1.8 à 1.12.2

#### Permissions

- `assault.admin` : Permission pour accéder aux commandes d'administration.
- `assault.updates` : Permission pour recevoir les notifications de mises à jour.
- `assault.bypass-restricted.items_interact` : Permission pour bypass la restriction d'interaction avec les items
- `assault.bypass-restricted.block_interact` : Permission pour bypass la restriction d'interaction avec les blocs
- `assault.bypass-restricted.block-place` : Permission pour bypass la restriction de la pose de blocs
- `assault.bypass-restricted.block-break` : Permission pour bypass la restriction du cassage de blocs

#### Contribution et Support

Je suis ouvert à toutes propositions ou rapports de bugs. N'hésitez pas à créer un problème sur GitHub ou à rejoindre notre serveur Discord : [AssaultPlugin Discord](https://discord.gg/GR5xdzvkXc).
