package dev.civmc.bumhug.hacks

import dev.civmc.bumhug.Bumhug
import dev.civmc.bumhug.Hack
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.time.LocalDateTime
import java.util.logging.Level
import kotlin.math.roundToLong

class IntervalAnnouncement: Hack() {
    override val configName = "intervalAnnouncement"
    override val prettyName = "Interval Announcements"

    companion object {
        const val HOURS_PER_DAY = 24
        const val MINUTES_PER_HOUR = 60
        const val SECONDS_PER_MINUTE = 60
    }

    private var announcements = HashSet<Announcement>()

    data class Announcement(
            /**
             * The key of the announcement in the config
             */
            val key: String,

            /**
             * How often, in seconds, the announcement should be made
             */
            val interval: Long,

            /**
             * The content of the announcement
             */
            val message: String,

            /**
             * The permission the announcement should be broadcasted to
             */
            val permission: String,

            /**
             * The last time the announcement was made.
             */
            var lastAnnouncementTime: LocalDateTime
    )

    init {
        // parse all the announcements from the config
        for (key in config.getConfigurationSection("announcements")!!.getKeys(false)) {
            val configSection = config.getConfigurationSection("announcements.$key")!!

            val message = ChatColor.translateAlternateColorCodes('&', configSection.getString("message")!!)
            val permission = configSection.getString("permission", "bumhug.intervalannouncement.everyone")!!

            val intervalDays = configSection.getDouble("intervalDays", 0.0)
            val intervalHours = configSection.getDouble("intervalHours", 0.0)
            val intervalMinutes = configSection.getDouble("intervalMinutes", 0.0)
            val intervalSeconds = configSection.getDouble("intervalSeconds", 0.0)

            if (intervalDays == 0.0 &&
                    intervalHours == 0.0 &&
                    intervalMinutes == 0.0 &&
                    intervalSeconds == 0.0) {
                Bumhug.instance.logger.log(Level.WARNING,
                        "all intervals of interval message '$key' are 0, skipping")
            }

            val seconds = intervalSeconds +
                    intervalMinutes * SECONDS_PER_MINUTE +
                    intervalHours * MINUTES_PER_HOUR * SECONDS_PER_MINUTE +
                    intervalDays * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE

            announcements.add(Announcement(
                    key = key,
                    interval = seconds.roundToLong(),
                    message = message,
                    permission = permission,
                    lastAnnouncementTime = LocalDateTime.now()
            ))
        }

        // start the tack to make the announcements
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Bumhug.instance, this::makeAnnouncementsAsNeeded, 1, 20)
    }

    /**
     * Scans over the announcements set, and if any of the announcements are due to be made, makes those announcements.
     */
    fun makeAnnouncementsAsNeeded() {
        for (announcement in announcements) {
            // if the announcement is due to be made
            if (announcement.lastAnnouncementTime
                            .plusSeconds(announcement.interval)
                            .isBefore(LocalDateTime.now())) {
                // make the announcement
                Bukkit.broadcast(announcement.message, announcement.permission)

                // update time
                announcement.lastAnnouncementTime = LocalDateTime.now()
            }
        }
    }
}