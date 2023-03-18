package dot.reyn.dotplaceholders

import com.google.gson.GsonBuilder
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.util.Identifier
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class DotPlaceholders : ModInitializer {

    private lateinit var config: PlaceholderConfig
    private var oldKeys = mutableListOf<Identifier>()

    override fun onInitialize() {
        this.loadConfig()

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ -> this.loadConfig() }
    }

    /**
     * Loads the configuration file.
     * If the config does not exist, it will be created.
     */
    private fun loadConfig() {
        oldKeys.forEach(Placeholders::remove)
        oldKeys.clear()

        val configDir = File("./config/")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        val configFile = File(configDir, "dotplaceholders.json")
        val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

        if (!configFile.exists()) {
            this.config = PlaceholderConfig()
            val fileWriter = FileWriter(configFile, Charsets.UTF_8)

            gson.toJson(this.config, fileWriter)

            fileWriter.flush()
            fileWriter.close()
        } else {
            val fileReader = FileReader(configFile, Charsets.UTF_8)
            this.config = gson.fromJson(fileReader, PlaceholderConfig::class.java)
            fileReader.close()
        }

        config.placeholders.forEach { (key, value) ->
            val id = Identifier(key)
            Placeholders.register(id) { _, _ ->
                return@register PlaceholderResult.value(value)
            }
            oldKeys.add(id)
        }
    }

}