package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player logs in (for the first time)
    // player (first) login
    //
    // @Regex ^on player (logs in( for the first time)?|( first)? login)$
    //
    // @Triggers when a player logs in to the server.
    //
    // @Context
    // <context.hostname> returns an ElementTag of the player's hostname.
    //
    // @Determine
    // "KICKED" to kick the player from the server.
    // "KICKED " + ElementTag to kick the player and specify a message to show.
    //
    // @Player Always.
    //
    // -->

    public PlayerLoginScriptEvent() {
        instance = this;
    }

    public static PlayerLoginScriptEvent instance;
    public PlayerLoginEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player") && (path.eventLower.contains("logs in") || path.eventLower.contains("login"));
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventLower.contains("first") && PlayerTag.isNoted(event.getPlayer())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerLogin";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            if (CoreUtilities.toLowerCase(determination).startsWith("kicked")) {
                String message = determination.length() > 7 ? determination.substring(7) : determination;
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, message);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("hostname")) {
            return new ElementTag(event.getAddress().toString());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}
