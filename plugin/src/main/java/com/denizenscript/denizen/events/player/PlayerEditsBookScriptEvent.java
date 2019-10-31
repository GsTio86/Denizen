package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.blocks.MaterialCompat;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class PlayerEditsBookScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player edits book
    // player signs book
    //
    // @Regex ^on player (edits|signs) book$
    //
    // @Triggers when a player edits or signs a book.
    // @Context
    // <context.title> returns the name of the book, if any.
    // <context.pages> returns the number of pages in the book.
    // <context.book> returns the book item being edited.
    // <context.signing> returns whether the book is about to be signed.
    //
    // @Determine
    // "NOT_SIGNING" to prevent the book from being signed.
    // ScriptTag to set the book information to set it to instead.
    //
    // @Player Always.
    //
    // -->

    PlayerEditsBookScriptEvent instance;
    PlayerEditBookEvent event;
    PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player edits book") || path.eventLower.startsWith("player signs book");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String action = path.eventArgLowerAt(1);
        if (action.equals("edits")) {
            return true;
        }
        if (action.equals("signs") && event.isSigning()) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "PlayerEditsBook";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.toUpperCase().equals("NOT_SIGNING")) {
            event.setSigning(false);
        }
        else if (ScriptTag.matches(determination)) {
            ScriptTag script = ScriptTag.valueOf(determination);
            if (script.getContainer() instanceof BookScriptContainer) {
                ItemTag dBook = ((BookScriptContainer) script.getContainer()).getBookFrom((BukkitTagContext) getScriptEntryData().getTagContext());
                BookMeta bookMeta = (BookMeta) dBook.getItemStack().getItemMeta();
                if (dBook.getMaterial().getMaterial() == MaterialCompat.WRITABLE_BOOK) {
                    event.setSigning(false);
                }
                event.setNewBookMeta(bookMeta);
            }
            else {
                Debug.echoError("Script '" + determination + "' is valid, but not of type 'book'!");
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("signing")) {
            return new ElementTag(event.isSigning());
        }
        if (name.equals("title")) {
            return event.isSigning() ? new ElementTag(event.getNewBookMeta().getTitle()) : null;
        }
        else if (name.equals("book")) {
            return new ItemTag(event.getPlayer().getInventory().getItem(event.getSlot()));
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEditsBook(PlayerEditBookEvent event) {
        player = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        this.event = event;
        fire(event);
    }
}
