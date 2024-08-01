[![](https://jitpack.io/v/HunterTagOG/CoinSystem.svg)](https://jitpack.io/#HunterTagOG/CoinSystem)

CoinSystem ist ein Minecraft-Plugin, das eine API zur Verwaltung von Spieler-Coins bereitstellt. Es bietet eine einfache Möglichkeit, Coins zu speichern, zu aktualisieren und Transaktionen zu protokollieren.

## Funktionen

- Unterstützung für Bukkit/Spigot/Paper (1.20.x)
- Einfach zu verwendende API für Coins
- Unterstützung für MySQL-Datenbanken mit HikariCP-Verbindungspool
- Automatische Protokollierung von Transaktionen
- Regelmäßige Backups von Spielerdaten
- Konfigurierbare Startbalance und Backup-Intervalle

## Importing

Wir verwenden JitPack, um die neueste Version von CoinSystem automatisch zu kompilieren und zu hosten. Um CoinSystem mit Maven zu installieren, öffnen Sie Ihre `pom.xml`, suchen Sie den `<repositories>` Abschnitt und fügen Sie dieses Repository hinzu:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Suchen Sie dann den `<dependencies>` Abschnitt Ihrer pom.xml und fügen Sie Folgendes hinzu. Ersetzen Sie "REPLACE_WITH_LATEST_VERSION" durch die neueste Version von: [Github](https://github.com/HunterTagOG/CoinSystem/releases)

```xml
<dependency>
    <groupId>com.github.HunterTagOG</groupId>
    <artifactId>CoinSystem</artifactId>
    <version>REPLACE_WITH_LATEST_VERSION</version>
    <scope>provided</scope>
</dependency>
```

Für weitere Informationen, einschließlich der Verwendung von CoinSystem mit anderen Tools als Maven, besuchen Sie bitte: [Jitpack](https://jitpack.io/#HunterTagOG/CoinSystem/)

## Shading (wichtig!)
Siehe Schritt 2 im Quick Start Guide oben.

CoinSystem kommt mit einigen Bibliotheken, die für Sie verfügbar sind, z.B. HikariCP, um sicherzustellen, dass sie beim Codieren zugänglich sind, aber nicht als Abhängigkeiten hinzugefügt werden müssen.

Maven hat eine Einschränkung, bei der diese Bibliotheken in Ihrer Plugin-JAR-Datei landen, wenn Sie die `<includes>` Sektion des maven-shade-plugin nicht richtig konfigurieren.

Kopieren Sie den folgenden Abschnitt und fügen Sie ihn in Ihren `<plugins>` Abschnitt der pom.xml ein (wenn Sie bereits einen solchen Abschnitt haben, entfernen Sie ihn).

Wenn Sie eine Abhängigkeit in Ihr Jar kompilieren möchten, installieren Sie sie normal über die `<dependency>` Direktive, setzen Sie ihren Scope auf "compile" und fügen Sie sie dann erneut hinzu. Sie können einfach die `<include>` duplizieren und für Ihre Abhängigkeit ändern.

### Wichtig

Nach dem erzeugen der jar achten sie darauf die jar mit folgendem kürzel `-shaded.jar` am Ende zu verwenden wie in folgendem Beispiel: `plugin-name-1.0-shaded.jar`

```xml
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.4</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <artifactSet>
                    <includes>
                        <include>de.huntertagog.locobroko.CoinSystem</include>
                    </includes>
                </artifactSet>
            </configuration>
        </plugin>

```


## Quick Start

1. Importieren Sie CoinSystem mit Maven oder Gradle (siehe Importing).
2. **WICHTIG**: Konfigurieren Sie das Shading, um nur CoinSystem und die benötigten Bibliotheken einzuschließen, damit nicht alle Abhängigkeiten in Ihr Jar aufgenommen werden.
3. Registrieren Sie die CoinAPI in Ihrem Plugin:
    ```java
    import de.huntertagog.locobroko.api.ICoinAPI;
    import de.huntertagog.locobroko.api.ICoinAPI;import org.bukkit.Bukkit;
    import org.bukkit.plugin.java.JavaPlugin;
    import java.util.UUID;

    public class MyPlugin extends JavaPlugin {
        @Override
        public void onEnable() {
            ICoinAPI coinAPI = Bukkit.getServicesManager().load(ICoinAPI.class);
            if (coinAPI != null) {
                // Verwenden Sie die API
                UUID playerUUID = ...; // Die UUID des Spielers
                int coins = coinAPI.getCoins(playerUUID);
                coinAPI.addCoins(playerUUID, 50);
            } else {
                getLogger().severe("CoinAPI not found!");
            }
        }
    }
    ```

## Template Plugin

Hier findet ihr ein Template Plugin wie ihr die API in eurem Plugin nutzen könnt: [Template Plugin](https://github.com/HunterTagOG/TemplateCoinSystemAPI?tab=readme-ov-file)


## Methoden

`int getCoins(UUID playerUUID)`
Gibt die Anzahl der Coins für einen bestimmten Spieler zurück.

`void addCoins(UUID playerUUID, int amount)`
Fügt eine bestimmte Anzahl Coins zum Guthaben eines Spielers hinzu.

`void removeCoins(UUID playerUUID, int amount)`
Entfernt eine bestimmte Anzahl Coins vom Guthaben eines Spielers.

`void setCoins(UUID playerUUID, int amount)`
Setzt das Guthaben eines Spielers auf eine bestimmte Anzahl Coins.

## Konfiguration
Die Konfiguration des CoinSystem-Plugins erfolgt über die config.yml-Datei. Hier ist ein Beispiel:

```yaml
# CoinSystem Configuration

# Database connection settings
database:
  # The JDBC URL of your MySQL database
  # Example: jdbc:mysql://localhost:3306/minecraft
  url: jdbc:mysql://localhost:3306/minecraft

  # The username for accessing your MySQL database
  user: yourDatabaseUser

  # The password for accessing your MySQL database
  password: yourDatabasePassword

# The currency used in chat messages for the CoinSystem plugin
prefix: ★ Stars

# The starting balance for new players
# This is the number of coins that new players will start with when they first join the server
starting_balance: 1000

# Backup settings
# Enable or disable regular backups of player coin balances
# Set to true to enable regular backups, false to disable
backup_enabled: true

# The interval for regular backups in minutes
# This determines how often the plugin will create a backup of player coin balances
# For example, setting this to 30 means a backup will be created every 30 minutes
backup_interval_minutes: 30

# Logging settings
# Enable or disable transaction logging
# Set to true to log all transactions, false to disable logging
logging_enabled: true
```

## Kompatibilität
Wir bemühen uns, eine breite Kompatibilität zu gewährleisten, die die folgenden Minecraft-Versionen unterstützt und weitere werden folgen:

1.20.x

CoinSystem funktioniert auf Bukkit, Spigot und Paper.

## Lizenzinformationen

© 2024 HUNTER DEVELOPMENT

Mit der MIT-Lizenz können Sie alles tun, was Sie möchten, solange Sie die ursprünglichen Urheberrechtsvermerke und Haftungsausschlüsse beibehalten. Eine Kopie der Lizenz finden Sie im Repository.
