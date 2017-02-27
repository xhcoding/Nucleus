/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.iapi.service.NucleusNoteService;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.datamodules.NoteUserDataModule;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.Optional;

public class NoteHandler implements NucleusNoteService {

    private final NucleusPlugin nucleus;
    @Inject private UserDataManager userDataManager;

    public NoteHandler(NucleusPlugin nucleus) {
        this.nucleus = nucleus;
    }

    @Override
    public List<NoteData> getNotes(User user) {
        Optional<ModularUserService> userService = userDataManager.get(user);
        if (userService.isPresent()) {
            return userService.get().get(NoteUserDataModule.class).getNotes();
        }
        return Lists.newArrayList();
    }

    @Override
    public boolean addNote(User user, NoteData note) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(note);

        Optional<ModularUserService> optUserService = userDataManager.get(user);
        if (!optUserService.isPresent()) {
            return false;
        }

        optUserService.get().get(NoteUserDataModule.class).addNote(note);
        return true;
    }

    @Override
    public boolean removeNote(User user, NoteData note) {
        Optional<ModularUserService> userService = userDataManager.get(user);
        if (userService.isPresent()) {
            userService.get().get(NoteUserDataModule.class).removeNote(note);
            return true;
        }

        return false;
    }

    @Override
    public boolean clearNotes(User user) {
        Optional<ModularUserService> userService = userDataManager.get(user);
        if (userService.isPresent()) {
            userService.get().get(NoteUserDataModule.class).clearNotes();
            return true;
        }

        return false;
    }
}
